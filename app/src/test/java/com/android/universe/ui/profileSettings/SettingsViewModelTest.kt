package com.android.universe.ui.profileSettings

import android.util.Log
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
import io.mockk.*
import java.time.LocalDate
import junit.framework.TestCase.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

  @get:Rule val mainCoroutineRule = MainCoroutineRule()
  private val testDispatcher
    get() = mainCoroutineRule.dispatcher

  private lateinit var fakeRepo: FakeUserRepository
  private lateinit var mockRepo: UserRepository
  private lateinit var mockFirebaseUser: FirebaseUser
  private lateinit var mockEmailTask: Task<Void>
  private lateinit var mockPasswordTask: Task<Void>

  private lateinit var viewModel: SettingsViewModel

  @Before
  fun setUp() {

    runTest(testDispatcher) {
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
              tags = emptySet(),
              profileImageUri =
                  "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 UWg//9k= "))
      fakeRepo.addUser(
          UserProfile(
              uid = "1",
              username = "u",
              firstName = "Ulysses",
              lastName = "Grant",
              country = "United States",
              description = "bio",
              dateOfBirth = LocalDate.of(1990, 8, 12),
              tags = emptySet(),
              profileImageUri = null))

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
      runTest(testDispatcher) {
        val fakeAuth = mockk<FirebaseAuth>(relaxed = true)
        every { FirebaseAuth.getInstance() } returns fakeAuth
        every { fakeAuth.currentUser } returns mockFirebaseUser

        val viewModel = SettingsViewModel(UserRepositoryProvider)
        assertEquals("old@epfl.ch", viewModel.uiState.value.email)
      }

  @Test
  fun `init does not set email if Firebase user is null`() =
      runTest(testDispatcher) { assertEquals("preview@epfl.ch", viewModel.uiState.value.email) }

  // loadUser Tests
  @Test
  fun `loadUser populates UiState from repository`() =
      runTest(testDispatcher) {
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
      runTest(testDispatcher) {
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
      runTest(testDispatcher) {
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
      runTest(testDispatcher) {
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
      runTest(testDispatcher) {
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
      runTest(testDispatcher) {
        val initialState = viewModel.uiState.value
        viewModel.updateTemp("invalid", "value")
        assertEquals(initialState, viewModel.uiState.value)
      }

  // openModal Tests
  @Test
  fun `openModal prefills temp for text fields`() =
      runTest(testDispatcher) {
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
      runTest(testDispatcher) {
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
      runTest(testDispatcher) {
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
      runTest(testDispatcher) {
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
      runTest(testDispatcher) {
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
      runTest(testDispatcher) {
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
      runTest(testDispatcher) {
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
      runTest(testDispatcher) {
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
      runTest(testDispatcher) {
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
      runTest(testDispatcher) {
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
      runTest(testDispatcher) {
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
      runTest(testDispatcher) {
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
      runTest(testDispatcher) {
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
      runTest(testDispatcher) {
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
      runTest(testDispatcher) {
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
      runTest(testDispatcher) {
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
      runTest(testDispatcher) {
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
      runTest(testDispatcher) {
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
      runTest(testDispatcher) {
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
      runTest(testDispatcher) {
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
      runTest(testDispatcher) {
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
      runTest(testDispatcher) {
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
      runTest(testDispatcher) {
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
    runTest(testDispatcher) {
      viewModel.signOut(clear = suspend { cleared = true }, navigate = { navigated = true })
      delay(1000)
    }
    assertTrue(cleared)
    assertTrue(navigated)
  }

  @Test
  fun savingProfilePictureIfEmptyPicture() = runTest {
    val profilePicture =
        "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 UWg//9k= "

    viewModel.updateProfilePicture(imageId = profilePicture, uid = "1")
    advanceUntilIdle()
    val resultProfilePicture = viewModel.uiState.value.profileImageUri
    assertEquals(profilePicture, resultProfilePicture)
  }

  @Test
  fun savingProfilePictureIfNonEmptyPicture() = runTest {
    val profilePicture =
        "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 L2Y+S/1//9k= "
    viewModel.updateProfilePicture(imageId = profilePicture, uid = "0")
    advanceUntilIdle()
    val resultProfilePicture = viewModel.uiState.value.profileImageUri
    assertEquals(profilePicture, resultProfilePicture)
  }
}
