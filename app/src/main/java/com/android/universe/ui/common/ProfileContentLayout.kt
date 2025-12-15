package com.android.universe.ui.common

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import com.android.universe.model.user.UserProfile
import com.android.universe.ui.components.ImageDisplay
import com.android.universe.ui.components.LiquidButton
import com.android.universe.ui.theme.Dimensions
import com.android.universe.ui.theme.UniverseTheme

object ProfileContentTestTags {
  const val PROFILE_IMAGE_CONTAINER = "user_profile_image_container"
  const val USERNAME = "profile_username"
  const val TAGS_COLUMN = "profile_tags_column"
  const val FULL_NAME = "profile_full_name"
  const val DATE_OF_BIRTH = "profile_date_birth"
  const val DESCRIPTION = "profile_description"
  const val FOLLOWERS_COUNT = "profile_followers_count"
  const val FOLLOWING_COUNT = "profile_following_count"
  const val ADD_BUTTON = "profile_add_button"

  const val SETTINGS_BUTTON = "profile_settings_button"
}

/**
 * Displays the main content layout of a User Profile.
 *
 * @param modifier Modifier for styling/layout.
 * @param userProfile The user's profile data.
 * @param isFollowing Whether the current user is following this user.
 * @param onToggleFollowing Callback for add/follow button.
 * @param onSettingsClick Optional callback for settings button. If null, settings button is not
 *   shown.
 */
@Composable
fun ProfileContentLayout(
    modifier: Modifier,
    userProfile: UserProfile,
    isFollowing: Boolean? = null,
    onToggleFollowing: () -> Unit,
    onSettingsClick: (() -> Unit)? = null
) {
  val scrollState = rememberScrollState()
  Column(modifier = modifier.fillMaxWidth().padding(Dimensions.PaddingLarge)) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
      Column(modifier = Modifier.weight(1.4f)) {
        Box(modifier = Modifier.fillMaxWidth()) {
          Box(
              modifier =
                  Modifier.height(Dimensions.CardImageHeight)
                      .clip(RoundedCornerShape(Dimensions.RoundedCornerLarge))
                      .testTag(
                          "${ProfileContentTestTags.PROFILE_IMAGE_CONTAINER}_${userProfile.uid}")) {
                ImageDisplay(
                    image = userProfile.profilePicture,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize())

                LiquidButton(
                    height = Dimensions.CardImageTagOverlayHeight,
                    width = Dimensions.CardImageTagOverlayWidth,
                    isInteractive = false,
                    tint = UniverseTheme.extendedColors.overImage,
                    enabled = false,
                    onClick = {},
                    modifier =
                        Modifier.align(Alignment.TopStart)
                            .padding(Dimensions.PaddingLarge)
                            .testTag("${ProfileContentTestTags.USERNAME}_${userProfile.uid}")) {
                      Row(modifier = Modifier.horizontalScroll(scrollState)) {
                        Text(
                            text = userProfile.username,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            overflow = TextOverflow.Clip)
                      }
                    }
              }
        }
      }

      Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
        TagColumn(
            tags = userProfile.tags.toList(),
            isSelectable = false,
            isSelected = { false },
            heightList = Dimensions.CardImageHeight,
            modifierBox =
                Modifier.testTag("${ProfileContentTestTags.TAGS_COLUMN}_${userProfile.uid}")
                    .padding(start = Dimensions.PaddingLarge))
      }
    }

    Spacer(Modifier.height(Dimensions.SpacerMedium))

    Row(modifier = Modifier.fillMaxWidth()) {
      Column(modifier = Modifier.weight(1.4f)) { UserInfoColumn(userProfile) }

      Column(modifier = Modifier.weight(1f).padding(start = Dimensions.PaddingLarge)) {
        FollowingOrSettingsButton(
            userProfile = userProfile,
            isFollowing = isFollowing ?: false,
            onToggleFollowing = onToggleFollowing,
            onSettingsClick = onSettingsClick)

        Spacer(Modifier.height(Dimensions.SpacerMedium))

        FollowersFollowingColumn(userProfile = userProfile)
      }
    }

    if (userProfile.description != null) {
      Spacer(Modifier.height(Dimensions.SpacerSmall))

      Text(
          text = userProfile.description,
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.testTag("${ProfileContentTestTags.DESCRIPTION}_${userProfile.uid}"))
    }
  }
}
