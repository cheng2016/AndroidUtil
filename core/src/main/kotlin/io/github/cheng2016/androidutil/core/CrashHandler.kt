package io.github.cheng2016.androidutil.core

import android.content.Context
import android.os.Build
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 本地崩溃日志记录器：显式 [install] 后接管未捕获异常，把报告写入应用私有目录
 * `{filesDir}/logs/crash/`，再交给此前的默认 [Thread.UncaughtExceptionHandler]。
 *
 * 相对旧版 `util/CrashHandler` 的安全改动：
 * - **不会**写入外部公共存储
 * - **不会**在工具层弹 Toast / 自己起 Looper
 * - **不会**反射整表 [Build] 字段，也不采集 IMEI/MAC 等受限标识
 * - **默认不** `killProcess`；优先链式交给系统 / 其他崩溃 SDK
 *
 * ```
 * CrashHandler.install(applicationContext)
 * // 崩溃文件目录：CrashHandler.crashDirectory(context)
 * ```
 *
 * Java：
 * ```
 * CrashHandler.install(context);
 * File dir = CrashHandler.crashDirectory(context);
 * ```
 */
object CrashHandler {
    private const val TAG = "CrashHandler"

    /**
     * @property chainToDefault 写完本地报告后是否调用安装前的默认处理器（推荐 true）
     * @property alsoLogToAndroidUtilLogger 若已 [AndroidUtilLogger.init]，是否再记一条 ERROR
     * @property includeBuildFingerprint 是否写入 `Build.FINGERPRINT`
     */
    data class Config
        @JvmOverloads
        constructor(
            val chainToDefault: Boolean = true,
            val alsoLogToAndroidUtilLogger: Boolean = true,
            val includeBuildFingerprint: Boolean = true,
        )

    /** 崩溃落盘后的可选回调（勿在回调里做耗时网络上传，除非自建后台线程）。 */
    fun interface Listener {
        fun onCrash(
            thread: Thread,
            throwable: Throwable,
            reportFile: File?,
        )
    }

    private val installed = AtomicBoolean(false)
    private val lock = Any()

    @Volatile
    private var config: Config = Config()

    @Volatile
    private var contextRef: WeakReference<Context>? = null

    @Volatile
    private var previousHandler: Thread.UncaughtExceptionHandler? = null

    @Volatile
    private var listener: Listener? = null

    @Volatile
    private var activeHandler: Thread.UncaughtExceptionHandler? = null

    /**
     * 安装未捕获异常处理器。可重复调用：会先卸载再装新配置。
     */
    @JvmStatic
    @JvmOverloads
    fun install(
        context: Context,
        config: Config = Config(),
    ) {
        synchronized(lock) {
            if (installed.get()) {
                uninstallLocked()
            }
            this.config = config
            val app = context.applicationContext
            contextRef = WeakReference(app)
            previousHandler = Thread.getDefaultUncaughtExceptionHandler()
            val handler =
                Thread.UncaughtExceptionHandler { thread, throwable ->
                    handleUncaught(thread, throwable)
                }
            activeHandler = handler
            Thread.setDefaultUncaughtExceptionHandler(handler)
            installed.set(true)
        }
    }

    /** 恢复安装前的默认处理器。 */
    @JvmStatic
    fun uninstall() {
        synchronized(lock) {
            uninstallLocked()
        }
    }

    @JvmStatic
    fun isInstalled(): Boolean = installed.get()

    /** 设置或清除崩溃回调。 */
    @JvmStatic
    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    /** 应用私有崩溃目录：`{filesDir}/logs/crash`。 */
    @JvmStatic
    fun crashDirectory(context: Context): File =
        File(AndroidUtilLogger.logDirectory(context.applicationContext), "crash")

    /** 列出已落盘的崩溃文件（按修改时间倒序）。 */
    @JvmStatic
    fun listCrashFiles(context: Context): List<File> {
        val dir = crashDirectory(context)
        if (!dir.isDirectory) return emptyList()
        return dir
            .listFiles()
            .orEmpty()
            .filter { it.isFile && it.name.endsWith(".txt", ignoreCase = true) }
            .sortedByDescending { it.lastModified() }
    }

