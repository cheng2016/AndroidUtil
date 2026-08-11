package io.github.cheng2016.androidutil.core

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.util.Properties

/**
 * Reads assets / raw resources with automatic stream closing.
 */
object ResourceReader {
    /**
     * Reads an entire asset file as text, or `null` if missing / unreadable.
     */
    @JvmStatic
    @JvmOverloads
    fun readAssetText(
        context: Context,
        fileName: String,
        charset: Charset = Charsets.UTF_8,
    ): String? {
        if (fileName.isBlank()) return null
        return try {
            context.applicationContext.assets.open(fileName).use { input ->
                InputStreamReader(input, charset).buffered().use { reader ->
                    reader.readText()
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Reads a raw resource as text, or `null` if unreadable.
     *
     * @param resId `R.raw.*` resource id
     */
    @JvmStatic
    @JvmOverloads
    fun readRawText(
        context: Context,
        resId: Int,
        charset: Charset = Charsets.UTF_8,
    ): String? =
        try {
            context.applicationContext.resources.openRawResource(resId).use { input ->
                InputStreamReader(input, charset).buffered().use { reader ->
                    reader.readText()
                }
            }
        } catch (_: Exception) {
            null
        }

    /**
     * Loads a `.properties` file from assets. Returns an empty [Properties] on failure.
     */
    @JvmStatic
    @JvmOverloads
    fun readAssetProperties(
        context: Context,
        fileName: String,
        charset: Charset = Charsets.UTF_8,
    ): Properties {
        val props = Properties()
        if (fileName.isBlank()) return props
        try {
            context.applicationContext.assets.open(fileName).use { input ->
                InputStreamReader(input, charset).use { reader ->
                    props.load(reader)
                }
            }
        } catch (_: Exception) {
            // leave empty
        }
        return props
    }

    /**
     * Convenience for a single property key from an assets properties file.
     */
    @JvmStatic
    @JvmOverloads
    fun getAssetProperty(
        context: Context,
        fileName: String,
        key: String,
        defaultValue: String = "",
        charset: Charset = Charsets.UTF_8,
    ): String =
        readAssetProperties(context, fileName, charset).getProperty(key, defaultValue)
            ?: defaultValue

    /**
     * Reads asset text line-by-line (joined without separators between lines — preserves
     * only newline characters from the source via [BufferedReader.readText] semantics).
     * Prefer [readAssetText] for new code.
     */
    @JvmStatic
    @JvmOverloads
    fun readAssetLines(
        context: Context,
        fileName: String,
        charset: Charset = Charsets.UTF_8,
    ): List<String> {
        if (fileName.isBlank()) return emptyList()
        return try {
            context.applicationContext.assets.open(fileName).use { input ->
                BufferedReader(InputStreamReader(input, charset)).use { reader ->
                    reader.readLines()
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
