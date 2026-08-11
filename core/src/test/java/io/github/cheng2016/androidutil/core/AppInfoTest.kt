package io.github.cheng2016.androidutil.core

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AppInfoTest {
    @Test
    fun readsPackageMetadata() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val pkg = AppInfo.packageName(context)
        assertNotNull(pkg)
        assertFalse(pkg.isBlank())
        // Robolectric default package / version are present
        assertTrue(AppInfo.versionCode(context) >= 0)
        // versionName may be null depending on manifest; must not throw
        AppInfo.versionName(context)
        AppInfo.appLabel(context)
        assertEquals(pkg, context.packageName)
    }
}
