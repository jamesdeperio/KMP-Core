package shared.core.util

import shared.core.platform.appContext

actual class FileStorage {
    actual fun getFilesDir(): String {
        return appContext.filesDir.absolutePath
    }
}
