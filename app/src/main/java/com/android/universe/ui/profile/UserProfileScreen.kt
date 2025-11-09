package com.android.universe.ui.profile

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.model.isoToCountryName
import com.android.universe.ui.navigation.NavigationBottomMenu
import com.android.universe.ui.navigation.NavigationTestTags
import com.android.universe.ui.navigation.Tab
import com.android.universe.ui.theme.DecorationBackground
import com.android.universe.ui.theme.Dimensions
import com.android.universe.ui.theme.Dimensions.PaddingLarge
import com.android.universe.ui.theme.UniverseTheme

/** Define all the tags for the UserProfile screen. Tags will be used to test the screen. */
object UserProfileScreenTestTags {
  const val FIRSTNAME = "userProfileFirstName"
  const val LASTNAME = "userProfileLastName"
  const val AGE = "userProfileAge"
  const val COUNTRY = "userProfileCountry"
  const val DESCRIPTION = "userProfileDescription"
  const val TAG = "userProfileTag"
  const val EDIT_BUTTON = "userProfileEditButton"
  const val TAGLIST = "userProfileTagList"

  fun getTagTestTag(index: Int): String {
    return "userProfileTag$index"
  }
}

/** Dimensions use in the screen for special cases. */
object UserProfileDimensions {
  val verticalPaddingColumn = 112.dp
  val profilePictureSize = 120.dp
  val curveDepthPx = 48.dp
  val backgroundHeight = 250.dp
}

/**
 * Coordinates use in the screen for the creation of the background. It groups together all the
 * coordinates and multiplier use in the creation of the path with Canvas for the background.
 */
object UserProfileBackGroundCoordinates {
  const val X_START = 0f
  const val Y_START = 0f
  const val Y_END = 0f
  const val X_LOW = 100f
  const val X_MEDIUM = 225f
  const val X_LARGE = 300f
  const val Y_MULTIPLIER_LOW = 1.25f
  const val Y_MULTIPLIER_MEDIUM = 2f
  const val Y_MULTIPLIER_LARGE = 4.25f
}

/** Line that separate components in the screen. */
@Composable
fun DividerProfileScreen() {
  HorizontalDivider(
      modifier = Modifier.padding(vertical = Dimensions.PaddingMedium),
      thickness = Dimensions.ThicknessMedium,
      color = DecorationBackground)
}

/**
 * Spacer to create a vertical space in the screen.
 *
 * @param dp The height of the spacer in density-independent pixels (Dp).
 */
@Composable
fun SpacerHeightUserProfile(dp: Dp) {
  Spacer(modifier = Modifier.height(dp))
}

/**
 * Spacer to create a horizontal space in the screen.
 *
 * @param dp The width of the spacer in density-independent pixels (Dp).
 */
@Composable
fun SpacerWidthUserProfile(dp: Dp) {
  Spacer(modifier = Modifier.width(dp))
}

