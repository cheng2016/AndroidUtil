package io.github.cheng2016.androidutil.core

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * 触感反馈（VibrationEffect），兼容新旧 API；无权限声明时短震动在多数设备可用。
 */
object Haptics {
    /** Platform default amplitude for [VibrationEffect] (`-1`). */
    private const val DEFAULT_AMPLITUDE = -1

    /** 轻触反馈（点击感）。 */
    @JvmStatic
    fun tick(context: Context) {
        vibrate(context, 20L, DEFAULT_AMPLITUDE)
    }

    /** 成功/确认短震。 */
    @JvmStatic
    fun confirm(context: Context) {
        vibrate(context, 40L, DEFAULT_AMPLITUDE)
    }

    /** 自定义时长与振幅（振幅 1–255，或 -1 表示默认）。 */
    @JvmStatic
    @JvmOverloads
    fun vibrate(
        context: Context,
        durationMs: Long,
        amplitude: Int = DEFAULT_AMPLITUDE,
    ) {
        val vibrator = vibrator(context) ?: return
        if (!vibrator.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(durationMs.coerceAtLeast(1L), amplitude))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(durationMs.coerceAtLeast(1L))
        }
    }

    /** 取消震动。 */
    @JvmStatic
    fun cancel(context: Context) {
        vibrator(context)?.cancel()
    }

    private fun vibrator(context: Context): Vibrator? {
        val app = context.applicationContext
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = app.getSystemService(VibratorManager::class.java)
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            app.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}
