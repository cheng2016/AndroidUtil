package io.github.cheng2016.androidutil.network

import java.io.IOException

/**
 * Unified outcome of an HTTP call performed by [AndroidHttpClient].
 *
 * Success means the server returned a 2xx response. Non-2xx responses are
 * [HttpFailure]. Transport-level failures are [NetworkFailure]. Caller or
 * coroutine cancellation yields [Cancelled].
 */
sealed class HttpResult {
    /**
     * A successful HTTP response (status code in `[200, 300)`).
     *
     * @property code HTTP status code.
     * @property headers Response headers as a multi-map (header name → values).
     * @property body Response body decoded as UTF-8 text. Empty when absent.
     */
    data class Success(
        val code: Int,
        val headers: Map<String, List<String>>,
        val body: String,
    ) : HttpResult()

    /**
     * The server responded with a non-successful HTTP status.
     *
     * @property code HTTP status code.
     * @property headers Response headers as a multi-map.
     * @property body Response body decoded as UTF-8 text, or empty when absent.
     */
    data class HttpFailure(
        val code: Int,
        val headers: Map<String, List<String>>,
        val body: String,
    ) : HttpResult()

    /**
     * The request failed due to a network / I/O error (timeouts, DNS, etc.).
     *
     * @property cause Underlying I/O exception.
     */
    data class NetworkFailure(
        val cause: IOException,
    ) : HttpResult()

    /**
     * The call was cancelled by the caller, by coroutine cancellation, or by
     * [CancellableCall.cancel].
     */
    data object Cancelled : HttpResult()

    /** `true` when this result is [Success]. */
    val isSuccess: Boolean
        get() = this is Success
}
