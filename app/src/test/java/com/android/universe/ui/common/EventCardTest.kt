package com.android.universe.ui.common

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.utils.EventTestData
import com.android.universe.utils.setContentWithStubBackdrop
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventCardTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val event = EventTestData.FullDescriptionEvent

  @Test
  fun eventCard_rendersAllCoreComponents() {
    composeTestRule.setContentWithStubBackdrop {
      EventCard(
          title = event.title,
          description = event.description,
          date = event.date,
          tags = event.tags.toList(),
          participants = event.participants.size,
          eventImage = event.eventPicture,
          isUserParticipant = true,
          onToggleEventParticipation = {},
          onChatClick = {},
          onLocationClick = {})
    }

    composeTestRule.onNodeWithTag(EventCardTestTags.EVENT_CARD).assertExists()
    composeTestRule.onNodeWithTag(EventCardTestTags.EVENT_IMAGE_CONTAINER).assertExists()
    composeTestRule.onNodeWithTag(EventCardTestTags.EVENT_TITLE).assertExists()
    composeTestRule.onNodeWithTag(EventCardTestTags.EVENT_DATE).assertExists()
    composeTestRule.onNodeWithTag(EventCardTestTags.EVENT_TIME).assertExists()
    composeTestRule.onNodeWithTag(EventCardTestTags.EVENT_DESCRIPTION).assertExists()
    composeTestRule.onNodeWithTag(EventCardTestTags.EVENT_PARTICIPANTS).assertExists()

    composeTestRule.onNodeWithTag(EventCardTestTags.CHAT_BUTTON).assertExists()
    composeTestRule.onNodeWithTag(EventCardTestTags.PARTICIPATION_BUTTON).assertExists()
    composeTestRule.onNodeWithTag(EventCardTestTags.EVENT_LOCATION_BUTTON).assertExists()
  }

  @Test
  fun eventCard_showsDefaultImageWhenEventImageIsNull() {
    composeTestRule.setContentWithStubBackdrop {
      EventCard(
          title = event.title,
          description = event.description,
          date = event.date,
          tags = event.tags.toList(),
          participants = event.participants.size,
          eventImage = null,
          isUserParticipant = true,
          onToggleEventParticipation = {},
          onChatClick = {},
          onLocationClick = {})
    }

    composeTestRule.onNodeWithTag(EventCardTestTags.DEFAULT_EVENT_IMAGE).assertExists()
    composeTestRule.onNodeWithTag(EventCardTestTags.EVENT_IMAGE).assertDoesNotExist()
  }

  @Test
  fun eventCard_showsLoadedImageWhenEventImageExists() {
    composeTestRule.setContentWithStubBackdrop {
      EventCard(
          title = event.title,
          description = event.description,
          date = event.date,
          tags = event.tags.toList(),
          participants = event.participants.size,
          eventImage = event.eventPicture,
          isUserParticipant = true,
          onToggleEventParticipation = {},
          onChatClick = {},
          onLocationClick = {})
    }

    composeTestRule.onNodeWithTag(EventCardTestTags.EVENT_IMAGE).assertExists()
    composeTestRule.onNodeWithTag(EventCardTestTags.DEFAULT_EVENT_IMAGE).assertDoesNotExist()
  }

  @Test
  fun eventCard_showsFallbackDescriptionWhenNull() {
    val event = EventTestData.NullDescriptionEvent

    composeTestRule.setContentWithStubBackdrop {
      EventCard(
          title = event.title,
          description = event.description,
          date = event.date,
          tags = event.tags.toList(),
          participants = event.participants.size,
          eventImage = event.eventPicture,
          isUserParticipant = false,
          onToggleEventParticipation = {},
          onChatClick = {},
          onLocationClick = {})
    }

    composeTestRule
        .onNodeWithTag(EventCardTestTags.EVENT_DESCRIPTION)
        .assertExists()
        .assertTextEquals("No description available")
  }

  @Test
  fun eventCard_hidesLocationButtonWhenMapScreenTrue() {
    composeTestRule.setContentWithStubBackdrop {
      EventCard(
          title = event.title,
          description = event.description,
          date = event.date,
          tags = event.tags.toList(),
          participants = event.participants.size,
          eventImage = event.eventPicture,
          isUserParticipant = false,
          onToggleEventParticipation = {},
          onChatClick = {},
          onLocationClick = {},
          isMapScreen = true)
    }

    composeTestRule.onNodeWithTag(EventCardTestTags.EVENT_LOCATION_BUTTON).assertDoesNotExist()
  }

  @Test
  fun eventCard_participationButtonShowsLeaveWhenUserParticipating() {
    composeTestRule.setContentWithStubBackdrop {
      EventCard(
          title = event.title,
          description = event.description,
          date = event.date,
          tags = event.tags.toList(),
          participants = event.participants.size,
          eventImage = event.eventPicture,
          isUserParticipant = true,
          onToggleEventParticipation = {},
          onChatClick = {},
          onLocationClick = {})
    }

    composeTestRule
        .onAllNodesWithTag(EventCardTestTags.PARTICIPATION_BUTTON, useUnmergedTree = true)
        .onFirst()
        .performClick()
  }

  @Test
  fun participationButton_calls_onToggleEventParticipation() {
    var toggleCalled = false

    composeTestRule.setContentWithStubBackdrop {
      EventCard(
          title = event.title,
          description = event.description,
          date = event.date,
          tags = event.tags.toList(),
          participants = event.participants.size,
          eventImage = event.eventPicture,
          isUserParticipant = true,
          onToggleEventParticipation = { toggleCalled = true },
          onChatClick = {},
          onLocationClick = {})
    }

    composeTestRule.onNodeWithTag(EventCardTestTags.PARTICIPATION_BUTTON).performClick()

    assertTrue(toggleCalled)
  }

  @Test
  fun chatButton_calls_onChatClick() {
    var chatCalled = false

    composeTestRule.setContentWithStubBackdrop {
      EventCard(
          title = event.title,
          description = event.description,
          date = event.date,
          tags = event.tags.toList(),
          participants = event.participants.size,
          eventImage = event.eventPicture,
          isUserParticipant = true,
          onToggleEventParticipation = {},
          onChatClick = { chatCalled = true },
          onLocationClick = {})
    }

    composeTestRule.onNodeWithTag(EventCardTestTags.CHAT_BUTTON).performClick()

    assertTrue(chatCalled)
  }

  @Test
  fun locationButton_calls_onLocationClick() {
    var locationCalled = false

    composeTestRule.setContentWithStubBackdrop {
      EventCard(
          title = event.title,
          description = event.description,
          date = event.date,
          tags = event.tags.toList(),
          participants = event.participants.size,
          eventImage = event.eventPicture,
          isUserParticipant = true,
          onToggleEventParticipation = {},
          onChatClick = {},
          onLocationClick = { locationCalled = true })
    }

    composeTestRule.onNodeWithTag(EventCardTestTags.EVENT_LOCATION_BUTTON).performClick()

    assertTrue(locationCalled)
  }
}
