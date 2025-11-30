package com.android.universe.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.android.universe.di.DefaultDP
import com.android.universe.ui.theme.Dimensions
import com.android.universe.ui.utils.toImageBitmap
import kotlinx.coroutines.CoroutineDispatcher

/**
 * A custom image picker component displaying a "liquid" style container.
 *
 * This component renders an image if provided (via [imageBytes]) or a placeholder icon if null.
 * When an image is present, it displays floating action buttons to both edit (replace) and delete
 * the image. If no image is present, it shows an "Add" button.
 *
 * @param imageBytes The raw byte array of the image to display. If null, a placeholder icon is
 *   shown.
 * @param onPickImage The callback lambda triggered when the edit/add button is clicked.
 * @param modifier The [Modifier] to be applied to the outer layout of the component.
 * @param onDeleteImage The callback lambda triggered when the delete button is clicked. The delete
 *   button is only visible when [imageBytes] is not null.
 * @param dispatcher The [CoroutineDispatcher] used for converting the byte array to a bitmap
 *   asynchronously. Defaults to [DefaultDP.default].
 */
@Composable
fun LiquidImagePicker(
    imageBytes: ByteArray?,
    onPickImage: () -> Unit,
    modifier: Modifier = Modifier,
    onDeleteImage: () -> Unit = {},
    dispatcher: CoroutineDispatcher = DefaultDP.default
) {
  val imageBitmap by
      produceState<ImageBitmap?>(initialValue = null, key1 = imageBytes) {
        value = imageBytes?.toImageBitmap(dispatcher)
      }

  LiquidBox(shape = RoundedCornerShape(24.dp), modifier = modifier) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Box(
          modifier =
              Modifier.matchParentSize()
                  .padding(Dimensions.PaddingMedium)
                  .clip(RoundedCornerShape(16.dp)),
          contentAlignment = Alignment.Center) {
            if (imageBitmap != null) {
              Image(
                  bitmap = imageBitmap!!,
                  contentDescription = "Selected Image",
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.fillMaxSize())
            } else {
              Icon(
                  imageVector = Icons.Outlined.Image,
                  contentDescription = "No Image",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(48.dp))
            }
          }

      if (imageBitmap != null) {
        LiquidButton(
            onClick = onDeleteImage,
            width = 32f,
            height = 32f,
            contentPadding = Dimensions.PaddingSmall,
            modifier =
                Modifier.align(Alignment.BottomStart)
                    .padding(
                        start = Dimensions.PaddingMedium + 4.dp,
                        bottom = Dimensions.PaddingMedium + 4.dp)) {
              Icon(
                  imageVector = Icons.Default.Delete,
                  contentDescription = "Delete Image",
                  tint = MaterialTheme.colorScheme.onBackground,
                  modifier = Modifier.size(24.dp))
            }
      }

      LiquidButton(
          onClick = onPickImage,
          width = 32f,
          height = 32f,
          contentPadding = Dimensions.PaddingSmall,
          modifier =
              Modifier.align(Alignment.BottomEnd)
                  .padding(
                      end = Dimensions.PaddingMedium + 4.dp,
                      bottom = Dimensions.PaddingMedium + 4.dp)) {
            Icon(
                imageVector = if (imageBitmap != null) Icons.Default.Edit else Icons.Default.Add,
                contentDescription = if (imageBitmap != null) "Edit Image" else "Add Image",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(24.dp))
          }
    }
  }
}
