package com.android.universe.ui.common

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import com.android.universe.R
import com.android.universe.di.DefaultDP
import kotlinx.coroutines.withContext

/** Object containing test tags for Event content components. */
object EventContentTestTags {
  const val EVENT_IMAGE_CONTAINER = "event_image_container"
  const val DEFAULT_EVENT_IMAGE = "default_event_image"
  const val EVENT_IMAGE = "event_image"
  const val EVENT_TITLE = "event_title"
  const val EVENT_DATE = "event_date"
  const val EVENT_TIME = "event_time"
  const val EVENT_DESCRIPTION = "event_description"
  const val EVENT_PARTICIPANTS = "event_participants"
  const val PARTICIPATION_BUTTON = "event_participation_button"
  const val CHAT_BUTTON = "event_chat_button"
}

/**
 * Helper composable that handles event image loading and display. Decodes ByteArray to Bitmap and
 * shows either the event image or default placeholder.
 *
 * @param eventImage ByteArray? representing the event image data.
 * @param modifier Modifier to be applied to the Image composable.
 */
@Composable
fun EventImageHelper(eventImage: ByteArray?, modifier: Modifier = Modifier) {
  val bitmap =
      produceState<Bitmap?>(initialValue = null, eventImage) {
            value =
                if (eventImage != null) {
                  withContext(DefaultDP.io) {
                    BitmapFactory.decodeByteArray(eventImage, 0, eventImage.size)
                  }
                } else {
                  null
                }
          }
          .value

  if (bitmap == null) {
    Image(
        painter = painterResource(id = R.drawable.default_event_img),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.testTag(EventContentTestTags.DEFAULT_EVENT_IMAGE))
  } else {
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.testTag(EventContentTestTags.EVENT_IMAGE))
  }
}
