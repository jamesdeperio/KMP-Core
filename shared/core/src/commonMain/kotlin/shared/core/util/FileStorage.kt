package shared.core.util

import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM

/**
 * Utility for saving and managing files in the app's internal storage.
 */
expect class FileStorage() {
    /**
     * Returns the absolute path to the app's private files directory.
     */
    fun getFilesDir(): String
}

/**
 * Helper to save bytes to a specific file in the "drafts" directory.
 */
fun FileStorage.saveDraft(fileName: String, bytes: ByteArray): String? {
    return try {
        val draftsDir = "${getFilesDir()}/drafts".toPath()
        val fileSystem = FileSystem.SYSTEM

        if (!fileSystem.exists(draftsDir)) {
            fileSystem.createDirectories(draftsDir)
        }

        val filePath = draftsDir / fileName
        fileSystem.write(filePath) {
            write(bytes)
        }

        filePath.toString()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Helper to delete all files in the drafts directory.
 */
fun FileStorage.clearDrafts() {
    try {
        val draftsDir = "${getFilesDir()}/drafts".toPath()
        val fileSystem = FileSystem.SYSTEM
        if (fileSystem.exists(draftsDir)) {
            fileSystem.deleteRecursively(draftsDir)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * Helper to delete a specific file given its path.
 */
fun FileStorage.deleteFile(path: String) {
    try {
        val okioPath = path.toPath()
        val fileSystem = FileSystem.SYSTEM
        if (fileSystem.exists(okioPath)) {
            fileSystem.delete(okioPath)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
