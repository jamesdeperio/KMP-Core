# 🧰 Utilities & Extensions Documentation

Our "Swiss Army Knife" of helper functions. These small tools stop us from writing the same code over and over again, ensuring consistency and safety across the entire app.

---

## 🔀 Coroutine Utilities (`shared.core.util`)
Helpers for managing background tasks and asynchronous code.

### `retryWithBackoff`
Automatically retries a failing task with increasing delays between each attempt.
*   **Example:**
    ```kotlin
    val data = retryWithBackoff(times = 5, initialDelay = 200) {
        api.getFlakyData()
    }
    ```

### `runNonCancellable`
Ensures a block of code finishes even if the user closes the screen or cancels the task. Great for cleanup or logging.
*   **Example:**
    ```kotlin
    runNonCancellable {
        analytics.logEvent("user_forced_exit")
        database.close()
    }
    ```

---

## ⏰ Date & Time Utilities (`shared.core.util`)
Standardizing how we show time and dates to users.

### `formatTime(seconds)`
Converts seconds into a long readable string.
*   **Result:** `61` -> `"1 minute 1 second"`

### `formatTimeShort(seconds)`
Converts seconds into a digital clock format.
*   **Result:** `125` -> `"2 : 05"`

### `getCurrentHour()`
Returns the current hour of the day (0-23) based on the user's local time.

### `String.reformatDate(pattern)`
Changes a `YYYY-MM-DD` string into a different format.
*   **Result:** `"2024-08-05".reformatDate("dd-MM-yyyy")` -> `"05-08-2024"`

### `Long.toTimeAgo()`
Converts a timestamp into a human-friendly "social media" style string.
*   **Result:** `"Just now"`, `"5 minutes ago"`, `"2 days ago"`.

### `getCurrentIsoTimestamp()`
Returns the current time in standard ISO-8601 format (e.g., `2024-05-20T10:00:00Z`).

---

## 🛡️ Error & Safety Helpers (`shared.core.util`)
Making sure we handle failures gracefully without crashing.

### `Throwable.resolveMessage()`
Turns a complex error/exception into a simple string you can show the user.
*   **Example:** `exception.resolveMessage()` -> `"Network error. Please check your connection."`

### `runSafe { ... }`
Executes a block and returns a standard Kotlin `Result` (Success or Failure).
*   **Example:**
    ```kotlin
    val result = runSafe { parseComplexJson(data) }
    ```

### `Result.getOrElse { ... }`
Returns the success value, or a default value if it failed.
*   **Example:** `result.getOrElse { "Guest User" }`

---

## 🌊 Flow Extensions (`shared.core.util`)
Making reactive programming easier.

### `MutableStateFlow.updateState { ... }`
A shorthand for updating complex MVI states.
*   **Example:**
    ```kotlin
    _state.updateState { copy(isLoading = true, error = null) }
    ```

---

## 📝 Logging (`shared.core.util`)
Debug tools that automatically hide themselves in the real app for security.

### `Logger.d` & `Logger.e`
Standard debug and error logs.

### `logD` & `logE` (Idiomatic)
Extension functions that use the class name as the tag automatically.
*   **Example:**
    ```kotlin
    class LoginService {
        fun start() {
            logD("Service started!") // Tag will be "LoginService"
        }
    }
    ```

---

## 💰 Number Utilities (`shared.core.util`)
Formatting numbers for the UI.

### `Double.formatPrice(currency, decimals)`
Formats a number as a price string.
*   **Result:** `12.5.formatPrice("PHP ")` -> `"PHP 12.50"`

### `Double.toPercentage(decimals)`
Formats a decimal (0.0 to 1.0) as a percentage.
*   **Result:** `0.854.toPercentage()` -> `"85.4%"`

---

## 🎲 Random Utilities (`shared.core.util`)
### `generateRandomString(length)`
Generates a random ID (e.g., `"aB3kL9..."`).

### `List.randomOrNull()`
Safely picks a random item from a list. Returns `null` if the list is empty.

---

## ✉️ String Extensions (`shared.core.util`)
### `isValidEmail()`
Returns `true` if the string matches a standard email format.

### `digitsOnly()`
Removes all letters and symbols, leaving only numbers.
*   **Result:** `"+63 912-345"` -> `"63912345"`

### `maskEmail()`
Hides the middle of an email for privacy.
*   **Result:** `"john@gmail.com"` -> `"jo***@gmail.com"`

### `capitalizeWords()`
Capitalizes the first letter of every word.
*   **Result:** `"hello world"` -> `"Hello World"`

---

## 🔑 JWT Security Utilities (`shared.core.security`)
Helpers for handling Login Tokens.

### `decodePayload(jwt)`
Returns the data inside a login token (JSON format).

### `isExpired(jwt)`
Checks if the user needs to log in again because their token is too old.

### `expiresInSeconds(jwt)`
Tells you exactly how many seconds are left before the user is logged out.

---

