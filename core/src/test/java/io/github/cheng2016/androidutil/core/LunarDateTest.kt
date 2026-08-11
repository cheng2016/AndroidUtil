package io.github.cheng2016.androidutil.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone

/**
 * Golden cases aligned with the legacy `util/Lunar.java` conversion table.
 */
class LunarDateTest {
    @Test
    fun baseDateIsSpringFestival1900() {
        val lunar = LunarDate.fromSolar(1900, 1, 31)
        assertEquals(1900, lunar.year)
        assertEquals(1, lunar.month)
        assertEquals(1, lunar.day)
        assertFalse(lunar.isLeapMonth)
        assertEquals("1900年一月初一", lunar.toChineseString())
    }

    @Test
    fun springFestival2020() {
        val lunar = LunarDate.fromSolar(2020, 1, 25)
        assertEquals(2020, lunar.year)
        assertEquals(1, lunar.month)
        assertEquals(1, lunar.day)
        assertFalse(lunar.isLeapMonth)
        assertEquals("鼠", lunar.animalsYear())
        assertEquals("庚子", lunar.cyclical())
        assertEquals("2020年一月初一", lunar.toChineseString())
    }

    @Test
    fun springFestival2024() {
        val lunar = LunarDate.fromSolar(2024, 2, 10)
        assertEquals(2024, lunar.year)
        assertEquals(1, lunar.month)
        assertEquals(1, lunar.day)
        assertEquals("龙", lunar.animalsYear())
        assertEquals("甲辰", lunar.cyclical())
    }

    @Test
    fun calendarOverloadMatchesYmd() {
        val cal =
            GregorianCalendar(TimeZone.getTimeZone("Asia/Shanghai"), Locale.CHINA).apply {
                set(2020, Calendar.JANUARY, 25, 8, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
        assertEquals(LunarDate.fromSolar(2020, 1, 25), LunarDate.fromSolar(cal))
    }

    @Test
    fun leapMonth2017() {
        // 2017-07-23 is 闰六月初一 in the legacy table
        val lunar = LunarDate.fromSolar(2017, 7, 23)
        assertEquals(2017, lunar.year)
        assertEquals(6, lunar.month)
        assertEquals(1, lunar.day)
        assertEquals(true, lunar.isLeapMonth)
        assertEquals("2017年闰六月初一", lunar.toChineseString())
    }

    @Test
    fun chinaDayLabels() {
        assertEquals("初一", LunarDate.chinaDayString(1))
        assertEquals("初十", LunarDate.chinaDayString(10))
        assertEquals("廿三", LunarDate.chinaDayString(23))
        assertEquals("三十", LunarDate.chinaDayString(30))
    }
}
