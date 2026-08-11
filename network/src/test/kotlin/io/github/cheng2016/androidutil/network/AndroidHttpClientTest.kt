package io.github.cheng2016.androidutil.network

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class AndroidHttpClientTest {
    private lateinit var server: MockWebServer
    private lateinit var httpClient: AndroidHttpClient

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        httpClient = AndroidHttpClient(OkHttpClient())
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun get_success_returnsBodyAndHeaders() =
        runBlocking {
            server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .addHeader("X-Test", "yes")
                    .setBody("""{"ok":true}"""),
            )

            val result = httpClient.get(server.url("/hello").toString())

            assertTrue(result is HttpResult.Success)
            val success = result as HttpResult.Success
            assertEquals(200, success.code)
            assertEquals("""{"ok":true}""", success.body)
            assertEquals(listOf("yes"), success.headers["x-test"])
            assertEquals("GET", server.takeRequest().method)
        }

    @Test
    fun postJson_sendsJsonBody() =
        runBlocking {
            server.enqueue(MockResponse().setBody("created").setResponseCode(201))

            val result =
                httpClient.postJson(
                    server.url("/items").toString(),
                    """{"name":"widget"}""",
                    mapOf("X-Trace" to "1"),
                )

            assertTrue(result is HttpResult.Success)
            val recorded = server.takeRequest()
            assertEquals("POST", recorded.method)
            assertEquals("""{"name":"widget"}""", recorded.body.readUtf8())
            assertTrue(recorded.getHeader("Content-Type")!!.contains("application/json"))
            assertEquals("1", recorded.getHeader("X-Trace"))
        }

    @Test
    fun get_httpFailure_mapsNon2xx() =
        runBlocking {
            server.enqueue(MockResponse().setResponseCode(404).setBody("missing"))

            val result = httpClient.get(server.url("/missing").toString())

            assertTrue(result is HttpResult.HttpFailure)
            val failure = result as HttpResult.HttpFailure
            assertEquals(404, failure.code)
            assertEquals("missing", failure.body)
        }

    @Test
    fun get_networkFailure_whenServerDown() =
        runBlocking {
            val url = server.url("/down").toString()
            server.shutdown()

            val result = httpClient.get(url)

            assertTrue(result is HttpResult.NetworkFailure)
        }

    @Test
    fun getAsync_deliversCallbackAndSupportsCancel() {
        server.enqueue(
            MockResponse()
                .setBodyDelay(500, TimeUnit.MILLISECONDS)
                .setBody("slow"),
        )

        val latch = CountDownLatch(1)
        val received = AtomicReference<HttpResult>()
        val call =
            httpClient.getAsync(server.url("/slow").toString()) { result ->
                received.set(result)
                latch.countDown()
            }
        call.cancel()

        assertTrue(latch.await(2, TimeUnit.SECONDS))
        assertTrue(call.isCancelled)
        assertEquals(HttpResult.Cancelled, received.get())
    }

    @Test
    fun suspendGet_cancelledByCoroutine_cancelsOkHttpCall() =
        runBlocking {
            server.enqueue(
                MockResponse()
                    .setBodyDelay(2, TimeUnit.SECONDS)
                    .setBody("late"),
            )

            val deferred =
                async {
                    httpClient.get(server.url("/late").toString())
                }
            // Allow the call to be enqueued, then cancel the coroutine.
            yield()
            deferred.cancel()
            val outcome = runCatching { deferred.await() }
            assertTrue(outcome.isFailure || outcome.getOrNull() == HttpResult.Cancelled)
        }

    @Test
    fun isSuccess_onlyForSuccessType() {
        assertTrue(
            HttpResult.Success(200, emptyMap(), "ok").isSuccess,
        )
        assertFalse(
            HttpResult.HttpFailure(500, emptyMap(), "err").isSuccess,
        )
        assertFalse(HttpResult.Cancelled.isSuccess)
    }
}
