package shared.core.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import shared.core.platform.appActivity
import kotlin.coroutines.resume

actual class BiometricAuthenticator actual constructor() {
    actual suspend fun authenticate(
        title: String,
        subtitle: String?,
        description: String?,
        negativeButtonText: String,
    ): BiometricResult = suspendCancellableCoroutine { continuation ->
        val executor = ContextCompat.getMainExecutor(appActivity)
        
        val biometricManager = BiometricManager.from(appActivity)
        val canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        
        if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
            when (canAuthenticate) {
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                    continuation.resume(BiometricResult.HardwareNotAvailable)
                    return@suspendCancellableCoroutine
                }
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                    continuation.resume(BiometricResult.HardwareDisabled)
                    return@suspendCancellableCoroutine
                }
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    continuation.resume(BiometricResult.HardwareDisabled)
                    return@suspendCancellableCoroutine
                }
                else -> {
                    continuation.resume(BiometricResult.Failure("Unknown error: $canAuthenticate"))
                    return@suspendCancellableCoroutine
                }
            }
        }

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode == BiometricPrompt.ERROR_USER_CANCELED || errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    continuation.resume(BiometricResult.Cancelled)
                } else {
                    continuation.resume(BiometricResult.Failure(errString.toString()))
                }
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                continuation.resume(BiometricResult.Success)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // This is called for each failed attempt (wrong finger), 
                // but we usually want to wait for onAuthenticationError or Success.
                // If we want to fail immediately after one attempt, we'd resume here.
            }
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .apply {
                subtitle?.let { setSubtitle(it) }
                description?.let { setDescription(it) }
            }
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        val biometricPrompt = BiometricPrompt(appActivity, executor, callback)
        biometricPrompt.authenticate(promptInfo)
        
        continuation.invokeOnCancellation {
            biometricPrompt.cancelAuthentication()
        }
    }
}
