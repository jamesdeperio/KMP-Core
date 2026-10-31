package shared.core.platform

/**
 * Enum defining the supported mobile platforms in the Trivana project.
 */
enum class PlatformType {
    ANDROID,
    IOS,
}

/**
 * Interface for accessing platform-specific metadata.
 */
interface Platform {
    /** The type of the current platform. */
    val name: PlatformType

    /** Returns true if the app is running in a debug configuration. */
    val isDebug: Boolean
}

/** Returns an instance of the current platform's metadata. */
expect fun getPlatform(): Platform

/** Information about the application bundle. */
expect object AppInfo {
    val version: String
    val OS: String
}

/** Detailed hardware and OS information about the device. */
expect object DeviceInfo {
    val model: String
    val osName: String
    val osVersion: String
    val platform: String
}
