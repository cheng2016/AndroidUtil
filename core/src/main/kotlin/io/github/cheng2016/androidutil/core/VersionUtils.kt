package io.github.cheng2016.androidutil.core

/**
 * 版本号比较（数字段为主，非数字段按字典序）。适用于 `1.2.0` / `1.2.0-rc1` 这类常见形式。
 */
object VersionUtils {
    /**
     * @return 负数表示 [left] &lt; [right]，0 相等，正数表示 [left] &gt; [right]
     */
    @JvmStatic
    fun compare(
        left: String,
        right: String,
    ): Int {
        val a = tokenize(left)
        val b = tokenize(right)
        val n = maxOf(a.size, b.size)
        for (i in 0 until n) {
            val x = a.getOrElse(i) { Segment.Number(0) }
            val y = b.getOrElse(i) { Segment.Number(0) }
            val c = x.compareTo(y)
            if (c != 0) return c
        }
        return 0
    }

    @JvmStatic
    fun isAtLeast(
        current: String,
        minimum: String,
    ): Boolean = compare(current, minimum) >= 0

    private fun tokenize(raw: String): List<Segment> {
        val cleaned = raw.trim().removePrefix("v").removePrefix("V")
        if (cleaned.isEmpty()) return listOf(Segment.Number(0))
        val parts = cleaned.split('.', '-', '_')
        return parts.map { part ->
            val num = part.takeWhile { it.isDigit() }
            if (num.isNotEmpty() && num == part) {
                Segment.Number(num.toLongOrNull() ?: 0L)
            } else if (num.isNotEmpty()) {
                Segment.Mixed(num.toLongOrNull() ?: 0L, part.removePrefix(num))
            } else {
                Segment.Text(part.lowercase())
            }
        }
    }

    private sealed class Segment : Comparable<Segment> {
        data class Number(
            val value: Long,
        ) : Segment()

        data class Text(
            val value: String,
        ) : Segment()

        data class Mixed(
            val number: Long,
            val suffix: String,
        ) : Segment()

        override fun compareTo(other: Segment): Int =
            when (this) {
                is Number ->
                    when (other) {
                        is Number -> value.compareTo(other.value)
                        is Mixed -> {
                            val c = value.compareTo(other.number)
                            if (c != 0) c else -1
                        }
                        is Text -> -1
                    }
                is Mixed ->
                    when (other) {
                        is Number -> {
                            val c = number.compareTo(other.value)
                            if (c != 0) c else 1
                        }
                        is Mixed -> {
                            val c = number.compareTo(other.number)
                            if (c != 0) c else suffix.compareTo(other.suffix)
                        }
                        is Text -> -1
                    }
                is Text ->
                    when (other) {
                        is Text -> value.compareTo(other.value)
                        else -> 1
                    }
            }
    }
}
