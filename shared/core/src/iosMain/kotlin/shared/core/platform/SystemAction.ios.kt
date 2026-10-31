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
                link.contains("instagram.com/reel", ignoreCase = true) || link.contains("instagram.com/reels", ignoreCase = true) -> {
                    val reelId = url.path?.substringAfter("/reels/")?.substringAfter("/reel/")?.substringBefore("/")
                    if (reelId != null) NSURL.URLWithString("instagram://reels/$reelId/") else null
                }
                link.contains("tiktok.com", ignoreCase = true) -> {
                    val videoId = url.path?.substringAfter("/video/")?.substringBefore("/")
                    if (videoId != null) NSURL.URLWithString("snssdk1128://aweme/detail/$videoId") else null
                }
                link.contains("youtube.com/shorts", ignoreCase = true) -> {
                    val shortId = url.path?.substringAfter("/shorts/")?.substringBefore("/")
                    if (shortId != null) NSURL.URLWithString("youtube://shorts/$shortId") else null
                }
                link.contains("youtu.be", ignoreCase = true) || link.contains("youtube.com/watch", ignoreCase = true) -> {
                    val videoId = if (link.contains("youtu.be")) {
                        url.path?.removePrefix("/")
                    } else {
                        url.query?.substringAfter("v=")?.substringBefore("&")
                    }
                    if (videoId != null) NSURL.URLWithString("youtube://watch?v=$videoId") else null
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
