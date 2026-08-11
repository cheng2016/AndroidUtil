package io.github.cheng2016.androidutil.core

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ResourceReaderTest {
    @Test
    fun readAssetTextAndProperties() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val text = ResourceReader.readAssetText(context, "sample.txt")
        assertEquals("hello-asset", text?.trim())

        val value = ResourceReader.getAssetProperty(context, "sample.properties", "greeting")
        assertEquals("world", value)

        val props = ResourceReader.readAssetProperties(context, "sample.properties")
        assertEquals("world", props.getProperty("greeting"))
    }

    @Test
    fun missingAssetReturnsNullOrEmpty() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertNull(ResourceReader.readAssetText(context, "does-not-exist.txt"))
        assertTrue(ResourceReader.readAssetProperties(context, "missing.properties").isEmpty)
        assertEquals("fallback", ResourceReader.getAssetProperty(context, "missing.properties", "k", "fallback"))
    }
}
