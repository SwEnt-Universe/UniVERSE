package com.android.universe.ui.profile

import com.android.universe.model.Tag
import com.android.universe.model.user.FakeUserRepository
import com.android.universe.model.user.UserProfile
import com.android.universe.model.user.UserRepositoryProvider
import com.google.firebase.auth.FirebaseAuth
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import java.time.LocalDate
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import testMain.MainDispatcherRule

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

  @get:Rule val mainDispatcherRule = MainDispatcherRule()
  private val testDispatcher
    get() = mainDispatcherRule.dispatcher

  private lateinit var fakeRepo: FakeUserRepository
  private lateinit var viewModel: SettingsViewModel

  @Before
  fun setUp() {
    // Mock FirebaseAuth (no real user)
    mockkStatic(FirebaseAuth::class)
    val fakeAuth = mockk<FirebaseAuth>(relaxed = true)
    every { FirebaseAuth.getInstance() } returns fakeAuth
    every { fakeAuth.currentUser } returns null

    fakeRepo = FakeUserRepository()

    // Seed repository inside a blocking coroutine
    runBlocking {
      fakeRepo.addUser(
        UserProfile(
          username = "emma",
          firstName = "Emma",
          lastName = "Stone",
          country = "Switzerland",
          description = "hello",
          dateOfBirth = LocalDate.of(2000, 1, 5),
          tags = emptySet()
        )
      )
      fakeRepo.addUser(
        UserProfile(
          username = "u",
          firstName = "Ulysses",
          lastName = "Grant",
          country = "United States",
          description = "bio",
          dateOfBirth = LocalDate.of(1990, 8, 12),
          tags = emptySet()
        )
      )
    }

    UserRepositoryProvider.repository = fakeRepo
    viewModel = SettingsViewModel(UserRepositoryProvider)
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  @Test
  fun loadUser_populatesUiState_fromRepository() =
    runTest(testDispatcher) {
      viewModel.loadUser("emma")
      advanceUntilIdle()

      val s = viewModel.uiState.value
      assertEquals("Emma", s.firstName)
      assertEquals("Stone", s.lastName)
      assertEquals("Switzerland", s.country)
      assertEquals("hello", s.description)
      assertEquals("5", s.day)
      assertEquals("1", s.month)
      assertEquals("2000", s.year)
    }

  @Test
  fun openModal_prefillsTemp_forTextFields() =
    runTest(testDispatcher) {
      viewModel.loadUser("emma")
      advanceUntilIdle()

      viewModel.openModal("firstName")
      assertEquals("Emma", viewModel.uiState.value.tempValue)

      viewModel.openModal("lastName")
      assertEquals("Stone", viewModel.uiState.value.tempValue)

      viewModel.openModal("description")
      assertEquals("hello", viewModel.uiState.value.tempValue)

      viewModel.openModal("country")
      assertEquals("Switzerland", viewModel.uiState.value.tempValue)
    }

  @Test
  fun openModal_prefillsDateTriplet() =
    runTest(testDispatcher) {
      viewModel.loadUser("emma")
      advanceUntilIdle()

      viewModel.openModal("date")

      val s = viewModel.uiState.value
      assertEquals("5", s.tempDay)
      assertEquals("1", s.tempMonth)
      assertEquals("2000", s.tempYear)
    }

  @Test
  fun saveModal_updatesEmail_whenValid() =
    runTest(testDispatcher) {
      viewModel.loadUser("emma")
      advanceUntilIdle()

      viewModel.openModal("email")
      viewModel.updateTemp("tempValue", "new@example.com")
      viewModel.saveModal("emma")
      advanceUntilIdle()

      assertEquals("new@example.com", viewModel.uiState.value.email)
      assertNull(viewModel.uiState.value.modalError)
    }

  @Test
  fun saveModal_updatesDate_whenValid() =
    runTest(testDispatcher) {
      viewModel.loadUser("emma")
      advanceUntilIdle()

      viewModel.openModal("date")
      viewModel.updateTemp("tempDay", "10")
      viewModel.updateTemp("tempMonth", "12")
      viewModel.updateTemp("tempYear", "1999")
      viewModel.saveModal("emma")
      advanceUntilIdle()

      val s = viewModel.uiState.value
      assertEquals("10", s.day)
      assertEquals("12", s.month)
      assertEquals("1999", s.year)
      assertNull(s.tempDayError)
      assertNull(s.tempMonthError)
      assertNull(s.tempYearError)
    }

  @Test
  fun saveModal_commitsSelectedInterestTags_replacingCategory() =
    runTest(testDispatcher) {
      viewModel.loadUser("emma")
      advanceUntilIdle()

      viewModel.openModal(Tag.Category.INTEREST.fieldName)
      val picks = Tag.getTagsForCategory(Tag.Category.INTEREST).take(2)
      picks.forEach { tag -> viewModel.addTag(tag) }

      viewModel.saveModal("emma")
      advanceUntilIdle()

      val tags = viewModel.uiState.value.selectedTags
      assertEquals(picks.toSet(), tags.toSet())
    }

  @Test
  fun saveProfile_updatesRepository_whenAllValid() =
    runTest(testDispatcher) {
      viewModel.loadUser("emma")
      advanceUntilIdle()

      viewModel.openModal("firstName")
      viewModel.updateTemp("tempValue", "Emilia")
      viewModel.saveModal("emma")
      advanceUntilIdle()

      viewModel.openModal("date")
      viewModel.updateTemp("tempDay", "9")
      viewModel.updateTemp("tempMonth", "7")
      viewModel.updateTemp("tempYear", "1998")
      viewModel.saveModal("emma")
      advanceUntilIdle()

      val updated = fakeRepo.getUser("emma")
      assertEquals("Emilia", updated.firstName)
      assertEquals(LocalDate.of(1998, 7, 9), updated.dateOfBirth)
    }
}