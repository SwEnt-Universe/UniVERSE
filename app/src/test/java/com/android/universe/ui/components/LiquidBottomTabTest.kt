package com.android.universe.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LiquidBottomTabTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun liquidBottomTab_onClick_isCalled() {
        var clicked = false
        composeTestRule.setContent {
            Row {
                LiquidBottomTab(
                    onClick = { clicked = true }
                ) {
                    Text("Home")
                }
            }
        }

        composeTestRule.onNodeWithText("Home").performClick()
        composeTestRule.runOnIdle {
            assert(clicked)
        }
    }

    @Test
    fun liquidBottomTab_consumes_localScale() {
        val testScale = 0.5f

        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalLiquidBottomTabScale provides { testScale }
            ) {
                Row {
                    LiquidBottomTab(onClick = {}) {
                        Text("Settings")
                    }
                }
            }
        }

        composeTestRule.onNodeWithText("Settings").assertExists()
    }
}