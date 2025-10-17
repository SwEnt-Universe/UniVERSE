package com.android.universe.ui.profile

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.universe.model.Tag
import com.android.universe.model.user.UserProfile
import com.android.universe.model.user.UserRepositoryFirestore
import com.android.universe.utils.FirestoreUserTest
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class UserProfileScreenTest : FirestoreUserTest() {

  @get:Rule val composeTestRule = createComposeRule()

  // ---- helper data
  private val allTags = Tag.Category.entries.flatMap { Tag.getTagsForCategory(it) }

  @Test
  fun profileDisplaysBasicInformationCorrectly() = runTest {
    val repository = UserRepositoryFirestore(emulator.firestore)
    val profile =
        UserProfile(
            uid = "profileDisplaysBasicInformationCorrectly",
            username = "emma",
            firstName = "Emma",
            lastName = "Rossi",
            country = "IT",
            description = "Coffee aficionado.",
            dateOfBirth = LocalDate.of(1993, 6, 18),
            tags = setOf(Tag.METAL))
    repository.addUser(profile)

    composeTestRule.setContent {
      val viewModel = UserProfileViewModel(repository)
      UserProfileScreen(uid = profile.uid, userProfileViewModel = viewModel)
    }

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
    repository.deleteUser(profile.uid)
  }

  @Test
  fun tooManyTagsImpliesScrollable() = runTest {
    val repository = UserRepositoryFirestore(emulator.firestore)
    val manyTags =
        (Tag.getTagsForCategory(Tag.Category.INTEREST) +
                Tag.getTagsForCategory(Tag.Category.CANTON))
            .toSet()

    val profile =
        UserProfile(
            uid = "overflow",
            username = "overflow",
            firstName = "Overflow",
            lastName = "Tester",
            country = "US",
            description = "Testing scrolling behavior.",
            dateOfBirth = LocalDate.of(1995, 1, 1),
            tags = manyTags)

    repository.addUser(profile)

    composeTestRule.setContent {
      val viewModel = UserProfileViewModel(repository)
      UserProfileScreen(uid = profile.uid, userProfileViewModel = viewModel)
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(UserProfileScreenTestTags.TAGLIST).assert(hasScrollAction())

    repository.deleteUser(profile.uid)
  }

  @Test
  fun tagsAreUniqueAndInAllowedList() = runTest {
    val repository = UserRepositoryFirestore(emulator.firestore)
    val testTags = setOf(Tag.ROCK, Tag.POP, Tag.METAL, Tag.JAZZ, Tag.BLUES, Tag.COUNTRY)
    val profile =
        UserProfile(
            uid = "music",
            username = "musiclover",
            firstName = "Alex",
            lastName = "Doe",
            country = "CH",
            description = "I love music!",
            dateOfBirth = LocalDate.of(1990, 1, 1),
            tags = testTags)
    repository.addUser(profile)

    composeTestRule.setContent {
      val viewModel = UserProfileViewModel(repository)
      UserProfileScreen(uid = profile.uid, userProfileViewModel = viewModel)
    }

    composeTestRule.waitForIdle()

    val seenTags = mutableSetOf<String>()
    for (i in 0 until testTags.size) {
      val tagText =
          composeTestRule
              .onNodeWithTag(UserProfileScreenTestTags.getTagTestTag(i))
              .fetchSemanticsNode()
              .config
              .getOrNull(SemanticsProperties.Text)
              ?.firstOrNull()
              ?.text
      assertNotNull("Tag text should not be null", tagText)
      assertTrue(
          "Tag text '$tagText' must be in allowed list",
          allTags.map { tag -> tag.displayName }.contains(tagText))
      assertTrue("Duplicate tag detected: $tagText", seenTags.add(tagText!!))
    }
    repository.deleteUser(profile.uid)
  }

  @Test
  fun descriptionDisplaysPlaceholderWhenNull() = runTest {
    val repository = UserRepositoryFirestore(emulator.firestore)
    val profile =
        UserProfile(
            uid = "tester",
            username = "tester",
            firstName = "testname",
            lastName = "testee",
            country = "FR",
            description = null,
            dateOfBirth = LocalDate.of(2000, 8, 11),
            tags = emptySet())
    repository.addUser(profile)

    composeTestRule.setContent {
      val viewModel = UserProfileViewModel(repository)
      UserProfileScreen(uid = profile.uid, userProfileViewModel = viewModel)
    }

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(UserProfileScreenTestTags.DESCRIPTION)
        .assertIsDisplayed()
        .assertTextEquals("No description")
    repository.deleteUser(profile.username)
  }

  @Test
  fun descriptionDisplaysFullDescriptionWhenNotNull() = runTest {
    val repository = UserRepositoryFirestore(emulator.firestore)
    val profile =
        UserProfile(
            uid = "tester",
            username = "tester",
            firstName = "testname",
            lastName = "testee",
            country = "FR",
            description = "Hello world",
            dateOfBirth = LocalDate.of(2000, 8, 11),
            tags = emptySet())
    repository.addUser(profile)

    composeTestRule.setContent {
      val viewModel = UserProfileViewModel(repository)
      UserProfileScreen(uid = profile.uid, userProfileViewModel = viewModel)
    }

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(UserProfileScreenTestTags.DESCRIPTION)
        .assertIsDisplayed()
        .assertTextEquals("Hello world")
    repository.deleteUser(profile.username)
  }

  @Test
  fun descriptionDisplaysNoDescriptionMessageWhenEmpty() = runTest {
    val repository = UserRepositoryFirestore(emulator.firestore)
    val profile =
        UserProfile(
            uid = "tester",
            username = "tester",
            firstName = "testname",
            lastName = "testee",
            country = "FR",
            description = "",
            dateOfBirth = LocalDate.of(2000, 8, 11),
            tags = emptySet())
    repository.addUser(profile)

    composeTestRule.setContent {
      val viewModel = UserProfileViewModel(repository)
      UserProfileScreen(uid = profile.uid, userProfileViewModel = viewModel)
    }

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(UserProfileScreenTestTags.DESCRIPTION)
        .assertIsDisplayed()
        .assertTextEquals("No description")
    repository.deleteUser(profile.username)
  }
}
