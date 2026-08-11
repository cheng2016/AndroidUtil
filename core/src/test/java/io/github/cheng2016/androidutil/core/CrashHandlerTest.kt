package io.github.cheng2016.androidutil.core

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class CrashHandlerTest {
    private lateinit var context: Context
    private var previous: Thread.UncaughtExceptionHandler? = null

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        previous = Thread.getDefaultUncaughtExceptionHandler()
        CrashHandler.uninstall()
        CrashHandler.setListener(null)
        crashDir().listFiles()?.forEach { it.delete() }
    }

    @After
    fun tearDown() {
        CrashHandler.uninstall()
        CrashHandler.setListener(null)
        Thread.setDefaultUncaughtExceptionHandler(previous)
    }

    @Test
    fun writesReportUnderAppPrivateCrashDirectory() {
        val file =
            CrashHandler.writeReport(
                context,
                IllegalStateException("boom-for-test"),
                threadName = "test-thread",
            )
        assertNotNull(file)
        assertTrue(file!!.exists())
        assertEquals(crashDir().absolutePath, file.parentFile?.absolutePath)
        assertTrue(file.parentFile!!.absolutePath.endsWith("files${File.separator}logs${File.separator}crash") ||
            file.parentFile!!.path.replace('\\', '/').endsWith("files/logs/crash"))

        val text = file.readText()
        assertTrue(text.contains("boom-for-test"))
        assertTrue(text.contains("thread=test-thread"))
        assertTrue(text.contains("package="))
        assertTrue(text.contains("sdkInt="))
        assertFalse(text.contains("IMEI", ignoreCase = true))
    }

    @Test
    fun installChainsToPreviousHandler() {
        var chained = false
        Thread.setDefaultUncaughtExceptionHandler { _, _ -> chained = true }

        CrashHandler.install(
            context,
            CrashHandler.Config(chainToDefault = true, alsoLogToAndroidUtilLogger = false),
        )
        assertTrue(CrashHandler.isInstalled())

        val handler = Thread.getDefaultUncaughtExceptionHandler()
        assertNotNull(handler)
        handler!!.uncaughtException(Thread.currentThread(), RuntimeException("chain-me"))

        assertTrue(chained)
        assertTrue(CrashHandler.listCrashFiles(context).isNotEmpty())
    }

    @Test
    fun uninstallRestoresPreviousHandler() {
        val sentinel = Thread.UncaughtExceptionHandler { _, _ -> }
        Thread.setDefaultUncaughtExceptionHandler(sentinel)
        CrashHandler.install(context)
        CrashHandler.uninstall()
        assertFalse(CrashHandler.isInstalled())
        assertEquals(sentinel, Thread.getDefaultUncaughtExceptionHandler())
    }

    private fun crashDir(): File = CrashHandler.crashDirectory(context)
}
