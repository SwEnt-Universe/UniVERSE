package com.android.universe.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen() {
  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Profile") },
            actions = {
              IconButton(onClick = { /* Edit action */}) {
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
              Text("Name Surname", fontSize = 20.sp, color = Color.Blue)
              Text("Age, Country", fontSize = 14.sp, color = Color.Gray)

              Spacer(modifier = Modifier.height(16.dp))

              // Tags row placeholder
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                repeat(3) {
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
                      Text("No description available")
                    }
              }

              Spacer(modifier = Modifier.height(16.dp))

              // Previous events placeholder
              Column(modifier = Modifier.fillMaxWidth()) {
                Text("Previous Events:", fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                repeat(3) {
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
  UserProfileScreen()
}