/**
 * Composable for displaying a user's profile.
 *
 * @param uid The uid of the user to display.
 * @param onTabSelected Callback invoked when a tab is selected to switch between screens
 * @param onEditProfileClick Callback when the edit profile button is clicked.
 * @param userProfileViewModel The ViewModel responsible for managing user profile data.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun UserProfileScreen(
    uid: String,
    onTabSelected: (Tab) -> Unit = {},
    onEditProfileClick: (String) -> Unit = {},
    userProfileViewModel: UserProfileViewModel = viewModel()
) {

  val userUIState by userProfileViewModel.userState.collectAsState()
  val errorMsg = userUIState.errorMsg
  LaunchedEffect(uid) {
    userProfileViewModel.loadUser(uid)
  }
  val userAge = userUIState.age

  val context = LocalContext.current
  // Observe and display asynchronous validation errors as Toast messages.
  LaunchedEffect(errorMsg) {
    if (errorMsg != null) {
      Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
      userProfileViewModel.clearErrorMsg()
    }
  }

  Scaffold(
      modifier = Modifier.testTag(NavigationTestTags.PROFILE_SCREEN),
      bottomBar = { NavigationBottomMenu(Tab.Profile, onTabSelected) }) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(Color.White)) {
          // Box that contains the decoration background.
          Box(modifier = Modifier.fillMaxWidth()) { CurvedTopHeader() }
          Column(
              modifier =
                  Modifier.fillMaxSize()
                      .padding(vertical = UserProfileDimensions.verticalPaddingColumn)
                      .padding(PaddingLarge),
              horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.fillMaxWidth()) {
                  // Profile picture of the user.
                  Box(
                      modifier =
                          Modifier.align(Alignment.Center)
                              .size(UserProfileDimensions.profilePictureSize)
                              .background(MaterialTheme.colorScheme.surface, CircleShape),
                      contentAlignment = Alignment.Center) {
                        Icon(
                            tint = MaterialTheme.colorScheme.onSurface,
                            contentDescription = "Image",
                            imageVector = Icons.Filled.Image,
                            modifier = Modifier.size(Dimensions.IconSizeLarge))
                      }
                  // Setting icon to navigate to the edit profile screen.
                  IconButton(
                      onClick = { onEditProfileClick(uid) },
                      modifier =
                          Modifier.align(Alignment.BottomEnd)
                              .testTag(UserProfileScreenTestTags.EDIT_BUTTON)) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Edit Profile",
                            modifier = Modifier.size(Dimensions.IconSizeLarge))
                      }
                }

                SpacerHeightUserProfile(Dimensions.SpacerMedium)
                // FirstName Text of the user.
                Text(
                    text = userUIState.userProfile.firstName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.testTag(UserProfileScreenTestTags.FIRSTNAME))
                // LastName Text of the user.
                Text(
                    text = userUIState.userProfile.lastName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.testTag(UserProfileScreenTestTags.LASTNAME))

                SpacerHeightUserProfile(Dimensions.SpacerMedium)

                // Country of the user with his icon.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center) {
                      Icon(
                          tint = MaterialTheme.colorScheme.onSurface,
                          contentDescription = "Location",
                          imageVector = Icons.Filled.LocationOn,
                          modifier = Modifier.size(Dimensions.IconSizeSmall))
                      SpacerWidthUserProfile(Dimensions.SpacerSmall)
                      Text(
                          // We display the country name and not in Iso.
                          text =
                              "Country: ${isoToCountryName.get(userUIState.userProfile.country)}",
                          style = MaterialTheme.typography.bodyLarge,
                          color = MaterialTheme.colorScheme.onBackground,
                          modifier = Modifier.testTag(UserProfileScreenTestTags.COUNTRY))
                    }

                SpacerHeightUserProfile(Dimensions.SpacerSmall)

                // Age of the user.
                Text(
                    text = "Age: $userAge",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.testTag(UserProfileScreenTestTags.AGE))

                SpacerHeightUserProfile(Dimensions.SpacerSmall)
                DividerProfileScreen()

                // We display the description only if it is not null.
                if (userUIState.userProfile.description != null) {
                  SpacerHeightUserProfile(Dimensions.SpacerSmall)
                  // Description of the user.
                  Box(
                      modifier = Modifier.fillMaxWidth().height(Dimensions.BoxDescriptionSize),
                      contentAlignment = Alignment.TopStart) {
                        val descriptionText = userUIState.userProfile.description
                        Text(
                            text = descriptionText!!,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier =
                                Modifier.padding(PaddingLarge)
                                    .testTag(UserProfileScreenTestTags.DESCRIPTION))
                      }
                  DividerProfileScreen()
                }

                SpacerHeightUserProfile(Dimensions.SpacerSmall)

                // Tags of the user.
                FlowRow(
                    modifier =
                        Modifier.testTag(UserProfileScreenTestTags.TAGLIST)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalArrangement = Arrangement.spacedBy(Dimensions.SpacerSmall)) {
                      userUIState.userProfile.tags.toList().forEachIndexed { index, tag ->
                        InterestTag(tag.displayName, index)
                      }
                    }
              }
        }
      }
}

/**
 * Composable for displaying an interest tag.
 *
 * @param text The text to display as the tag.
 * @param testTagIndex The index of the test tag. Used to generate a unique test tag.
 */
