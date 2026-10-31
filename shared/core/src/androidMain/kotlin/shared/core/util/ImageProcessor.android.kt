package shared.core.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import shared.core.platform.appContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.math.min

actual class ImageProcessor {
    actual suspend fun process(
        uri: String,
        maxWidth: Int,
        maxHeight: Int,
        quality: Int
    ): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val contentUri = Uri.parse(uri)
            val inputStream = appContext.contentResolver.openInputStream(contentUri) ?: return@withContext null

            // 1. Decode bounds to calculate sample size
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            // 2. Calculate sample size (for memory efficiency)
            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
            options.inJustDecodeBounds = false

            // 3. Decode actual bitmap with sample size
            val resizedStream = appContext.contentResolver.openInputStream(contentUri) ?: return@withContext null
            val bitmap = BitmapFactory.decodeStream(resizedStream, null, options) ?: return@withContext null
            resizedStream.close()

            // 4. Rotate bitmap based on EXIF
            val orientation = getOrientation(contentUri)
            val rotatedBitmap = rotateBitmap(bitmap, orientation)

            // 5. Precise scaling if needed
            val scaledBitmap = if (rotatedBitmap.width > maxWidth || rotatedBitmap.height > maxHeight) {
                val ratio = min(maxWidth.toFloat() / rotatedBitmap.width, maxHeight.toFloat() / rotatedBitmap.height)
                Bitmap.createScaledBitmap(
                    rotatedBitmap,
                    (rotatedBitmap.width * ratio).toInt(),
                    (rotatedBitmap.height * ratio).toInt(),
                    true
                )
            } else {
                rotatedBitmap
            }

            // 6. Compress to JPEG
            val out = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            
            // Cleanup
            if (scaledBitmap != rotatedBitmap) scaledBitmap.recycle()
            if (rotatedBitmap != bitmap) rotatedBitmap.recycle() // This is now redundant due to rotateBitmap recycling, but safe
            if (!bitmap.isRecycled) bitmap.recycle() // Final safety check

            out.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getOrientation(uri: Uri): Int {
        return try {
            appContext.contentResolver.openInputStream(uri)?.use { input ->
                val exifInterface = ExifInterface(input)
                exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            } ?: ExifInterface.ORIENTATION_NORMAL
        } catch (_: Exception) {
            ExifInterface.ORIENTATION_NORMAL
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(270f)
                matrix.postScale(-1f, 1f)
            }
            else -> return bitmap
        }
        
        return try {
            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (rotated != bitmap) {
                bitmap.recycle()
            }
            rotated
        } catch (e: Throwable) {
            e.printStackTrace()
            bitmap
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
