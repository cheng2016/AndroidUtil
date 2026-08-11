package io.github.cheng2016.androidutil.core

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.PersistableBundle

/**
 * 现代剪贴板封装：支持 Android 13+ 敏感内容标记，避免系统自动弹出完整预览。
 */
object ClipboardHelper {
    /** 读取当前主剪贴文本，无内容时返回 `null`。 */
    @JvmStatic
    fun getText(context: Context): CharSequence? {
        val clipboard = clipboard(context) ?: return null
        val clip = clipboard.primaryClip ?: return null
        if (clip.itemCount <= 0) return null
        return clip.getItemAt(0).coerceToText(context.applicationContext)
    }

    /**
     * 写入纯文本。
     *
     * @param isSensitive API 33+ 时标记为敏感（密码等），系统将弱化预览。
     */
    @JvmStatic
    @JvmOverloads
    fun setText(
        context: Context,
        text: CharSequence,
        label: String = "text",
        isSensitive: Boolean = false,
    ) {
        val clipboard = clipboard(context) ?: return
        val clip = ClipData.newPlainText(label, text)
        if (isSensitive && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val extras = PersistableBundle()
            extras.putBoolean(ClipDescriptionCompat.EXTRA_IS_SENSITIVE, true)
            clip.description.extras = extras
        }
        clipboard.setPrimaryClip(clip)
    }

    /** 清空主剪贴板（API 28+ 用 clearPrimaryClip，更低版本写入空文本）。 */
    @JvmStatic
    fun clear(context: Context) {
        val clipboard = clipboard(context) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            clipboard.clearPrimaryClip()
        } else {
            clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
        }
    }

    /** 是否有主剪贴内容。 */
    @JvmStatic
    fun hasText(context: Context): Boolean = !getText(context).isNullOrEmpty()

    private fun clipboard(context: Context): ClipboardManager? =
        context.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager

    /** 避免直接依赖 hidden 常量名差异。 */
    private object ClipDescriptionCompat {
        const val EXTRA_IS_SENSITIVE = "android.content.extra.IS_SENSITIVE"
    }
}
