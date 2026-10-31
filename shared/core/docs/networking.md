# 🌐 Networking Documentation

This package handles all communication between the app and the internet using **Ktor**. It is designed to be safe, reliable, highly configurable, and easy to use.

---

## 💡 Why we use this?
Network calls are risky. The internet can be slow or disconnected. Our networking tools provide a "Safety Net" to ensure that an error doesn't crash the entire app, automatically handles authentication refresh, and optionally encrypts payloads in transit.

---

## 🛠 Key Classes & Interfaces

### `KmpNetworkConfig`
The configuration model for initializing the multiplatform HTTP client:
```kotlin
data class KmpNetworkConfig(
    val baseUrl: String,
    val isDebugMode: Boolean = false,
    val payloadEncryptionEnabled: Boolean = false,
    val connectTimeoutMs: Long = 30_000,
    val requestTimeoutMs: Long = 30_000,
    val socketTimeoutMs: Long = 30_000,
)
```

### `createHttpClient(...)`
The primary factory function for building the app's single shared `HttpClient`. It configures essential Ktor plugins:
1. **Timeouts**: Connect, Request, and Socket timeouts.
2. **Logging**: Pretty-printed JSON request/response logs in debug mode.
3. **Payload Encryption**: Automatically decrypts AES-256 encrypted server responses (when enabled).
4. **Auth**: Handles Bearer token injection and automatic 401 token refreshing via `installAuth`.
5. **Content Negotiation**: Uses Kotlinx Serialization for JSON parsing.

Example setup:
```kotlin
val networkConfig = KmpNetworkConfig(
    baseUrl = "https://api.example.com",
    isDebugMode = true,
    payloadEncryptionEnabled = true
)

val httpClient = createHttpClient(
    tokenProvider = tokenProvider,
    refreshTokens = { refreshToken -> authRepository.refreshTokens(refreshToken) },
    networkConfig = networkConfig,
    payloadCipherKeyProvider = { getAesKeyBytes() },
    onAuthFailed = { logout() }
)
```

### `ApiResult<T>` (Sealed Interface)
Instead of returning raw data or throwing unhandled exceptions, repository calls return an `ApiResult`:
* **`Success(data)`**: Contains the model returned by the server.
* **`Error(error)`**: Contains:
    * `statusCode`: The HTTP status code (e.g., 401 Unauthorized, 404 Not Found, 500 Server Error).
    * `message`: Human-readable error description.
    * `cause`: The original underlying exception.

### `safeApiCall { ... }`
A higher-order function that executes a Ktor request safely inside a `try-catch` block, transforming network exceptions, timeouts, and HTTP errors into an `ApiResult.Error`.

### `NetworkRefreshHandler`
Bridge interface for automatic token refreshing. If a request returns `401 Unauthorized`, the client uses `refresh()` to obtain new tokens and retries the request seamlessly.

### `ConnectivityMonitor`
Monitors internet reachability across Android (`ConnectivityManager`) and iOS (`NWPathMonitor`):
* `isConnected`: A `StateFlow<Boolean>` you can observe in the UI to display offline state banners.

---

## 🚀 Real-World Examples

### 1. Repository Layer Usage
```kotlin
class ProductRepository(private val client: HttpClient) {

    suspend fun getProducts(): ApiResult<List<Product>> {
        return safeApiCall {
            client.get("products").body<List<Product>>()
        }
    }
}
```

### 2. ViewModel Layer Usage
```kotlin
fun loadProfile() {
    viewModelScope.launch {
        updateState { copy(isLoading = true) }

        repository.getUserProfile()
            .onSuccess { profile ->
                updateState { copy(data = profile, isLoading = false) }
            }
            .onError { error ->
                if (error.statusCode == 401) {
                    emitEffect(Effect.NavigateToLogin)
                } else {
                    updateState { copy(errorMessage = error.message, isLoading = false) }
                }
            }
    }
}
```

### 3. Monitoring Internet Connectivity
```kotlin
@Composable
fun MainScreen(connectivity: ConnectivityMonitor) {
    val isOnline by connectivity.isConnected.collectAsState()

    if (!isOnline) {
        Box(Modifier.background(Color.Red).fillMaxWidth().padding(8.dp)) {
            Text("You are currently offline", color = Color.White)
        }
    }
}
```
