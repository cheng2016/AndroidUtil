package io.github.cheng2016.androidutil.core

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor

/**
 * 主线程调度：判断、投递、延迟执行。不引入额外线程池依赖。
 */
object MainThread {
    private val handler = Handler(Looper.getMainLooper())

    /** 是否在主线程。 */
    @JvmStatic
    fun isMainThread(): Boolean = Looper.myLooper() == Looper.getMainLooper()

    /** 在主线程执行；若已在主线程则同步执行。 */
    @JvmStatic
    fun run(action: Runnable) {
        if (isMainThread()) {
            action.run()
        } else {
            handler.post(action)
        }
    }

    /** 投递到主线程队列（始终异步）。 */
    @JvmStatic
    fun post(action: Runnable) {
        handler.post(action)
    }

    /** 延迟投递到主线程。 */
    @JvmStatic
    fun postDelayed(
        delayMs: Long,
        action: Runnable,
    ) {
        handler.postDelayed(action, delayMs)
    }

    /** 取消尚未执行的回调（按 Runnable 引用）。 */
    @JvmStatic
    fun remove(action: Runnable) {
        handler.removeCallbacks(action)
    }

    /** 可作为 [Executor] 使用的主线程执行器。 */
    @JvmStatic
    fun executor(): Executor = Executor { command -> run(command) }
}
