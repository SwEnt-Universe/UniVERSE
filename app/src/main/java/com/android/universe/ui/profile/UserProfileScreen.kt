package com.android.universe.ui.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.ui.navigation.NavigationBottomMenu
import com.android.universe.ui.navigation.NavigationTestTags
import com.android.universe.ui.navigation.Tab
import com.android.universe.ui.theme.Dimensions
import com.android.universe.ui.theme.UniverseTheme
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener

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

object FieldFontSizes {
  const val NAMES = 16
  const val AGE = 14
  const val COUNTRY = 14
  const val DESCRIPTION = 14
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
  userProfileViewModel.loadUser(uid)

  val context = LocalContext.current
  // Observe and display asynchronous validation errors as Toast messages.
  LaunchedEffect(errorMsg) {
    if (errorMsg != null) {
      Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
      userProfileViewModel.clearErrorMsg()
    }
  }
  val userAge = userProfileViewModel.calculateAge(dateOfBirth = userUIState.userProfile.dateOfBirth)
  Scaffold(
      modifier = Modifier.testTag(NavigationTestTags.PROFILE_SCREEN),
      topBar = {
        TopAppBar(
            title = { Text("Profile") },
            actions = {
              IconButton(
                  onClick = { onEditProfileClick(uid) },
                  modifier = Modifier
                      .testTag(UserProfileScreenTestTags.EDIT_BUTTON)) {
                    Icon(
                      Icons.Default.Settings,
                      contentDescription = "Edit Profile",
                      modifier = Modifier.size(Dimensions.IconSizeLarge))
                  }
            })
      },
      bottomBar = { NavigationBottomMenu(Tab.Profile, onTabSelected) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceAround) {
                    // Profile picture placeholder
                    Box(
                        modifier = Modifier
                          .size(80.dp)
                          .background(MaterialTheme.colorScheme.surface, CircleShape),
                        contentAlignment = Alignment.Center) {
                          Text("Img", color = MaterialTheme.colorScheme.onSurface)
                        }

                    Column(
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally) {

                          // First and last name
                          Text(
                              text = userUIState.userProfile.firstName,
                              fontSize = FieldFontSizes.NAMES.sp,
                              color = MaterialTheme.colorScheme.onBackground,
                              modifier = Modifier.testTag(UserProfileScreenTestTags.FIRSTNAME))
                          Spacer(modifier = Modifier.width(4.dp))
                          Text(
                              text = userUIState.userProfile.lastName,
                              fontSize = FieldFontSizes.NAMES.sp,
                              color = MaterialTheme.colorScheme.onBackground,
                              modifier = Modifier.testTag(UserProfileScreenTestTags.LASTNAME))

                          // Age and country (split for tagging, same line)
                          Row(
                              verticalAlignment = Alignment.CenterVertically,
                              horizontalArrangement = Arrangement.Center) {
                                Text(
                                    text = "Age: $userAge",
                                    fontSize = FieldFontSizes.AGE.sp,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.testTag(UserProfileScreenTestTags.AGE))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Country: ${userUIState.userProfile.country}",
                                    fontSize = FieldFontSizes.COUNTRY.sp,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.testTag(UserProfileScreenTestTags.COUNTRY))
                              }
                        }
                  }

              Spacer(modifier = Modifier.height(16.dp))
              Box(
                  modifier =
                      Modifier.fillMaxWidth()
                          .height(100.dp)
                          .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(Dimensions.RoundedCorner)
                          ),
                  contentAlignment = Alignment.Center) {
                    FlowRow(
                        modifier =
                            Modifier.testTag(UserProfileScreenTestTags.TAGLIST)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalArrangement = Arrangement.spacedBy(4.dp)) {
                          userUIState.userProfile.tags.toList().forEachIndexed { index, tag ->
                            InterestTag(tag.displayName, index)
                          }
                        }
                  }

              Spacer(modifier = Modifier.height(16.dp))
              val descriptionSize = 100
              // Description box
              Column(modifier = Modifier.fillMaxWidth()) {
                Text("Description:", fontSize = 16.sp)
                Box(
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(descriptionSize.dp)
                            .background(
                              color = MaterialTheme.colorScheme.surface,
                              shape = RoundedCornerShape(Dimensions.RoundedCorner)
                            ),
                    contentAlignment = Alignment.Center) {
                      val descriptionText =
                          userUIState.userProfile.description.takeUnless { it.isNullOrBlank() }
                              ?: "No description"
                      Text(
                          text = descriptionText,
                          fontSize = FieldFontSizes.DESCRIPTION.sp,
                          modifier =
                              Modifier.padding(16.dp)
                                  .testTag(UserProfileScreenTestTags.DESCRIPTION))
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
      tonalElevation = 1.dp) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.labelMedium,
            modifier =
                Modifier.testTag(UserProfileScreenTestTags.getTagTestTag(testTagIndex))
                    .padding(horizontal = 12.dp, vertical = 6.dp))
      }
}

/* Preview should be commented out in production */
/*

 */
@Preview
@Composable
fun UserProfileScreenPreview() {
  UniverseTheme {
    UserProfileScreen(uid = "1")
  }
}
