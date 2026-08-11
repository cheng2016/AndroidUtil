package io.github.cheng2016.androidutil.network

/**
 * Java-friendly callback for asynchronous HTTP calls.
 *
 * Invoked on OkHttp's dispatcher thread. Switch to the main thread in the
 * host app if UI work is required.
 */
fun interface HttpCallback {
    /**
     * Called exactly once with the terminal [HttpResult].
     */
    fun onResult(result: HttpResult)
}
