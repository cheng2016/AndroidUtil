package io.github.cheng2016.androidutil.core

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * 通知渠道一键创建（API 26+；更低版本 no-op）。
 */
object NotificationChannels {
    /**
     * 创建或更新渠道。已存在时系统会忽略 importance 变更（平台限制）。
     *
     * @param importance 使用 [NotificationManager.IMPORTANCE_*] 常量；默认 DEFAULT。
     */
    @JvmStatic
    @JvmOverloads
    fun ensure(
        context: Context,
        id: String,
        name: CharSequence,
        importance: Int = defaultImportance(),
        description: String? = null,
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        ensureOreo(context, id, name, importance, description)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Suppress("WrongConstant")
    private fun ensureOreo(
        context: Context,
        id: String,
        name: CharSequence,
        importance: Int,
        description: String?,
    ) {
        val manager =
            context.applicationContext.getSystemService(NotificationManager::class.java) ?: return
        val channel =
            NotificationChannel(id, name, importance).apply {
                if (description != null) this.description = description
            }
        manager.createNotificationChannel(channel)
    }

    /** 渠道是否已存在（API 26+）。 */
    @JvmStatic
    fun exists(
        context: Context,
        id: String,
    ): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return true
        val manager =
            context.applicationContext.getSystemService(NotificationManager::class.java) ?: return false
        return manager.getNotificationChannel(id) != null
    }

    private fun defaultImportance(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NotificationManager.IMPORTANCE_DEFAULT
        } else {
            3
        }
}
