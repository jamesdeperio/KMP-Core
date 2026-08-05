# 📱 Platform & Lifecycle Documentation

This package provides information about the current device hardware and monitors whether the app is in the foreground or background.

---

## 💡 Why we use this?
Sometimes we need to change how the app works based on the device. For example:
*   Show a different message for iPhone users.
*   Pause a video or music when the user leaves the app (background).

---

## 🛠 Classes & Methods

### `AppInfo`
General info about the build.
*   `version`: Returns the "1.0.0" string.
*   `OS`: Returns "Android" or "iOS".

### `DeviceInfo`
Detailed hardware info.
*   `model`: e.g., "Pixel 8" or "iPhone 15 Pro".
*   `osVersion`: e.g., "14" or "17.4".

### `AppLifecycleObserver`
A "Flow" of lifecycle changes.
*   **`FOREGROUND`**: User is looking at the app.
*   **`BACKGROUND`**: User left the app.

---

## 🛠 System Actions (`shared.core.platform`)
These methods allow the app to interact with the device's operating system.

### `SystemAction.openLink(url)`
Opens a website in the device's default web browser (Safari or Chrome).
*   **Example:** `SystemAction.openLink("https://example.com")`

### `SystemAction.exitApp()`
Forcefully closes the application. 
> [!CAUTION]
> Use this sparingly, usually only for critical security violations (like a compromised device).

---

## 🚀 Real-World Example

Stopping a timer when the app is backgrounded:

```kotlin
class TimerViewModel(private val lifecycle: AppLifecycleObserver) : ViewModel() {
    init {
        viewModelScope.launch {
            lifecycle.state.collect { state ->
                if (state == AppLifecycle.BACKGROUND) {
                    stopTimer()
                } else {
                    startTimer()
                }
            }
        }
    }
}
```

---
