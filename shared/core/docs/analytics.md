# 📊 Analytics Documentation

Analytics helps us understand how users interact with our app. By tracking screen views and button clicks, we can make data-driven decisions to improve the user experience.

---

## 💡 Why we use this?
Instead of using a specific tool like Firebase directly everywhere, we use an **Interface** called `AnalyticsTracker`. This means if we ever want to switch from Firebase to another tool (like Mixpanel), we only have to change it in one place!

---

## 🛠 Classes & Methods

### `AnalyticsTracker` (Interface)
This is the main tool you will inject into your ViewModels.

| Method | What it does | Example |
| :--- | :--- | :--- |
| `trackScreen` | Records when a user opens a screen. This is crucial for "funnel" analysis. | `analytics.trackScreen("registration_form")` |
| `logEvent` | Records a specific action like a button click or a successful form submission. | `analytics.logEvent("click_register_button", mapOf("source" to "onboarding"))` |
| `identify` | Tells the system who the current user is. This links all future events to their ID. | `analytics.identify("user_99", mapOf("user_type" to "beta_tester"))` |
| `trackError` | Logs a "soft" crash or API failure that doesn't stop the app but we should know about. | `analytics.trackError("photo_upload_failed", "User has no storage left")` |

---

## 🚀 Real-World Examples

### 1. Tracking a Screen
In MVI, the best place to track a screen is usually in the `init` block of your ViewModel or when a "Reset/Init" Intent is received.

```kotlin
class HomeViewModel(private val analytics: AnalyticsTracker) : ViewModel() {
    init {
        // Track the screen name as soon as the ViewModel is created
        analytics.trackScreen("home_main_feed")
    }
}
```

### 2. Tracking an Action with Parameters
Parameters (the `mapOf`) give us extra details. For example, if a user clicks a "Buy" button, we want to know *what* they are buying.

```kotlin
fun onPurchaseClicked(itemId: String, price: Double) {
    analytics.logEvent("purchase_initiated", mapOf(
        "item_id" to itemId,
        "price" to price,
        "currency" to "USD"
    ))
}
```

### 3. Tracking an Error
Don't just catch an error and do nothing. Track it so the developers can fix it!

```kotlin
try {
    saveDataToDisk()
} catch (e: Exception) {
    analytics.trackError(
        name = "data_save_error",
        message = e.message ?: "Unknown error",
        params = mapOf("file_size" to 1024)
    )
}
```

---

## 🏗 How it works under the hood
*   **LoggerAnalyticsTracker**: In **Debug mode**, we use this version which simply prints the events to your console (Logcat) so you can verify they are firing.
*   **Production Implementation**: In the future, we can add a `FirebaseAnalyticsTracker` that actually sends data to the cloud. Koin makes this swap easy!

---
> [!TIP]
> Always check `AnalyticsConstants.kt` in the `:shared:data` module. Never hardcode strings like `"login"` if there is already a constant like `AnalyticsConstants.Screen.LOGIN`.
