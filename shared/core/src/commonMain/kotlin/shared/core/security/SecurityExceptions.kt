package shared.core.security

/**
 * Thrown when an operation requires the user to authenticate via biometrics or device PIN,
 * usually because the hardware-backed key has timed out.
 */
class SecurityAuthenticationRequiredException(message: String? = null) : Exception(message)
