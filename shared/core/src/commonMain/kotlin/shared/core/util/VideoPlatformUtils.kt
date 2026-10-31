package shared.core.util

object VideoPlatformUtils {

    enum class Platform(val displayName: String) {
        TIKTOK("TikTok"),
        INSTAGRAM("Instagram"),
        YOUTUBE("YouTube"),
        OTHER("Video")
    }

    fun parsePlatform(url: String): Platform {
        return when {
            url.contains("tiktok.com", ignoreCase = true) -> Platform.TIKTOK
            url.contains("instagram.com", ignoreCase = true) -> Platform.INSTAGRAM
            url.contains("youtube.com", ignoreCase = true) || url.contains("youtu.be", ignoreCase = true) -> Platform.YOUTUBE
            else -> Platform.OTHER
        }
    }

    /**
     * Returns true if the URL is from one of the supported platforms (TikTok, Instagram, YouTube)
     * or if the URL is empty (as it is optional).
     */
    fun isValidVideoUrl(url: String): Boolean {
        if (url.isBlank()) return true
        return parsePlatform(url) != Platform.OTHER
    }
}
