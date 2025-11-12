package com.android.universe.ui.chat.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.universe.ui.theme.Dimensions

@Composable
fun MessageItem(
    senderID: String,
    message: String,
    time: String,
    isUserMe: Boolean = false,
    vm: MessageItemViewModel = viewModel()
) {

  val userName by vm.userName.collectAsState()
  val messageCornerRadius: Dp = Dimensions.RoundedCorner * 2
  val arrangement = if (isUserMe) Arrangement.End else Arrangement.Start

  LaunchedEffect(senderID) { vm.loadUserName(senderID) }

  Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = arrangement) {
    Row(modifier = Modifier.fillMaxWidth(0.9f), horizontalArrangement = arrangement) {
      Column(
          modifier =
              Modifier.wrapContentWidth()
                  .background(
                      color =
                          if (isUserMe) MaterialTheme.colorScheme.primary
                          else MaterialTheme.colorScheme.surfaceVariant,
                      shape =
                          RoundedCornerShape(
                              topStart = messageCornerRadius,
                              topEnd = messageCornerRadius,
                              bottomStart = if (isUserMe) messageCornerRadius else 0.dp,
                              bottomEnd = if (isUserMe) 0.dp else messageCornerRadius))
                  .padding(12.dp),
          horizontalAlignment = Alignment.End) {
            Column {

              // Author, hidden if it's the user)

              if (!isUserMe) {
                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary)
              }

              // Message text
              Text(
                  text = message,
                  style = MaterialTheme.typography.bodyMedium,
                  color =
                      if (isUserMe) MaterialTheme.colorScheme.onPrimary
                      else MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.padding(vertical = 4.dp))
            }
            // Time at bottom right

            Text(
                text = time,
                style = MaterialTheme.typography.labelSmall,
                color =
                    if (isUserMe) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    else MaterialTheme.colorScheme.onSurfaceVariant)
          }
    }
  }
}
