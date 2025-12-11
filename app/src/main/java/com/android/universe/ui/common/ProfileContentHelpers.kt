package com.android.universe.ui.common

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import com.android.universe.model.user.UserProfile
import com.android.universe.ui.components.LiquidButton
import com.android.universe.ui.theme.Dimensions
import java.time.format.DateTimeFormatter

private val DOB_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy")

/**
 * A composable function that displays user information in a column layout.
 *
 * @param userProfile The user's profile data.
 */
@Composable
fun UserInfoColumn(userProfile: UserProfile) {
  val scrollState = rememberScrollState()

  Column {
    Row(modifier = Modifier.horizontalScroll(scrollState)) {
      Text(
          text = "${userProfile.firstName} ${userProfile.lastName}",
          style = MaterialTheme.typography.headlineMedium,
          color = MaterialTheme.colorScheme.onSurface,
          overflow = TextOverflow.Clip,
          modifier = Modifier.testTag("${ProfileContentTestTags.FULL_NAME}_${userProfile.uid}"))
    }

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
        userProfile.dateOfBirth.format(DOB_FORMATTER),
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
 * A composable function that displays a statistic in a circular button with a label below it.
 *
 * @param label The label for the statistic.
 * @param count The count/value of the statistic.
 * @param testTag The test tag for UI testing.
 */
@Composable
fun StatCircleColumn(label: String, count: Int, testTag: String) {
  Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.testTag(testTag)) {
    LiquidButton(
        modifier = Modifier.size(Dimensions.NumberCircleSize).clip(CircleShape),
        enabled = false,
        isInteractive = false,
        onClick = {},
        contentPadding = Dimensions.PaddingSmall) {
          Text(
              text = "$count",
              style = MaterialTheme.typography.labelSmall,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              color = MaterialTheme.colorScheme.onSurface)
        }

    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurface,
        softWrap = false,
        overflow = TextOverflow.Clip,
        maxLines = 1)
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
    StatCircleColumn(
        label = "followers",
        count = userProfile.followers.size,
        testTag = "${ProfileContentTestTags.FOLLOWERS_COUNT}_${userProfile.uid}")

    Spacer(Modifier.width(Dimensions.SpacerSmall))

    StatCircleColumn(
        label = "following",
        count = userProfile.following.size,
        testTag = "${ProfileContentTestTags.FOLLOWING_COUNT}_${userProfile.uid}")
  }
}
