package shared.core.util

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.posix.memcpy
import platform.CoreGraphics.CGSize
import kotlinx.cinterop.useContents
import kotlin.math.min

actual class ImageProcessor {
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun process(
        uri: String,
        maxWidth: Int,
        maxHeight: Int,
        quality: Int
    ): ByteArray? {
        val image = UIImage.imageWithContentsOfFile(uri) ?: return null
        
        val size = image.size
        val width = size.useContents { width }
        val height = size.useContents { height }

        val ratio = min(maxWidth.toDouble() / width, maxHeight.toDouble() / height)
        
        if (ratio >= 1.0) {
            val data = UIImageJPEGRepresentation(image, quality / 100.0) ?: return null
            return data.toByteArray()
        }

        val newSize = CGSizeMake(width * ratio, height * ratio)

        UIGraphicsBeginImageContextWithOptions(newSize, false, 1.0)
        image.drawInRect(CGRectMake(0.0, 0.0, newSize.useContents { width }, newSize.useContents { height }))
        val resizedImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()

        if (resizedImage == null) return null

        val data = UIImageJPEGRepresentation(resizedImage, quality / 100.0) ?: return null
        return data.toByteArray()
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun NSData.toByteArray(): ByteArray {
        val bytes = ByteArray(length.toInt())
        bytes.usePinned { pinned ->
            memcpy(pinned.addressOf(0), bytes, length)
        }
        return bytes
    }
}
