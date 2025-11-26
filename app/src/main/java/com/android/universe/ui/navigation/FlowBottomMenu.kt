package com.android.universe.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import com.android.universe.R
import com.android.universe.ui.components.LiquidBottomTab
import com.android.universe.ui.components.LiquidBottomTabs
import com.android.universe.ui.theme.Dimensions

/** Constants used for tagging UI elements in [FlowBottomMenu] for testing purposes. */
object FlowBottomMenuTestTags {
  const val MENU = "FlowBottomMenu"
  const val BACK_BUTTON = "BtnBack"
  const val CONFIRM_BUTTON = "BtnConfirm"
  const val GOOGLE_BUTTON = "BtnGoogle"
  const val PASSWORD_BUTTON = "BtnPassword"
}

/**
 * Represents an action tab in the flow bottom bar.
 *
 * @property icon the [ImageVector] to display.
 * @property label the label to display.
 */
sealed class FlowTab(
    val icon: @Composable () -> Unit,
    val label: String,
    val testTag: String = "",
    val onClick: () -> Unit = {}
) {
  class Back(onClick: () -> Unit) :
      FlowTab(
          icon = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier.size(Dimensions.IconSizeLarge))
          },
          label = "Back",
          testTag = FlowBottomMenuTestTags.BACK_BUTTON,
          onClick = onClick)

  class Confirm(onClick: () -> Unit, enabled: Boolean) :
      FlowTab(
          icon = {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Confirm",
                modifier = Modifier.size(Dimensions.IconSizeLarge),
                tint =
                    if (enabled) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f))
          },
          label = "Confirm",
          testTag = FlowBottomMenuTestTags.CONFIRM_BUTTON,
          onClick = onClick)

  class Google(onClick: () -> Unit) :
      FlowTab(
          icon = {
            Image(
                painter = painterResource(id = R.drawable.google_logo),
                contentDescription = "Google",
                modifier = Modifier.size(Dimensions.IconSizeLarge))
          },
          label = "Google",
          testTag = FlowBottomMenuTestTags.GOOGLE_BUTTON,
          onClick = onClick)

  class Password(onClick: () -> Unit) :
      FlowTab(
          icon = {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Password",
                modifier = Modifier.size(Dimensions.IconSizeLarge))
          },
          label = "Password",
          testTag = FlowBottomMenuTestTags.PASSWORD_BUTTON,
          onClick = onClick)
}

@Composable
fun FlowBottomMenu(flowTabs: List<FlowTab>) {
  val selectedTabIndex = remember { mutableIntStateOf(-1) }

  LiquidBottomTabs(
      selectedTabIndex = { selectedTabIndex.intValue },
      onTabSelected = { index ->
        selectedTabIndex.intValue = index
        flowTabs[index].onClick()
      },
      tabsCount = flowTabs.count(),
      modifier = Modifier.testTag(FlowBottomMenuTestTags.MENU)) {
        flowTabs.forEach { tab ->
          LiquidBottomTab(onClick = tab.onClick, modifier = Modifier.testTag(tab.testTag)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                  tab.icon()
                  Text(
                      text = tab.label,
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurface)
                }
          }
        }
      }
}
