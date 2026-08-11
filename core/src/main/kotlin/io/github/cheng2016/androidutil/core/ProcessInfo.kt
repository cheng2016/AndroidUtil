package io.github.cheng2016.androidutil.core

import android.app.ActivityManager
import android.content.Context
import android.os.Process

/**
 * 进程信息：判断主进程、当前进程名（不采集设备标识）。
 */
object ProcessInfo {
    /** 当前是否运行在应用主进程（包名与进程名相同）。 */
    @JvmStatic
    fun isMainProcess(context: Context): Boolean {
        val packageName = context.applicationContext.packageName
        return processName(context) == packageName
    }

    /** 当前进程名；失败时返回空字符串。 */
    @JvmStatic
    fun processName(context: Context): String {
        val pid = Process.myPid()
        val am =
            context.applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
                ?: return ""
        return try {
            am.runningAppProcesses
                ?.firstOrNull { it.pid == pid }
                ?.processName
                .orEmpty()
        } catch (_: Exception) {
            ""
        }
    }
}
