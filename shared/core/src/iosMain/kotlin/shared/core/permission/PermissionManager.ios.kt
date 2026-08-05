package shared.core.permission

import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVAuthorizationStatusRestricted
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.Foundation.NSURL
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusDenied
import platform.Photos.PHAuthorizationStatusLimited
import platform.Photos.PHAuthorizationStatusNotDetermined
import platform.Photos.PHAuthorizationStatusRestricted
import platform.Photos.PHPhotoLibrary
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

class IosPermissionManager : PermissionManager {
    override suspend fun checkPermission(permission: Permission): PermissionState {
        return when (permission) {
            Permission.CAMERA -> checkCamera()
            Permission.GALLERY -> checkGallery()
            Permission.LOCATION, Permission.COARSE_LOCATION -> checkLocation()
            else -> PermissionState.GRANTED
        }
    }

    private fun checkCamera(): PermissionState {
        val status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
        return when (status) {
            AVAuthorizationStatusAuthorized -> PermissionState.GRANTED
            AVAuthorizationStatusDenied, AVAuthorizationStatusRestricted -> PermissionState.PERMANENTLY_DENIED
            AVAuthorizationStatusNotDetermined -> PermissionState.NOT_DETERMINED
            else -> PermissionState.DENIED
        }
    }

    private fun checkGallery(): PermissionState {
        val status = PHPhotoLibrary.authorizationStatus()
        return when (status) {
            PHAuthorizationStatusAuthorized, PHAuthorizationStatusLimited -> PermissionState.GRANTED
            PHAuthorizationStatusDenied, PHAuthorizationStatusRestricted -> PermissionState.PERMANENTLY_DENIED
            PHAuthorizationStatusNotDetermined -> PermissionState.NOT_DETERMINED
            else -> PermissionState.DENIED
        }
    }

    private fun checkLocation(): PermissionState {
        val status = CLLocationManager.authorizationStatus()
        return when (status) {
            kCLAuthorizationStatusAuthorizedAlways, kCLAuthorizationStatusAuthorizedWhenInUse -> PermissionState.GRANTED
            kCLAuthorizationStatusDenied, kCLAuthorizationStatusRestricted -> PermissionState.PERMANENTLY_DENIED
            kCLAuthorizationStatusNotDetermined -> PermissionState.NOT_DETERMINED
            else -> PermissionState.DENIED
        }
    }

    override suspend fun requestPermission(permission: Permission): PermissionState {
        // Implementation for iOS permission request requires callbacks/closures.
        // For now, return check status.
        return checkPermission(permission)
    }

    override fun shouldShowRationale(permission: Permission): Boolean = false

    override fun openAppSettings() {
        val url = NSURL(string = UIApplicationOpenSettingsURLString)
        if (UIApplication.sharedApplication.canOpenURL(url)) {
            UIApplication.sharedApplication.openURL(url)
        }
    }
}

actual fun createPermissionManager(): PermissionManager = IosPermissionManager()
