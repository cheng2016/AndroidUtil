package io.github.cheng2016.androidutil.core

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream

/**
 * `content://` Uri 元数据与安全拷贝到应用私有缓存（适配分区存储选文件场景）。
 *
 * 命名刻意避开 [android.content.ContentUris]，避免 import 冲突。
 */
object UriFiles {
    data class Meta(
        val displayName: String?,
        val sizeBytes: Long,
        val mimeType: String?,
    )

    /** 查询显示名、大小、MIME；失败时字段可能为 null / -1。 */
    @JvmStatic
    fun meta(
        context: Context,
        uri: Uri,
    ): Meta {
        val resolver = context.applicationContext.contentResolver
        val mime = resolver.getType(uri)
        var name: String? = null
        var size = -1L
        query(resolver, uri)?.use { cursor ->
            val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                if (nameIdx >= 0) name = cursor.getString(nameIdx)
                if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) size = cursor.getLong(sizeIdx)
            }
        }
        return Meta(displayName = name, sizeBytes = size, mimeType = mime)
    }

    /**
     * 将 Uri 内容拷贝到 cacheDir 下的文件。
     * @return 成功返回目标文件，失败返回 null
     */
    @JvmStatic
    @JvmOverloads
    fun copyToCache(
        context: Context,
        uri: Uri,
        fileName: String? = null,
    ): File? {
        val app = context.applicationContext
        val meta = meta(app, uri)
        val targetName =
            (fileName ?: meta.displayName ?: "content_${System.currentTimeMillis()}")
                .replace(Regex("""[\\/:*?"<>|]"""), "_")
        val outFile = File(app.cacheDir, targetName)
        return try {
            app.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(outFile).use { output -> input.copyTo(output) }
            } ?: return null
            outFile
        } catch (_: Exception) {
            outFile.delete()
            null
        }
    }

    private fun query(
        resolver: ContentResolver,
        uri: Uri,
    ): Cursor? =
        try {
            resolver.query(uri, null, null, null, null)
        } catch (_: Exception) {
            null
        }
}
