package com.android.universe.ui.profileSettings

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.di.DefaultDP
import com.android.universe.model.tag.Tag
import com.android.universe.model.user.FakeUserRepository
import com.android.universe.model.user.UserProfile
import com.android.universe.model.user.UserRepository
import com.android.universe.model.user.UserRepositoryProvider
import com.android.universe.utils.MainCoroutineRule
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class SettingsViewModelTest {

  @get:Rule val mainCoroutineRule = MainCoroutineRule()

  private lateinit var fakeRepo: FakeUserRepository
  private lateinit var mockRepo: UserRepository
  private lateinit var mockFirebaseUser: FirebaseUser
  private lateinit var mockEmailTask: Task<Void>
  private lateinit var mockPasswordTask: Task<Void>

  private lateinit var viewModel: SettingsViewModel

  @Before
  fun setUp() {

    runTest {
      // Mock Dispatchers
      mockkObject(DefaultDP)
      every { DefaultDP.io } returns UnconfinedTestDispatcher()
      every { DefaultDP.default } returns UnconfinedTestDispatcher()
      // Mock FirebaseAuth
      mockkStatic(FirebaseAuth::class)
      val fakeAuth = mockk<FirebaseAuth>(relaxed = true)
      every { FirebaseAuth.getInstance() } returns fakeAuth
      every { fakeAuth.currentUser } returns null

      mockkStatic(FirebaseFirestore::class)
      every { FirebaseFirestore.getInstance() } returns mockk(relaxed = true)

      fakeRepo = FakeUserRepository()
      mockkObject(UserRepositoryProvider)
      every { UserRepositoryProvider.repository } returns fakeRepo

      // Mock Log for tag error cases
      mockkStatic(Log::class)
      every { Log.e(any(), any()) } returns 0

      // Initialize repositories
      mockRepo = mockk<UserRepository>(relaxed = true)

      // Mock Firebase user and tasks
      mockFirebaseUser = mockk()
      mockEmailTask = mockk(relaxed = true)
      mockPasswordTask = mockk(relaxed = true)

      every { mockFirebaseUser.email } returns "old@epfl.ch"
      every { mockFirebaseUser.updateEmail(any()) } returns mockEmailTask
      every { mockFirebaseUser.updatePassword(any()) } returns mockPasswordTask

      // Seed fake repository
      fakeRepo.addUser(
          UserProfile(
              uid = "0",
              username = "emma",
              firstName = "Emma",
              lastName = "Stone",
              country = "Switzerland",
              description = "hello",
              dateOfBirth = LocalDate.of(2000, 1, 5),
              tags = emptySet()))
      fakeRepo.addUser(
          UserProfile(
              uid = "1",
              username = "u",
              firstName = "Ulysses",
              lastName = "Grant",
              country = "United States",
              description = "bio",
              dateOfBirth = LocalDate.of(1990, 8, 12),
              tags = emptySet()))

      // Set up ViewModel
      viewModel = SettingsViewModel(UserRepositoryProvider)
    }
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  // Initialization Tests
  @Test
  fun `init sets email from Firebase if available`() =
      runTest  {
        val fakeAuth = mockk<FirebaseAuth>(relaxed = true)
        every { FirebaseAuth.getInstance() } returns fakeAuth
        every { fakeAuth.currentUser } returns mockFirebaseUser

        val viewModel = SettingsViewModel(UserRepositoryProvider)
        assertEquals("old@epfl.ch", viewModel.uiState.value.email)
      }

  @Test
  fun `init does not set email if Firebase user is null`() =
      runTest  { assertEquals("preview@epfl.ch", viewModel.uiState.value.email) }

  // loadUser Tests
  @Test
  fun `loadUser populates UiState from repository`() =
      runTest {
        viewModel.loadUser("0")
        advanceUntilIdle()

        val s = viewModel.uiState.value
        assertEquals("Emma", s.firstName)
        assertEquals("Stone", s.lastName)
        assertEquals("Switzerland", s.country)
        assertEquals("hello", s.description)
        assertEquals("5", s.day)
        assertEquals("1", s.month)
        assertEquals("2000", s.year)
        assertEquals(emptyList<Tag>(), s.selectedTags)
      }

  @Test
  fun `loadUser sets errorMsg on repository failure`() =
      runTest {
        coEvery { mockRepo.getUser("0") } throws NoSuchElementException("No user found")
        every { UserRepositoryProvider.repository } returns mockRepo
        val viewModel = SettingsViewModel(UserRepositoryProvider)

        viewModel.loadUser("0")
        advanceUntilIdle()

        assertEquals("Failed to load user: No user found", viewModel.uiState.value.errorMsg)
      }

  // clearErrorMsg Tests
  @Test
  fun `clearErrorMsg resets errorMsg`() =
      runTest  {
        coEvery { mockRepo.getUser("0") } throws NoSuchElementException("No user found")
        every { UserRepositoryProvider.repository } returns mockRepo
        val viewModel = SettingsViewModel(UserRepositoryProvider)

        viewModel.loadUser("0")
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.errorMsg)

        viewModel.clearErrorMsg()
        assertNull(viewModel.uiState.value.errorMsg)
      }

  // updateTemp Tests
  @Test
  fun `updateTemp updates tempValue and clears modalError`() =
      runTest  {
        viewModel.openModal("email")
        viewModel.updateTemp("tempValue", "invalid")
        viewModel.saveModal("0")
        assertNotNull(viewModel.uiState.value.modalError)

        viewModel.updateTemp("tempValue", "test@epfl.ch")
        val s = viewModel.uiState.value
        assertEquals("test@epfl.ch", s.tempValue)
        assertNull(s.modalError)
      }

  @Test
  fun `updateTemp updates tempDay and clears tempDayError`() =
      runTest  {
        viewModel.openModal("date")
        viewModel.updateTemp("tempDay", "32")
        viewModel.updateTemp("tempMonth", "13")
        viewModel.updateTemp("tempYear", "1800")
        viewModel.saveModal("0")
        assertNotNull(viewModel.uiState.value.tempDayError)

        viewModel.updateTemp("tempDay", "15")
        val s = viewModel.uiState.value
        assertEquals("15", s.tempDay)
        assertNull(s.tempDayError)
      }

  @Test
  fun `updateTemp ignores invalid key`() =
      runTest  {
        val initialState = viewModel.uiState.value
        viewModel.updateTemp("invalid", "value")
        assertEquals(initialState, viewModel.uiState.value)
      }

  // openModal Tests
  @Test
  fun `openModal prefills temp for text fields`() =
      runTest  {
        viewModel.loadUser("0")
        advanceUntilIdle()

        viewModel.openModal("email")
        assertEquals("preview@epfl.ch", viewModel.uiState.value.tempValue)

        viewModel.openModal("password")
        assertEquals("", viewModel.uiState.value.tempValue)

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
  fun `openModal prefills date triplet`() =
      runTest  {
        viewModel.loadUser("0")
        advanceUntilIdle()

        viewModel.openModal("date")
        val s = viewModel.uiState.value
        assertEquals("5", s.tempDay)
        assertEquals("1", s.tempMonth)
        assertEquals("2000", s.tempYear)
      }

  @Test
  fun `openModal sets tempSelectedTags for tag category`() =
      runTest  {
        val interestTags = Tag.getTagsForCategory(Tag.Category.INTEREST).take(2)
        fakeRepo.updateUser(
            "0",
            UserProfile(
                uid = "0",
                username = "emma",
                firstName = "Emma",
                lastName = "Stone",
                country = "Switzerland",
                description = "hello",
                dateOfBirth = LocalDate.of(2000, 1, 5),
                tags = interestTags.toSet()))
        viewModel.loadUser("0")
        advanceUntilIdle()

        viewModel.openModal(Tag.Category.INTEREST.fieldName)
        assertEquals(interestTags, viewModel.uiState.value.tempSelectedTags)
      }

  // closeModal Tests
  @Test
  fun `closeModal resets modal state`() =
      runTest  {
        viewModel.openModal("date")
        viewModel.updateTemp("tempDay", "32")
        viewModel.updateTemp("tempMonth", "13")
        viewModel.updateTemp("tempYear", "1800")
        viewModel.saveModal("0")
        viewModel.closeModal()
        val s = viewModel.uiState.value
        assertFalse(s.showModal)
        assertEquals("", s.currentField)
        assertNull(s.modalError)
        assertNull(s.tempDayError)
        assertNull(s.tempMonthError)
        assertNull(s.tempYearError)
      }

  // toggleCountryDropdown Tests
  @Test
  fun `toggleCountryDropdown updates showCountryDropdown`() =
      runTest  {
        viewModel.toggleCountryDropdown(true)
        assertTrue(viewModel.uiState.value.showCountryDropdown)
        viewModel.toggleCountryDropdown(false)
        assertFalse(viewModel.uiState.value.showCountryDropdown)
      }

  // addTag and removeTag Tests
  @Test
  fun `addTag adds new tag and logs error for duplicate`() =
      runTest  {
        viewModel.openModal(Tag.Category.INTEREST.fieldName)
        val tag = Tag.getTagsForCategory(Tag.Category.INTEREST).first()

        viewModel.addTag(tag)
        assertEquals(listOf(tag), viewModel.uiState.value.tempSelectedTags)

        viewModel.addTag(tag)
        verify { Log.e("SettingsViewModel", "Tag '${tag.displayName}' is already selected") }
      }

  @Test
  fun `removeTag removes tag and logs error for non-existent`() =
      runTest  {
        viewModel.openModal(Tag.Category.INTEREST.fieldName)
        val tag = Tag.getTagsForCategory(Tag.Category.INTEREST).first()
        viewModel.addTag(tag)

        viewModel.removeTag(tag)
        assertEquals(emptyList<Tag>(), viewModel.uiState.value.tempSelectedTags)

        viewModel.removeTag(tag)
        verify { Log.e("SettingsViewModel", "Tag '${tag.displayName}' is not selected") }
      }

  // saveModal Tests
  @Test
  fun `saveModal updates email when valid`() =
      runTest  {
        viewModel.loadUser("0")
        advanceUntilIdle()
        viewModel.openModal("email")
        viewModel.updateTemp("tempValue", "new@epfl.ch")
        viewModel.saveModal("0")
        advanceUntilIdle()

        assertEquals("new@epfl.ch", viewModel.uiState.value.email)
        assertNull(viewModel.uiState.value.modalError)
        assertFalse(viewModel.uiState.value.showModal)
      }

  @Test
  fun `saveModal sets modalError for invalid email`() =
      runTest  {
        viewModel.loadUser("0")
        advanceUntilIdle()
        viewModel.openModal("email")
        viewModel.updateTemp("tempValue", "invalid")
        viewModel.saveModal("0")
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.modalError)
        assertTrue(viewModel.uiState.value.showModal)
      }

  @Test
  fun `saveModal updates password when valid`() =
      runTest  {
        viewModel.loadUser("0")
        advanceUntilIdle()
        viewModel.openModal("password")
        viewModel.updateTemp("tempValue", "ValidPass123")
        viewModel.saveModal("0")
        advanceUntilIdle()

        assertEquals("ValidPass123", viewModel.uiState.value.password)
        assertNull(viewModel.uiState.value.modalError)
        assertFalse(viewModel.uiState.value.showModal)
      }

  @Test
  fun `saveModal sets modalError for invalid password`() =
      runTest  {
        viewModel.loadUser("0")
        advanceUntilIdle()
        viewModel.openModal("password")
        viewModel.updateTemp("tempValue", "weak")
        viewModel.saveModal("0")
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.modalError)
        assertTrue(viewModel.uiState.value.showModal)
      }

  @Test
  fun `saveModal updates firstName when valid`() =
      runTest  {
        viewModel.loadUser("0")
        advanceUntilIdle()
        viewModel.openModal("firstName")
        viewModel.updateTemp("tempValue", "Emilia")
        viewModel.saveModal("0")
        advanceUntilIdle()

        assertEquals("Emilia", viewModel.uiState.value.firstName)
        assertNull(viewModel.uiState.value.modalError)
        assertFalse(viewModel.uiState.value.showModal)
      }

  @Test
  fun `saveModal sets modalError for invalid firstName`() =
      runTest  {
        viewModel.loadUser("0")
        advanceUntilIdle()
        viewModel.openModal("firstName")
        viewModel.updateTemp("tempValue", "")
        viewModel.saveModal("0")
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.modalError)
        assertTrue(viewModel.uiState.value.showModal)
      }

  @Test
  fun `saveModal updates lastName when valid`() =
      runTest  {
        viewModel.loadUser("0")
        advanceUntilIdle()
        viewModel.openModal("lastName")
        viewModel.updateTemp("tempValue", "Smith")
        viewModel.saveModal("0")
        advanceUntilIdle()

        assertEquals("Smith", viewModel.uiState.value.lastName)
        assertNull(viewModel.uiState.value.modalError)
        assertFalse(viewModel.uiState.value.showModal)
      }

  @Test
  fun `saveModal sets modalError for invalid lastName`() =
      runTest  {
        viewModel.loadUser("0")
        advanceUntilIdle()
        viewModel.openModal("lastName")
        viewModel.updateTemp("tempValue", "")
        viewModel.saveModal("0")
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.modalError)
        assertTrue(viewModel.uiState.value.showModal)
      }

  @Test
  fun `saveModal updates description when valid`() =
      runTest  {
        viewModel.loadUser("0")
        advanceUntilIdle()
        viewModel.openModal("description")
        viewModel.updateTemp("tempValue", "New bio")
        viewModel.saveModal("0")
        advanceUntilIdle()

        assertEquals("New bio", viewModel.uiState.value.description)
        assertNull(viewModel.uiState.value.modalError)
        assertFalse(viewModel.uiState.value.showModal)
      }

  @Test
  fun `saveModal sets modalError for invalid description`() =
      runTest  {
        viewModel.loadUser("0")
        advanceUntilIdle()
        viewModel.openModal("description")
        viewModel.updateTemp("tempValue", "a".repeat(1000)) // Assuming too long
        viewModel.saveModal("0")
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.modalError)
        assertTrue(viewModel.uiState.value.showModal)
      }

  @Test
  fun `saveModal updates country when valid`() =
      runTest  {
        viewModel.loadUser("0")
        advanceUntilIdle()
        viewModel.openModal("country")
        viewModel.updateTemp("tempValue", "Canada")
        viewModel.saveModal("0")
        advanceUntilIdle()

        assertEquals("Canada", viewModel.uiState.value.country)
        assertNull(viewModel.uiState.value.modalError)
        assertFalse(viewModel.uiState.value.showModal)
      }

  @Test
  fun `saveModal sets modalError for empty country`() =
      runTest  {
        viewModel.loadUser("0")
        advanceUntilIdle()
        viewModel.openModal("country")
        viewModel.updateTemp("tempValue", "")
        viewModel.saveModal("0")
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.modalError)
        assertTrue(viewModel.uiState.value.showModal)
      }

  @Test
  fun `saveModal updates date when valid`() =
      runTest  {
        viewModel.loadUser("0")
        advanceUntilIdle()
        viewModel.openModal("date")
        viewModel.updateTemp("tempDay", "10")
        viewModel.updateTemp("tempMonth", "12")
        viewModel.updateTemp("tempYear", "1999")
        viewModel.saveModal("0")
        advanceUntilIdle()

        val s = viewModel.uiState.value
        assertEquals("10", s.day)
        assertEquals("12", s.month)
        assertEquals("1999", s.year)
        assertNull(s.tempDayError)
        assertNull(s.tempMonthError)
        assertNull(s.tempYearError)
        assertFalse(s.showModal)
      }

  @Test
  fun `saveModal sets date errors for invalid date`() =
      runTest  {
        viewModel.loadUser("0")
        advanceUntilIdle()
        viewModel.openModal("date")
        viewModel.updateTemp("tempDay", "32")
        viewModel.updateTemp("tempMonth", "13")
        viewModel.updateTemp("tempYear", "1800")
        viewModel.saveModal("0")
        advanceUntilIdle()

        val s = viewModel.uiState.value
        assertNotNull(s.tempDayError)
        assertNotNull(s.tempMonthError)
        assertNotNull(s.tempYearError)
        assertTrue(s.showModal)
      }

  @Test
  fun `saveModal commits selected interest tags replacing category`() =
      runTest  {
        viewModel.loadUser("0")
        advanceUntilIdle()
        viewModel.openModal(Tag.Category.INTEREST.fieldName)
        val picks = Tag.getTagsForCategory(Tag.Category.INTEREST).take(2)
        picks.forEach { viewModel.addTag(it) }
        viewModel.saveModal("0")
        advanceUntilIdle()

        assertEquals(picks.toSet(), viewModel.uiState.value.selectedTags.toSet())
        assertFalse(viewModel.uiState.value.showModal)
      }

  // saveProfile Tests
  @Test
  fun `saveProfile updates repository when all valid`() =
      runTest  {
        viewModel.loadUser("0")
        advanceUntilIdle()
        viewModel.openModal("firstName")
        viewModel.updateTemp("tempValue", "Emilia")
        viewModel.saveModal("0")
        advanceUntilIdle()
        viewModel.openModal("date")
        viewModel.updateTemp("tempDay", "9")
        viewModel.updateTemp("tempMonth", "7")
        viewModel.updateTemp("tempYear", "1998")
        viewModel.saveModal("0")
        advanceUntilIdle()

        val updated = fakeRepo.getUser("0")
        assertEquals("Emilia", updated.firstName)
        assertEquals(LocalDate.of(1998, 7, 9), updated.dateOfBirth)
        assertEquals("CH", updated.country)
        assertEquals("hello", updated.description)
      }

  @Test
  fun `saveProfile updates email in Firebase when changed`() =
      runTest  {
        val fakeAuth = mockk<FirebaseAuth>(relaxed = true)
        every { FirebaseAuth.getInstance() } returns fakeAuth
        every { fakeAuth.currentUser } returns mockFirebaseUser
        every { mockEmailTask.addOnFailureListener(any()) } answers { mockEmailTask }

        viewModel.loadUser("0")
        advanceUntilIdle()
        viewModel.openModal("email")
        viewModel.updateTemp("tempValue", "new@epfl.ch")
        viewModel.saveModal("0")
        advanceUntilIdle()

        verify { mockFirebaseUser.updateEmail("new@epfl.ch") }
        assertNull(viewModel.uiState.value.errorMsg)
      }

  @Test
  fun `saveProfile does not update email if unchanged`() =
      runTest  {
        val fakeAuth = mockk<FirebaseAuth>(relaxed = true)
        every { FirebaseAuth.getInstance() } returns fakeAuth
        every { fakeAuth.currentUser } returns mockFirebaseUser
        every { mockFirebaseUser.email } returns "preview@epfl.ch"

        viewModel.openModal("email")
        viewModel.updateTemp("tempValue", "preview@epfl.ch")
        viewModel.saveModal("0")
        viewModel.saveProfile("0")
        advanceUntilIdle()
        verify(exactly = 0) { mockFirebaseUser.updateEmail(any()) }
      }

  @Test
  fun `saveProfile updates password in Firebase when provided`() =
      runTest  {
        val fakeAuth = mockk<FirebaseAuth>(relaxed = true)
        every { FirebaseAuth.getInstance() } returns fakeAuth
        every { fakeAuth.currentUser } returns mockFirebaseUser
        every { mockPasswordTask.addOnFailureListener(any()) } answers { mockPasswordTask }

        viewModel.loadUser("0")
        advanceUntilIdle()
        viewModel.openModal("password")
        viewModel.updateTemp("tempValue", "NewPass123")
        viewModel.saveModal("0")
        advanceUntilIdle()

        verify { mockFirebaseUser.updatePassword("NewPass123") }
        assertNull(viewModel.uiState.value.errorMsg)
      }

  @Test
  fun `saveProfile does not update password if empty`() =
      runTest  {
        val fakeAuth = mockk<FirebaseAuth>(relaxed = true)
        every { FirebaseAuth.getInstance() } returns fakeAuth
        every { fakeAuth.currentUser } returns mockFirebaseUser

        viewModel.loadUser("0")
        advanceUntilIdle()
        viewModel.saveProfile("0")
        advanceUntilIdle()

        verify(exactly = 0) { mockFirebaseUser.updatePassword(any()) }
      }

  @Test
  fun signOutTest() {
    var cleared = false
    var navigated = false
    runTest  {
      viewModel.signOut(clear = suspend { cleared = true }, navigate = { navigated = true })
      delay(1000)
    }
    assertTrue(cleared)
    assertTrue(navigated)
  }
}
