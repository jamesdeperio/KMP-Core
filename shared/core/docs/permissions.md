# 🛠 Permissions Documentation

This package provides a simple, unified way to ask for device permissions (like Camera or Location) on both Android and iOS.

---

## 💡 Why we use this?
Android and iOS handle permissions very differently. This package "hides" that complexity so you can write one line of code that works on both platforms.

---

## 🛠 Classes & Methods

### `PermissionManager`
The main tool for checking and requesting permissions.

| Method | What it does |
| :--- | :--- |
| `checkPermission` | Checks if we already have the permission. |
| `requestPermission` | Shows the native OS popup to the user. |
| `openAppSettings` | Sends the user to the phone's settings if they blocked us. |

---

## 🚀 Real-World Example

If your screen needs to take a photo:

```kotlin
suspend fun onCameraClicked() {
    // 1. Check/Request the permission
    val status = permissionManager.requestPermission(Permission.CAMERA)

    // 2. React to the result
    when (status) {
        PermissionState.GRANTED -> openNativeCamera()
        PermissionState.DENIED -> showToast("We need camera access to work!")
        PermissionState.PERMANENTLY_DENIED -> permissionManager.openAppSettings()
        else -> {}
    }
}
```

---
> [!TIP]
> Always use `shouldShowRationale` before requesting a permission for the second time to explain *why* the app needs it.
