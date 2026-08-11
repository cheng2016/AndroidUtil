package io.github.cheng2016.androidutil.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RedactingSanitizerTest {
    @Test
    fun redactsPasswordAndToken() {
        val out =
            RedactingSanitizer.sanitize(
                AndroidUtilLogger.Level.INFO,
                "t",
                "login password=secret token=xyz Authorization: Bearer abc.def",
            )
        assertFalse(out.contains("secret"))
        assertFalse(out.contains("xyz"))
        assertFalse(out.contains("abc.def"))
        assertTrue(out.contains("***"))
    }
}
