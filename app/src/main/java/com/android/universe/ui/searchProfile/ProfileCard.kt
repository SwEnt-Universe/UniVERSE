package com.android.universe.ui.searchProfile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android.universe.R
import com.android.universe.ui.common.ProfileContentLayout
import com.android.universe.ui.components.LiquidBox
import com.android.universe.ui.profile.rememberImageBitmap
import com.android.universe.ui.theme.CardShape
import com.android.universe.ui.theme.Dimensions

object ProfileCardTestTags {
  const val PROFILE_CARD = "profile_card"
}

@Composable
fun ProfileCard(
    profile: ProfileUIState,
    viewModel: SearchProfileViewModel,
    onChatNavigate: () -> Unit,
    onCardClick: () -> Unit
) {
  val imageToDisplay =
      rememberImageBitmap(
          bytes = profile.user.profilePicture, defaultImageId = R.drawable.default_profile_img)
  LiquidBox(
      shape = CardShape,
      modifier =
          Modifier.padding(Dimensions.PaddingMedium)
              .testTag("${ProfileCardTestTags.PROFILE_CARD}_${profile.user.uid}")
              .clickable { onCardClick() }) {
        ProfileContentLayout(
            modifier = Modifier.padding(Dimensions.PaddingLarge),
            userProfile = profile.user,
            userProfileImage = imageToDisplay,
            followers = 0,
            following = 0,
            heightTagList = 260.dp,
            actionRowEnabled = true,
            onChatClick = { onChatNavigate() },
            onAddClick = {})
      }
}
