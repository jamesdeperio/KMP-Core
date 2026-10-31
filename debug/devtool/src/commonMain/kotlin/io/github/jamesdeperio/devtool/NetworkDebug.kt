package io.github.jamesdeperio.devtool


import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlin.time.Clock

data class ApiLogData(
    val path: String,
    val statusCode: Int,
    val method: String = "POST",
    val request: String,
    val response: String,
    val headers: String,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds(),
)

object NetworkDebug {
    private const val MAX_LOGS = 50
    private val _logs = MutableStateFlow<List<ApiLogData>>(emptyList())
    val logs: StateFlow<List<ApiLogData>> = _logs.asStateFlow()

    fun add(entry: ApiLogData) {
        println(
            """
            ========== API CALL ==========
            Method: ${entry.method}
            URL: ${entry.path}
            Status: ${entry.statusCode}
            Headers:
            ${prettifyJson(entry.headers)}
            Body:
            ==============================
            """.trimIndent(),
        )
        println("===REQUEST===")
        println(prettifyJson(entry.request))
        println("===RESPONSE===")
        println(prettifyJson(entry.response))
        println("========== END API CALL ==========  ")

        _logs.update { current ->
            val updated = current + entry
            if (updated.size > MAX_LOGS) updated.drop(1) else updated
        }
    }

    // --- Mock data for testing ---
    fun addMockData() {
        val now: Long = Clock.System.now().toEpochMilliseconds()
        _logs.update { current ->
            current + listOf(
                ApiLogData(
                    path = "/api/login",
                    statusCode = 200,
                    request =
                        """
                        {
                            "username": "user1",
                            "password": "***"
                        }
                        """.trimIndent(),
                    response =
                        """
                        {
                            "token": "abc123",
                            "expires_in": 3600,
                            "user": {
                                "id": 101,
                                "name": "John Doe",
                                "email": "john@example.com",
                                "roles": ["user", "admin"]
                            }
                        }
                        """.trimIndent(),
                    headers =
                        """
                        Content-Type: application/json
                        X-Request-ID: 123e4567-e89b-12d3-a456-426614174000
                        """.trimIndent(),
                    timestamp = now,
                ),
                ApiLogData(
                    path = "/api/profile",
                    statusCode = 404,
                    request = "{}",
                    response =
                        """
                        {
                            "error": "User not found",
                            "code": 404,
                            "details": {
                                "query": "userId=999"
                            }
                        }
                        """.trimIndent(),
                    headers = "Authorization: Bearer abc123",
                    timestamp = now - 60000,
                ),
                ApiLogData(
                    path = "/api/transactions",
                    statusCode = 500,
                    request =
                        """
                        {
                            "from": "2025-01-01",
                            "to": "2025-11-22",
                            "filter": {
                                "type": ["debit", "credit"],
                                "status": ["pending", "completed"]
                            }
                        }
                        """.trimIndent(),
                    response =
                        """
                        {
                            "error": "Server error",
                            "code": 500,
                            "message": "Database unavailable"
                        }
                        """.trimIndent(),
                    headers = "Authorization: Bearer abc123",
                    timestamp = now - 120000,
                ),
                ApiLogData(
                    path = "/api/orders",
                    statusCode = 200,
                    request =
                        """
                        {
                            "userId": 101,
                            "limit": 5,
                            "offset": 0
                        }
                        """.trimIndent(),
                    response =
                        """
                        {
                            "orders": [
                                {
                                    "id": "ORD-1001",
                                    "amount": 120.50,
                                    "currency": "USD",
                                    "items": [
                                        {"name": "Laptop", "qty": 1, "price": 120.50}
                                    ],
                                    "status": "completed",
                                    "created_at": "2025-11-21T12:30:00Z"
                                },
                                {
                                    "id": "ORD-1002",
                                    "amount": 50.00,
                                    "currency": "USD",
                                    "items": [
                                        {"name": "Mouse", "qty": 2, "price": 25.00}
                                    ],
                                    "status": "pending",
                                    "created_at": "2025-11-21T14:00:00Z"
                                }
                            ]
                        }
                        """.trimIndent(),
                    headers = "Authorization: Bearer abc123",
                    timestamp = now - 180000,
                ),
                ApiLogData(
                    path = "/api/products",
                    statusCode = 200,
                    request =
                        """
                        {
                            "category": "electronics",
                            "sort": "price_desc",
                            "page": 1
                        }
                        """.trimIndent(),
                    response =
                        """
                        {
                            "products": [
                                {
                                    "id": 501,
                                    "name": "Smartphone X",
                                    "price": 999.99,
                                    "stock": 12,
                                    "features": ["OLED", "5G", "128GB Storage"]
                                },
                                {
                                    "id": 502,
                                    "name": "Wireless Earbuds",
                                    "price": 199.99,
                                    "stock": 50,
                                    "features": ["Noise Cancelling", "Bluetooth 5.2"]
                                },
                                {
                                    "id": 503,
                                    "name": "Smartwatch Z",
                                    "price": 299.99,
                                    "stock": 20,
                                    "features": ["Heart Rate Monitor", "GPS", "Water Resistant"]
                                }
                            ]
                        }
                        """.trimIndent(),
                    headers = "Authorization: Bearer abc123\nContent-Type: application/json",
                    timestamp = now - 240000,
                ),
                ApiLogData(
                    path = "/api/notifications",
                    statusCode = 200,
                    request =
                        """
                        {
                            "userId": 101,
                            "unreadOnly": true
                        }
                        """.trimIndent(),
                    response =
                        """
                        {
                            "notifications": [
                                {
                                    "id": "N-1001",
                                    "title": "New Message",
                                    "body": "You have a new message from Alice.",
                                    "read": false,
                                    "timestamp": "2025-11-22T09:30:00Z"
                                },
                                {
                                    "id": "N-1002",
                                    "title": "Weekly Summary",
                                    "body": "Your account activity summary for the week.",
                                    "read": false,
                                    "timestamp": "2025-11-22T08:00:00Z"
                                }
                            ]
                        }
                        """.trimIndent(),
                    headers = "Authorization: Bearer abc123",
                    timestamp = now - 300000,
                ),
            )
        }
    }
}

fun formatTimestamp(ts: Long): String {
    val nowMillis: Long = Clock.System.now().toEpochMilliseconds()
    val diff = (nowMillis - ts) / 1000
    return when {
        diff < 60 -> "${diff}s ago"
        diff < 3600 -> "${diff / 60}m ago"
        diff < 86400 -> "${diff / 3600}h ago"
        else -> "${diff / 86400}d ago"
    }
}

fun prettifyJson(jsonString: String): String =
    try {
        val jsonElement = Json.parseToJsonElement(jsonString)
        val prettyJson = Json { prettyPrint = true }
        prettyJson.encodeToString(JsonElement.serializer(), jsonElement)
    } catch (_: Exception) {
        jsonString
    }

