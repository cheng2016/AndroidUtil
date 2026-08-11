package io.github.cheng2016.androidutil.core

import java.nio.charset.Charset
import java.security.MessageDigest

/**
 * Message-digest helpers.
 *
 * Prefer [sha256Hex] for integrity checks. [md5Hex] is provided **only** for
 * legacy / non-security checksums (cache keys, content fingerprints). Never use
 * MD5 for passwords, tokens, signatures, or security-sensitive integrity.
 */
object DigestUtils {
    private val HEX =
        charArrayOf(
            '0',
            '1',
            '2',
            '3',
            '4',
            '5',
            '6',
            '7',
            '8',
            '9',
            'a',
            'b',
            'c',
            'd',
            'e',
            'f',
        )

    /** SHA-256 hex digest of [input] using [charset] (default UTF-8). */
    @JvmStatic
    @JvmOverloads
    fun sha256Hex(
        input: String,
        charset: Charset = Charsets.UTF_8,
    ): String = sha256Hex(input.toByteArray(charset))

    /** SHA-256 hex digest of raw [bytes]. */
    @JvmStatic
    fun sha256Hex(bytes: ByteArray): String = digestHex("SHA-256", bytes)

    /**
     * MD5 hex digest for **non-security** checksums only.
     *
     * MD5 is cryptographically broken; do not use for authentication or
     * integrity protection. Prefer [sha256Hex].
     */
    @JvmStatic
    @JvmOverloads
    fun md5Hex(
        input: String,
        charset: Charset = Charsets.UTF_8,
    ): String = md5Hex(input.toByteArray(charset))

    /**
     * MD5 hex digest of [bytes] for **non-security** checksums only.
     *
     * @see md5Hex
     */
    @JvmStatic
    fun md5Hex(bytes: ByteArray): String = digestHex("MD5", bytes)

    private fun digestHex(
        algorithm: String,
        bytes: ByteArray,
    ): String {
        val digest = MessageDigest.getInstance(algorithm).digest(bytes)
        val out = CharArray(digest.size * 2)
        var i = 0
        for (b in digest) {
            val v = b.toInt() and 0xff
            out[i++] = HEX[v ushr 4]
            out[i++] = HEX[v and 0x0f]
        }
        return String(out)
    }
}
