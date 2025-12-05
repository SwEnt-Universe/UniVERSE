package com.android.universe.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.universe.model.user.UserProfile
import com.android.universe.ui.components.ImageDisplay
import com.android.universe.ui.components.LiquidButton
import com.android.universe.ui.theme.Dimensions

object ProfileContentTestTags {
  const val PROFILE_IMAGE_CONTAINER = "user_profile_image_container"
  const val USERNAME = "profile_username"
  const val TAGS_COLUMN = "profile_tags_column"
  const val FULL_NAME = "profile_full_name"
  const val DATE_OF_BIRTH = "profile_date_birth"
  const val LOCATION = "profile_location"
  const val DESCRIPTION = "profile_description"
  const val CHAT_BUTTON = "profile_chat_button"
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
 * @param followers Number of followers the user has (Implemented as optional because the logic is
 *   not created yet).
 * @param following Number of users the user is following (Implemented as optional because the logic
 *   is not created yet).
 * @param heightTagList Height of the tag list/row.
 * @param actionRowEnabled Whether to show the action row (chat, follow buttons) or not.
 * @param onChatClick Callback for chat button.
 * @param onToggleFollowing Callback for add/follow button.
 * @param onSettingsClick Optional callback for settings button. If null, settings button is not
 *   shown.
 */
@Composable
fun ProfileContentLayout(
    modifier: Modifier,
    userProfile: UserProfile,
    followers: Int? = 0,
    following: Int? = 0,
    heightTagList: Dp = 260.dp,
    actionRowEnabled: Boolean = true,
    onChatClick: () -> Unit,
    onToggleFollowing: () -> Unit,
    onSettingsClick: (() -> Unit)? = null
) {
  Column(modifier = modifier.fillMaxWidth().padding(Dimensions.PaddingLarge)) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
      Column(modifier = Modifier.weight(2f)) {
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
                    enabled = false,
                    onClick = {},
                    modifier =
                        Modifier.align(Alignment.TopStart)
                            .padding(Dimensions.PaddingLarge)
                            .testTag("${ProfileContentTestTags.USERNAME}_${userProfile.uid}")) {
                      Text(
                          text = userProfile.username,
                          style = MaterialTheme.typography.labelLarge,
                          color = MaterialTheme.colorScheme.onPrimary,
                          maxLines = 1,
                          overflow = TextOverflow.Ellipsis)
                    }
              }
        }

        Spacer(Modifier.height(Dimensions.SpacerMedium))

        UserInfoColumn(userProfile)
      }

      Column(modifier = Modifier.weight(1f)) {
        TagColumn(
            tags = userProfile.tags.toList(),
            isSelectable = false,
            isSelected = { false },
            heightList = heightTagList,
            modifierBox =
                Modifier.testTag("${ProfileContentTestTags.TAGS_COLUMN}_${userProfile.uid}")
                    .padding(start = Dimensions.PaddingLarge))

        if (onSettingsClick != null) {
          Spacer(Modifier.height(Dimensions.SpacerLarge))

          LiquidButton(
              onClick = onSettingsClick,
              height = Dimensions.CardButtonHeight,
              width = Dimensions.CardButtonWidth,
              contentPadding = Dimensions.PaddingMedium,
              modifier =
                  Modifier.testTag(
                      "${ProfileContentTestTags.SETTINGS_BUTTON}_${userProfile.uid}")) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                      imageVector = Icons.Outlined.Settings,
                      contentDescription = "Settings",
                      modifier = Modifier.size(Dimensions.IconSizeMedium))
                  Spacer(Modifier.width(Dimensions.SpacerSmall))
                  Text(text = "Settings", style = MaterialTheme.typography.labelLarge)
                }
              }
        }
      }
    }

    Spacer(Modifier.height(Dimensions.SpacerMedium))

    Text(
        userProfile.description ?: "No description available",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        modifier =
            Modifier.fillMaxWidth()
                .testTag("${ProfileContentTestTags.DESCRIPTION}_${userProfile.uid}"))

    if (actionRowEnabled) {
      Spacer(Modifier.height(Dimensions.SpacerMedium))

      ProfileCardActionsRow(
          userProfile = userProfile,
          followers = followers ?: 0,
          following = following ?: 0,
          onChatClick = onChatClick,
          onToggleFollowing = onToggleFollowing,
          modifier = Modifier.fillMaxWidth())
    }
  }
}
