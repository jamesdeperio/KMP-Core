@file:OptIn(ExperimentalNativeApi::class)

package shared.core.platform
import platform.Foundation.NSBundle
import platform.Foundation.NSProcessInfo
import platform.UIKit.UIDevice
import platform.UIKit.UIViewController
import platform.UIKit.UIApplication
import kotlin.experimental.ExperimentalNativeApi

lateinit var appActivity: UIViewController

fun initKmpAppActivity(viewController: UIViewController) {
    appActivity = viewController
}

class IOSPlatform : Platform {
    override val name: PlatformType = PlatformType.IOS
    override val isDebug: Boolean =
        NSProcessInfo.processInfo.environment["DEBUG"] == "true" || NSProcessInfo.processInfo.environment["XCODE_CONFIGURATION"] == "Debug"
}

actual fun getPlatform(): Platform = IOSPlatform()

actual object AppInfo {
    actual val version: String
        get() {
            val infoDict = NSBundle.mainBundle.infoDictionary
            return infoDict?.get("CFBundleShortVersionString")?.toString() ?: "unknown"
        }
    actual val OS: String
        get() = "IOS"
}

actual object DeviceInfo {
    actual val model: String = UIDevice.currentDevice.model
    actual val osName: String = UIDevice.currentDevice.systemName
    actual val osVersion: String = UIDevice.currentDevice.systemVersion
    actual val platform: String = "ios"
}
