package shared.core.analytics

import shared.core.util.Logger

/**
 * Abstracted tracking system for analytics, screen views, and error reporting.
 *
 * This interface allows the app to track data without being coupled to a specific
 * provider (like Firebase, Mixpanel, or Amplitude).
 */
interface AnalyticsTracker {
    /**
     * Logs a custom event with optional parameters.
     * @param name The name of the event (e.g., "login_clicked").
     * @param params Key-value pairs providing additional context.
     */
    fun logEvent(name: String, params: Map<String, Any> = emptyMap())

    /**
     * Associates current tracking with a specific user.
     * @param userId The unique identifier for the user.
     * @param traits Optional properties of the user (e.g., "tier", "region").
     */
    fun identify(userId: String, traits: Map<String, Any> = emptyMap())

    /**
     * Tracks a screen/page view.
     * @param screenName The name of the screen (e.g., "login_screen").
     */
    fun trackScreen(screenName: String)

    /**
     * Tracks a non-fatal error or exception.
     * @param name A short identifier for the error.
     * @param message The detailed error message.
     * @param params Additional metadata about the error.
     */
    fun trackError(name: String, message: String, params: Map<String, Any> = emptyMap())
}

/**
 * A simple implementation that logs all analytics events to the console.
 * Used primarily in debug builds for developers to verify tracking is working.
 */
class LoggerAnalyticsTracker : AnalyticsTracker {
    override fun logEvent(name: String, params: Map<String, Any>) {
        val paramString = if (params.isNotEmpty()) " | Params: $params" else ""
        Logger.d("ANALYTICS", "📊 [EVENT]    $name$paramString")
    }

    override fun identify(userId: String, traits: Map<String, Any>) {
        val traitString = if (traits.isNotEmpty()) " | Traits: $traits" else ""
        Logger.d("ANALYTICS", "👤 [IDENTIFY] $userId$traitString")
    }

    override fun trackScreen(screenName: String) {
        Logger.d("ANALYTICS", "📱 [SCREEN]   $screenName")
    }

    override fun trackError(name: String, message: String, params: Map<String, Any>) {
        val paramString = if (params.isNotEmpty()) " | Params: $params" else ""
        Logger.e("ANALYTICS", "🛑 [ERROR]    $name: $message$paramString")
    }
}
