package io.github.cheng2016.androidutil.network

/**
 * Handle for an in-flight HTTP request that can be cancelled.
 */
interface CancellableCall {
    /** Cancels the underlying OkHttp [okhttp3.Call] if still active. */
    fun cancel()

    /** `true` after [cancel] or after the call completed as cancelled. */
    val isCancelled: Boolean
}
