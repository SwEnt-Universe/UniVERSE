package com.android.universe.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import com.android.universe.ui.signIn.UniverseIcon
import com.android.universe.ui.theme.CapsuleLarge
import com.android.universe.ui.theme.Dimensions

@Composable
fun LiquidSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search...",
    onClick: (() -> Unit)? = null
) {
  val focusRequester = remember { FocusRequester() }
  var isFocused by remember { mutableStateOf(false) }

  LiquidBox(
      modifier =
          modifier.fillMaxWidth().height(Dimensions.SearchBarHeight).clip(CapsuleLarge).clickable {
            onClick?.invoke()
            focusRequester.requestFocus()
          },
      shape = CapsuleLarge,
      contentAlignment = Alignment.CenterStart) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = Dimensions.PaddingLarge),
            verticalAlignment = Alignment.CenterVertically) {
              Icon(
                  imageVector = Icons.Default.Search,
                  contentDescription = "search_icon",
                  modifier = Modifier.size(Dimensions.SearchBarIconSize),
                  tint = MaterialTheme.colorScheme.onSurface)

              Spacer(modifier = Modifier.width(Dimensions.SearchBarSpacing))

              Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty() && !isFocused) {
                  Text(
                      text = placeholder,
                      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                }

                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    modifier =
                        Modifier.testTag(LiquidSearchBarTestTags.SEARCH_INPUT)
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .onFocusChanged { isFocused = it.isFocused },
                    textStyle =
                        MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary))
              }

              Spacer(modifier = Modifier.width(Dimensions.SearchBarSpacing))

              UniverseIcon()
            }
      }
}

object LiquidSearchBarTestTags {
  const val SEARCH_BAR = "liquid_search_bar"
  const val SEARCH_INPUT = "liquid_search_input"
}
