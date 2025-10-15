package com.android.universe.ui.profile

import android.util.Log
import com.android.universe.model.Tag
import com.android.universe.model.user.FakeUserRepository
import com.android.universe.model.user.UserProfile
import com.android.universe.model.user.UserRepository
import com.android.universe.model.user.UserRepositoryProvider
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.*
import java.time.LocalDate
import junit.framework.TestCase.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
  private lateinit var mockRepo: UserRepository
  private lateinit var mockFirebaseUser: FirebaseUser
  private lateinit var mockEmailTask: Task<Void>
  private lateinit var mockPasswordTask: Task<Void>

  private lateinit var viewModel: SettingsViewModel

  @Before
  fun setUp() =
      runTest(testDispatcher) {
        // Mock FirebaseAuth
        mockkStatic(FirebaseAuth::class)
        val fakeAuth = mockk<FirebaseAuth>(relaxed = true)
        every { FirebaseAuth.getInstance() } returns fakeAuth
        every { fakeAuth.currentUser } returns null

        // Mock Log for tag error cases
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0

        // Initialize repositories
        fakeRepo = FakeUserRepository()
        mockRepo = mockk<UserRepository>(relaxed = true)

        // Mock Firebase user and tasks
        mockFirebaseUser = mockk()
        mockEmailTask = mockk(relaxed = true)
        mockPasswordTask = mockk(relaxed = true)
        every { mockFirebaseUser.email } returns "old@example.com"
        every { mockFirebaseUser.updateEmail(any()) } returns mockEmailTask
        every { mockFirebaseUser.updatePassword(any()) } returns mockPasswordTask

        // Seed fake repository
        fakeRepo.addUser(
            UserProfile(
                username = "emma",
                firstName = "Emma",
                lastName = "Stone",
                country = "Switzerland",
                description = "hello",
                dateOfBirth = LocalDate.of(2000, 1, 5),
                tags = emptySet()))
        fakeRepo.addUser(
            UserProfile(
                username = "u",
                firstName = "Ulysses",
                lastName = "Grant",
                country = "United States",
                description = "bio",
                dateOfBirth = LocalDate.of(1990, 8, 12),
                tags = emptySet()))

        // Set up ViewModel
        UserRepositoryProvider.repository = fakeRepo
        viewModel = SettingsViewModel(UserRepositoryProvider)
      }

  @After
  fun tearDown() {
    unmockkAll()
  }

  // Initialization Tests
  @Test
  fun `init sets email from Firebase if available`() =
      runTest(testDispatcher) {
        val fakeAuth = mockk<FirebaseAuth>(relaxed = true)
        every { FirebaseAuth.getInstance() } returns fakeAuth
        every { fakeAuth.currentUser } returns mockFirebaseUser

        val viewModel = SettingsViewModel(UserRepositoryProvider)
        assertEquals("old@example.com", viewModel.uiState.value.email)
      }

  @Test
  fun `init does not set email if Firebase user is null`() =
      runTest(testDispatcher) { assertEquals("preview@example.com", viewModel.uiState.value.email) }

  // loadUser Tests
  @Test
  fun `loadUser populates UiState from repository`() =
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
        assertEquals(emptyList<Tag>(), s.selectedTags)
      }

  @Test
  fun `loadUser sets errorMsg on repository failure`() =
      runTest(testDispatcher) {
        coEvery { mockRepo.getUser("emma") } throws NoSuchElementException("No user found")
        UserRepositoryProvider.repository = mockRepo
        val viewModel = SettingsViewModel(UserRepositoryProvider)

        viewModel.loadUser("emma")
        advanceUntilIdle()

        assertEquals("Failed to load user: No user found", viewModel.uiState.value.errorMsg)
      }

  // clearErrorMsg Tests
  @Test
  fun `clearErrorMsg resets errorMsg`() =
      runTest(testDispatcher) {
        coEvery { mockRepo.getUser("emma") } throws NoSuchElementException("No user found")
        UserRepositoryProvider.repository = mockRepo
        val viewModel = SettingsViewModel(UserRepositoryProvider)

        viewModel.loadUser("emma")
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.errorMsg)

        viewModel.clearErrorMsg()
        assertNull(viewModel.uiState.value.errorMsg)
      }

  // updateTemp Tests
  @Test
  fun `updateTemp updates tempValue and clears modalError`() =
      runTest(testDispatcher) {
        viewModel.openModal("email")
        viewModel.updateTemp("tempValue", "invalid")
        viewModel.saveModal("emma")
        assertNotNull(viewModel.uiState.value.modalError)

        viewModel.updateTemp("tempValue", "test@example.com")
        val s = viewModel.uiState.value
        assertEquals("test@example.com", s.tempValue)
        assertNull(s.modalError)
      }

  @Test
  fun `updateTemp updates tempDay and clears tempDayError`() =
      runTest(testDispatcher) {
        viewModel.openModal("date")
        viewModel.updateTemp("tempDay", "32")
        viewModel.updateTemp("tempMonth", "13")
        viewModel.updateTemp("tempYear", "1800")
        viewModel.saveModal("emma")
        assertNotNull(viewModel.uiState.value.tempDayError)

        viewModel.updateTemp("tempDay", "15")
        val s = viewModel.uiState.value
        assertEquals("15", s.tempDay)
        assertNull(s.tempDayError)
      }

  @Test
  fun `updateTemp ignores invalid key`() =
      runTest(testDispatcher) {
        val initialState = viewModel.uiState.value
        viewModel.updateTemp("invalid", "value")
        assertEquals(initialState, viewModel.uiState.value)
      }

  // openModal Tests
  @Test
  fun `openModal prefills temp for text fields`() =
      runTest(testDispatcher) {
        viewModel.loadUser("emma")
        advanceUntilIdle()

        viewModel.openModal("email")
        assertEquals("preview@example.com", viewModel.uiState.value.tempValue)

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
  fun `openModal sets tempSelectedTags for tag category`() =
      runTest(testDispatcher) {
        val interestTags = Tag.getTagsForCategory(Tag.Category.INTEREST).take(2)
        fakeRepo.updateUser(
            "emma",
            UserProfile(
                username = "emma",
                firstName = "Emma",
                lastName = "Stone",
                country = "Switzerland",
                description = "hello",
                dateOfBirth = LocalDate.of(2000, 1, 5),
                tags = interestTags.toSet()))
        viewModel.loadUser("emma")
        advanceUntilIdle()

        viewModel.openModal(Tag.Category.INTEREST.fieldName)
        assertEquals(interestTags, viewModel.uiState.value.tempSelectedTags)
      }

  // closeModal Tests
  @Test
  fun `closeModal resets modal state`() =
      runTest(testDispatcher) {
        viewModel.openModal("date")
        viewModel.updateTemp("tempDay", "32")
        viewModel.updateTemp("tempMonth", "13")
        viewModel.updateTemp("tempYear", "1800")
        viewModel.saveModal("emma")

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
      runTest(testDispatcher) {
        viewModel.toggleCountryDropdown(true)
        assertTrue(viewModel.uiState.value.showCountryDropdown)
        viewModel.toggleCountryDropdown(false)
        assertFalse(viewModel.uiState.value.showCountryDropdown)
      }

  // addTag and removeTag Tests
  @Test
  fun `addTag adds new tag and logs error for duplicate`() =
      runTest(testDispatcher) {
        viewModel.openModal(Tag.Category.INTEREST.fieldName)
        val tag = Tag.getTagsForCategory(Tag.Category.INTEREST).first()

        viewModel.addTag(tag)
        assertEquals(listOf(tag), viewModel.uiState.value.tempSelectedTags)

        viewModel.addTag(tag)
        verify { Log.e("SettingsViewModel", "Tag '${tag.displayName}' is already selected") }
      }

  @Test
  fun `removeTag removes tag and logs error for non-existent`() =
      runTest(testDispatcher) {
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
      runTest(testDispatcher) {
        viewModel.loadUser("emma")
        advanceUntilIdle()
        viewModel.openModal("email")
        viewModel.updateTemp("tempValue", "new@example.com")
        viewModel.saveModal("emma")
        advanceUntilIdle()

        assertEquals("new@example.com", viewModel.uiState.value.email)
        assertNull(viewModel.uiState.value.modalError)
        assertFalse(viewModel.uiState.value.showModal)
      }

  @Test
  fun `saveModal sets modalError for invalid email`() =
      runTest(testDispatcher) {
        viewModel.loadUser("emma")
        advanceUntilIdle()
        viewModel.openModal("email")
        viewModel.updateTemp("tempValue", "invalid")
        viewModel.saveModal("emma")
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.modalError)
        assertTrue(viewModel.uiState.value.showModal)
      }

  @Test
  fun `saveModal updates password when valid`() =
      runTest(testDispatcher) {
        viewModel.loadUser("emma")
        advanceUntilIdle()
        viewModel.openModal("password")
        viewModel.updateTemp("tempValue", "ValidPass123")
        viewModel.saveModal("emma")
        advanceUntilIdle()

        assertEquals("ValidPass123", viewModel.uiState.value.password)
        assertNull(viewModel.uiState.value.modalError)
        assertFalse(viewModel.uiState.value.showModal)
      }

  @Test
  fun `saveModal sets modalError for invalid password`() =
      runTest(testDispatcher) {
        viewModel.loadUser("emma")
        advanceUntilIdle()
        viewModel.openModal("password")
        viewModel.updateTemp("tempValue", "weak")
        viewModel.saveModal("emma")
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.modalError)
        assertTrue(viewModel.uiState.value.showModal)
      }

  @Test
  fun `saveModal updates firstName when valid`() =
      runTest(testDispatcher) {
        viewModel.loadUser("emma")
        advanceUntilIdle()
        viewModel.openModal("firstName")
        viewModel.updateTemp("tempValue", "Emilia")
        viewModel.saveModal("emma")
        advanceUntilIdle()

        assertEquals("Emilia", viewModel.uiState.value.firstName)
        assertNull(viewModel.uiState.value.modalError)
        assertFalse(viewModel.uiState.value.showModal)
      }

  @Test
  fun `saveModal sets modalError for invalid firstName`() =
      runTest(testDispatcher) {
        viewModel.loadUser("emma")
        advanceUntilIdle()
        viewModel.openModal("firstName")
        viewModel.updateTemp("tempValue", "")
        viewModel.saveModal("emma")
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.modalError)
        assertTrue(viewModel.uiState.value.showModal)
      }

  @Test
  fun `saveModal updates lastName when valid`() =
      runTest(testDispatcher) {
        viewModel.loadUser("emma")
        advanceUntilIdle()
        viewModel.openModal("lastName")
        viewModel.updateTemp("tempValue", "Smith")
        viewModel.saveModal("emma")
        advanceUntilIdle()

        assertEquals("Smith", viewModel.uiState.value.lastName)
        assertNull(viewModel.uiState.value.modalError)
        assertFalse(viewModel.uiState.value.showModal)
      }

  @Test
  fun `saveModal sets modalError for invalid lastName`() =
      runTest(testDispatcher) {
        viewModel.loadUser("emma")
        advanceUntilIdle()
        viewModel.openModal("lastName")
        viewModel.updateTemp("tempValue", "")
        viewModel.saveModal("emma")
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.modalError)
        assertTrue(viewModel.uiState.value.showModal)
      }

  @Test
  fun `saveModal updates description when valid`() =
      runTest(testDispatcher) {
        viewModel.loadUser("emma")
        advanceUntilIdle()
        viewModel.openModal("description")
        viewModel.updateTemp("tempValue", "New bio")
        viewModel.saveModal("emma")
        advanceUntilIdle()

        assertEquals("New bio", viewModel.uiState.value.description)
        assertNull(viewModel.uiState.value.modalError)
        assertFalse(viewModel.uiState.value.showModal)
      }

  @Test
  fun `saveModal sets modalError for invalid description`() =
      runTest(testDispatcher) {
        viewModel.loadUser("emma")
        advanceUntilIdle()
        viewModel.openModal("description")
        viewModel.updateTemp("tempValue", "a".repeat(1000)) // Assuming too long
        viewModel.saveModal("emma")
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.modalError)
        assertTrue(viewModel.uiState.value.showModal)
      }

  @Test
  fun `saveModal updates country when valid`() =
      runTest(testDispatcher) {
        viewModel.loadUser("emma")
        advanceUntilIdle()
        viewModel.openModal("country")
        viewModel.updateTemp("tempValue", "Canada")
        viewModel.saveModal("emma")
        advanceUntilIdle()

        assertEquals("Canada", viewModel.uiState.value.country)
        assertNull(viewModel.uiState.value.modalError)
        assertFalse(viewModel.uiState.value.showModal)
      }

  @Test
  fun `saveModal sets modalError for empty country`() =
      runTest(testDispatcher) {
        viewModel.loadUser("emma")
        advanceUntilIdle()
        viewModel.openModal("country")
        viewModel.updateTemp("tempValue", "")
        viewModel.saveModal("emma")
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.modalError)
        assertTrue(viewModel.uiState.value.showModal)
      }

  @Test
  fun `saveModal updates date when valid`() =
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
        assertFalse(s.showModal)
      }

  @Test
  fun `saveModal sets date errors for invalid date`() =
      runTest(testDispatcher) {
        viewModel.loadUser("emma")
        advanceUntilIdle()
        viewModel.openModal("date")
        viewModel.updateTemp("tempDay", "32")
        viewModel.updateTemp("tempMonth", "13")
        viewModel.updateTemp("tempYear", "1800")
        viewModel.saveModal("emma")
        advanceUntilIdle()

        val s = viewModel.uiState.value
        assertNotNull(s.tempDayError)
        assertNotNull(s.tempMonthError)
        assertNotNull(s.tempYearError)
        assertTrue(s.showModal)
      }

  @Test
  fun `saveModal commits selected interest tags replacing category`() =
      runTest(testDispatcher) {
        viewModel.loadUser("emma")
        advanceUntilIdle()
        viewModel.openModal(Tag.Category.INTEREST.fieldName)
        val picks = Tag.getTagsForCategory(Tag.Category.INTEREST).take(2)
        picks.forEach { viewModel.addTag(it) }
        viewModel.saveModal("emma")
        advanceUntilIdle()

        assertEquals(picks.toSet(), viewModel.uiState.value.selectedTags.toSet())
        assertFalse(viewModel.uiState.value.showModal)
      }

  // saveProfile Tests
  @Test
  fun `saveProfile updates repository when all valid`() =
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
        assertEquals("Switzerland", updated.country)
        assertEquals("hello", updated.description)
      }

  @Test
  fun `saveProfile updates email in Firebase when changed`() =
      runTest(testDispatcher) {
        val fakeAuth = mockk<FirebaseAuth>(relaxed = true)
        every { FirebaseAuth.getInstance() } returns fakeAuth
        every { fakeAuth.currentUser } returns mockFirebaseUser
        every { mockEmailTask.addOnFailureListener(any()) } answers { mockEmailTask }

        viewModel.loadUser("emma")
        advanceUntilIdle()
        viewModel.openModal("email")
        viewModel.updateTemp("tempValue", "new@example.com")
        viewModel.saveModal("emma")
        advanceUntilIdle()

        verify { mockFirebaseUser.updateEmail("new@example.com") }
        assertNull(viewModel.uiState.value.errorMsg)
      }

  @Test
  fun `saveProfile does not update email if unchanged`() =
      runTest(testDispatcher) {
        val fakeAuth = mockk<FirebaseAuth>(relaxed = true)
        every { FirebaseAuth.getInstance() } returns fakeAuth
        every { fakeAuth.currentUser } returns mockFirebaseUser
        every { mockFirebaseUser.email } returns "preview@example.com"

        viewModel.openModal("email")
        viewModel.updateTemp("tempValue", "preview@example.com")
        viewModel.saveModal("emma")

        viewModel.saveProfile("emma")
        advanceUntilIdle()

        verify(exactly = 0) { mockFirebaseUser.updateEmail(any()) }
      }

  @Test
  fun `saveProfile updates password in Firebase when provided`() =
      runTest(testDispatcher) {
        val fakeAuth = mockk<FirebaseAuth>(relaxed = true)
        every { FirebaseAuth.getInstance() } returns fakeAuth
        every { fakeAuth.currentUser } returns mockFirebaseUser
        every { mockPasswordTask.addOnFailureListener(any()) } answers { mockPasswordTask }

        viewModel.loadUser("emma")
        advanceUntilIdle()
        viewModel.openModal("password")
        viewModel.updateTemp("tempValue", "NewPass123")
        viewModel.saveModal("emma")
        advanceUntilIdle()

        verify { mockFirebaseUser.updatePassword("NewPass123") }
        assertNull(viewModel.uiState.value.errorMsg)
      }

  @Test
  fun `saveProfile does not update password if empty`() =
      runTest(testDispatcher) {
        val fakeAuth = mockk<FirebaseAuth>(relaxed = true)
        every { FirebaseAuth.getInstance() } returns fakeAuth
        every { fakeAuth.currentUser } returns mockFirebaseUser

        viewModel.loadUser("emma")
        advanceUntilIdle()
        viewModel.saveProfile("emma")
        advanceUntilIdle()

        verify(exactly = 0) { mockFirebaseUser.updatePassword(any()) }
      }
}
