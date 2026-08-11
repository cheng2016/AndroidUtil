package io.github.cheng2016.androidutil.core

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class PreferencesTest {
    private lateinit var prefs: Preferences

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        prefs = Preferences.of(context, "prefs_test")
        prefs.clear()
    }

    @Test
    fun putAndGetTypedValues() {
        prefs.putString("s", "hello")
        prefs.putBoolean("b", true)
        prefs.putInt("i", 42)
        prefs.putLong("l", 99L)
        prefs.putFloat("f", 1.5f)

        assertEquals("hello", prefs.getString("s"))
        assertTrue(prefs.getBoolean("b"))
        assertEquals(42, prefs.getInt("i"))
        assertEquals(99L, prefs.getLong("l"))
        assertEquals(1.5f, prefs.getFloat("f"), 0.001f)
        assertTrue(prefs.contains("s"))
    }

    @Test
    fun removeAndDefaults() {
        prefs.putString("k", "v")
        prefs.remove("k")
        assertFalse(prefs.contains("k"))
        assertEquals("fallback", prefs.getString("k", "fallback"))
        assertEquals(7, prefs.getInt("missing", 7))
    }
}
