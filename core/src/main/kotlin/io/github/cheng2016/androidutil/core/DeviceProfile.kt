package io.github.cheng2016.androidutil.core

import android.os.Build

/**
 * 安全的设备展示信息（型号 / 品牌 / 厂商），**不含** IMEI、MAC、广告 ID。
 */
object DeviceProfile {
    @JvmStatic
    fun manufacturer(): String = Build.MANUFACTURER.orEmpty()

    @JvmStatic
    fun brand(): String = Build.BRAND.orEmpty()

    @JvmStatic
    fun model(): String = Build.MODEL.orEmpty()

    @JvmStatic
    fun device(): String = Build.DEVICE.orEmpty()

    @JvmStatic
    fun release(): String = Build.VERSION.RELEASE.orEmpty()

    @JvmStatic
    fun sdkInt(): Int = Build.VERSION.SDK_INT

    /** 人类可读的一行摘要，例如 `Samsung SM-G9910 · Android 14 (API 34)`。 */
    @JvmStatic
    fun summary(): String {
        val name = listOf(manufacturer(), model()).filter { it.isNotBlank() }.joinToString(" ")
        return "$name · Android ${release()} (API ${sdkInt()})"
    }
}
