package com.android.universe.ui.searchProfile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.universe.ui.common.ProfileContentLayout
import com.android.universe.ui.components.LiquidBox
import com.android.universe.ui.theme.CardShape
import com.android.universe.ui.theme.Dimensions

object ProfileCardTestTags {
  const val PROFILE_CARD = "profile_card"
}

/**
 * Composable function that displays a user profile card with profile details inside a styled
 * LiquidBox.
 *
 * @param profile The [ProfileUIState] object containing profile details to be displayed.
 * @param viewModel The [SearchProfileViewModel] used to handle user interactions such as following
 *   or unfollowing the user.
 * @param onCardClick Callback function invoked when the card is clicked.
 */
@Composable
fun ProfileCard(
    profile: ProfileUIState,
    viewModel: SearchProfileViewModel,
    onCardClick: () -> Unit
) {
  LiquidBox(
      shape = CardShape,
      modifier =
          Modifier.padding(Dimensions.PaddingMedium)
              .testTag("${ProfileCardTestTags.PROFILE_CARD}_${profile.user.uid}")
              .clickable { onCardClick() }) {
        ProfileContentLayout(
            modifier = Modifier,
            userProfile = profile.user,
            isFollowing = profile.isFollowing,
            onToggleFollowing = { viewModel.followOrUnfollowUser(profile) },
        )
      }
}
