package io.github.cheng2016.androidutil.network

import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Optional OkHttp interceptor that logs request/response traffic while
 * redacting sensitive values.
 *
 * Logging is **disabled by default** ([Level.NONE]). Add this interceptor to
 * a debug [okhttp3.OkHttpClient] and raise [level] only when needed.
 *
 * Redacts:
 * - Headers named `Authorization`, `Cookie`, `Set-Cookie`
 * - Header names containing `token` or `password` (case-insensitive)
 * - Query parameters / form fields / JSON string values whose names contain
 *   `token` or `password`
 *
 * ```
 * val logging = RedactingLoggingInterceptor(level = RedactingLoggingInterceptor.Level.BASIC)
 * val client = OkHttpClient.Builder().addInterceptor(logging).build()
 * ```
 */
class RedactingLoggingInterceptor
    @JvmOverloads
    constructor(
        private val logger: Logger = Logger { message -> println(message) },
        @Volatile var level: Level = Level.NONE,
    ) : Interceptor {
        /** Log sink used by this interceptor. */
        fun interface Logger {
            fun log(message: String)
        }

        /** How much traffic to log. */
        enum class Level {
            /** No logging. */
            NONE,

            /** Logs method, URL (redacted), and response code / timing. */
            BASIC,

            /** [BASIC] plus redacted headers. */
            HEADERS,

            /** [HEADERS] plus redacted bodies when present. */
            BODY,
        }

        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val logLevel = level
            if (logLevel == Level.NONE) {
                return chain.proceed(request)
            }

            val requestBody = request.body
            val logBody = logLevel == Level.BODY
            val logHeaders = logLevel == Level.HEADERS || logBody

            val url = redactUrl(request.url.toString())
            logger.log("--> ${request.method} $url")

            if (logHeaders) {
                logHeaders(request.headers)
                if (requestBody != null) {
                    requestBody.contentType()?.let { logger.log("Content-Type: $it") }
                    val contentLength = requestBody.contentLength()
                    if (contentLength >= 0L) {
                        logger.log("Content-Length: $contentLength")
                    }
                }
            }

            if (logBody && requestBody != null) {
                val buffer = Buffer()
                requestBody.writeTo(buffer)
                val charset = requestBody.contentType()?.charset(Charsets.UTF_8) ?: Charsets.UTF_8
                if (isPlaintext(buffer)) {
                    logger.log(redactSensitiveText(buffer.readString(charset)))
                } else {
                    logger.log("(binary ${requestBody.contentLength()}-byte body omitted)")
                }
            }

            val startNs = System.nanoTime()
            val response: Response =
                try {
                    chain.proceed(request)
                } catch (e: Exception) {
                    logger.log("<-- HTTP FAILED: $e")
                    throw e
                }
            val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)

            val responseBody = response.body
            val contentLength = responseBody?.contentLength() ?: -1L
            val summary =
                if (logHeaders) {
                    "<-- ${response.code} ${response.message} $url (${tookMs}ms)"
                } else {
                    "<-- ${response.code} ${response.message} $url (${tookMs}ms, $contentLength-byte body)"
                }
            logger.log(summary)

            if (logHeaders) {
                logHeaders(response.headers)
            }

            if (logBody && responseBody != null) {
                val source = responseBody.source()
                source.request(Long.MAX_VALUE)
                val buffer = source.buffer.clone()
                val charset = responseBody.contentType()?.charset(Charsets.UTF_8) ?: Charsets.UTF_8
                if (contentLength != 0L) {
                    if (isPlaintext(buffer)) {
                        val bufferSize = buffer.size
                        logger.log(redactSensitiveText(buffer.readString(charset)))
                        logger.log("<-- END HTTP ($bufferSize-byte body)")
                    } else {
                        logger.log("<-- END HTTP (binary ${buffer.size}-byte body omitted)")
                    }
                }
            }

            return response
        }

        private fun logHeaders(headers: Headers) {
            for (i in 0 until headers.size) {
                val name = headers.name(i)
                val value = if (isSensitiveHeader(name)) REDACTED else headers.value(i)
                logger.log("$name: $value")
            }
        }

        companion object {
            private const val REDACTED = "██"

            private val SENSITIVE_HEADER_EXACT =
                setOf("authorization", "cookie", "set-cookie", "proxy-authorization")

            private val FORM_SENSITIVE =
                Regex("""(?i)([?&]?(?:[^=&]*?(?:token|password)[^=&]*?))=(?:[^&\s]*)""")

            private val JSON_SENSITIVE =
                Regex("""(?i)("(?:[^"]*?(?:token|password)[^"]*?)"\s*:\s*)"[^"]*"""")

            internal fun isSensitiveHeader(name: String): Boolean {
                val lower = name.lowercase(Locale.US)
                if (lower in SENSITIVE_HEADER_EXACT) return true
                return lower.contains("token") || lower.contains("password")
            }

            internal fun redactUrl(url: String): String {
                val queryIndex = url.indexOf('?')
                if (queryIndex < 0) return url
                val base = url.substring(0, queryIndex + 1)
                val query = url.substring(queryIndex + 1)
                val redacted =
                    query.split('&').joinToString("&") { pair ->
                        val eq = pair.indexOf('=')
                        if (eq <= 0) {
                            pair
                        } else {
                            val key = pair.substring(0, eq)
                            if (isSensitiveKey(key)) "$key=$REDACTED" else pair
                        }
                    }
                return base + redacted
            }

            internal fun redactSensitiveText(text: String): String {
                var result =
                    FORM_SENSITIVE.replace(text) { match ->
                        "${match.groupValues[1]}=$REDACTED"
                    }
                result =
                    JSON_SENSITIVE.replace(result) { match ->
                        "${match.groupValues[1]}\"██\""
                    }
                return result
            }

            private fun isSensitiveKey(key: String): Boolean {
                val lower = key.lowercase(Locale.US)
                return lower.contains("token") || lower.contains("password")
            }

            private fun isPlaintext(buffer: Buffer): Boolean {
                return try {
                    val prefix = Buffer()
                    val byteCount = minOf(buffer.size, 64)
                    buffer.copyTo(prefix, 0, byteCount)
                    repeat(16) {
                        if (prefix.exhausted()) return@repeat
                        val codePoint = prefix.readUtf8CodePoint()
                        if (Character.isISOControl(codePoint) &&
                            !Character.isWhitespace(codePoint)
                        ) {
                            return false
                        }
                    }
                    true
                } catch (_: Exception) {
                    false
                }
            }
        }
    }
