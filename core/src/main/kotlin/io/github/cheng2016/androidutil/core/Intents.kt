package io.github.cheng2016.androidutil.core

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.net.toUri

/**
 * 常用系统 Intent 构造与安全启动（找不到 Activity 时返回 false，不抛给调用方）。
 */
object Intents {
    /** 分享纯文本。 */
    @JvmStatic
    @JvmOverloads
    fun shareText(
        context: Context,
        text: CharSequence,
        title: CharSequence? = null,
    ): Boolean {
        val intent =
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text.toString())
                if (title != null) putExtra(Intent.EXTRA_TITLE, title.toString())
            }
        return startChooser(context, intent, title)
    }

    /** 打开应用详情设置页（引导用户开权限时常用）。 */
    @JvmStatic
    fun appDetailsSettings(context: Context): Boolean {
        val intent =
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.packageName, null),
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return start(context, intent)
    }

    /** 打开系统浏览器。 */
    @JvmStatic
    fun openUrl(
        context: Context,
        url: String,
    ): Boolean {
        val normalized = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
        val intent = Intent(Intent.ACTION_VIEW, normalized.toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return start(context, intent)
    }

    /** 拨号盘（不直接拨出，无需 CALL_PHONE）。 */
    @JvmStatic
    fun dial(
        context: Context,
        phoneNumber: String,
    ): Boolean {
        val intent =
            Intent(Intent.ACTION_DIAL, "tel:${Uri.encode(phoneNumber)}".toUri())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return start(context, intent)
    }

    /** 发送邮件。 */
    @JvmStatic
    @JvmOverloads
    fun sendEmail(
        context: Context,
        address: String,
        subject: String = "",
        body: String = "",
    ): Boolean {
        val intent =
            Intent(Intent.ACTION_SENDTO, "mailto:".toUri()).apply {
                putExtra(Intent.EXTRA_EMAIL, arrayOf(address))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        return start(context, intent)
    }

    @JvmStatic
    fun start(
        context: Context,
        intent: Intent,
    ): Boolean =
        try {
            if (intent.resolveActivity(context.packageManager) == null) return false
            context.startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        } catch (_: SecurityException) {
            false
        }

    @JvmStatic
    @JvmOverloads
    fun startChooser(
        context: Context,
        intent: Intent,
        title: CharSequence? = null,
    ): Boolean {
        val chooser = Intent.createChooser(intent, title).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return start(context, chooser)
    }
}
