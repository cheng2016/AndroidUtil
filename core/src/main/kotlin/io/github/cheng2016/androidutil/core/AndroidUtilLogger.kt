package io.github.cheng2016.androidutil.core

import android.content.Context
import android.util.Log
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Thread-safe logger that prints to Logcat and optionally appends to app-private
 * `files/logs` (never public external storage).
 *
 * File writing is **disabled by default**. Call [init] before use when file output
 * or package metadata is required; Logcat-only calls work without init when
 * [Config.logcatEnabled] is true.
 *
 * Java usage:
 * ```
 * AndroidUtilLogger.init(context);
 * AndroidUtilLogger.init(context, new AndroidUtilLogger.Config(
 *     AndroidUtilLogger.Level.DEBUG, "App", true, true, 0));
 * AndroidUtilLogger.i("TAG", "hello");
 * AndroidUtilLogger.flush();
 * ```
 */
object AndroidUtilLogger {
    /** Log severity; [OFF] suppresses all output. */
    enum class Level(
        val priority: Int,
    ) {
        VERBOSE(Log.VERBOSE),
        DEBUG(Log.DEBUG),
        INFO(Log.INFO),
        WARN(Log.WARN),
        ERROR(Log.ERROR),
        ASSERT(Log.ASSERT),
        OFF(Int.MAX_VALUE),
    }

    /**
     * Transforms a message before Logcat / file output (e.g. redact tokens).
     */
    fun interface Sanitizer {
        fun sanitize(
            level: Level,
            tag: String,
            message: String,
        ): String
    }

    /**
     * Immutable configuration snapshot.
     *
     * @property minLevel messages below this level are dropped
     * @property defaultTag used when the tag overload is omitted
     * @property logcatEnabled whether to print via [Log]
     * @property fileEnabled whether to append under app-private `files/logs`
     * @property maxMessageLength truncate long messages (0 = no truncate)
     */
    data class Config
        @JvmOverloads
        constructor(
            val minLevel: Level = Level.VERBOSE,
            val defaultTag: String = "AndroidUtil",
            val logcatEnabled: Boolean = true,
            val fileEnabled: Boolean = false,
            val maxMessageLength: Int = 0,
        )

    private val stateLock = Any()

    @Volatile
    private var config: Config = Config()

    @Volatile
    private var sanitizer: Sanitizer? = null

    @Volatile
    private var contextRef: WeakReference<Context>? = null

    @Volatile
    private var packageName: String = ""

    @Volatile
    private var pid: Int = 0

    @Volatile
    private var closed: Boolean = false

    private val pendingWrites = AtomicInteger(0)

    @Volatile
    private var executor: ExecutorService? = null

    private val timeFormat =
        object : ThreadLocal<SimpleDateFormat>() {
            override fun initialValue(): SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
        }

    private val LEVEL_CHARS = charArrayOf('V', 'D', 'I', 'W', 'E', 'A')

    /**
     * Initializes logger with [context] (application context retained weakly)
     * and optional [config]. Safe to call multiple times.
     */
    @JvmStatic
    @JvmOverloads
    fun init(
        context: Context,
        config: Config = Config(),
    ) {
        synchronized(stateLock) {
            closed = false
            this.config = config
            val app = context.applicationContext
            contextRef = WeakReference(app)
            packageName = app.packageName
            pid = android.os.Process.myPid()
            if (config.fileEnabled) {
                ensureExecutorLocked()
                ensureLogDir(app)
            }
        }
    }

    /** Replaces the active [Config]. */
    @JvmStatic
    fun setConfig(config: Config) {
        synchronized(stateLock) {
            this.config = config
            if (config.fileEnabled && !closed) {
                ensureExecutorLocked()
                contextRef?.get()?.let { ensureLogDir(it) }
            }
        }
    }

    /** Current configuration snapshot. */
    @JvmStatic
    fun getConfig(): Config = config

    /** Sets or clears a message [Sanitizer]. */
    @JvmStatic
    fun setSanitizer(sanitizer: Sanitizer?) {
        this.sanitizer = sanitizer
    }

    @JvmStatic
    fun setMinLevel(level: Level) {
        synchronized(stateLock) {
            config = config.copy(minLevel = level)
        }
    }

    @JvmStatic
    @JvmOverloads
    fun v(
        tag: String = config.defaultTag,
        message: String,
        throwable: Throwable? = null,
    ) {
        log(Level.VERBOSE, tag, message, throwable)
    }

    @JvmStatic
    @JvmOverloads
    fun d(
        tag: String = config.defaultTag,
        message: String,
        throwable: Throwable? = null,
    ) {
        log(Level.DEBUG, tag, message, throwable)
    }

    @JvmStatic
    @JvmOverloads
    fun i(
        tag: String = config.defaultTag,
        message: String,
        throwable: Throwable? = null,
    ) {
        log(Level.INFO, tag, message, throwable)
    }

