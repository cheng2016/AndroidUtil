package io.github.cheng2016.androidutil.permissions

import android.Manifest
import android.os.Build

/**
 * Helpers that expand logical permission groups into the concrete runtime
 * permissions required on the current SDK.
 *
 * Especially useful for Android 13+ (API 33) notification and photo/video/audio
 * media permissions, while remaining safe on older devices.
 */
object PermissionGroups {
    /**
     * Notification permission for Android 13+.
     *
     * Returns an empty array on API &lt; 33 where the permission is not required.
     */
    @JvmStatic
    fun notifications(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            emptyArray()
        }

    /**
     * Read access to images.
     *
     * - API 33+: [Manifest.permission.READ_MEDIA_IMAGES]
     * - API 32 and below: [Manifest.permission.READ_EXTERNAL_STORAGE]
     */
    @JvmStatic
    fun images(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

    /**
     * Read access to video.
     *
     * - API 33+: [Manifest.permission.READ_MEDIA_VIDEO]
     * - API 32 and below: [Manifest.permission.READ_EXTERNAL_STORAGE]
     */
    @JvmStatic
    fun video(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

    /**
     * Read access to audio.
     *
     * - API 33+: [Manifest.permission.READ_MEDIA_AUDIO]
     * - API 32 and below: [Manifest.permission.READ_EXTERNAL_STORAGE]
     */
    @JvmStatic
    fun audio(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

    /**
     * Combined visual media (images + video).
     *
     * On API 33+ this returns both media permissions; on older APIs it returns
     * a single [Manifest.permission.READ_EXTERNAL_STORAGE] entry.
     */
    @JvmStatic
    fun mediaVisual(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

    /**
     * All partitioned media permissions (images, video, audio).
     */
    @JvmStatic
    fun mediaAll(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO,
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
}
