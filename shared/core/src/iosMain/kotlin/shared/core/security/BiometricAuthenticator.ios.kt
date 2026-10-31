package shared.core.security

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthenticationWithBiometrics
import kotlin.coroutines.resume

actual open class BiometricAuthenticator actual constructor() {
    @OptIn(ExperimentalForeignApi::class)
    actual open suspend fun canAuthenticate(): BiometricResult {
        val context = LAContext()
        val canEvaluate = context.canEvaluatePolicy(LAPolicyDeviceOwnerAuthenticationWithBiometrics, null)
        return if (canEvaluate) BiometricResult.Success else BiometricResult.HardwareNotAvailable
    }

    @OptIn(ExperimentalForeignApi::class)
    actual open suspend fun authenticate(
        title: String,
        subtitle: String?,
        description: String?,
        negativeButtonText: String,
    ): BiometricResult = suspendCancellableCoroutine { continuation ->
        val context = LAContext()
        
        val canEvaluate = context.canEvaluatePolicy(LAPolicyDeviceOwnerAuthenticationWithBiometrics, null)
        
        if (!canEvaluate) {
            continuation.resume(BiometricResult.HardwareNotAvailable)
            return@suspendCancellableCoroutine
        }

        context.evaluatePolicy(
            policy = LAPolicyDeviceOwnerAuthenticationWithBiometrics,
            localizedReason = description ?: title,
            reply = { success, nsError ->
                if (success) {
                    continuation.resume(BiometricResult.Success)
                } else {
                    val message = nsError?.localizedDescription ?: "Authentication failed"
                    if (nsError?.code?.toInt() == -2) { // LAErrorUserCancel
                        continuation.resume(BiometricResult.Cancelled)
                    } else {
                        continuation.resume(BiometricResult.Failure(message))
                    }
                }
            }
        )
    }
}
