package io.github.cheng2016.androidutil.permissions

/**
 * Outcome of a runtime permission request.
 *
 * Use [PermissionRequestResult] when you need the per-permission breakdown;
 * [status] collapses mixed results into a single sealed value.
 */
sealed class PermissionStatus {
    /** Every requested permission was granted. */
    data object Granted : PermissionStatus()

    /**
     * At least one permission was denied, but none are permanently denied.
     *
     * @property permissions Permissions that were denied (can show rationale).
     */
    data class Denied(
        val permissions: List<String>,
    ) : PermissionStatus()

    /**
     * At least one permission was permanently denied (user selected
     * "Don't ask again" / system no longer shows the dialog).
     *
     * @property permissions Permissions that are permanently denied.
     */
    data class PermanentlyDenied(
        val permissions: List<String>,
    ) : PermissionStatus()
}

/**
 * Detailed result of a permission request batch.
 *
 * @property granted Permissions granted in this request (or already held).
 * @property denied Temporary denials where a rationale may still be shown.
 * @property permanentlyDenied Denials where the system will not show the dialog again.
 */
data class PermissionRequestResult(
    val granted: List<String>,
    val denied: List<String>,
    val permanentlyDenied: List<String>,
) {
    /** `true` when [denied] and [permanentlyDenied] are both empty. */
    val allGranted: Boolean
        get() = denied.isEmpty() && permanentlyDenied.isEmpty()

    /**
     * Collapsed status:
     * - [PermissionStatus.Granted] when everything was granted
     * - [PermissionStatus.PermanentlyDenied] when any permission is permanently denied
     * - [PermissionStatus.Denied] otherwise
     */
    val status: PermissionStatus
        get() =
            when {
                allGranted -> PermissionStatus.Granted
                permanentlyDenied.isNotEmpty() ->
                    PermissionStatus.PermanentlyDenied(permanentlyDenied)
                else -> PermissionStatus.Denied(denied)
            }
}
