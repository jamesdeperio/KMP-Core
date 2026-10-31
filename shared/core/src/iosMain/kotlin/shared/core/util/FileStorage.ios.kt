package shared.core.util

import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

actual class FileStorage {
    actual fun getFilesDir(): String {
        val paths = NSFileManager.defaultManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask)
        val documentsDirectory = paths.first() as NSURL
        return documentsDirectory.path ?: ""
    }
}
