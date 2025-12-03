package com.android.universe.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.utils.setContentWithStubBackdrop
import java.time.LocalDateTime
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventContentLayoutTest {
  @get:Rule val composeTestRule = createComposeRule()

  private val defaultEventId = "TEST_ID"
  private val defaultTitle = "Test Title"
  private val defaultDescription = "Test Description"
  private val defaultDate = LocalDateTime.of(2025, 1, 1, 15, 30)
  private val defaultParticipants = 20

  private var chatClicked = false
  private var participationClicked = false
  private var locationClicked = false

  private fun setContentForTest(
      eventId: String = defaultEventId,
      title: String = defaultTitle,
      description: String? = defaultDescription,
      isUserParticipant: Boolean = true
  ) {
    composeTestRule.setContentWithStubBackdrop {
      EventContentLayout(
          eventId = eventId,
          title = title,
          description = description,
          date = defaultDate,
          tags = emptyList(),
          participants = defaultParticipants,
          creator = "Test Creator",
          imageContent = { Box(Modifier.testTag("FAKE_IMAGE")) },
          isUserParticipant = isUserParticipant,
          onToggleEventParticipation = { participationClicked = true },
          onChatClick = { chatClicked = true })
    }
  }

  // Reset callback flags before each test
  @Before
  fun setup() {
    chatClicked = false
    participationClicked = false
    locationClicked = false
  }

  @Test
  fun showsBasicContent() {
    setContentForTest()

    composeTestRule
        .onNodeWithTag("${EventContentTestTags.EVENT_TITLE}_$defaultEventId")
        .assertExists()

    composeTestRule
        .onNodeWithTag("${EventContentTestTags.EVENT_DESCRIPTION}_$defaultEventId")
        .assertExists()

    composeTestRule
        .onNodeWithTag("${EventContentTestTags.EVENT_DATE}_$defaultEventId")
        .assertExists()

    composeTestRule
        .onNodeWithTag("${EventContentTestTags.EVENT_CREATOR}_$defaultEventId")
        .assertExists()

    composeTestRule
        .onNodeWithTag("${EventContentTestTags.EVENT_TIME}_$defaultEventId")
        .assertExists()
  }

  @Test
  fun showsImageContainer() {
    setContentForTest()

    composeTestRule
        .onNodeWithTag("${EventContentTestTags.EVENT_IMAGE_CONTAINER}_$defaultEventId")
        .assertExists()

    composeTestRule.onNodeWithTag("FAKE_IMAGE").assertExists()
  }

  @Test
  fun chatButtonNotShown_whenUserIsNotParticipant() {
    setContentForTest(isUserParticipant = false)

    composeTestRule
        .onNodeWithTag("${EventContentTestTags.CHAT_BUTTON}_$defaultEventId")
        .assertDoesNotExist()
  }

  @Test
  fun clickingChatButtonCallsChatCallback() {
    setContentForTest()

    composeTestRule
        .onNodeWithTag("${EventContentTestTags.CHAT_BUTTON}_$defaultEventId")
        .performClick()

    assert(chatClicked)
  }

  @Test
  fun clickingParticipationButtonCallsParticipationCallback() {
    setContentForTest()

    composeTestRule
        .onNodeWithTag("${EventContentTestTags.PARTICIPATION_BUTTON}_$defaultEventId")
        .performClick()

    assert(participationClicked)
  }

  @Test
  fun showsJoinButton_whenUserNotParticipant() {
    setContentForTest(isUserParticipant = false)

    composeTestRule
        .onNodeWithTag("${EventContentTestTags.PARTICIPATION_BUTTON}_$defaultEventId")
        .assert(hasText("Join"))
  }

  @Test
  fun showsLeaveButton_whenUserIsParticipant() {
    setContentForTest(isUserParticipant = true)

    composeTestRule
        .onNodeWithTag("${EventContentTestTags.PARTICIPATION_BUTTON}_$defaultEventId")
        .assert(hasText("Leave"))
  }

  @Test
  fun showsDefaultDescription_whenNullDescriptionProvided() {
    setContentForTest(description = null)

    composeTestRule
        .onNodeWithTag("${EventContentTestTags.EVENT_DESCRIPTION}_$defaultEventId")
        .assert(hasText("No description available"))
  }
}
