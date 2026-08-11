package io.github.cheng2016.androidutil.permissions

/**
 * Java-friendly callback for permission request completion.
 */
fun interface PermissionCallback {
    /**
     * Called once when the Activity Result contract finishes (or immediately
     * when all permissions were already granted).
     */
    fun onResult(result: PermissionRequestResult)
}
