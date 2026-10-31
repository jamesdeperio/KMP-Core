package shared.core.permission

enum class Permission {
    CAMERA,
    GALLERY,
    LOCATION,
    COARSE_LOCATION,
    NOTIFICATIONS,
    RECORD_AUDIO
}

enum class PermissionState {
    GRANTED,
    PARTIAL,
    DENIED,
    NOT_DETERMINED,
    PERMANENTLY_DENIED
}

/**
 * Common interface for handling platform permissions in ViewModels or UseCases.
 */
interface PermissionManager {
    /**
     * Returns the current state of a specific permission.
     */
    suspend fun checkPermission(permission: Permission): PermissionState

    /**
     * Requests a permission and returns the result.
     */
    suspend fun requestPermission(permission: Permission): PermissionState

    /**
     * Returns true if the app should show a rationale for the permission.
     */
    fun shouldShowRationale(permission: Permission): Boolean

    /**
     * Opens the app settings so the user can manually grant permissions.
     */
    fun openAppSettings()
}

/**
 * Factory hook for platform-specific implementations.
 */
expect fun createPermissionManager(): PermissionManager
