package shared.core.permission

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.content.ContextCompat
import shared.core.platform.appActivity
import shared.core.platform.appContext

class AndroidPermissionManager : PermissionManager {

    private fun Permission.toAndroidPermission(): String = when (this) {
        Permission.CAMERA -> Manifest.permission.CAMERA
        Permission.GALLERY -> if (android.os.Build.VERSION.SDK_INT >= 33) {
            "" // On Android 13+, use Photo Picker (no permission needed)
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        Permission.LOCATION -> Manifest.permission.ACCESS_FINE_LOCATION
        Permission.COARSE_LOCATION -> Manifest.permission.ACCESS_COARSE_LOCATION
        Permission.NOTIFICATIONS -> if (android.os.Build.VERSION.SDK_INT >= 33) {
            Manifest.permission.POST_NOTIFICATIONS
        } else {
            "" // No permission needed for older versions
        }
        Permission.RECORD_AUDIO -> Manifest.permission.RECORD_AUDIO
    }

    override suspend fun checkPermission(permission: Permission): PermissionState {
        val androidPermission = permission.toAndroidPermission()
        if (androidPermission.isEmpty()) return PermissionState.GRANTED

        if (ContextCompat.checkSelfPermission(appContext, androidPermission) == PackageManager.PERMISSION_GRANTED) {
            return PermissionState.GRANTED
        }

        // Check for Partial Access (Android 14+)
        if (permission == Permission.GALLERY && android.os.Build.VERSION.SDK_INT >= 34) {
            val partialPermission = "android.permission.READ_MEDIA_VISUAL_USER_SELECTED"
            if (ContextCompat.checkSelfPermission(appContext, partialPermission) == PackageManager.PERMISSION_GRANTED) {
                return PermissionState.PARTIAL
            }
        }

        return PermissionState.DENIED
    }

    override suspend fun requestPermission(permission: Permission): PermissionState {
        // Note: Real request requires Activity Result API. 
        // This is a bridge that assumes the UI handles the actual trigger or we use a hidden fragment.
        // For this core bridge, we provide the status check.
        return checkPermission(permission)
    }

    override fun shouldShowRationale(permission: Permission): Boolean {
        val androidPermission = permission.toAndroidPermission()
        if (androidPermission.isEmpty()) return false
        return appActivity.shouldShowRequestPermissionRationale(androidPermission)
    }

    override fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", appContext.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        appContext.startActivity(intent)
    }
}

actual fun createPermissionManager(): PermissionManager = AndroidPermissionManager()
