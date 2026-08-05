package io.github.jamesdeperio.devtool

data class ApiLogData(
    val path: String = "",
    val statusCode: Int = 0,
    val method: String = "",
    val request: String = "",
    val response: String = "",
    val headers: String = "",
    val timestamp: Long = 0L,
)

object NetworkDebug {
    fun add(entry: ApiLogData) {
        // No-op
    }
}
