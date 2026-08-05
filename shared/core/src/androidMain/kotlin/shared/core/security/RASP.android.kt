package shared.core.security

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Debug
import android.provider.Settings
import android.view.WindowManager
import android.view.accessibility.AccessibilityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import shared.core.platform.appActivity
import shared.core.platform.appContext

actual suspend fun checkEmulator(): Boolean {
    return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
        Build.FINGERPRINT.startsWith("generic") ||
        Build.FINGERPRINT.startsWith("unknown") ||
        Build.HARDWARE.contains("goldfish") ||
        Build.HARDWARE.contains("ranchu") ||
        Build.MODEL.contains("google_sdk") ||
        Build.MODEL.contains("Emulator") ||
        Build.MODEL.contains("Android SDK built for x86") ||
        Build.MANUFACTURER.contains("Genymotion") ||
        Build.PRODUCT.contains("sdk_google") ||
        Build.PRODUCT.contains("google_sdk") ||
        Build.PRODUCT.contains("sdk") ||
        Build.PRODUCT.contains("sdk_x86") ||
        Build.PRODUCT.contains("vbox86p") ||
        Build.PRODUCT.contains("emulator") ||
        Build.PRODUCT.contains("simulator")
}

actual suspend fun checkRootedOrJailbroken(): Boolean {
    val paths =
        arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su",
        )
    val fileExists = paths.any { java.io.File(it).exists() }
    if (fileExists) return true

    // Check by executing 'which su'
    return withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            val result = process.inputStream.bufferedReader().use { it.readText() }
            result.isNotEmpty()
        } catch (_: Throwable) {
            false
        }
    }
}

actual suspend fun checkDebuggerAttached(): Boolean = Debug.isDebuggerConnected()

actual fun appCompromiseFlow(
    config: RASPConfig,
    intervalMs: Long
): Flow<Boolean> =
    flow {
        while (true) {
            emit(isAppCompromised(config))
            delay(intervalMs)
        }
    }

actual fun shouldPreventScreenSniffing(enable: Boolean) {
    appActivity.window?.let { window ->
        if (enable) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE,
            )
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}

actual suspend fun checkDeveloperOptionsEnabled(): Boolean =
    try {
        Settings.Global.getInt(appContext.contentResolver, Settings.Global.ADB_ENABLED, 0) != 0
    } catch (e: Settings.SettingNotFoundException) {
        false
    }

actual suspend fun checkProxyOrVpn(): Boolean {
    val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
    val activeNetwork = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(activeNetwork) ?: return false
    
    val isVpn = caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
    val hasProxy = System.getProperty("http.proxyHost") != null || System.getProperty("https.proxyHost") != null
    
    return isVpn || hasProxy
}

actual suspend fun checkOverlayThreat(): Boolean {
    val am = appContext.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager ?: return false
    val enabledServices = am.getEnabledAccessibilityServiceList(-1) // -1 is FEEDBACK_ALL_MASK
    
    // Flag if there are more than a few accessibility services enabled (common for malware/overlays)
    // or if the app itself is being covered by another window (handled by FLAG_SECURE mostly, 
    // but we can check canDrawOverlays permission for other apps too).
    // A simpler check: if any non-system app has overlay permission. 
    // (This is a bit aggressive but good for high-security apps).
    return enabledServices.any { 
        val packageName = it.resolveInfo.serviceInfo.packageName
        !packageName.startsWith("com.android.") && !packageName.startsWith("com.google.") 
    }
}
