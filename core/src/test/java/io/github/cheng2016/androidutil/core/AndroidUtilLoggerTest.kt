package io.github.cheng2016.androidutil.core

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
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
class AndroidUtilLoggerTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        AndroidUtilLogger.close()
        AndroidUtilLogger.setSanitizer(null)
    }

    @Test
    fun fileLoggingDisabledByDefault() {
        AndroidUtilLogger.init(context)
        assertEquals(false, AndroidUtilLogger.getConfig().fileEnabled)
    }

    @Test
    fun writesToAppPrivateLogsDirectory() {
        val logDir = AndroidUtilLogger.logDirectory(context)
        logDir.listFiles()?.forEach { it.delete() }

        AndroidUtilLogger.init(
            context,
            AndroidUtilLogger.Config(
                minLevel = AndroidUtilLogger.Level.DEBUG,
                logcatEnabled = false,
                fileEnabled = true,
            ),
        )
        AndroidUtilLogger.i("TestTag", "hello-private-log")
        AndroidUtilLogger.flush()

        assertTrue(logDir.exists())
        assertEquals(context.filesDir.resolve("logs").absolutePath, logDir.absolutePath)
        val files = logDir.listFiles().orEmpty().filter { it.isFile }
        assertTrue(files.isNotEmpty())
        val content = files.joinToString("\n") { it.readText() }
        assertTrue(content.contains("hello-private-log"))
        assertTrue(content.contains("TestTag"))
    }

    @Test
    fun sanitizerRedactsSecrets() {
        val logDir = AndroidUtilLogger.logDirectory(context)
        logDir.mkdirs()
        logDir.listFiles()?.forEach { it.delete() }

        AndroidUtilLogger.init(
            context,
            AndroidUtilLogger.Config(logcatEnabled = false, fileEnabled = true),
        )
        AndroidUtilLogger.setSanitizer(RedactingSanitizer)
        AndroidUtilLogger.w("Auth", "password=super-secret token=abc123")
        AndroidUtilLogger.flush()

        val content =
            logDir
                .listFiles()
                .orEmpty()
                .filter { it.isFile }
                .joinToString("\n") { it.readText() }
        assertTrue(content.contains("***"))
        assertTrue(!content.contains("super-secret"))
        assertTrue(!content.contains("abc123"))
    }

    @Test
    fun logDirectoryIsUnderFilesDir() {
        val dir = AndroidUtilLogger.logDirectory(context)
        assertNotNull(dir)
        assertTrue(dir.path.contains(File.separator + "files"))
        assertTrue(dir.name == "logs" || dir.path.endsWith("${File.separator}logs"))
    }
}
