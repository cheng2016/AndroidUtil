package io.github.cheng2016.androidutil.core

import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone

/**
 * Chinese lunar calendar date converted from a Gregorian (solar) calendar day.
 *
 * Algorithm and `lunarInfo` table are migrated from the legacy `util/Lunar.java`
 * (coverage roughly solar 1900-01-31 … 2049). This is a maintainable port with
 * focused unit tests — not a full astronomical ephemeris.
 *
 * ```
 * val lunar = LunarDate.fromSolar(2020, 1, 25)
 * println(lunar.toChineseString()) // 2020年正月初一
 * ```
 */
data class LunarDate(
    val year: Int,
    val month: Int,
    val day: Int,
    val isLeapMonth: Boolean,
) {
    /** Chinese zodiac animal for [year]. */
    fun animalsYear(): String {
        val animals =
            arrayOf("鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪")
        return animals[(year - 4).floorMod(12)]
    }

    /** Ganzhi (stem-branch) year label, e.g. `庚子`. */
    fun cyclical(): String {
        val num = year - 1900 + 36
        return cyclicalm(num)
    }

    /**
     * Human-readable lunar date, e.g. `2020年正月初一` or `2017年闰六月初一`.
     */
    fun toChineseString(): String {
        val leap = if (isLeapMonth) "闰" else ""
        return "${year}年$leap${CHINESE_NUMBER[month - 1]}月${chinaDayString(day)}"
    }

    override fun toString(): String = toChineseString()

    companion object {
        private val CHINESE_NUMBER =
            arrayOf("一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "十二")

        /**
         * Encoded lunar year table from 1900 inclusive (index 0) — legacy data.
         */
        private val LUNAR_INFO =
            longArrayOf(
                0x04bd8,
                0x04ae0,
                0x0a570,
                0x054d5,
                0x0d260,
                0x0d950,
                0x16554,
                0x056a0,
                0x09ad0,
                0x055d2,
                0x04ae0,
                0x0a5b6,
                0x0a4d0,
                0x0d250,
                0x1d255,
                0x0b540,
                0x0d6a0,
                0x0ada2,
                0x095b0,
                0x14977,
                0x04970,
                0x0a4b0,
                0x0b4b5,
                0x06a50,
                0x06d40,
                0x1ab54,
                0x02b60,
                0x09570,
                0x052f2,
                0x04970,
                0x06566,
                0x0d4a0,
                0x0ea50,
                0x06e95,
                0x05ad0,
                0x02b60,
                0x186e3,
                0x092e0,
                0x1c8d7,
                0x0c950,
                0x0d4a0,
                0x1d8a6,
                0x0b550,
                0x056a0,
                0x1a5b4,
                0x025d0,
                0x092d0,
                0x0d2b2,
                0x0a950,
                0x0b557,
                0x06ca0,
                0x0b550,
                0x15355,
                0x04da0,
                0x0a5d0,
                0x14573,
                0x052d0,
                0x0a9a8,
                0x0e950,
                0x06aa0,
                0x0aea6,
                0x0ab50,
                0x04b60,
                0x0aae4,
                0x0a570,
                0x05260,
                0x0f263,
                0x0d950,
                0x05b57,
                0x056a0,
                0x096d0,
                0x04dd5,
                0x04ad0,
                0x0a4d0,
                0x0d4d4,
                0x0d250,
                0x0d558,
                0x0b540,
                0x0b5a0,
                0x195a6,
                0x095b0,
                0x049b0,
                0x0a974,
                0x0a4b0,
                0x0b27a,
                0x06a50,
                0x06d40,
                0x0af46,
                0x0ab60,
                0x09570,
                0x04af5,
                0x04970,
                0x064b0,
                0x074a3,
                0x0ea50,
                0x06b58,
                0x055c0,
                0x0ab60,
                0x096d5,
                0x092e0,
                0x0c960,
                0x0d954,
                0x0d4a0,
                0x0da50,
                0x07552,
                0x056a0,
                0x0abb7,
                0x025d0,
                0x092d0,
                0x0cab5,
                0x0a950,
                0x0b4a0,
                0x0baa4,
                0x0ad50,
                0x055d9,
                0x04ba0,
                0x0a5b0,
                0x15176,
                0x052b0,
                0x0a930,
                0x07954,
                0x06aa0,
                0x0ad50,
                0x05b52,
                0x04b60,
                0x0a6e6,
                0x0a4e0,
                0x0d260,
                0x0ea65,
                0x0d530,
                0x05aa0,
                0x076a3,
                0x096d0,
                0x04bd7,
                0x04ad0,
                0x0a4d0,
                0x1d0b6,
                0x0d250,
                0x0d520,
                0x0dd45,
                0x0b5a0,
                0x056d0,
                0x055b2,
                0x049b0,
                0x0a577,
                0x0a4b0,
                0x0aa50,
                0x1b255,
                0x06d20,
                0x0ada0,
            )

        /** Base solar date: 1900-01-31 (lunar 1900-1-1). UTC noon avoids DST edge cases. */
        private val BASE_MILLIS: Long =
            GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US)
                .apply {
                    isLenient = false
                    set(Calendar.YEAR, 1900)
                    set(Calendar.MONTH, Calendar.JANUARY)
                    set(Calendar.DAY_OF_MONTH, 31)
                    set(Calendar.HOUR_OF_DAY, 12)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

        /**
         * Converts a solar [year]/[month]/[day] (1-based month) to a [LunarDate].
         *
         * @throws IllegalArgumentException when the date is outside the supported table range
         */
        @JvmStatic
        fun fromSolar(
            year: Int,
            month: Int,
            day: Int,
        ): LunarDate {
            require(month in 1..12) { "month must be 1..12, was $month" }
            require(day in 1..31) { "day must be 1..31, was $day" }
            val cal =
                GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US).apply {
                    isLenient = false
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month - 1)
                    set(Calendar.DAY_OF_MONTH, day)
                    set(Calendar.HOUR_OF_DAY, 12)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
            return fromSolar(cal)
        }

        /**
         * Converts the calendar's year/month/day (local fields) to lunar.
         * Time-of-day is ignored; conversion uses a UTC noon anchor.
         */
        @JvmStatic
        fun fromSolar(calendar: Calendar): LunarDate {
            val y = calendar.get(Calendar.YEAR)
            val m = calendar.get(Calendar.MONTH) + 1
            val d = calendar.get(Calendar.DAY_OF_MONTH)
            val utc =
                GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US).apply {
                    isLenient = false
                    set(Calendar.YEAR, y)
                    set(Calendar.MONTH, m - 1)
                    set(Calendar.DAY_OF_MONTH, d)
                    set(Calendar.HOUR_OF_DAY, 12)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
            val offset = ((utc.timeInMillis - BASE_MILLIS) / 86_400_000L).toInt()
            require(offset >= 0) {
                "Date before supported range (1900-01-31): $y-$m-$d"
            }

            var remaining = offset
            var iYear = 1900
            var daysOfYear = 0
            while (iYear < 2050 && remaining > 0) {
                daysOfYear = yearDays(iYear)
                remaining -= daysOfYear
                iYear++
            }
            if (remaining < 0) {
                remaining += daysOfYear
                iYear--
            }

            val leapMonth = leapMonth(iYear)
            var leap = false
            var iMonth = 1
            var daysOfMonth = 0
            while (iMonth < 13 && remaining > 0) {
                if (leapMonth > 0 && iMonth == leapMonth + 1 && !leap) {
                    iMonth--
                    leap = true
                    daysOfMonth = leapDays(iYear)
                } else {
                    daysOfMonth = monthDays(iYear, iMonth)
                }
                remaining -= daysOfMonth
                if (leap && iMonth == leapMonth + 1) {
                    leap = false
                }
                iMonth++
            }
            if (remaining == 0 && leapMonth > 0 && iMonth == leapMonth + 1) {
                if (leap) {
                    leap = false
                } else {
                    leap = true
                    iMonth--
                }
            }
            if (remaining < 0) {
                remaining += daysOfMonth
                iMonth--
            }

            return LunarDate(
                year = iYear,
                month = iMonth,
                day = remaining + 1,
                isLeapMonth = leap,
            )
        }

        /** Chinese day label such as `初一`, `廿三`. */
        @JvmStatic
        fun chinaDayString(day: Int): String {
            val chineseTen = arrayOf("初", "十", "廿", "三")
            if (day > 30) return ""
            if (day == 10) return "初十"
            val n = if (day % 10 == 0) 9 else day % 10 - 1
            return chineseTen[day / 10] + CHINESE_NUMBER[n]
        }

        private fun yearDays(y: Int): Int {
            var sum = 348
            var i = 0x8000
            while (i > 0x8) {
                if ((LUNAR_INFO[y - 1900] and i.toLong()) != 0L) sum++
                i = i shr 1
            }
            return sum + leapDays(y)
        }

        private fun leapDays(y: Int): Int {
            if (leapMonth(y) == 0) return 0
            return if ((LUNAR_INFO[y - 1900] and 0x10000L) != 0L) 30 else 29
        }

        private fun leapMonth(y: Int): Int = (LUNAR_INFO[y - 1900] and 0xfL).toInt()

        private fun monthDays(
            y: Int,
            m: Int,
        ): Int = if ((LUNAR_INFO[y - 1900] and (0x10000L shr m)) == 0L) 29 else 30

        private fun cyclicalm(num: Int): String {
            val gan = arrayOf("甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸")
            val zhi = arrayOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")
            return gan[num.floorMod(10)] + zhi[num.floorMod(12)]
        }

        private fun Int.floorMod(m: Int): Int {
            val r = this % m
            return if (r >= 0) r else r + m
        }
    }
}
