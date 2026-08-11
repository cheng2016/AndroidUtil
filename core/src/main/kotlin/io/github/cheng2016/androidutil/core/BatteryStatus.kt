package io.github.cheng2016.androidutil.core

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import java.io.File

/**
 * 电池状态快照（不持续监听；需要监听请自行注册系统广播或用 Job）。
 */
object BatteryStatus {
    data class Snapshot(
        val levelPercent: Int,
        val charging: Boolean,
        val chargingUsb: Boolean,
        val chargingAc: Boolean,
        val chargingWireless: Boolean,
    )

    @JvmStatic
    fun snapshot(context: Context): Snapshot {
        val app = context.applicationContext
        val bm = app.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY).coerceIn(0, 100)
        val intent = stickyBattery(app)
        val plugged = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
        val usb = plugged == BatteryManager.BATTERY_PLUGGED_USB
        val ac = plugged == BatteryManager.BATTERY_PLUGGED_AC
        val wireless = plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS
        val charging = bm.isCharging || usb || ac || wireless
        return Snapshot(
            levelPercent = level,
            charging = charging,
            chargingUsb = usb,
            chargingAc = ac,
            chargingWireless = wireless,
        )
    }

    private fun stickyBattery(app: Context): Intent? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            app.registerReceiver(
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED),
                Context.RECEIVER_NOT_EXPORTED,
            )
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            app.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        }
}

/**
 * 存储空间查询（应用私有目录 / 外部文件目录可用空间）。
 */
object StorageSpace {
    data class Space(
        val totalBytes: Long,
        val freeBytes: Long,
    ) {
        val usedBytes: Long get() = (totalBytes - freeBytes).coerceAtLeast(0L)
    }

    /** 内部 files 目录所在分区空间。 */
    @JvmStatic
    fun internal(context: Context): Space = of(context.applicationContext.filesDir)

    /** 外部应用专属目录空间；不可用时回退 internal。 */
    @JvmStatic
    fun externalApp(context: Context): Space {
        val dir = context.applicationContext.getExternalFilesDir(null) ?: return internal(context)
        return of(dir)
    }

    @JvmStatic
    fun of(path: File): Space {
        return try {
            val stat = StatFs(path.absolutePath)
            val total = stat.blockCountLong * stat.blockSizeLong
            val free = stat.availableBlocksLong * stat.blockSizeLong
            Space(totalBytes = total, freeBytes = free)
        } catch (_: Exception) {
            Space(0L, 0L)
        }
    }

    /** 外部共享存储是否已挂载可读。 */
    @JvmStatic
    fun isExternalMounted(): Boolean =
        Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED ||
            Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED_READ_ONLY
}
