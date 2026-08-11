package io.github.cheng2016.androidutil.permissions

import android.Manifest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class PermissionGroupsTest {
    @Test
    @Config(sdk = [32])
    fun notifications_emptyBeforeTiramisu() {
        assertTrue(PermissionGroups.notifications().isEmpty())
    }

    @Test
    @Config(sdk = [33])
    fun notifications_postNotificationsOnTiramisu() {
        assertArrayEquals(
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            PermissionGroups.notifications(),
        )
    }

    @Test
    @Config(sdk = [32])
    fun media_usesStorageBeforeTiramisu() {
        assertArrayEquals(
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            PermissionGroups.images(),
        )
        assertArrayEquals(
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            PermissionGroups.video(),
        )
        assertArrayEquals(
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            PermissionGroups.audio(),
        )
        assertArrayEquals(
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            PermissionGroups.mediaVisual(),
        )
        assertArrayEquals(
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            PermissionGroups.mediaAll(),
        )
    }

    @Test
    @Config(sdk = [33])
    fun media_usesGranularPermissionsOnTiramisu() {
        assertArrayEquals(
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
            PermissionGroups.images(),
        )
        assertArrayEquals(
            arrayOf(Manifest.permission.READ_MEDIA_VIDEO),
            PermissionGroups.video(),
        )
        assertArrayEquals(
            arrayOf(Manifest.permission.READ_MEDIA_AUDIO),
            PermissionGroups.audio(),
        )
        assertEquals(
            listOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
            ),
            PermissionGroups.mediaVisual().toList(),
        )
        assertEquals(
            listOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO,
            ),
            PermissionGroups.mediaAll().toList(),
        )
    }

    @Test
    @Config(sdk = [34])
    fun media_stableOnApi34() {
        assertEquals(
            Manifest.permission.READ_MEDIA_IMAGES,
            PermissionGroups.images().single(),
        )
        assertEquals(
            Manifest.permission.POST_NOTIFICATIONS,
            PermissionGroups.notifications().single(),
        )
    }
}
