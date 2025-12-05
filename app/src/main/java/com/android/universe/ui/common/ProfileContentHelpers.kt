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
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.PersonAdd
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

    Spacer(Modifier.height(Dimensions.SpacerSmall))

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.testTag("${ProfileContentTestTags.LOCATION}_${userProfile.uid}")) {
          Icon(
              imageVector = Icons.Filled.LocationOn,
              contentDescription = "Location",
              modifier = Modifier.size(Dimensions.IconSizeMedium))
          Spacer(Modifier.width(Dimensions.SpacerSmall))
          Text(
              text = userProfile.country,
              style = MaterialTheme.typography.bodyLarge,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis)
        }

    Spacer(Modifier.height(Dimensions.SpacerSmall))

    Text(
        userProfile.dateOfBirth.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
        style = MaterialTheme.typography.bodyLarge,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.testTag("${ProfileContentTestTags.DATE_OF_BIRTH}_${userProfile.uid}"))
  }
}

/**
 * A composable function that displays a row of action buttons and follower/following counts on a
 * user's profile card.
 *
 * @param userProfile The user's profile data.
 * @param followers The number of followers the user has.
 * @param following The number of users the user is following.
 * @param onChatClick A lambda function that is called when the "Chat" button is clicked.
 * @param onToggleFollowing A lambda function that is called when the "Add" button is clicked.
 * @param modifier The modifier to be applied to the row.
 */
@Composable
fun ProfileCardActionsRow(
    userProfile: UserProfile,
    followers: Int,
    following: Int,
    onChatClick: () -> Unit,
    onToggleFollowing: () -> Unit,
    modifier: Modifier
) {
  Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
    LiquidButton(
        onClick = onChatClick,
        height = Dimensions.CardButtonHeight,
        width = Dimensions.CardButtonWidth,
        modifier = Modifier.testTag("${ProfileContentTestTags.CHAT_BUTTON}_${userProfile.uid}")) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Chat,
                contentDescription = "Chat",
                modifier = Modifier.size(Dimensions.IconSizeMedium))
            Spacer(Modifier.width(Dimensions.SpacerSmall))
            Text(text = "Chat", style = MaterialTheme.typography.labelLarge)
          }
        }

    Spacer(Modifier.width(Dimensions.SpacerSmall))

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
                    text = "$followers",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
              }
          Text(text = "followers", style = MaterialTheme.typography.labelSmall)
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
                    text = "$following",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
              }
          Text(text = "following", style = MaterialTheme.typography.labelSmall)
        }

    Spacer(Modifier.width(Dimensions.SpacerSmall))

    LiquidButton(
        onClick = onToggleFollowing,
        height = Dimensions.CardButtonHeight,
        width = Dimensions.CardButtonWidth,
        modifier = Modifier.testTag("${ProfileContentTestTags.ADD_BUTTON}_${userProfile.uid}")) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.PersonAdd,
                contentDescription = "Add",
                modifier = Modifier.size(Dimensions.IconSizeMedium))
            Spacer(Modifier.width(Dimensions.SpacerSmall))
            Text(text = "Add", style = MaterialTheme.typography.labelLarge)
          }
        }
  }
}
