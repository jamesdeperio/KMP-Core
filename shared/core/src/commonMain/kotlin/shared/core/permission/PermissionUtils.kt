package shared.core.permission

import androidx.compose.runtime.Composable

/**
 * Remembers a launcher for requesting a permission.
 * Returns a lambda that takes a [Permission] and triggers the request.
 */
@Composable
expect fun rememberPermissionRequester(
    onResult: (PermissionState) -> Unit
): (Permission) -> Unit

/**
 * Remembers a launcher for picking an image from the gallery.
 * Returns a lambda that triggers the picker.
 */
@Composable
expect fun rememberGalleryPicker(
    onImagePicked: (String) -> Unit
): () -> Unit
