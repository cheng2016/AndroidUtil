package io.github.cheng2016.androidutil.core

import android.util.Base64
import java.nio.charset.Charset

/**
 * Base64 / Hex 编解码（Android [Base64] 旗标明确，避免 Java 版本差异）。
 */
object EncodingUtils {
    @JvmStatic
    @JvmOverloads
    fun base64Encode(
        input: String,
        charset: Charset = Charsets.UTF_8,
        urlSafe: Boolean = false,
    ): String = base64Encode(input.toByteArray(charset), urlSafe)

    @JvmStatic
    @JvmOverloads
    fun base64Encode(
        bytes: ByteArray,
        urlSafe: Boolean = false,
    ): String {
        val flags = Base64.NO_WRAP or if (urlSafe) Base64.URL_SAFE else Base64.DEFAULT
        return Base64.encodeToString(bytes, flags)
    }

    @JvmStatic
    @JvmOverloads
    fun base64Decode(
        encoded: String,
        urlSafe: Boolean = false,
    ): ByteArray {
        val flags = Base64.NO_WRAP or if (urlSafe) Base64.URL_SAFE else Base64.DEFAULT
        return Base64.decode(encoded, flags)
    }

    @JvmStatic
    @JvmOverloads
    fun base64DecodeToString(
        encoded: String,
        charset: Charset = Charsets.UTF_8,
        urlSafe: Boolean = false,
    ): String = String(base64Decode(encoded, urlSafe), charset)

    @JvmStatic
    fun hexEncode(bytes: ByteArray): String {
        val out = CharArray(bytes.size * 2)
        val hex = "0123456789abcdef"
        var i = 0
        for (b in bytes) {
            val v = b.toInt() and 0xff
            out[i++] = hex[v ushr 4]
            out[i++] = hex[v and 0x0f]
        }
        return String(out)
    }

    @JvmStatic
    fun hexDecode(hex: String): ByteArray {
        val clean = hex.replace(" ", "")
        require(clean.length % 2 == 0) { "hex length must be even" }
        val out = ByteArray(clean.length / 2)
        var i = 0
        while (i < clean.length) {
            out[i / 2] = clean.substring(i, i + 2).toInt(16).toByte()
            i += 2
        }
        return out
    }
}