    /**
     * 主动写入一份崩溃报告（便于测试或捕获已处理但仍需留档的异常）。
     * 不触发进程退出。
     *
     * @return 成功时返回文件，失败返回 `null`
     */
    @JvmStatic
    @JvmOverloads
    fun writeReport(
        context: Context,
        throwable: Throwable,
        threadName: String = Thread.currentThread().name,
    ): File? = writeReportInternal(context.applicationContext, throwable, threadName)

    private fun uninstallLocked() {
        if (!installed.get()) return
        val current = Thread.getDefaultUncaughtExceptionHandler()
        if (current === activeHandler) {
            Thread.setDefaultUncaughtExceptionHandler(previousHandler)
        }
        activeHandler = null
        previousHandler = null
        contextRef = null
        installed.set(false)
    }

    private fun handleUncaught(
        thread: Thread,
        throwable: Throwable,
    ) {
        var reportFile: File? = null
        try {
            val ctx = contextRef?.get()
            if (ctx != null) {
                reportFile = writeReportInternal(ctx, throwable, thread.name)
            }
            if (config.alsoLogToAndroidUtilLogger) {
                AndroidUtilLogger.e(
                    TAG,
                    "Uncaught exception on thread=${thread.name}; file=${reportFile?.absolutePath}",
                    throwable,
                )
                AndroidUtilLogger.flush()
            }
            listener?.onCrash(thread, throwable, reportFile)
        } catch (t: Throwable) {
            Log.e(TAG, "Failed while recording crash", t)
        } finally {
            val previous = previousHandler
            if (config.chainToDefault && previous != null && previous !== activeHandler) {
                previous.uncaughtException(thread, throwable)
            } else {
                // 无上游处理器时仍打印，避免静默吞掉
                Log.e(TAG, "Uncaught exception on thread ${thread.name}", throwable)
            }
        }
    }

    private fun writeReportInternal(
        context: Context,
        throwable: Throwable,
        threadName: String,
    ): File? {
        return try {
            val dir = crashDirectory(context)
            if (!dir.exists() && !dir.mkdirs()) {
                Log.e(TAG, "Unable to create crash directory: $dir")
                return null
            }
            val stamp =
                SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_SSS", Locale.US).format(Date())
            val file = File(dir, "crash_$stamp.txt")
            val body = buildReport(context, throwable, threadName)
            file.writeText(body, Charsets.UTF_8)
            file
        } catch (e: Exception) {
            Log.e(TAG, "Unable to write crash report", e)
            null
        }
    }

    private fun buildReport(
        context: Context,
        throwable: Throwable,
        threadName: String,
    ): String {
        val sb = StringBuilder(4096)
        sb.append("time=").append(SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())).append('\n')
        sb.append("thread=").append(threadName).append('\n')
        sb.append("package=").append(AppInfo.packageName(context)).append('\n')
        sb.append("versionName=").append(AppInfo.versionName(context) ?: "null").append('\n')
        sb.append("versionCode=").append(AppInfo.versionCode(context)).append('\n')
        sb.append("appLabel=").append(AppInfo.appLabel(context) ?: "null").append('\n')
        sb.append("sdkInt=").append(Build.VERSION.SDK_INT).append('\n')
        sb.append("release=").append(Build.VERSION.RELEASE).append('\n')
        sb.append("brand=").append(Build.BRAND).append('\n')
        sb.append("manufacturer=").append(Build.MANUFACTURER).append('\n')
        sb.append("model=").append(Build.MODEL).append('\n')
        sb.append("device=").append(Build.DEVICE).append('\n')
        sb.append("product=").append(Build.PRODUCT).append('\n')
        if (config.includeBuildFingerprint) {
            sb.append("fingerprint=").append(Build.FINGERPRINT).append('\n')
        }
        sb.append('\n')
        sb.append(stackTraceOf(throwable))
        return sb.toString()
    }

    private fun stackTraceOf(throwable: Throwable): String {
        val sw = StringWriter()
        PrintWriter(sw).use { pw ->
            throwable.printStackTrace(pw)
            var cause = throwable.cause
            while (cause != null) {
                pw.println("Caused by:")
                cause.printStackTrace(pw)
                cause = cause.cause
            }
        }
        return sw.toString()
    }
}
