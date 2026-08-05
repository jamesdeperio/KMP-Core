# 🔐 Security Documentation

Security is our top priority. This package ensures that user sessions are protected and sensitive data is encrypted.

---

## 💡 Why we use this?
We handle sensitive user information. We must ensure that:
1.  The app locks itself if left unattended.
2.  Passwords/Tokens are never saved in "plain text."
3.  The device itself is safe (not rooted or compromised).

---

## 🛠 Classes & Methods

### `SessionManager`
Automatically manages app-level locking. It is like an inactivity timer for a bank app.
*   `reportActivity()`: **Crucial.** You must call this on every user interaction (tap, scroll, etc.) or the app will lock in 5 minutes.
*   `lock()`: Manually lock the screen (e.g., when the user backgrounds the app).
*   `unlock()`: Restarts the 5-minute timer (call this after successful login).

### `SecureStorage`
This is a wrapper around the **EncryptedSharedPreferences** (Android) and **Keychain** (iOS).
*   It encrypts your data **at rest**. If someone steals the phone and copies the storage file, they can't read your tokens.
*   `putString(key, value)`: Encrypts and saves a string.
*   `getString(key)`: Decrypts and returns the string.

### `PayloadCipher` (AES-256-GCM)
This is for **In-Transit Encryption**. 
*   Standard TLS (HTTPS) protects the connection.
*   `PayloadCipher` protects the **content**. Even if someone breaks the HTTPS encryption, the actual data (JSON) is still scrambled with a private key.

### `BiometricAuthenticator`
A "Bridge" class. It works on both Android and iOS.
*   `authenticate(title, description)`: Returns a `BiometricResult`.
*   Handle `Success`, `Cancelled`, or `Failure` results to let the user in.

### `RASP` (Runtime Application Self-Protection)
A set of detection tools to ensure the app environment is not hostile.

#### `RASPConfig` (Configuration)
You can choose which security checks to enable using this data class.

| Check | Description |
| :--- | :--- |
| `checkEmulator` | Detects if the app is running on a virtual device (Simulator/Emulator). |
| `checkRooted` | Detects if the OS has been compromised (Root on Android / Jailbreak on iOS). |
| `checkDebugger` | Detects if a debugger is attached to the process (common in reverse engineering). |
| `checkDeveloperOptions` | Detects if Android "Developer Options" or "USB Debugging" is enabled. |
| `checkProxy` | Detects if a VPN or Proxy is intercepting network traffic (Man-in-the-Middle attack). |
| `checkOverlay` | Detects if another app is drawing over our app (Malware can use this to steal taps). |

**How we use it in the App:**
In our `AppModule.kt`, we automatically loosen the rules for developers but keep them strict for real users:
```kotlin
single {
    if (Config.isDebugMode) {
        RASPConfig(
            checkEmulator = false,
            checkDebugger = false,
            checkDeveloperOptions = false
        )
    } else {
        RASPConfig.Strict
    }
}
```

---

## 🚀 Real-World Examples

### 1. Securely Saving a Login Token
Never save tokens as plain text! Use `SecureStorage`.

```kotlin
suspend fun saveLogin(token: String) {
    storage.putString("auth_token", token)
}
```

### 2. Auto-Locking during long tasks
If you have a feature where a user reads a long article, they might not touch the screen. You should manually report activity.

```kotlin
// Inside your screen logic
LaunchedEffect(Unit) {
    while(true) {
        delay(1000)
        session.reportActivity() // Keep the user session alive!
    }
}
```

### 3. Using RASP to prevent sensitive actions
Block the user from seeing their credit card number if their phone is rooted.

```kotlin
fun onShowCreditCard() {
    if (rasp.isAppCompromised(RASPConfig.Strict)) {
        showToast("Access blocked: Device is insecure")
    } else {
        showCard()
    }
}
```

---

## 🛡️ Best Practices
*   **Debug vs Release**: Always disable Emulator checks for developers in `AppModule.kt` so they can actually work!
*   **Fail Fast**: Check for `isAppCompromised` as soon as the app starts (we already do this in `App.kt`).
*   **No Magic Strings**: Use constants for keys when saving to `SecureStorage`.

---
> [!CAUTION]
> Never log raw passwords or tokens to the console. Use `DataMasker` if you must log sensitive strings for debugging.
