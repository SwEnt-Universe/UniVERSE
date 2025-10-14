package com.android.universe.ui.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.android.universe.model.Tag
import com.android.universe.ui.navigation.NavigationBottomMenu
import com.android.universe.ui.navigation.NavigationScreens
import com.android.universe.ui.navigation.NavigationTestTags
import com.android.universe.ui.navigation.Tab
import kotlin.collections.chunked

object UserProfileScreenTestTags {
  const val FIRSTNAME = "userProfileFirstName"
  const val LASTNAME = "userProfileLastName"
  const val AGE = "userProfileAge"
  const val COUNTRY = "userProfileCountry"
  const val DESCRIPTION = "userProfileDescription"
  const val TAG = "userProfileTag"
  const val EDIT_BUTTON = "userProfileEditButton"
}

/**
 * Composable for displaying a user's profile.
 *
 * @param username The username of the user to display.
 * @param onTabSelected Callback when a bottom navigation tab is selected.
 * @param navController The NavController for handling navigation actions.
 * @param userProfileViewModel The ViewModel responsible for managing user profile data.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    username: String,
    onTabSelected: (Tab) -> Unit = {},
    navController: NavController,
    userProfileViewModel: UserProfileViewModel = viewModel()
) {

  val userUIState by userProfileViewModel.userState.collectAsState()
  val errorMsg = userUIState.errorMsg
  userProfileViewModel.loadUser(username)

  val context = LocalContext.current
  // Observe and display asynchronous validation errors as Toast messages.
  LaunchedEffect(errorMsg) {
    if (errorMsg != null) {
      Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
      userProfileViewModel.clearErrorMsg()
    }
  }
  val userAge = userProfileViewModel.calculateAge(dateOfBirth = userUIState.userProfile.dateOfBirth)
  val nb_tags = userUIState.userProfile.tags.size
  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Profile") },
            actions = {
              IconButton(
                  onClick = {
                    navController.navigate(
                        NavigationScreens.Settings.route.replace("{username}", username))
                  },
                  modifier = Modifier.testTag(UserProfileScreenTestTags.EDIT_BUTTON)) {
                    Icon(Icons.Default.Settings, contentDescription = "Edit Profile")
                  }
            })
      },
      bottomBar = { NavigationBottomMenu(Tab.Profile, onTabSelected) }) { padding ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .testTag(NavigationTestTags.PROFILE_SCREEN),
            horizontalAlignment = Alignment.CenterHorizontally) {
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceAround) {
                    // Profile picture placeholder
                    Box(
                        modifier = Modifier.size(80.dp).background(Color.Gray, CircleShape),
                        contentAlignment = Alignment.Center) {
                          Text("Img", color = Color.White)
                        }

                    // Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally) {
                          // Name and details
                          Row(
                              verticalAlignment = Alignment.CenterVertically,
                              horizontalArrangement = Arrangement.Center) {
                                Text(
                                    text = userUIState.userProfile.firstName,
                                    fontSize = 20.sp,
                                    color = Color.Blue,
                                    modifier =
                                        Modifier.testTag(UserProfileScreenTestTags.FIRSTNAME))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = userUIState.userProfile.lastName,
                                    fontSize = 20.sp,
                                    color = Color.Blue,
                                    modifier = Modifier.testTag(UserProfileScreenTestTags.LASTNAME))
                              }

                          // Age and country (split for tagging, same line)
                          Row(
                              verticalAlignment = Alignment.CenterVertically,
                              horizontalArrangement = Arrangement.Center) {
                                Text(
                                    text = "Age: $userAge",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.testTag(UserProfileScreenTestTags.AGE))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Country: ${userUIState.userProfile.country}",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.testTag(UserProfileScreenTestTags.COUNTRY))
                              }
                        }
                  }

              Spacer(modifier = Modifier.height(16.dp))
              val nb_chunks = 4
              val row_height = 35
              // Tag chips
              LazyColumn(
                  modifier =
                      Modifier.fillMaxWidth()
                          .height(
                              height =
                                  if (nb_tags <= nb_chunks) row_height.dp
                                  else (2 * row_height).dp)) {
                    val rows: List<List<Tag>> = userUIState.userProfile.tags.chunked(nb_chunks)
                    items(rows.size) { index ->
                      Row(
                          modifier = Modifier.fillMaxWidth(),
                          horizontalArrangement = Arrangement.SpaceEvenly,
                          verticalAlignment = Alignment.CenterVertically) {
                            rows[index].forEach { tag -> TagChip(tag) }
                          }
                      Spacer(modifier = Modifier.height(8.dp))
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
                            .background(Color.LightGray.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center) {
                      val descriptionText =
                          userUIState.userProfile.description.takeUnless { it.isNullOrBlank() }
                              ?: "No description"
                      Text(
                          text = descriptionText,
                          modifier =
                              Modifier.padding(16.dp)
                                  .testTag(UserProfileScreenTestTags.DESCRIPTION))
                      /*
                      if (userUIState.userProfile.description != null &&
                          userUIState.userProfile.description!!.isNotEmpty()) {
                        Text(
                            text = userUIState.userProfile.description.toString(),
                            modifier =
                                Modifier.padding(16.dp)
                                    .testTag(UserProfileScreenTestTags.DESCRIPTION))
                      } else {
                        Text(
                            text = "No description",
                            modifier = Modifier.testTag(UserProfileScreenTestTags.DESCRIPTION))
                      }*/
                    }
              }
            }
      }
}

/**
 * Composable for displaying a tag chip.
 *
 * @param tag The tag containing the text to display.
 */
@Composable
private fun TagChip(tag: Tag) {
  Box(
      modifier =
          Modifier.background(Color(0xFF6650a4), shape = CircleShape).height(30.dp).width(80.dp)) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
              Text(text = tag.name)
            }
      }
}

@Preview
@Composable
fun UserProfileScreenPreview() {
  UserProfileScreen(username = "emma", navController = rememberNavController())
}
