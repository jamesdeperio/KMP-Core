package shared.core.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.darwin.NSObject
import shared.core.platform.appActivity

@Composable
actual fun rememberPermissionRequester(
    onResult: (PermissionState) -> Unit,
): (Permission) -> Unit {
    return { onResult(PermissionState.GRANTED) }
}

private class GalleryDelegate(
    private val onImagePicked: (String) -> Unit,
) : NSObject(), PHPickerViewControllerDelegateProtocol {
    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        picker.dismissViewControllerAnimated(true, null)
        val result = didFinishPicking.firstOrNull() as? PHPickerResult ?: return
        val itemProvider = result.itemProvider

        if (itemProvider.canLoadObjectOfClass(platform.UIKit.UIImage)) {
            itemProvider.loadObjectOfClass(platform.UIKit.UIImage) { image, _ ->
                if (image != null && image is platform.UIKit.UIImage) {
                    val data = platform.UIKit.UIImageJPEGRepresentation(image, 1.0)
                    if (data != null) {
                        val tempPath = "${platform.Foundation.NSTemporaryDirectory()}temp_picker.jpg"
                        data.writeToFile(tempPath, true)
                        onImagePicked(tempPath)
                    }
                }
            }
        }
    }
}

@Composable
actual fun rememberGalleryPicker(
    onImagePicked: (String) -> Unit,
): () -> Unit {
    val delegate = remember { GalleryDelegate(onImagePicked) }

    return {
        val configuration = PHPickerConfiguration()
        configuration.filter = PHPickerFilter.imagesFilter
        configuration.selectionLimit = 1

        val picker = PHPickerViewController(configuration)
        picker.delegate = delegate

        appActivity.presentViewController(picker, true, null)
    }
}
