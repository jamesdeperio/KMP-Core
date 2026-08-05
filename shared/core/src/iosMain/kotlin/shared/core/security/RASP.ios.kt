package shared.core.security

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import platform.CFNetwork.CFNetworkCopySystemProxySettings
import platform.Foundation.NSDictionary
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSProcessInfo
import platform.Foundation.allKeys
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIApplicationWillResignActiveNotification
import platform.UIKit.UIBlurEffect
import platform.UIKit.UIBlurEffectStyle
import platform.UIKit.UIScreen
import platform.UIKit.UIScreenCapturedDidChangeNotification
import platform.UIKit.UIVisualEffectView
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.darwin.NSObjectProtocol

actual suspend fun checkEmulator(): Boolean {
    val deviceName = NSProcessInfo.processInfo().environment["SIMULATOR_DEVICE_NAME"]
    return deviceName != null
}

actual suspend fun checkRootedOrJailbroken(): Boolean {
    // Common simple jailbreak indicators
    val paths =
        listOf(
            "/Applications/Cydia.app",
            "/Library/MobileSubstrate/MobileSubstrate.dylib",
            "/bin/bash",
            "/usr/sbin/sshd",
            "/etc/apt",
        )
    return paths.any {
        platform.Foundation.NSFileManager.defaultManager
            .fileExistsAtPath(it)
    }
}

actual suspend fun checkDebuggerAttached(): Boolean {
    // Simple check: on iOS, NSProcessInfo is used for environment flags
    return NSProcessInfo.processInfo().arguments.contains("XCTestConfigurationFilePath")
}

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

@OptIn(ExperimentalForeignApi::class)
private var blurView: UIVisualEffectView? = null
private var backgroundObserver: NSObjectProtocol? = null
private var foregroundObserver: NSObjectProtocol? = null
private var captureObserver: NSObjectProtocol? = null

@OptIn(ExperimentalForeignApi::class)
private fun getKeyWindow(): UIWindow? {
    // iOS 13+ method using scenes
    val scenes = UIApplication.sharedApplication.connectedScenes
    for (scene in scenes) {
        if (scene is UIWindowScene) {
            for (window in scene.windows) {
                if ((window as? UIWindow)?.isKeyWindow() == true) {
                    return window
                }
            }
            // fallback: first window
            return scene.windows.firstOrNull() as? UIWindow
        }
    }
    // fallback for older iOS
    return UIApplication.sharedApplication.keyWindow
}

@OptIn(ExperimentalForeignApi::class)
actual fun shouldPreventScreenSniffing(enable: Boolean) {
    val window =
        getKeyWindow() ?: run {
            println("⚠️ Unable to get key window")
            return
        }

    if (enable) {
        addBlur(window)
        setupObservers(window)
        checkIfRecording()
    } else {
        removeObservers()
        removeBlur()
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun addBlur(window: UIWindow) {
    if (blurView != null) return

    val blurEffect = UIBlurEffect.effectWithStyle(UIBlurEffectStyle.UIBlurEffectStyleRegular)
    val visualEffectView = UIVisualEffectView(effect = blurEffect)
    visualEffectView.setFrame(window.bounds)
    visualEffectView.autoresizingMask = ((1u shl 1) or (1u shl 4)).toULong()
    window.addSubview(visualEffectView)
    window.bringSubviewToFront(visualEffectView)
    blurView = visualEffectView
    blurView?.alpha = 0.0
}

@OptIn(ExperimentalForeignApi::class)
private fun showBlur() {
    blurView?.alpha = 1.0
}

@OptIn(ExperimentalForeignApi::class)
private fun hideBlur() {
    blurView?.alpha = 0.0
}

@OptIn(ExperimentalForeignApi::class)
private fun removeBlur() {
    blurView?.removeFromSuperview()
    blurView = null
}

@OptIn(ExperimentalForeignApi::class)
private fun setupObservers(window: UIWindow) {
    // App going to background
    backgroundObserver =
        NSNotificationCenter.defaultCenter.addObserverForName(
            name = UIApplicationWillResignActiveNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue,
        ) { _ -> showBlur() }

    // App becoming active
    foregroundObserver =
        NSNotificationCenter.defaultCenter.addObserverForName(
            name = UIApplicationDidBecomeActiveNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue,
        ) { _ ->
            if (!UIScreen.mainScreen.isCaptured()) hideBlur()
        }

    // Screen recording detection
    captureObserver =
        NSNotificationCenter.defaultCenter.addObserverForName(
            name = UIScreenCapturedDidChangeNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue,
        ) { _ ->
            if (UIScreen.mainScreen.isCaptured()) showBlur() else hideBlur()
        }
}

@OptIn(ExperimentalForeignApi::class)
private fun checkIfRecording() {
    if (UIScreen.mainScreen.isCaptured()) showBlur()
}

@OptIn(ExperimentalForeignApi::class)
private fun removeObservers() {
    backgroundObserver?.let {
        NSNotificationCenter.defaultCenter.removeObserver(it)
        backgroundObserver = null
    }
    foregroundObserver?.let {
        NSNotificationCenter.defaultCenter.removeObserver(it)
        foregroundObserver = null
    }
    captureObserver?.let {
        NSNotificationCenter.defaultCenter.removeObserver(it)
        captureObserver = null
    }
}

actual suspend fun checkDeveloperOptionsEnabled(): Boolean = false

@OptIn(ExperimentalForeignApi::class)
actual suspend fun checkProxyOrVpn(): Boolean {
    val proxySettings = CFNetworkCopySystemProxySettings() ?: return false
    // In KMP, CFDictionary can often be cast to NSDictionary via bridge
    val dict = proxySettings as? NSDictionary ?: return false
    
    val keys = dict.allKeys
    return keys.any { 
        val key = it.toString()
        key.contains("HTTP") || key.contains("HTTPS") || key.contains("SOCKS") || key.contains("Proxy")
    }
}

actual suspend fun checkOverlayThreat(): Boolean = false
