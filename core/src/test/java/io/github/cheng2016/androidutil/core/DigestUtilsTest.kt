package io.github.cheng2016.androidutil.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class DigestUtilsTest {
    @Test
    fun sha256OfEmpty() {
        assertEquals(
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            DigestUtils.sha256Hex(""),
        )
    }

    @Test
    fun sha256OfPassword() {
        assertEquals(
            "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8",
            DigestUtils.sha256Hex("password"),
        )
    }

    @Test
    fun md5OfPassword_nonSecurityChecksumOnly() {
        assertEquals(
            "5f4dcc3b5aa765d61d8327deb882cf99",
            DigestUtils.md5Hex("password"),
        )
    }

    @Test
    fun sha256DiffersFromMd5() {
        assertNotEquals(
            DigestUtils.sha256Hex("hello"),
            DigestUtils.md5Hex("hello"),
        )
    }
}
