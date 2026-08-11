package io.github.cheng2016.androidutil.core

import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.ln
import kotlin.math.pow

/**
 * 字节大小与人类可读格式（如 `1.5 MB`）。
 */
object ByteSize {
    private val UNITS = arrayOf("B", "KB", "MB", "GB", "TB", "PB")

    /** 格式化为最多 [fractionDigits] 位小数的可读字符串。 */
    @JvmStatic
    @JvmOverloads
    fun format(
        bytes: Long,
        fractionDigits: Int = 1,
        locale: Locale = Locale.US,
    ): String {
        if (bytes < 0) return format(0, fractionDigits, locale)
        if (bytes < 1024) return "$bytes B"
        val exp = (ln(bytes.toDouble()) / ln(1024.0)).toInt().coerceAtMost(UNITS.lastIndex)
        val value = bytes / 1024.0.pow(exp.toDouble())
        return String.format(locale, "%.${fractionDigits}f %s", value, UNITS[exp])
    }

    @JvmStatic
    fun kb(value: Long): Long = value * 1024

    @JvmStatic
    fun mb(value: Long): Long = value * 1024 * 1024

    @JvmStatic
    fun gb(value: Long): Long = value * 1024 * 1024 * 1024
}

/**
 * 时间格式与相对时间文案（中文友好，也提供英文单位可选）。
 */
object TimeFormats {
    /**
     * 将 [epochMs] 格式化为相对「现在」的描述。
     *
     * @param chinese true 使用「刚刚 / 分钟前 / 小时前 / 天前」；false 使用 `just now / m ago / …`
     */
    @JvmStatic
    @JvmOverloads
    fun relativeToNow(
        epochMs: Long,
        nowMs: Long = System.currentTimeMillis(),
        chinese: Boolean = true,
    ): String {
        val delta = (nowMs - epochMs).coerceAtLeast(0L)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(delta)
        val hours = TimeUnit.MILLISECONDS.toHours(delta)
        val days = TimeUnit.MILLISECONDS.toDays(delta)
        return when {
            minutes < 1 -> if (chinese) "刚刚" else "just now"
            minutes < 60 -> if (chinese) "${minutes}分钟前" else "${minutes}m ago"
            hours < 24 -> if (chinese) "${hours}小时前" else "${hours}h ago"
            days < 30 -> if (chinese) "${days}天前" else "${days}d ago"
            else -> {
                val months = days / 30
                if (months < 12) {
                    if (chinese) "${months}个月前" else "${months}mo ago"
                } else {
                    val years = months / 12
                    if (chinese) "${years}年前" else "${years}y ago"
                }
            }
        }
    }

    /** 毫秒格式化为 `HH:mm:ss` 或 `mm:ss`（媒体进度常用）。 */
    @JvmStatic
    fun durationClock(durationMs: Long): String {
        val totalSec = (durationMs.coerceAtLeast(0L) / 1000L)
        val h = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val s = totalSec % 60
        return if (h > 0) {
            String.format(Locale.US, "%d:%02d:%02d", h, m, s)
        } else {
            String.format(Locale.US, "%02d:%02d", m, s)
        }
    }
}
