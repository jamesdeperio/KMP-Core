package shared.core.util

/**
 * Utility for processing (resizing/compressing) images.
 * Works across Android and iOS via expect/actual.
 */
expect class ImageProcessor() {
    /**
     * Resizes and compresses the image at the given URI/Path.
     * Returns the processed image as a ByteArray (JPEG format).
     * @param uri The platform-specific URI or file path to the image.
     * @param maxWidth The maximum width for the resized image.
     * @param maxHeight The maximum height for the resized image.
     * @param quality The JPEG compression quality (0-100).
     */
    suspend fun process(
        uri: String,
        maxWidth: Int = 1080,
        maxHeight: Int = 1080,
        quality: Int = 80
    ): ByteArray?
}