@Composable
fun InterestTag(text: String, testTagIndex: Int) {
  Surface(
      color = MaterialTheme.colorScheme.primary,
      shape = RoundedCornerShape(50),
      shadowElevation = Dimensions.ShadowElevationTags,
      tonalElevation = Dimensions.TonalElevationTags,
      modifier =
          Modifier.height(height = Dimensions.HeightTags)
              .widthIn(min = Dimensions.WidthMinimumTags, max = Dimensions.WidthMaximumTags)) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
          Text(
              text = text,
              color = MaterialTheme.colorScheme.onPrimary,
              style = MaterialTheme.typography.labelMedium,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier =
                  Modifier.testTag(UserProfileScreenTestTags.getTagTestTag(testTagIndex))
                      .padding(
                          horizontal = Dimensions.HorizontalPaddingTagsText,
                          vertical = Dimensions.VerticalPaddingTagsText))
        }
      }
}

@Preview
@Composable
fun UserProfileScreenPreview() {
  UniverseTheme { UserProfileScreen(uid = "1") }
}

/**
 * Background for the profile screen. It uses Canvas to make the form of a circle surrounding the
 * profile picture.
 */
@Composable
fun CurvedTopHeader() {
  Canvas(modifier = Modifier.fillMaxWidth().height(UserProfileDimensions.backgroundHeight)) {
    val width = size.width
    val heightPx = size.height

    // We define the curveDepth in out shape.
    val curveDepthPx = UserProfileDimensions.curveDepthPx.toPx()

    // We define the path of point that will be display.
    val path =
        Path().apply {
          moveTo(
              x = UserProfileBackGroundCoordinates.X_START,
              y = UserProfileBackGroundCoordinates.Y_START)
          lineTo(x = UserProfileBackGroundCoordinates.X_START, y = heightPx - curveDepthPx)
          lineTo(x = UserProfileBackGroundCoordinates.X_LOW, y = heightPx - curveDepthPx)
          quadraticTo(
              x1 = UserProfileBackGroundCoordinates.X_MEDIUM,
              y1 = heightPx - curveDepthPx * UserProfileBackGroundCoordinates.Y_MULTIPLIER_LOW,
              x2 = UserProfileBackGroundCoordinates.X_LARGE,
              y2 = heightPx - curveDepthPx * UserProfileBackGroundCoordinates.Y_MULTIPLIER_MEDIUM)
          quadraticTo(
              x1 = width / 2,
              y1 = heightPx - curveDepthPx * UserProfileBackGroundCoordinates.Y_MULTIPLIER_LARGE,
              x2 = width - UserProfileBackGroundCoordinates.X_LARGE,
              y2 = heightPx - curveDepthPx * UserProfileBackGroundCoordinates.Y_MULTIPLIER_MEDIUM)
          quadraticTo(
              x1 = width - UserProfileBackGroundCoordinates.X_MEDIUM,
              y1 = heightPx - curveDepthPx * UserProfileBackGroundCoordinates.Y_MULTIPLIER_LOW,
              x2 = width - UserProfileBackGroundCoordinates.X_LOW,
              y2 = heightPx - curveDepthPx)
          lineTo(x = width, y = heightPx - curveDepthPx)
          lineTo(x = width, y = UserProfileBackGroundCoordinates.Y_END)
          close()
        }

    // We draw according to the path.
    drawPath(path = path, color = DecorationBackground)
  }
}
