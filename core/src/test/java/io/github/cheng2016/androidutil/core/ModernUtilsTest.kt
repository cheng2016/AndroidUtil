package io.github.cheng2016.androidutil.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ByteSizeTest {
    @Test
    fun formatsHumanReadable() {
        assertEquals("512 B", ByteSize.format(512))
        assertTrue(ByteSize.format(ByteSize.kb(2)).contains("KB"))
        assertTrue(ByteSize.format(ByteSize.mb(3)).contains("MB"))
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class VersionUtilsTest {
    @Test
    fun comparesSemanticLikeVersions() {
        assertTrue(VersionUtils.compare("1.2.0", "1.10.0") < 0)
        assertTrue(VersionUtils.isAtLeast("2.0.0", "1.9.9"))
        assertEquals(0, VersionUtils.compare("1.0.0", "1.0.0"))
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class EncodingUtilsTest {
    @Test
    fun roundTripsBase64AndHex() {
        val raw = "AndroidUtil"
        val b64 = EncodingUtils.base64Encode(raw)
        assertEquals(raw, EncodingUtils.base64DecodeToString(b64))
        val hex = EncodingUtils.hexEncode(raw.toByteArray())
        assertEquals(raw, String(EncodingUtils.hexDecode(hex)))
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class DebouncerTest {
    @Test
    fun suppressesRapidAcquires() {
        val debounce = Debouncer(1_000)
        assertTrue(debounce.tryAcquire())
        assertFalse(debounce.tryAcquire())
        debounce.reset()
        assertTrue(debounce.tryAcquire())
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class TimeFormatsTest {
    @Test
    fun relativeAndDuration() {
        val now = 1_700_000_000_000L
        assertEquals("刚刚", TimeFormats.relativeToNow(now - 10_000, now, chinese = true))
        assertEquals("3分钟前", TimeFormats.relativeToNow(now - 3 * 60_000, now, chinese = true))
        assertEquals("01:05", TimeFormats.durationClock(65_000))
        assertEquals("1:01:05", TimeFormats.durationClock(3_665_000))
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class TextSpansTest {
    @Test
    fun buildsSpannable() {
        val text =
            TextSpans.build {
                append("a")
                bold("b")
                color(0xFF0000, "c")
            }
        assertEquals("abc", text.toString())
    }
}
