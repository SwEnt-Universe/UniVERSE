package com.android.universe.ui.profile

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.model.Tag
import com.android.universe.model.user.UserProfile
import com.android.universe.model.user.UserRepositoryFirestore
import com.android.universe.utils.FirestoreUserTest
import com.android.universe.utils.UserTestData
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class UserProfileScreenTest : FirestoreUserTest() {

  @get:Rule
  val composeTestRule = createComposeRule()

  companion object {
      private val allTags = Tag.Category.entries.flatMap { Tag.Companion.getTagsForCategory(it) }
      private val dummyUser = UserTestData.SomeTagsUser
      private val dummyUser2 = UserTestData.ManyTagsUser
      private val dummyUser3 = UserTestData.NullDescription
      private val dummyUser4 = UserTestData.NoTagsUser
      private val dummyUser5 = UserTestData.EmptyDescription

      private const val NO_DESC = "No description"
  }

  @Test
  fun profileDisplaysBasicInformationCorrectly() = runTest {
      val repository = UserRepositoryFirestore(emulator.firestore)
      repository.addUser(dummyUser)

      composeTestRule.setContent {
          val viewModel = UserProfileViewModel(repository)
          UserProfileScreen(uid = dummyUser.uid, userProfileViewModel = viewModel)
      }

      composeTestRule
          .onNodeWithTag(UserProfileScreenTestTags.FIRSTNAME)
          .assertIsDisplayed()
          .assertTextEquals(dummyUser.firstName)

      composeTestRule
          .onNodeWithTag(UserProfileScreenTestTags.LASTNAME)
          .assertIsDisplayed()
          .assertTextEquals(dummyUser.lastName)

      composeTestRule
          .onNodeWithTag(UserProfileScreenTestTags.COUNTRY)
          .assertIsDisplayed()
          .assertTextContains(dummyUser.country, ignoreCase = true, substring = true)

      composeTestRule
          .onNodeWithTag(UserProfileScreenTestTags.DESCRIPTION)
          .assertIsDisplayed()
          .assertTextContains(dummyUser.description!!)

      composeTestRule
          .onNodeWithTag(UserProfileScreenTestTags.AGE)
          .assertIsDisplayed()
          .assertTextContains("Age:", ignoreCase = true, substring = true)
  }

  @Test
  fun tooManyTagsImpliesScrollable() = runTest {
      val repository = UserRepositoryFirestore(emulator.firestore)

      repository.addUser(dummyUser2)

      composeTestRule.setContent {
          val viewModel = UserProfileViewModel(repository)
          UserProfileScreen(uid = dummyUser2.uid, userProfileViewModel = viewModel)
      }

      composeTestRule.onNodeWithTag(UserProfileScreenTestTags.TAGLIST).assert(hasScrollAction())
  }

  @Test
  fun tagsAreUniqueAndInAllowedList() = runTest {
      val repository = UserRepositoryFirestore(emulator.firestore)
      repository.addUser(dummyUser2)

      composeTestRule.setContent {
          val viewModel = UserProfileViewModel(repository)
          UserProfileScreen(uid = dummyUser2.uid, userProfileViewModel = viewModel)
      }

      val seenTags = mutableSetOf<String>()
      for (i in 0 until UserTestData.manyTags.size) {
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
              allTags.map { tag -> tag.displayName }.contains(tagText)
          )
          assertTrue("Duplicate tag detected: $tagText", seenTags.add(tagText!!))
      }
  }

  @Test
  fun descriptionDisplaysPlaceholderWhenNull() = runTest {
      val repository = UserRepositoryFirestore(emulator.firestore)
      repository.addUser(dummyUser3)

      composeTestRule.setContent {
          val viewModel = UserProfileViewModel(repository)
          UserProfileScreen(uid = dummyUser3.uid, userProfileViewModel = viewModel)
      }

      composeTestRule
          .onNodeWithTag(UserProfileScreenTestTags.DESCRIPTION)
          .assertIsDisplayed()
          .assertTextEquals(NO_DESC)
  }

  @Test
  fun descriptionDisplaysFullDescriptionWhenNotNull() = runTest {
      val repository = UserRepositoryFirestore(emulator.firestore)
      val profile = dummyUser4.copy(description = "Hello World")
      repository.addUser(profile)

      composeTestRule.setContent {
          val viewModel = UserProfileViewModel(repository)
          UserProfileScreen(uid = profile.uid, userProfileViewModel = viewModel)
      }

      composeTestRule
          .onNodeWithTag(UserProfileScreenTestTags.DESCRIPTION)
          .assertIsDisplayed()
          .assertTextEquals("Hello World")
  }

  @Test
  fun descriptionDisplaysNoDescriptionMessageWhenEmpty() = runTest {
      val repository = UserRepositoryFirestore(emulator.firestore)
      repository.addUser(dummyUser5)

      composeTestRule.setContent {
          val viewModel = UserProfileViewModel(repository)
          UserProfileScreen(uid = dummyUser5.uid, userProfileViewModel = viewModel)
      }

      composeTestRule
          .onNodeWithTag(UserProfileScreenTestTags.DESCRIPTION)
          .assertIsDisplayed()
          .assertTextEquals(NO_DESC)
  }
}