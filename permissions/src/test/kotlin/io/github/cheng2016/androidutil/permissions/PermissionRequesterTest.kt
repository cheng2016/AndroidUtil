package io.github.cheng2016.androidutil.permissions

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import java.util.concurrent.atomic.AtomicReference

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class PermissionRequesterTest {
    @Test
    fun hasPermissions_falseWhenMissing() {
        val controller = Robolectric.buildActivity(TestActivity::class.java)
        val activity = controller.get()
        val requester = PermissionRequester.register(activity)
        controller.setup()

        assertFalse(requester.hasPermissions(Manifest.permission.CAMERA))
    }

    @Test
    fun hasPermissions_trueWhenGranted() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        Shadows.shadowOf(app).grantPermissions(Manifest.permission.CAMERA)

        val controller = Robolectric.buildActivity(TestActivity::class.java)
        val activity = controller.get()
        val requester = PermissionRequester.register(activity)
        controller.setup()

        assertTrue(requester.hasPermissions(Manifest.permission.CAMERA))
        assertEquals(
            PackageManager.PERMISSION_GRANTED,
            ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA),
        )
    }

    @Test
    fun request_whenAlreadyGranted_invokesCallbackWithoutDialog() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        Shadows.shadowOf(app).grantPermissions(Manifest.permission.CAMERA)

        val controller = Robolectric.buildActivity(TestActivity::class.java)
        val activity = controller.get()
        val requester = PermissionRequester.register(activity)
        controller.setup()
        val received = AtomicReference<PermissionRequestResult>()

        requester.request(Manifest.permission.CAMERA) { result ->
            received.set(result)
        }

        val result = received.get()
        assertTrue(result.allGranted)
        assertEquals(PermissionStatus.Granted, result.status)
        assertEquals(listOf(Manifest.permission.CAMERA), result.granted)
    }

    @Test
    fun request_emptyLogicalGroup_isImmediatelyGranted() {
        val controller = Robolectric.buildActivity(TestActivity::class.java)
        val activity = controller.get()
        val requester = PermissionRequester.register(activity)
        controller.setup()
        val received = AtomicReference<PermissionRequestResult>()

        requester.request(*emptyArray()) { result -> received.set(result) }

        assertTrue(received.get().allGranted)
        assertEquals(PermissionStatus.Granted, received.get().status)
    }

    @Test
    fun permissionRequestResult_statusPrefersPermanentlyDenied() {
        val mixed =
            PermissionRequestResult(
                granted = listOf(Manifest.permission.CAMERA),
                denied = listOf(Manifest.permission.RECORD_AUDIO),
                permanentlyDenied = listOf(Manifest.permission.READ_CONTACTS),
            )
        assertTrue(mixed.status is PermissionStatus.PermanentlyDenied)
        assertEquals(
            listOf(Manifest.permission.READ_CONTACTS),
            (mixed.status as PermissionStatus.PermanentlyDenied).permissions,
        )

        val deniedOnly =
            PermissionRequestResult(
                granted = emptyList(),
                denied = listOf(Manifest.permission.CAMERA),
                permanentlyDenied = emptyList(),
            )
        assertTrue(deniedOnly.status is PermissionStatus.Denied)
    }

    @Test
    fun register_fromFragment_usesFragmentActivityHost() {
        val activity = Robolectric.buildActivity(TestActivity::class.java).setup().get()
        val fragment = TestFragment()
        val requester = PermissionRequester.register(fragment)
        activity.supportFragmentManager
            .beginTransaction()
            .add(fragment, "test")
            .commitNow()

        assertFalse(requester.hasPermissions(Manifest.permission.CAMERA))
    }

    class TestActivity : FragmentActivity()

    class TestFragment : Fragment()
}
