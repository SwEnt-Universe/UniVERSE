package com.android.universe.ui.profileSettings

import android.net.Uri
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.di.DispatcherProvider
import com.android.universe.model.image.ImageBitmapManager
import com.android.universe.model.user.FakeUserRepository
import com.android.universe.model.user.UserRepository
import com.android.universe.model.user.UserRepositoryProvider
import com.android.universe.ui.common.InputLimits
import com.android.universe.ui.common.ValidationState
import com.android.universe.utils.MainCoroutineRule
import com.android.universe.utils.UserTestData
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
import java.time.LocalDate
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class SettingsViewModelTest {

  companion object {
    var user = UserTestData.Alice
    val unusedUser = UserTestData.Bob
    val baseType = ModalType.USERNAME
    val newName = "newusername"
    val youngDate = LocalDate.of(2025, 5, 5)
  }

  @get:Rule val mainCoroutineRule = MainCoroutineRule()

  private lateinit var fakeRepo: FakeUserRepository
  private lateinit var mockRepo: UserRepository
  private lateinit var mockFirebaseUser: FirebaseUser
  private lateinit var mockEmailTask: Task<Void>
  private lateinit var mockPasswordTask: Task<Void>
  private lateinit var imageManager: ImageBitmapManager

  private lateinit var viewModel: SettingsViewModel

  @Before
  fun setUp() {

    runTest {
      imageManager = mockk()

      val testDispatcher = UnconfinedTestDispatcher()
      val dispatcherProvider =
          object : DispatcherProvider {
            override val main: CoroutineDispatcher = testDispatcher
            override val default: CoroutineDispatcher = testDispatcher
            override val io: CoroutineDispatcher = testDispatcher
            override val unconfined: CoroutineDispatcher = testDispatcher
          }

      mockkStatic(FirebaseAuth::class)
      val fakeAuth = mockk<FirebaseAuth>(relaxed = true)
      every { FirebaseAuth.getInstance() } returns fakeAuth
      every { fakeAuth.currentUser } returns null

      mockkStatic(FirebaseFirestore::class)
      every { FirebaseFirestore.getInstance() } returns mockk(relaxed = true)

      fakeRepo = FakeUserRepository()
      mockkObject(UserRepositoryProvider)
      every { UserRepositoryProvider.repository } returns fakeRepo

      mockkStatic(Log::class)
      every { Log.e(any(), any()) } returns 0

      mockRepo = mockk<UserRepository>(relaxed = true)

      mockFirebaseUser = mockk()
      mockEmailTask = mockk(relaxed = true)
      mockPasswordTask = mockk(relaxed = true)

      every { mockFirebaseUser.email } returns "old@epfl.ch"
      every { mockFirebaseUser.updateEmail(any()) } returns mockEmailTask
      every { mockFirebaseUser.updatePassword(any()) } returns mockPasswordTask
      fakeRepo.addUser(user)

      viewModel =
          SettingsViewModel(
              uid = user.uid,
              userRepository = fakeRepo,
              imageManager = imageManager,
              dispatcherProvider = dispatcherProvider)
    }
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  // Initialization Tests
  @Test
  fun `init sets email from Firebase if available`() = runTest {
    val fakeAuth = mockk<FirebaseAuth>(relaxed = true)
    every { FirebaseAuth.getInstance() } returns fakeAuth
    every { fakeAuth.currentUser } returns mockFirebaseUser

    val testDispatcher = UnconfinedTestDispatcher()
    val dispatcherProvider =
        object : DispatcherProvider {
          override val main: CoroutineDispatcher = testDispatcher
          override val default: CoroutineDispatcher = testDispatcher
          override val io: CoroutineDispatcher = testDispatcher
          override val unconfined: CoroutineDispatcher = testDispatcher
        }

    val viewModel =
        SettingsViewModel(
            uid = user.uid,
            userRepository = fakeRepo,
            imageManager = imageManager,
            dispatcherProvider = dispatcherProvider)
    assertEquals("old@epfl.ch", viewModel.uiState.value.email)
  }

  @Test
  fun `init does not set email if Firebase user is null`() = runTest {
    assertEquals("preview@epfl.ch", viewModel.uiState.value.email)
  }

  @Test
  fun userLoaded() {
    assertEquals(viewModel.uiState.value.username, user.username)
    assertEquals(viewModel.uiState.value.firstName, user.firstName)
  }

  @Test
  fun setModalTypeChangesFields() {
    for (type in ModalType.entries) {
      viewModel.setModalType(type)
      assertNotNull(viewModel.uiState.value.modalText)
      assertTrue(viewModel.uiState.value.showModal)
      assertEquals(type, viewModel.uiState.value.modalType)
      viewModel.stopModal()
    }
  }

  @Test
  fun saveProfileUpdatesUser() {
    viewModel.setModalType(baseType)
    viewModel.setModalText(newName)
    viewModel.saveTempModal()
    viewModel.saveProfile(user.uid)

    runTest { assertEquals(newName, fakeRepo.getUser(user.uid).username) }
  }

  @Test
  fun malformedUserLoadErrorTest() {
    runTest { viewModel.loadUser(unusedUser.uid) }
    assertNotNull(viewModel.uiState.value.errorMsg)
    viewModel.clearErrorMsg()
    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun setterHelperWorks() {
    viewModel.setModalType(baseType)
    viewModel.setterHelper(InputLimits.USERNAME, newName) { ValidationState.Valid }
    assertEquals(newName, viewModel.uiState.value.modalText)
    assertEquals(ValidationState.Valid, viewModel.uiState.value.modalValState)
  }

  @Test
  fun setDateWorks() {
    viewModel.setDate(null)
    assertEquals(user.dateOfBirth, viewModel.uiState.value.date)
    assertEquals(
        viewModel.formatter.format(user.dateOfBirth), viewModel.uiState.value.formattedDate)
    viewModel.setDate(unusedUser.dateOfBirth)
    assertEquals(unusedUser.dateOfBirth, viewModel.uiState.value.date)
    viewModel.setDate(youngDate)
    assertEquals(youngDate, viewModel.uiState.value.date)
    assertTrue(viewModel.uiState.value.dateValidation is ValidationState.Invalid)
  }

  @Test
  fun signOutTest() {
    var cleared = false
    var navigated = false
    runTest {
      viewModel.signOut(clear = suspend { cleared = true }, navigate = { navigated = true })
      delay(1000)
    }
    assertTrue(cleared)
    assertTrue(navigated)
  }

  @Test
  fun `setProfilePicture with valid uri compresses and updates state`() = runTest {
    val mockUri = mockk<Uri>()
    val expectedBytes = byteArrayOf(10, 20, 30) // Dummy data

    coEvery { imageManager.resizeAndCompressImage(mockUri) } returns expectedBytes

    viewModel.setProfilePicture(mockUri)

    val resultBytes = viewModel.uiState.value.profilePicture
    Assert.assertNotNull("Profile picture bytes should not be null", resultBytes)
    Assert.assertTrue(
        "Byte array should match expected", resultBytes!!.contentEquals(expectedBytes))
  }

  @Test
  fun `setProfilePicture handles null uri`() = runTest {
    viewModel.setProfilePicture(null)
    assertNull(viewModel.uiState.value.profilePicture)
  }

  @Test
  fun `deleteImage clears profile picture`() = runTest {
    val mockUri = mockk<Uri>()
    coEvery { imageManager.resizeAndCompressImage(mockUri) } returns byteArrayOf(1, 2, 3)
    viewModel.setProfilePicture(mockUri)
    assertNotNull(viewModel.uiState.value.profilePicture)

    viewModel.deleteImage()

    assertNull(viewModel.uiState.value.profilePicture)
  }
}
