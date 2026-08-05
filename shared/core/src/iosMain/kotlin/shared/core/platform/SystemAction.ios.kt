package shared.core.platform

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSSelectorFromString
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual object SystemAction {
    @OptIn(ExperimentalForeignApi::class)
    actual fun exitApp() {
        UIApplication.sharedApplication.performSelector(
            NSSelectorFromString("terminateWithSuccess"),
        )
    }

    actual fun openLink(link: String) {
        val url = NSURL.URLWithString(link) ?: return
        val sharedApplication = UIApplication.sharedApplication

        // Determine app scheme if needed
        val appUrl =
            when {
                link.contains("instagram.com/reel") -> {
                    val reelId = url.lastPathComponent
                    NSURL.URLWithString("instagram://reel/$reelId")
                }
                link.contains("tiktok.com") -> {
                    val videoId = url.lastPathComponent
                    NSURL.URLWithString("snssdk1128://aweme/detail/$videoId")
                }
                link.contains("youtube.com/shorts") -> {
                    val shortId = url.lastPathComponent
                    NSURL.URLWithString("youtube://shorts/$shortId")
                }
                link.contains("youtu.be") -> {
                    val videoId = url.lastPathComponent
                    NSURL.URLWithString("youtube://watch?v=$videoId")
                }
                else -> null
            }

        // Try app URL first if available and can be opened, otherwise fall back to browser
        val urlToOpen =
            if (appUrl != null && sharedApplication.canOpenURL(appUrl)) {
                appUrl
            } else {
                url // Fall back to original web URL
            }

        sharedApplication.openURL(
            urlToOpen,
            options = emptyMap<Any?, Any>(),
            completionHandler = null,
        )
    }
}
