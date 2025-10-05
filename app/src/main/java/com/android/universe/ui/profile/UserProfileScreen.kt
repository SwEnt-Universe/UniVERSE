package com.android.universe.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object UserProfileScreenTestTags {
  const val FIRSTNAME = "userProfileFirstName"
  const val LASTNAME = "userProfileLastName"
  const val AGE = "userProfileAge"
  const val COUNTRY = "userProfileCountry"
  const val DESCRIPTION = "userProfileDescription"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    username: String = "",
    userProfileViewModel: UserProfileViewModel = UserProfileViewModel(),
    onEditProfile: () -> Unit = {}
) {

  LaunchedEffect(username) { userProfileViewModel.loadUser(username) }
  val userUIState by userProfileViewModel.userState.collectAsState()
  val userAge = userProfileViewModel.calculateAge(dateOfBirth = userUIState.userProfile.dateOfBirth)
  val previousEventsSize = 3
  val tagsSize = 3
  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Profile") },
            actions = {
              IconButton(onClick = { onEditProfile() }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
              }
            })
      },
      bottomBar = {
        // Placeholder for bottom navigation bar
        BottomAppBar {
          Box(
              modifier = Modifier.fillMaxWidth().height(56.dp),
              contentAlignment = Alignment.Center) {
                Text("Bottom Navigation Placeholder")
              }
        }
      }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
              // Profile picture placeholder
              Box(
                  modifier = Modifier.size(80.dp).background(Color.Gray, CircleShape),
                  contentAlignment = Alignment.Center) {
                    Text("Img", color = Color.White)
                  }

              Spacer(modifier = Modifier.height(8.dp))

              // Name and details
              Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.Center) {
                    Text(
                        text = userUIState.userProfile.firstName,
                        fontSize = 20.sp,
                        color = Color.Blue,
                        modifier = Modifier.testTag(UserProfileScreenTestTags.FIRSTNAME))
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

              Spacer(modifier = Modifier.height(16.dp))

              // Tags row placeholder
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                repeat(tagsSize) {
                  AssistChip(
                      onClick = {},
                      label = { Text("TAG") },
                      modifier = Modifier.padding(horizontal = 4.dp))
                }
              }

              Spacer(modifier = Modifier.height(16.dp))

              // Description box
              Column(modifier = Modifier.fillMaxWidth()) {
                Text("Description:", fontSize = 16.sp)
                Box(
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(100.dp)
                            .background(Color.LightGray.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center) {
                      if (userUIState.userProfile.description != null) {
                        Text(
                            text = userUIState.userProfile.description.toString(),
                            modifier =
                                Modifier.padding(16.dp)
                                    .testTag(UserProfileScreenTestTags.DESCRIPTION))
                      } else {
                        Text(
                            text = "No description",
                            modifier = Modifier.testTag(UserProfileScreenTestTags.DESCRIPTION))
                      }
                    }
              }

              Spacer(modifier = Modifier.height(16.dp))

              // Previous events placeholder
              Column(modifier = Modifier.fillMaxWidth()) {
                Text("Previous Events:", fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                repeat(previousEventsSize) {
                  Box(
                      modifier =
                          Modifier.fillMaxWidth()
                              .height(60.dp)
                              .padding(vertical = 4.dp)
                              .background(Color(0xFFEFEFEF)),
                      contentAlignment = Alignment.CenterStart) {
                        Text("Event Placeholder", modifier = Modifier.padding(16.dp))
                      }
                }
              }
            }
      }
}

@Preview
@Composable
fun UserProfileScreenPreview() {
  UserProfileScreen(username = "emma")
}
