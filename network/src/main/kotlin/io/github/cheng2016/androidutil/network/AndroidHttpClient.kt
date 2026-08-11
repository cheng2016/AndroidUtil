package io.github.cheng2016.androidutil.network

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resume

/**
 * Lightweight HTTP client built on OkHttp and Kotlin coroutines.
 *
 * - Inject any [OkHttpClient] (timeouts, interceptors, cache, etc.).
 * - Prefer [get] / [postJson] from Kotlin; use [getAsync] / [postJsonAsync] from Java.
 * - Does **not** depend on Android `Context`, dialogs, or a custom thread pool —
 *   callbacks run on OkHttp's own dispatcher.
 *
 * ```
 * val client = AndroidHttpClient()
 * when (val result = client.get("https://example.com/api")) {
 *   is HttpResult.Success -> println(result.body)
 *   is HttpResult.HttpFailure -> println(result.code)
 *   is HttpResult.NetworkFailure -> println(result.cause)
 *   HttpResult.Cancelled -> Unit
 * }
 * ```
 */
class AndroidHttpClient
    @JvmOverloads
    constructor(
        private val client: OkHttpClient = OkHttpClient(),
    ) {
        /**
         * Performs an HTTP GET and suspends until the call completes or is cancelled.
         *
         * @param url Absolute request URL.
         * @param headers Optional request headers.
         */
        @JvmOverloads
        suspend fun get(
            url: String,
            headers: Map<String, String> = emptyMap(),
        ): HttpResult = execute(buildGetRequest(url, headers))

        /**
         * Performs an HTTP POST with a JSON body and suspends until completion.
         *
         * @param url Absolute request URL.
         * @param json UTF-8 JSON payload.
         * @param headers Optional request headers (`Content-Type` is set automatically).
         */
        @JvmOverloads
        suspend fun postJson(
            url: String,
            json: String,
            headers: Map<String, String> = emptyMap(),
        ): HttpResult = execute(buildPostJsonRequest(url, json, headers))

        /**
         * Java-friendly asynchronous GET. Returns a [CancellableCall] immediately.
         *
         * Overloads omit [headers] so Java can call `getAsync(url, callback)`.
         *
         * @param url Absolute request URL.
         * @param callback Receives the terminal [HttpResult] on OkHttp's dispatcher thread.
         * @param headers Optional request headers; may be `null`.
         */
        fun getAsync(
            url: String,
            callback: HttpCallback,
        ): CancellableCall = getAsync(url, emptyMap(), callback)

        /** Asynchronous GET with explicit [headers]. */
        fun getAsync(
            url: String,
            headers: Map<String, String>,
            callback: HttpCallback,
        ): CancellableCall = enqueue(buildGetRequest(url, headers), callback)

        /**
         * Java-friendly asynchronous JSON POST. Returns a [CancellableCall] immediately.
         *
         * Overloads omit [headers] so Java can call `postJsonAsync(url, json, callback)`.
         *
         * @param url Absolute request URL.
         * @param json UTF-8 JSON payload.
         * @param callback Receives the terminal [HttpResult] on OkHttp's dispatcher thread.
         * @param headers Optional request headers; may be `null`.
         */
        fun postJsonAsync(
            url: String,
            json: String,
            callback: HttpCallback,
        ): CancellableCall = postJsonAsync(url, json, emptyMap(), callback)

        /** Asynchronous JSON POST with explicit [headers]. */
        fun postJsonAsync(
            url: String,
            json: String,
            headers: Map<String, String>,
            callback: HttpCallback,
        ): CancellableCall = enqueue(buildPostJsonRequest(url, json, headers), callback)

        private suspend fun execute(request: Request): HttpResult =
            suspendCancellableCoroutine { continuation ->
                val call = client.newCall(request)
                continuation.invokeOnCancellation { call.cancel() }
                call.enqueue(
                    object : Callback {
                        override fun onFailure(
                            call: Call,
                            e: IOException,
                        ) {
                            val result =
                                if (call.isCanceled()) {
                                    HttpResult.Cancelled
                                } else {
                                    HttpResult.NetworkFailure(e)
                                }
                            if (continuation.isActive) {
                                continuation.resume(result)
                            }
                        }

                        override fun onResponse(
                            call: Call,
                            response: Response,
                        ) {
                            if (continuation.isActive) {
                                continuation.resume(response.toHttpResult())
                            } else {
                                response.close()
                            }
                        }
                    },
                )
            }

        private fun enqueue(
            request: Request,
            callback: HttpCallback,
        ): CancellableCall {
            val call = client.newCall(request)
            call.enqueue(
                object : Callback {
                    override fun onFailure(
                        call: Call,
                        e: IOException,
                    ) {
                        val result =
                            if (call.isCanceled()) {
                                HttpResult.Cancelled
                            } else {
                                HttpResult.NetworkFailure(e)
                            }
                        callback.onResult(result)
                    }

                    override fun onResponse(
                        call: Call,
                        response: Response,
                    ) {
                        callback.onResult(response.toHttpResult())
                    }
                },
            )
            return object : CancellableCall {
                override fun cancel() {
                    call.cancel()
                }

                override val isCancelled: Boolean
                    get() = call.isCanceled()
            }
        }

        private fun buildGetRequest(
            url: String,
            headers: Map<String, String>,
        ): Request =
            Request
                .Builder()
                .url(url)
                .headers(headers.toHeaders())
                .get()
                .build()

        private fun buildPostJsonRequest(
            url: String,
            json: String,
            headers: Map<String, String>,
        ): Request {
            val body = json.toRequestBody(JSON_MEDIA_TYPE)
            return Request
                .Builder()
                .url(url)
                .headers(headers.toHeaders())
                .post(body)
                .build()
        }

        private companion object {
            val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

            fun Map<String, String>.toHeaders(): Headers {
                if (isEmpty()) return Headers.headersOf()
                val builder = Headers.Builder()
                for ((name, value) in this) {
                    builder.add(name, value)
                }
                return builder.build()
            }

            fun Response.toHttpResult(): HttpResult {
                use { response ->
                    val bodyText = response.body?.string().orEmpty()
                    val headerMap = response.headers.toMultimap()
                    return if (response.isSuccessful) {
                        HttpResult.Success(
                            code = response.code,
                            headers = headerMap,
                            body = bodyText,
                        )
                    } else {
                        HttpResult.HttpFailure(
                            code = response.code,
                            headers = headerMap,
                            body = bodyText,
                        )
                    }
                }
            }
        }
    }
