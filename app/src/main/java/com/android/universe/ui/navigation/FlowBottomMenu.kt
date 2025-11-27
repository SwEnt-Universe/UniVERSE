package com.android.universe.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
  const val EMAIL_BUTTON = "BtnEmail"
}

/**
 * Represents the different types of tabs that can be displayed in the [FlowBottomMenu].
 *
 * This sealed class defines the common properties for a tab, such as its icon, label, test tag, and
 * click action. Each specific tab type is implemented as a nested class.
 *
 * @param icon A composable lambda that renders the icon for the tab.
 * @param label The text label displayed below the icon.
 * @param testTag A unique string used to identify the tab in UI tests.
 * @param onClick A lambda function to be executed when the tab is clicked.
 */
sealed class FlowTab(
    val icon: @Composable () -> Unit,
    val label: String,
    val testTag: String = "",
    val onClick: () -> Unit = {}
) {
  /**
   * Represents the "Back" navigation tab. This tab displays a back arrow icon and triggers a
   * provided `onClick` action, typically used for navigating to the previous screen.
   *
   * @param onClick The lambda function to be executed when the back tab is clicked.
   */
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

  /**
   * Represents the "Confirm" action tab. This tab displays a checkmark icon. Its appearance changes
   * based on the `enabled` state. It's typically used to finalize a user action or flow.
   *
   * @param onClick The lambda function to be executed when the confirm tab is clicked.
   * @param enabled A boolean indicating whether the confirm action is enabled. If false, the icon
   *   is dimmed.
   */
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

  /**
   * Represents the "Google" authentication tab. This tab displays the Google logo and is used to
   * initiate a Google sign-in or related flow.
   *
   * @param onClick The lambda function to be executed when the Google tab is clicked.
   */
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

  /**
   * Represents the "Password" authentication tab. This tab displays a lock icon and is used to
   * initiate a password-based authentication flow.
   *
   * @param onClick The lambda function to be executed when the password tab is clicked.
   */
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

class Email(onClick: () -> Unit, enabled: Boolean) :
  FlowTab(
    icon = {
      Icon(
        imageVector = Icons.Filled.MarkEmailUnread,
        contentDescription = "Email",
        modifier = Modifier.size(Dimensions.IconSizeLarge),
        tint =
          if (enabled) MaterialTheme.colorScheme.primary
          else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f))
    },
    label = "Resend Email",
    testTag = FlowBottomMenuTestTags.EMAIL_BUTTON,
    onClick = onClick)

/**
 * A composable that displays a bottom navigation menu with a liquid-style animation. This menu is
 * designed for navigation flows, presenting a list of actions as tabs.
 *
 * It utilizes [LiquidBottomTabs] to create an interactive and visually appealing navigation bar.
 * Each tab is represented by a [FlowTab] object, which defines its icon, label, and click action.
 *
 * @param flowTabs A list of [FlowTab] objects that define the items to be displayed in the bottom
 *   menu. Each tab has an associated icon, label, test tag, and onClick action.
 */
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
