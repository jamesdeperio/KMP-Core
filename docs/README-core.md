# 🛠 Core Module: The App's Foundation

Welcome to the **Core Module**! If you're a new developer joining the project, this is the best place to start. 

Think of this module as the "Engine Room" of the app. It doesn't show any UI screens, but it provides all the tools that the screens need to work—like talking to the internet, saving your login info, and tracking app crashes.

---

## 📦 Required Libraries & Versions

The Core module relies on the following key libraries. Versions are managed centrally in `libs.versions.toml`.

| Library | Version | Purpose |
| :--- | :--- | :--- |
| **Kotlin** | `2.4.10` | Language runtime and standard library. |
| **Kotlinx Coroutines** | `1.11.0` | Asynchronous programming and background tasks. |
| **Kotlinx Serialization** | `1.11.0` | JSON parsing and data serialization. |
| **Kotlinx DateTime** | `0.8.0` | Multiplatform date and time handling. |
| **Ktor Client** | `3.5.1` | Multiplatform networking (Core, Logging, Auth). |
| **Koin** | `4.2.2` | Dependency injection (Core, Android). |
| **Cryptography (KMP)** | `0.6.0` | Multiplatform AES and key management. |
| **Multiplatform Settings** | `1.3.0` | Key-value storage (with Coroutines support). |
| **AndroidX Biometric** | `1.1.0` | Native fingerprint and FaceID for Android. |
| **BuildKonfig** | `0.22.0` | Environment-specific code generation. |

---

## 📖 Detailed Item Documentation
Click a category below to see a detailed guide with code examples:

*   [📊 **Analytics**](../shared/core/docs/analytics.md) - How to track user actions.
*   [🌐 **Networking**](../shared/core/docs/networking.md) - Safe API calls and connectivity.
*   [🔐 **Security**](../shared/core/docs/security.md) - Encryption, Biometrics, and Auto-Lock.
*   [🛠 **Permissions**](../shared/core/docs/permissions.md) - Camera, Location, and Settings.
*   [📱 **Platform & Lifecycle**](../shared/core/docs/platform_lifecycle.md) - Device info and Foreground state.
*   [🧰 **Utilities**](../shared/core/docs/utilities.md) - String, Date, and Coroutine helpers.

---

## 🏗 Dependency Injection (Koin)
To use any of these tools in your module, ensure you include the `coreModule`:

```kotlin
startKoin {
    modules(coreModule, yourFeatureModule)
}
```

---

## 💡 Pro-Tips for Beginners
**Core is UI-Free**: Never put a Compose `@Composable` in this module. Core is for logic only.

---
