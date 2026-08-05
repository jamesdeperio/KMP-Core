package io.github.jamesdeperio.devtool

import androidx.compose.runtime.mutableStateListOf
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class ApiLogData
    @OptIn(ExperimentalTime::class)
    constructor(
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
    val logs = mutableStateListOf<ApiLogData>()

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

        logs.add(entry)
        if (logs.size > MAX_LOGS) {
            logs.removeFirst()
        }
    }
}

@OptIn(ExperimentalTime::class)
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
    } catch (e: Exception) {
        jsonString
    }