    @JvmStatic
    @JvmOverloads
    fun w(
        tag: String = config.defaultTag,
        message: String,
        throwable: Throwable? = null,
    ) {
        log(Level.WARN, tag, message, throwable)
    }

    @JvmStatic
    @JvmOverloads
    fun e(
        tag: String = config.defaultTag,
        message: String,
        throwable: Throwable? = null,
    ) {
        log(Level.ERROR, tag, message, throwable)
    }

    @JvmStatic
    fun log(
        level: Level,
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ) {
        val cfg = config
        if (closed || level == Level.OFF || level.priority < cfg.minLevel.priority) {
            return
        }
        var text = message
        if (cfg.maxMessageLength > 0 && text.length > cfg.maxMessageLength) {
            text = text.substring(0, cfg.maxMessageLength) + "…"
        }
        sanitizer?.let { text = it.sanitize(level, tag, text) }
        if (throwable != null) {
            text = text + '\n' + stackTraceOf(throwable)
        }
        if (cfg.logcatEnabled) {
            Log.println(level.priority, tag, text)
        }
        if (cfg.fileEnabled) {
            enqueueFileWrite(level, tag, text)
        }
    }

    /**
     * Blocks until pending file writes complete (best-effort).
     * No-op when the background writer is unavailable.
     */
    @JvmStatic
    fun flush() {
        val exec = executor ?: return
        val latch = CountDownLatch(1)
        try {
            exec.execute { latch.countDown() }
            latch.await(5, TimeUnit.SECONDS)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        } catch (_: Exception) {
            // ignored — best effort
        }
        var spins = 0
        while (pendingWrites.get() > 0 && spins++ < 200) {
            try {
                Thread.sleep(10)
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
                break
            }
        }
    }

    /**
     * Flushes and shuts down the background writer. Subsequent file writes are ignored
     * until [init] is called again.
     */
    @JvmStatic
    fun close() {
        synchronized(stateLock) {
            closed = true
        }
        flush()
        synchronized(stateLock) {
            val exec = executor
            executor = null
            exec?.shutdown()
            try {
                exec?.awaitTermination(3, TimeUnit.SECONDS)
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
    }

    /** App-private log directory: `{filesDir}/logs`. */
    @JvmStatic
    fun logDirectory(context: Context): File = File(context.applicationContext.filesDir, "logs")

    private fun ensureExecutorLocked() {
        if (executor == null || executor!!.isShutdown) {
            executor =
                Executors.newSingleThreadExecutor { runnable ->
                    Thread(runnable, "AndroidUtilLogger").apply { isDaemon = true }
                }
        }
    }

    private fun ensureLogDir(context: Context): File {
        val dir = logDirectory(context)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun enqueueFileWrite(
        level: Level,
        tag: String,
        message: String,
    ) {
        if (closed) return
        val ctx = contextRef?.get() ?: return
        val exec =
            synchronized(stateLock) {
                if (closed) return
                ensureExecutorLocked()
                executor
            } ?: return
        val line = formatFileLine(level, tag, message)
        val dir = ensureLogDir(ctx)
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val file = File(dir, "$date.txt")
        pendingWrites.incrementAndGet()
        try {
            exec.execute {
                try {
                    appendLine(file, line)
                } finally {
                    pendingWrites.decrementAndGet()
                }
            }
        } catch (_: Exception) {
            pendingWrites.decrementAndGet()
        }
    }

    private fun formatFileLine(
        level: Level,
        tag: String,
        message: String,
    ): String {
        val ts = timeFormat.get()!!.format(Date())
        val levelChar =
            when (level) {
                Level.VERBOSE -> LEVEL_CHARS[0]
                Level.DEBUG -> LEVEL_CHARS[1]
                Level.INFO -> LEVEL_CHARS[2]
                Level.WARN -> LEVEL_CHARS[3]
                Level.ERROR -> LEVEL_CHARS[4]
                Level.ASSERT -> LEVEL_CHARS[5]
                Level.OFF -> '-'
            }
        return "$ts  $pid/$packageName  $levelChar/$tag  $message${System.lineSeparator()}"
    }

    private fun appendLine(
        file: File,
        line: String,
    ) {
        file.parentFile?.mkdirs()
        BufferedWriter(FileWriter(file, true)).use { writer ->
            writer.write(line)
            writer.flush()
        }
    }

    private fun stackTraceOf(throwable: Throwable): String {
        val sw = StringWriter()
        PrintWriter(sw).use { pw ->
            throwable.printStackTrace(pw)
            var cause = throwable.cause
            while (cause != null) {
                cause.printStackTrace(pw)
                cause = cause.cause
            }
        }
        return sw.toString()
    }
}
