package com.android.universe.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.PersonRemove
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
import androidx.compose.ui.unit.dp
import com.android.universe.model.user.UserProfile
import com.android.universe.ui.components.LiquidButton
import com.android.universe.ui.theme.Dimensions
import java.time.format.DateTimeFormatter

/**
 * A composable function that displays user information in a column layout.
 *
 * @param userProfile The user's profile data.
 */
@Composable
fun UserInfoColumn(userProfile: UserProfile) {
  Column {
    Text(
        text = "${userProfile.firstName} ${userProfile.lastName}",
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.testTag("${ProfileContentTestTags.FULL_NAME}_${userProfile.uid}"))

    Spacer(Modifier.height(Dimensions.SpacerMedium))

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.testTag("${ProfileContentTestTags.LOCATION}_${userProfile.uid}")) {
          Icon(
              imageVector = Icons.Filled.LocationOn,
              contentDescription = "Location",
              modifier = Modifier.size(Dimensions.IconSizeMedium),
              tint = MaterialTheme.colorScheme.onSurface)
          Spacer(Modifier.width(Dimensions.SpacerSmall))
          Text(
              text = userProfile.country,
              style = MaterialTheme.typography.bodyLarge,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              color = MaterialTheme.colorScheme.onSurface)
        }

    Spacer(Modifier.height(Dimensions.SpacerMedium))

    Text(
        userProfile.dateOfBirth.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
        style = MaterialTheme.typography.bodyLarge,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.testTag("${ProfileContentTestTags.DATE_OF_BIRTH}_${userProfile.uid}"))
  }
}

/**
 * A composable function that displays either a "Follow/Unfollow" button or a "Settings" button
 * based on the provided parameters.
 *
 * @param userProfile The user's profile data.
 * @param isFollowing Whether the current user is following this user.
 * @param onToggleFollowing Callback for follow/unfollow button.
 * @param onSettingsClick Optional callback for settings button. If null, follow/unfollow button is
 *   shown.
 */
@Composable
fun FollowingOrSettingsButton(
    userProfile: UserProfile,
    isFollowing: Boolean,
    onToggleFollowing: () -> Unit,
    onSettingsClick: (() -> Unit)? = null
) {
  if (onSettingsClick != null) {
    LiquidButton(
        onClick = onSettingsClick,
        height = Dimensions.CardButtonHeight,
        contentPadding = Dimensions.PaddingMedium,
        modifier =
            Modifier.testTag("${ProfileContentTestTags.SETTINGS_BUTTON}_${userProfile.uid}")) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Settings",
                modifier = Modifier.size(Dimensions.IconSizeSmall),
                tint = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.width(Dimensions.SpacerSmall))
            Text(
                text = "Settings",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface)
          }
        }
  } else {
    LiquidButton(
        onClick = onToggleFollowing,
        height = Dimensions.CardButtonHeight,
        contentPadding = Dimensions.PaddingMedium,
        modifier = Modifier.testTag("${ProfileContentTestTags.ADD_BUTTON}_${userProfile.uid}")) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector =
                    if (isFollowing) Icons.Outlined.PersonRemove else Icons.Outlined.PersonAdd,
                contentDescription = if (isFollowing) "Unfollow" else "Follow",
                modifier = Modifier.size(Dimensions.IconSizeSmall),
                tint = MaterialTheme.colorScheme.onSurface)

            Spacer(Modifier.width(Dimensions.SpacerSmall))

            Text(
                text = if (isFollowing) "Unfollow" else "Follow",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface)
          }
        }
  }
}

/**
 * A composable function that displays the number of followers and following in a row layout.
 *
 * @param userProfile The user's profile data.
 * @param modifier Modifier for styling/layout.
 */
@Composable
fun FollowersFollowingColumn(userProfile: UserProfile, modifier: Modifier) {
  Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier.testTag("${ProfileContentTestTags.FOLLOWERS_COUNT}_${userProfile.uid}")) {
          LiquidButton(
              modifier = Modifier.size(35.dp).clip(CircleShape),
              enabled = false,
              isInteractive = false,
              onClick = {},
              contentPadding = Dimensions.PaddingSmall) {
                Text(
                    text = "${userProfile.followers.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface)
              }
          Text(
              text = "followers",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurface,
              softWrap = false,
              overflow = TextOverflow.Clip,
              maxLines = 1)
        }

    Spacer(Modifier.width(Dimensions.SpacerSmall))

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier.testTag("${ProfileContentTestTags.FOLLOWING_COUNT}_${userProfile.uid}")) {
          LiquidButton(
              modifier = Modifier.size(35.dp).clip(CircleShape),
              enabled = false,
              isInteractive = false,
              onClick = {},
              contentPadding = Dimensions.PaddingSmall) {
                Text(
                    text = "${userProfile.following.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface)
              }
          Text(
              text = "following",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurface,
              softWrap = false,
              overflow = TextOverflow.Clip,
              maxLines = 1)
        }
  }
}
