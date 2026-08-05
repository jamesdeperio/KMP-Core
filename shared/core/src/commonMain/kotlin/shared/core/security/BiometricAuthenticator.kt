package shared.core.security

/**
 * Platform-independent helper for biometric authentication (Fingerprint, FaceID, etc.).
 * Implementation is provided in platform-specific source sets (androidMain, iosMain).
 */
expect class BiometricAuthenticator() {
    /**
     * Attempts to authenticate the user using the device's biometric sensors.
     *
     * @param title The title displayed on the biometric prompt.
     * @param subtitle An optional subtitle for the biometric prompt.
     * @param description An optional description explaining why authentication is needed.
     * @param negativeButtonText The text for the cancellation button (Android only).
     * @return A [BiometricResult] indicating success, failure, or unavailability.
     */
    suspend fun authenticate(
        title: String,
        subtitle: String? = null,
        description: String? = null,
        negativeButtonText: String = "Cancel",
    ): BiometricResult
}

/**
 * Represents the various outcomes of a biometric authentication attempt.
 */
sealed class BiometricResult {
    /** Authentication was successful. */
    object Success : BiometricResult()

    /** Authentication failed with a specific [message]. */
    data class Failure(val message: String) : BiometricResult()

    /** The device does not have biometric hardware. */
    object HardwareNotAvailable : BiometricResult()

    /** Biometric hardware is available but has been disabled or not set up. */
    object HardwareDisabled : BiometricResult()

    /** The user manually cancelled the authentication prompt. */
    object Cancelled : BiometricResult()
}
