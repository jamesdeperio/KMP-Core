package shared.core.platform

import android.content.Context
import android.os.Build
import androidx.fragment.app.FragmentActivity

lateinit var appContext: Context
lateinit var appActivity: FragmentActivity

fun initKmpAppContext(context: Context) {
    appContext = context.applicationContext
}

fun initKmpAppActivity(activity: FragmentActivity) {
    appActivity = activity
}

class AndroidPlatform : Platform {
    override val name: PlatformType = PlatformType.ANDROID
    override val isDebug: Boolean = true // AppConfig.ENV != "PROD"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual object AppInfo {
    actual val version: String
        get() {
            val packageInfo = appContext.packageManager.getPackageInfo(appContext.packageName, 0)
            return packageInfo.versionName ?: "unknown"
        }
    actual val OS: String
        get() = "ANDROID"
}

actual object DeviceInfo {
    actual val model: String = Build.MODEL ?: "unknown"
    actual val osName: String = "Android"
    actual val osVersion: String = Build.VERSION.RELEASE ?: "unknown"
    actual val platform: String = "android"
}
