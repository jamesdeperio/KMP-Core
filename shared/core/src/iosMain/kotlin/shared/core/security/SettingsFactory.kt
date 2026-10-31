package shared.core.security

import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings
import shared.core.Config

/**
 * Creates a SecureTokenProvider using platform-native Keychain.
 * iOS Keychain is already secure at rest, so manual encryption is optional.
 */
@OptIn(ExperimentalSettingsImplementation::class)
fun createSecureTokenProvider(): SecureTokenProvider {
    val settings = KeychainSettings(service = Config.SECURE_STORAGE_NAME)
    return SecureTokenProvider(settings)
}

/**
 * Creates a generic SecureStorage instance using iOS Keychain.
 * @param serviceName The Keychain service identifier.
 */
@OptIn(ExperimentalSettingsImplementation::class)
fun createSecureStorage(serviceName: String): SecureStorage {
    val settings = KeychainSettings(service = serviceName)
    return SecureStorage(settings)
}

/**
 * On iOS, the Keychain is already secure at rest and doesn't require
 * additional manual encryption unless cross-app sharing is needed.
 * Returning null means the storage will use plain text before passing to Keychain.
 */
fun createAtRestCipher(): PayloadCipher? {
    return null
}
