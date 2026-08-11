package io.github.cheng2016.androidutil.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RegexUtilsTest {
    @Test
    fun mobileSimple() {
        assertTrue(RegexUtils.isMobileSimple("13800138000"))
        assertFalse(RegexUtils.isMobileSimple("23800138000"))
        assertFalse(RegexUtils.isMobileSimple(null))
    }

    @Test
    fun email() {
        assertTrue(RegexUtils.isEmail("user@example.com"))
        assertFalse(RegexUtils.isEmail("not-an-email"))
    }

    @Test
    fun idCard18() {
        assertTrue(RegexUtils.isIdCard18("11010519491231002X"))
        assertFalse(RegexUtils.isIdCard18("123"))
    }

    @Test
    fun dateLeapYear() {
        assertTrue(RegexUtils.isDate("2020-02-29"))
        assertFalse(RegexUtils.isDate("2019-02-29"))
    }

    @Test
    fun getMatches() {
        val matches = RegexUtils.getMatches("\\d+", "a1b22c")
        assertEquals(listOf("1", "22"), matches)
    }

    @Test
    fun replaceAll() {
        assertEquals("a-b-c", RegexUtils.replaceAll("a_b_c", "_", "-"))
    }
}
