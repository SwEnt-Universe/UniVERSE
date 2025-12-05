package com.android.universe.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.utils.setContentWithStubBackdrop
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScreenLayoutTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun topBarHeightIsMeasuredAndAppliedToContent() {
    val topBarHeightDp = 60.dp
    var receivedPadding by mutableStateOf(0.dp)

    composeTestRule.setContentWithStubBackdrop {
      MaterialTheme {
        ScreenLayout(
            topBar = { Box(Modifier.testTag("FakeTopBar").height(topBarHeightDp)) },
            bottomBar = {}) { padding ->
              receivedPadding = padding.calculateTopPadding()
            }
      }
    }

    composeTestRule.waitForIdle()

    assert(receivedPadding == topBarHeightDp)
  }

  @Test
  fun bottomBarHeightIsMeasuredAndAppliedToContent() {
    val topBarHeightDp = 60.dp
    var receivedPadding by mutableStateOf(0.dp)

    composeTestRule.setContentWithStubBackdrop {
      MaterialTheme {
        ScreenLayout(
            bottomBar = { Box(Modifier.testTag("FakeBottomBar").height(topBarHeightDp)) }) { padding
              ->
              receivedPadding = padding.calculateBottomPadding()
            }
      }
    }

    composeTestRule.waitForIdle()

    assert(receivedPadding == topBarHeightDp)
  }

  @Test
  fun topAndBottomBarsSetCombinedPaddingCorrectly() {
    val topHeightDp = 50.dp
    val bottomHeightDp = 30.dp

    var receivedTop by mutableStateOf(0.dp)
    var receivedBottom by mutableStateOf(0.dp)

    composeTestRule.setContent {
      MaterialTheme {
        ScreenLayout(
            topBar = { Box(Modifier.height(topHeightDp)) },
            bottomBar = { Box(Modifier.height(bottomHeightDp)) }) { padding ->
              receivedTop = padding.calculateTopPadding()
              receivedBottom = padding.calculateBottomPadding()
            }
      }
    }

    composeTestRule.waitForIdle()

    assert(receivedTop == topHeightDp)
    assert(receivedBottom == bottomHeightDp)
  }

  @Test
  fun worksWithoutBars() {
    var receivedTop by mutableStateOf(10.dp) // will be replaced
    var receivedBottom by mutableStateOf(10.dp)

    composeTestRule.setContent {
      MaterialTheme {
        ScreenLayout(topBar = {}, bottomBar = {}) { padding ->
          receivedTop = padding.calculateTopPadding()
          receivedBottom = padding.calculateBottomPadding()
        }
      }
    }

    composeTestRule.waitForIdle()

    assert(receivedTop == 0.dp)
    assert(receivedBottom == 0.dp)
  }
}
