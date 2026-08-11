package io.github.cheng2016.androidutil.core

import android.os.SystemClock
import java.util.concurrent.atomic.AtomicLong

/**
 * 点击 / 事件防抖：在 [intervalMs] 内重复触发会被忽略。
 *
 * ```
 * val debounce = Debouncer(600)
 * button.setOnClickListener {
 *   if (debounce.tryAcquire()) doSubmit()
 * }
 * ```
 */
class Debouncer
    @JvmOverloads
    constructor(
        private val intervalMs: Long = 600L,
    ) {
        private val last = AtomicLong(0L)

        /** 若距离上次成功获取已超过间隔，返回 true 并更新时间戳。 */
        fun tryAcquire(): Boolean {
            val now = SystemClock.elapsedRealtime()
            while (true) {
                val prev = last.get()
                // 0 means "never acquired" — do not treat as a timestamp at boot.
                if (prev != 0L && now - prev < intervalMs) return false
                if (last.compareAndSet(prev, now)) return true
            }
        }

        /** 重置，使下一次 [tryAcquire] 必然成功。 */
        fun reset() {
            last.set(0L)
        }
    }

/**
 * 节流：保证两次成功执行至少间隔 [intervalMs]（尾部不补触发）。
 */
class Throttler
    @JvmOverloads
    constructor(
        private val intervalMs: Long = 600L,
    ) {
        private val last = AtomicLong(0L)

        fun tryAcquire(): Boolean {
            val now = SystemClock.elapsedRealtime()
            while (true) {
                val prev = last.get()
                if (prev != 0L && now - prev < intervalMs) return false
                if (last.compareAndSet(prev, now)) return true
            }
        }

        fun reset() {
            last.set(0L)
        }
    }
