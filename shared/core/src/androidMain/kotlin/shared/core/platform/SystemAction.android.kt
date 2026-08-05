package shared.core.platform

import android.content.Intent
import androidx.core.net.toUri

actual object SystemAction {
    actual fun exitApp() {
        appActivity.finishAffinity()
    }

    actual fun openLink(link: String) {
        val uri = link.toUri()
        val intent =
            when {
                link.contains("instagram.com/reel") ->
                    Intent(Intent.ACTION_VIEW, uri).apply {
                        `package` = "com.instagram.android"
                    }
                link.contains("tiktok.com") ->
                    Intent(Intent.ACTION_VIEW, uri).apply {
                        `package` = "com.zhiliaoapp.musically"
                    }
                link.contains("youtube.com/shorts") || link.contains("youtu.be") ->
                    Intent(Intent.ACTION_VIEW, uri).apply {
                        `package` = "com.google.android.youtube"
                    }
                else -> Intent(Intent.ACTION_VIEW, uri)
            }

        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            appContext.startActivity(intent)
        } catch (e: Exception) {
            // fallback: open in browser
            appContext.startActivity(
                Intent(Intent.ACTION_VIEW, uri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                },
            )
        }
    }
}
