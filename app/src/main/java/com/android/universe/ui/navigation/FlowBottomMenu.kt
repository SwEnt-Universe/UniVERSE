package com.android.universe.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.tooling.preview.Preview
import com.android.universe.ui.components.LiquidBottomTab
import com.android.universe.ui.components.LiquidBottomTabs
import com.android.universe.ui.theme.Dimensions

/** Constants used for tagging UI elements in [FlowBottomMenu] for testing purposes. */
object FlowBottomMenuTestTags {
  const val MENU = "FlowBottomMenu"
  const val BACK_BUTTON = "BtnBack"
  const val CONTINUE_BUTTON = "BtnContinue"
}

/**
 * Represents an action tab in the flow bottom bar.
 *
 * @property icon the [ImageVector] to display.
 * @property label the label to display.
 */
sealed class FlowTab(val icon: ImageVector, val label: String) {
  object Back : FlowTab(Icons.AutoMirrored.Filled.ArrowBack, "Back")

  object Continue : FlowTab(Icons.Filled.Check, "Continue")
}

val flowTabs = listOf(FlowTab.Back, FlowTab.Continue)

/**
 * A bottom bar specifically for linear flows (Sign in, Create Event, Settings).
 *
 * @param onBackClicked Callback for the left 'Back' action.
 * @param onContinueClicked Callback for the right 'Continue' action.
 */
@Composable
fun FlowBottomMenu(
    onBackClicked: () -> Unit,
    onContinueClicked: () -> Unit,
) {
  val selectedTabIndex = remember { mutableIntStateOf(-1) }

  LiquidBottomTabs(
      selectedTabIndex = { selectedTabIndex.intValue },
      onTabSelected = { index ->
        selectedTabIndex.intValue = index
        if (flowTabs[index] is FlowTab.Back) {
          onBackClicked()
        } else {
          onContinueClicked()
        }
      },
      tabsCount = flowTabs.count(),
      modifier = Modifier.testTag(FlowBottomMenuTestTags.MENU)) {
        flowTabs.forEach { tab ->
          LiquidBottomTab(
              onClick = {
                selectedTabIndex.intValue = flowTabs.indexOf(tab)

                if (tab is FlowTab.Back) onBackClicked() else onContinueClicked()
              },
              modifier =
                  Modifier.testTag(
                      if (tab is FlowTab.Back) FlowBottomMenuTestTags.BACK_BUTTON
                      else FlowBottomMenuTestTags.CONTINUE_BUTTON)) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center) {
                      Icon(
                          imageVector = tab.icon,
                          contentDescription = tab.label,
                          modifier = Modifier.size(Dimensions.IconSizeLarge),
                          tint = MaterialTheme.colorScheme.onBackground)
                      Text(
                          text = tab.label,
                          style = MaterialTheme.typography.labelSmall,
                          color = MaterialTheme.colorScheme.onBackground)
                    }
              }
        }
      }
}

@Preview
@Composable
fun FlowBottomMenuPreview() {
  FlowBottomMenu(onBackClicked = {}, onContinueClicked = {})
}
