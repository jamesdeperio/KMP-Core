# 🌐 Networking Documentation

This package handles all communication between the app and the internet using **Ktor**. It is designed to be safe, reliable, and easy to use.

---

## 💡 Why we use this?
Network calls are risky. The internet can be slow or disconnected. Our networking tools provide a "Safety Net" to ensure that an error doesn't crash the entire app.

---

## 🛠 Classes & Methods

### `ApiResult<T>` (Sealed Class)
Instead of returning raw data, our APIs return an `ApiResult`. This forced you to think about what happens when things fail.

*   **`Success(data)`**: The "Happy Path." Contains the model returned by the server.
*   **`Error(error)`**: The "Failure Path." Contains:
    *   `statusCode`: The HTTP code (e.g., 401 for unauthorized).
    *   `message`: The reason for failure.
    *   `cause`: The original exception (for debugging).

### `safeApiCall { ... }`
This is a **higher-order function** (a function that takes another function). It wraps your code in a `try-catch` block automatically.
*   It catches **Timeouts**, **Server Errors**, and even **JSON Parsing** errors.

### `NetworkRefreshHandler`
This interface is the bridge for **Automatic Token Refreshing**. 
*   If a request fails with a `401 Unauthorized`, the networking layer automatically uses the `refresh()` method to get a new token and retries the request without the user even noticing!

### `ConnectivityMonitor`
A "Stream" of truth about the device's internet status.
*   **`isConnected`**: A StateFlow you can observe to show an "Offline" banner.

---

## 🛠 Advanced Components

### `ApiResponse<T>`
Every successful response from our backend is wrapped in this standard "Envelope":
*   `status`: The internal status code.
*   `data`: The actual JSON payload you requested.
*   `message`: An optional status message from the server.

### `NetworkConfig`
The central hub for all networking constants. 
*   **`baseUrl`**: Changes automatically based on your environment (Local/Staging/Prod).
*   **`certificatePins`**: List of allowed SSL certificates for high-security pinning.
*   **`payloadEncryptionEnabled`**: Toggle for the AES-256 layer.

---

## 🚀 Real-World Examples

### 1. The Repository Layer (The Data Provider)
Your Repository should always return an `ApiResult`. Use `safeApiCall` to protect the app.

```kotlin
class ProductRepository(private val client: HttpClient) {

    suspend fun getProducts(): ApiResult<List<Product>> {
        // Use safeApiCall to prevent crashes!
        return safeApiCall {
            client.get("products").body<List<Product>>()
        }
    }
}
```

### 2. The ViewModel Layer (The Decision Maker)
The ViewModel handles the result and updates the UI state. Use the `.onSuccess` and `.onError` helpers to keep your code clean.

```kotlin
fun loadProfile() {
    viewModelScope.launch {
        updateState { copy(isLoading = true) }

        repository.getUserProfile()
            .onSuccess { profile ->
                // Everything went well!
                updateState { copy(data = profile, isLoading = false) }
            }
            .onError { error ->
                // Handle common status codes specifically
                if (error.isUnauthorized) {
                    emitEffect(Effect.NavigateToLogin)
                } else {
                    updateState { copy(errorMessage = error.message, isLoading = false) }
                }
            }
    }
}
```

### 3. Monitoring Internet Status
Use the `ConnectivityMonitor` to show a "No Connection" bar at the top of your screen.

```kotlin
@Composable
fun MyScreen(connectivity: ConnectivityMonitor) {
    val isOnline by connectivity.isConnected.collectAsState()

    if (!isOnline) {
        Box(Modifier.background(Color.Red)) {
            Text("You are currently offline")
        }
    }
}
```

---

## 🏗 Ktor Plugins (Built-in Magic)
Our `HttpClientFactory` automatically installs:
1.  **Logging**: Prints every request and response to your console in a pretty format.
2.  **ContentNegotiation**: Converts JSON strings into Kotlin classes automatically.
3.  **Auth**: Injects your Bearer token into the headers of every request.
4.  **Timeouts**: Stops a request if the server takes more than 30 seconds to answer.

---
> [!IMPORTANT]
> **Rule:** Never use `try-catch` inside a Repository if you are using `safeApiCall`. It's redundant!
