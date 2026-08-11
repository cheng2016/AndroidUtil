package io.github.cheng2016.androidutil.core

import android.content.Context
import android.widget.Toast

/**
 * 轻量 Toast：始终使用 Application Context，避免短生命周期泄漏；支持取消上一次。
 *
 * 仅作轻提示胶水，不承载复杂 UI。
 */
object Toasts {
    @Volatile
    private var last: Toast? = null

    @JvmStatic
    fun short(
        context: Context,
        message: CharSequence,
    ) {
        show(context, message, Toast.LENGTH_SHORT)
    }

    @JvmStatic
    fun long(
        context: Context,
        message: CharSequence,
    ) {
        show(context, message, Toast.LENGTH_LONG)
    }

    @JvmStatic
    fun cancel() {
        last?.cancel()
        last = null
    }

    private fun show(
        context: Context,
        message: CharSequence,
        duration: Int,
    ) {
        MainThread.run {
            cancel()
            val toast = Toast.makeText(context.applicationContext, message, duration)
            last = toast
            toast.show()
        }
    }
}
