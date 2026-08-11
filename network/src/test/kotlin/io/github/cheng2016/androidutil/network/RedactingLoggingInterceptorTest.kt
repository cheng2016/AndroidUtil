package io.github.cheng2016.androidutil.network

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CopyOnWriteArrayList

class RedactingLoggingInterceptorTest {
    private lateinit var server: MockWebServer
    private val lines = CopyOnWriteArrayList<String>()

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        lines.clear()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun defaultLevel_isNoneAndLogsNothing() {
        val interceptor =
            RedactingLoggingInterceptor(
                logger = RedactingLoggingInterceptor.Logger { lines += it },
            )
        assertEquals(RedactingLoggingInterceptor.Level.NONE, interceptor.level)

        server.enqueue(MockResponse().setBody("ok"))
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        client.newCall(Request.Builder().url(server.url("/")).build()).execute().close()

        assertTrue(lines.isEmpty())
    }

    @Test
    fun headersLevel_redactsAuthorizationCookieTokenPassword() {
        val interceptor =
            RedactingLoggingInterceptor(
                logger = RedactingLoggingInterceptor.Logger { lines += it },
                level = RedactingLoggingInterceptor.Level.HEADERS,
            )
        server.enqueue(MockResponse().setBody("ok"))
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        client
            .newCall(
                Request
                    .Builder()
                    .url(server.url("/secure"))
                    .header("Authorization", "Bearer secret-token")
                    .header("Cookie", "session=abc")
                    .header("X-Access-Token", "tok-123")
                    .header("X-Password", "hunter2")
                    .header("X-Trace", "visible")
                    .build(),
            ).execute()
            .close()

        val joined = lines.joinToString("\n")
        assertTrue(joined.contains("Authorization: ██"))
        assertTrue(joined.contains("Cookie: ██"))
        assertTrue(joined.contains("X-Access-Token: ██"))
        assertTrue(joined.contains("X-Password: ██"))
        assertTrue(joined.contains("X-Trace: visible"))
        assertFalse(joined.contains("Bearer secret-token"))
        assertFalse(joined.contains("hunter2"))
    }

    @Test
    fun redactUrl_masksSensitiveQueryParams() {
        val redacted =
            RedactingLoggingInterceptor.redactUrl(
                "https://example.com/api?user=ada&token=secret&password=pw&ok=1",
            )
        assertEquals(
            "https://example.com/api?user=ada&token=██&password=██&ok=1",
            redacted,
        )
    }

    @Test
    fun redactSensitiveText_masksJsonAndFormFields() {
        val json =
            RedactingLoggingInterceptor.redactSensitiveText(
                """{"user":"ada","access_token":"abc","password":"pw"}""",
            )
        assertTrue(json.contains(""""access_token":"██""""))
        assertTrue(json.contains(""""password":"██""""))
        assertTrue(json.contains(""""user":"ada""""))
        assertFalse(json.contains("abc"))

        val form =
            RedactingLoggingInterceptor.redactSensitiveText(
                "user=ada&refresh_token=rrr&password=pw",
            )
        assertTrue(form.contains("refresh_token=██"))
        assertTrue(form.contains("password=██"))
        assertTrue(form.contains("user=ada"))
    }

    @Test
    fun isSensitiveHeader_matchesExpectedNames() {
        assertTrue(RedactingLoggingInterceptor.isSensitiveHeader("Authorization"))
        assertTrue(RedactingLoggingInterceptor.isSensitiveHeader("COOKIE"))
        assertTrue(RedactingLoggingInterceptor.isSensitiveHeader("Set-Cookie"))
        assertTrue(RedactingLoggingInterceptor.isSensitiveHeader("X-Auth-Token"))
        assertTrue(RedactingLoggingInterceptor.isSensitiveHeader("user-password"))
        assertFalse(RedactingLoggingInterceptor.isSensitiveHeader("X-Trace-Id"))
    }
}
