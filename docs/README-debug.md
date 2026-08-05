# 🛠 Debug Module: On-Device API Monitor

The `:debug:devtool` module provides a powerful on-device API monitor and debugging tools for Kotlin Multiplatform projects.

## 🚀 How to Use

### 1. Integrate with Networking
If you are using the `:shared:core` module, the `HttpClient` is already pre-configured to log into `NetworkDebug` when the debug module is included.

For custom implementations, manually add logs:
```kotlin
NetworkDebug.add(
    ApiLogData(
        path = "https://api.example.com/data",
        statusCode = 200,
        method = "GET",
        request = "{...}",
        response = "{...}",
        headers = "{...}"
    )
)
```

### 2. Trigger the UI
There are two ways to activate the debug monitor in your app:

#### **Composable Way (Recommended)**
Add `ApiMonitorEffect` to your top-level `@Composable` (e.g., in your `App()` or `MainScreen()`):
```kotlin
@Composable
fun App() {
    // This will start listening for shakes and clean up when the screen is disposed
    ApiMonitorEffect(enabled = true)
    
    // ... your app content
}
```

#### **Manual Way (Activity-based)**
In your `MainActivity.kt`:
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    ApiMonitor.init(this)
    ApiMonitor.listen()
}

override fun onDestroy() {
    super.onDestroy()
    ApiMonitor.stop()
}
```

### 3. How to Trigger
Once the monitor is listening, simply **shake your device 3 times within 1 second** to launch the API logging screen.

---

## 📦 Module Structure
- **`:debug:devtool`**: The active implementation containing the UI and shake detection logic.
- **`:debug:devtool-noop`**: A "no-op" version for production builds to ensure zero overhead and no leaked debug code.
