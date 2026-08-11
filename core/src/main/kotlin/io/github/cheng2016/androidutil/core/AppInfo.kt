package io.github.cheng2016.androidutil.core

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

/**
 * Read-only helpers for the host application's package metadata.
 *
 * Does **not** collect device identifiers (IMEI, MAC, advertising id, etc.).
 */
object AppInfo {
    /** Application package name. */
    @JvmStatic
    fun packageName(context: Context): String = context.applicationContext.packageName

    /**
     * [android.content.pm.PackageInfo.versionName], or `null` if unavailable.
     */
    @JvmStatic
    fun versionName(context: Context): String? = packageInfo(context)?.versionName

    /**
     * Long version code (`versionCode` / `getLongVersionCode()`), or `0` if unavailable.
     */
    @JvmStatic
    fun versionCode(context: Context): Long {
        val info = packageInfo(context) ?: return 0L
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            info.versionCode.toLong()
        }
    }

    /**
     * Localized application label from the package manager, or `null` if unavailable.
     */
    @JvmStatic
    fun appLabel(context: Context): CharSequence? {
        val app = context.applicationContext
        return try {
            val pm = app.packageManager
            val appInfo = pm.getApplicationInfo(app.packageName, 0)
            pm.getApplicationLabel(appInfo)
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
    }

    private fun packageInfo(context: Context): android.content.pm.PackageInfo? {
        val app = context.applicationContext
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                app.packageManager.getPackageInfo(
                    app.packageName,
                    PackageManager.PackageInfoFlags.of(0),
                )
            } else {
                @Suppress("DEPRECATION")
                app.packageManager.getPackageInfo(app.packageName, 0)
            }
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
    }
}
