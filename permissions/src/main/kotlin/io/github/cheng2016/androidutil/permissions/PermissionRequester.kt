package io.github.cheng2016.androidutil.permissions

import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

/**
 * Runtime permission helper built on the AndroidX Activity Result API.
 *
 * Register once from a [FragmentActivity] or [Fragment] (typically in
 * `onCreate` / as a property initializer). Hosts do **not** need to forward
 * `onRequestPermissionsResult`.
 *
 * ```
 * class MainActivity : AppCompatActivity() {
 *   private val permissions = PermissionRequester.register(this)
 *
 *   fun askCamera() {
 *     permissions.request(android.Manifest.permission.CAMERA) { result ->
 *       when (result.status) {
 *         PermissionStatus.Granted -> { /* proceed */ }
 *         is PermissionStatus.Denied -> { /* show rationale */ }
 *         is PermissionStatus.PermanentlyDenied -> { /* open settings */ }
 *       }
 *     }
 *   }
 * }
 * ```
 */
class PermissionRequester private constructor(
    private val launcher: ActivityResultLauncher<Array<String>>,
    private val isGranted: (String) -> Boolean,
    private val showRationale: (String) -> Boolean,
) {
    @Volatile
    private var pendingCallback: PermissionCallback? = null

    @Volatile
    private var pendingPermissions: List<String> = emptyList()

    /**
     * Returns `true` when every permission in [permissions] is already granted.
     */
    fun hasPermissions(vararg permissions: String): Boolean = permissions.all(isGranted)

    /**
     * Whether the system recommends showing a rationale UI for [permission].
     *
     * Equivalent to [ActivityCompat.shouldShowRequestPermissionRationale].
     */
    fun shouldShowRationale(permission: String): Boolean = showRationale(permission)

    /**
     * Requests the given [permissions].
     *
     * If they are already granted, [callback] is invoked immediately with an
     * all-granted [PermissionRequestResult] without launching a system dialog.
     *
     * Only one request may be in flight at a time.
     */
    fun request(
        vararg permissions: String,
        callback: PermissionCallback,
    ) {
        if (permissions.isEmpty()) {
            callback.onResult(
                PermissionRequestResult(
                    granted = emptyList(),
                    denied = emptyList(),
                    permanentlyDenied = emptyList(),
                ),
            )
            return
        }
        check(pendingCallback == null) { "A permission request is already in progress." }
        val distinct = permissions.distinct()
        if (hasPermissions(*distinct.toTypedArray())) {
            callback.onResult(
                PermissionRequestResult(
                    granted = distinct,
                    denied = emptyList(),
                    permanentlyDenied = emptyList(),
                ),
            )
            return
        }
        pendingPermissions = distinct
        pendingCallback = callback
        launcher.launch(distinct.toTypedArray())
    }

    private fun dispatchResult(grantResults: Map<String, Boolean>) {
        val callback = pendingCallback
        val requested = pendingPermissions
        pendingCallback = null
        pendingPermissions = emptyList()
        if (callback == null) return

        val granted = mutableListOf<String>()
        val denied = mutableListOf<String>()
        val permanentlyDenied = mutableListOf<String>()

        for (permission in requested) {
            val grantedNow = grantResults[permission] == true || isGranted(permission)
            when {
                grantedNow -> granted += permission
                shouldShowRationale(permission) -> denied += permission
                else -> permanentlyDenied += permission
            }
        }

        callback.onResult(
            PermissionRequestResult(
                granted = granted,
                denied = denied,
                permanentlyDenied = permanentlyDenied,
            ),
        )
    }

    companion object {
        /**
         * Registers a requester bound to [activity].
         *
         * Must be called while the activity is at least in the `CREATED` state
         * (property initializer or `onCreate` before `super` returns is fine
         * with AndroidX Activity 1.2+).
         */
        @JvmStatic
        fun register(activity: FragmentActivity): PermissionRequester {
            lateinit var requester: PermissionRequester
            val launcher =
                activity.registerForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions(),
                ) { result ->
                    requester.dispatchResult(result)
                }
            requester =
                PermissionRequester(
                    launcher = launcher,
                    isGranted = { permission ->
                        ContextCompat.checkSelfPermission(activity, permission) ==
                            PackageManager.PERMISSION_GRANTED
                    },
                    showRationale = { permission ->
                        ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
                    },
                )
            return requester
        }

        /**
         * Registers a requester bound to [fragment]'s activity host.
         */
        @JvmStatic
        fun register(fragment: Fragment): PermissionRequester {
            lateinit var requester: PermissionRequester
            val launcher =
                fragment.registerForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions(),
                ) { result ->
                    requester.dispatchResult(result)
                }
            requester =
                PermissionRequester(
                    launcher = launcher,
                    isGranted = { permission ->
                        ContextCompat.checkSelfPermission(fragment.requireContext(), permission) ==
                            PackageManager.PERMISSION_GRANTED
                    },
                    showRationale = fragment::shouldShowRequestPermissionRationale,
                )
            return requester
        }
    }
}
