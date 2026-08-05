package shared.core.security

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Utilities to mask sensitive data (PII) before logging or displaying in non-secure contexts.
 */
object DataMasker {

    private val sensitiveKeys = setOf(
        "password", "token", "secret", "access_token", "refresh_token",
        "authorization", "api_key", "apikey", "credential", "cvv", "card_number"
    )

    /**
     * Parses the JSON and masks values for sensitive keys.
     */
    fun maskJson(json: String?): String {
        if (json.isNullOrBlank()) return ""
        return try {
            val element = Json.parseToJsonElement(json)
            val masked = maskJsonElement(element)
            Json.encodeToString(JsonElement.serializer(), masked)
        } catch (e: Exception) {
            json // Return original if not valid JSON
        }
    }

    private fun maskJsonElement(element: JsonElement): JsonElement {
        return when (element) {
            is JsonObject -> {
                val newMap = element.mapValues { (key, value) ->
                    if (sensitiveKeys.any { key.contains(it, ignoreCase = true) }) {
                        JsonPrimitive("***")
                    } else {
                        maskJsonElement(value)
                    }
                }
                JsonObject(newMap)
            }
            is JsonArray -> {
                JsonArray(element.map { maskJsonElement(it) })
            }
            else -> element
        }
    }

    /** Masks an email: "kaizendeperio@gmail.com" -> "k***********o@gmail.com" */
    fun maskEmail(email: String?): String {
        if (email == null) return ""
        val parts = email.split("@")
        if (parts.size != 2) return maskGeneral(email)
        
        val name = parts[0]
        val domain = parts[1]
        
        return when {
            name.length <= 2 -> name.first() + "*" + "@" + domain
            else -> name.first() + "*".repeat(name.length - 2) + name.last() + "@" + domain
        }
    }

    /** Masks a phone number. */
    fun maskPhone(phone: String?): String {
        if (phone == null) return ""
        return when {
            phone.length <= 6 -> maskGeneral(phone, 2)
            else -> phone.take(3) + "*".repeat(phone.length - 5) + phone.takeLast(2)
        }
    }

    /** General masking for strings of unknown format. */
    fun maskGeneral(text: String?, visibleChars: Int = 2): String {
        if (text == null) return ""
        if (text.length <= visibleChars) return "*".repeat(text.length)
        return text.take(visibleChars) + "*".repeat(text.length - visibleChars)
    }
}
