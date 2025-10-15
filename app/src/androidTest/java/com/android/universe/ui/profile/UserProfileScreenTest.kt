package com.android.universe.ui.profile

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.universe.model.Tag
import com.android.universe.model.user.UserProfile
import com.android.universe.model.user.UserRepositoryProvider
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class UserProfileScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  // ---- helper data
  private val allTags = Tag.Category.entries.flatMap { Tag.getTagsForCategory(it) }

  @Test
  fun profileDisplaysBasicInformationCorrectly() = runTest {
    val profile =
      UserProfile(
        username = "emma",
        firstName = "Emma",
        lastName = "Rossi",
        country = "IT",
        description = "Coffee aficionado.",
        dateOfBirth = LocalDate.of(1993, 6, 18),
        tags = setOf(Tag.METAL))
    UserRepositoryProvider.repository.addUser(profile)

    composeTestRule.setContent { UserProfileScreen(username = profile.username) }

    // Wait for recomposition / viewmodel
    composeTestRule.waitForIdle()

    composeTestRule
      .onNodeWithTag(UserProfileScreenTestTags.FIRSTNAME)
      .assertIsDisplayed()
      .assertTextEquals(profile.firstName)

    composeTestRule
      .onNodeWithTag(UserProfileScreenTestTags.LASTNAME)
      .assertIsDisplayed()
      .assertTextEquals(profile.lastName)

    composeTestRule
      .onNodeWithTag(UserProfileScreenTestTags.COUNTRY)
      .assertIsDisplayed()
      .assertTextContains(profile.country, ignoreCase = true, substring = true)

    composeTestRule
      .onNodeWithTag(UserProfileScreenTestTags.DESCRIPTION)
      .assertIsDisplayed()
      .assertTextContains(profile.description!!)

    composeTestRule
      .onNodeWithTag(UserProfileScreenTestTags.AGE)
      .assertIsDisplayed()
      .assertTextContains("Age:", ignoreCase = true, substring = true)
    UserRepositoryProvider.repository.deleteUser(profile.username)
  }

  @Test
  fun moreThanEightTagsScrollsAndShowsPartialContent() = runTest {
    val manyTags =
      setOf(
        Tag.METAL,
        Tag.TABLE_TENNIS,
        Tag.ARTIFICIAL_INTELLIGENCE,
        Tag.CYCLING,
        Tag.HANDBALL,
        Tag.BASKETBALL,
        Tag.MUSIC,
        Tag.GENEVA,
        Tag.AARGAU,
        Tag.FOOT,
        Tag.VAUD,
        Tag.YOGA)
    val profile =
      UserProfile(
        username = "overflow",
        firstName = "Overflow",
        lastName = "Tester",
        country = "US",
        description = "Testing scrolling behavior.",
        dateOfBirth = LocalDate.of(1995, 1, 1),
        tags = manyTags)
    UserRepositoryProvider.repository.addUser(profile)
    composeTestRule.setContent { UserProfileScreen(username = profile.username) }

    composeTestRule.waitForIdle()

    val visibleTags =
      composeTestRule.onAllNodesWithTag(UserProfileScreenTestTags.TAG).fetchSemanticsNodes()

    assertTrue(
      "Should not display all tags at once when overflow occurs",
      visibleTags.size < manyTags.size)
    UserRepositoryProvider.repository.deleteUser(profile.username)
  }

  @Test
  fun tagsAreUniqueAndInAllowedList() = runTest {
    val testTags = setOf(Tag.ROCK, Tag.POP, Tag.METAL, Tag.JAZZ, Tag.BLUES, Tag.COUNTRY)
    val profile =
      UserProfile(
        username = "musiclover",
        firstName = "Alex",
        lastName = "Doe",
        country = "CH",
        description = "I love music!",
        dateOfBirth = LocalDate.of(1990, 1, 1),
        tags = testTags)
    UserRepositoryProvider.repository.addUser(profile)

    composeTestRule.setContent { UserProfileScreen(username = profile.username) }

    composeTestRule.waitForIdle()

    val seenTags = mutableSetOf<String>()
    composeTestRule
      .onAllNodesWithTag(UserProfileScreenTestTags.TAG)
      .fetchSemanticsNodes()
      .forEach { node ->
        val tagText = node.config.getOrNull(SemanticsProperties.Text)?.firstOrNull()?.text
        assertNotNull("Tag text should not be null", tagText)
        assertTrue(
          "Tag text '$tagText' must be in allowed list",
          allTags.map { tag -> tag.displayName }.contains(tagText))
        assertTrue("Duplicate tag detected: $tagText", seenTags.add(tagText!!))
      }
    UserRepositoryProvider.repository.deleteUser(profile.username)
  }

  @Test
  fun descriptionDisplaysPlaceholderWhenNull() = runTest {
    val profile =
      UserProfile(
        username = "tester",
        firstName = "testname",
        lastName = "testee",
        country = "FR",
        description = null,
        dateOfBirth = LocalDate.of(2000, 8, 11),
        tags = emptySet())
    UserRepositoryProvider.repository.addUser(profile)

    composeTestRule.setContent { UserProfileScreen(username = profile.username) }

    composeTestRule.waitForIdle()

    composeTestRule
      .onNodeWithTag(UserProfileScreenTestTags.DESCRIPTION)
      .assertIsDisplayed()
      .assertTextEquals("No description")
    UserRepositoryProvider.repository.deleteUser(profile.username)
  }

  @Test
  fun descriptionDisplaysFullDescriptionWhenNotNull() = runTest {
    val profile =
      UserProfile(
        username = "tester",
        firstName = "testname",
        lastName = "testee",
        country = "FR",
        description = "Hello world",
        dateOfBirth = LocalDate.of(2000, 8, 11),
        tags = emptySet())
    UserRepositoryProvider.repository.addUser(profile)

    composeTestRule.setContent { UserProfileScreen(username = profile.username) }

    composeTestRule.waitForIdle()

    composeTestRule
      .onNodeWithTag(UserProfileScreenTestTags.DESCRIPTION)
      .assertIsDisplayed()
      .assertTextEquals("Hello world")
    UserRepositoryProvider.repository.deleteUser(profile.username)
  }

  @Test
  fun descriptionDisplaysNoDescriptionMessageWhenEmpty() = runTest {
    val profile =
      UserProfile(
        username = "tester",
        firstName = "testname",
        lastName = "testee",
        country = "FR",
        description = "",
        dateOfBirth = LocalDate.of(2000, 8, 11),
        tags = emptySet())
    UserRepositoryProvider.repository.addUser(profile)

    composeTestRule.setContent { UserProfileScreen(username = profile.username) }

    composeTestRule.waitForIdle()

    composeTestRule
      .onNodeWithTag(UserProfileScreenTestTags.DESCRIPTION)
      .assertIsDisplayed()
      .assertTextEquals("No description")
    UserRepositoryProvider.repository.deleteUser(profile.username)
  }
}