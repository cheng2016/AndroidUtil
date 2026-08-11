package io.github.cheng2016.androidutil.core

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Thin, type-safe wrapper around [SharedPreferences] that always persists with [SharedPreferences.Editor.apply].
 *
 * ```
 * val prefs = Preferences.of(context)
 * prefs.putString("token", value)
 * val token = prefs.getString("token")
 * ```
 *
 * Java:
 * ```
 * Preferences prefs = Preferences.of(context, "my_prefs");
 * prefs.putBoolean("dark", true);
 * ```
 */
class Preferences private constructor(
    private val prefs: SharedPreferences,
) {
    /** Returns whether [key] exists. */
    fun contains(key: String): Boolean = prefs.contains(key)

    @JvmOverloads
    fun getString(
        key: String,
        defaultValue: String = "",
    ): String = prefs.getString(key, defaultValue) ?: defaultValue

    fun putString(
        key: String,
        value: String,
    ) {
        prefs.edit { putString(key, value) }
    }

    @JvmOverloads
    fun getBoolean(
        key: String,
        defaultValue: Boolean = false,
    ): Boolean = prefs.getBoolean(key, defaultValue)

    fun putBoolean(
        key: String,
        value: Boolean,
    ) {
        prefs.edit { putBoolean(key, value) }
    }

    @JvmOverloads
    fun getInt(
        key: String,
        defaultValue: Int = 0,
    ): Int = prefs.getInt(key, defaultValue)

    fun putInt(
        key: String,
        value: Int,
    ) {
        prefs.edit { putInt(key, value) }
    }

    @JvmOverloads
    fun getLong(
        key: String,
        defaultValue: Long = 0L,
    ): Long = prefs.getLong(key, defaultValue)

    fun putLong(
        key: String,
        value: Long,
    ) {
        prefs.edit { putLong(key, value) }
    }

    @JvmOverloads
    fun getFloat(
        key: String,
        defaultValue: Float = 0f,
    ): Float = prefs.getFloat(key, defaultValue)

    fun putFloat(
        key: String,
        value: Float,
    ) {
        prefs.edit { putFloat(key, value) }
    }

    /** Removes [key] asynchronously via apply. */
    fun remove(key: String) {
        prefs.edit { remove(key) }
    }

    /** Clears all keys asynchronously via apply. */
    fun clear() {
        prefs.edit { clear() }
    }

    /** Underlying [SharedPreferences] for advanced listeners / migration. */
    fun sharedPreferences(): SharedPreferences = prefs

    companion object {
        private const val DEFAULT_NAME = "android_util_prefs"

        /**
         * Opens preferences named [name], or the library default when [name] is null/blank.
         */
        @JvmStatic
        @JvmOverloads
        fun of(
            context: Context,
            name: String? = null,
        ): Preferences {
            val prefsName = name?.takeIf { it.isNotBlank() } ?: DEFAULT_NAME
            val prefs =
                context.applicationContext.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
            return Preferences(prefs)
        }

        /** Wraps an existing [SharedPreferences] instance. */
        @JvmStatic
        fun wrap(sharedPreferences: SharedPreferences): Preferences = Preferences(sharedPreferences)
    }
}
