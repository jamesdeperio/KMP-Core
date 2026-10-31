package shared.core.permission

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

@Composable
actual fun rememberPermissionRequester(
    onResult: (PermissionState) -> Unit
): (Permission) -> Unit {
    val singleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onResult(if (isGranted) PermissionState.GRANTED else PermissionState.DENIED)
    }

    return { permission ->
        when (permission) {
            Permission.GALLERY -> {
                if (android.os.Build.VERSION.SDK_INT >= 33) {
                    // On Android 13+, Photo Picker is the standard and doesn't require permissions.
                    // Requesting it triggers the confusing "Limited Access" dialog which is redundant.
                    onResult(PermissionState.GRANTED)
                } else {
                    singleLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
            else -> {
                val androidPermission = when (permission) {
                    Permission.CAMERA -> Manifest.permission.CAMERA
                    Permission.LOCATION -> Manifest.permission.ACCESS_FINE_LOCATION
                    Permission.COARSE_LOCATION -> Manifest.permission.ACCESS_COARSE_LOCATION
                    Permission.NOTIFICATIONS -> if (android.os.Build.VERSION.SDK_INT >= 33) {
                        Manifest.permission.POST_NOTIFICATIONS
                    } else {
                        null
                    }
                    Permission.RECORD_AUDIO -> Manifest.permission.RECORD_AUDIO
                    Permission.GALLERY -> null // Handled above
                }
                androidPermission?.let { singleLauncher.launch(it) } ?: onResult(PermissionState.GRANTED)
            }
        }
    }
}

@Composable
actual fun rememberGalleryPicker(
    onImagePicked: (String) -> Unit
): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { onImagePicked(it.toString()) }
    }

    return {
        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
}
