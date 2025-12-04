package com.android.universe.ui.profileSettings

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.di.DefaultDP
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
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate

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
      fakeRepo.addUser(user)

      // Set up ViewModel
      viewModel = SettingsViewModel(user.uid, fakeRepo)
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

    val viewModel = SettingsViewModel(user.uid, fakeRepo)
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

  /** Taken directly from add profile */
  @Test
  fun `setProfilePicture with valid uri compresses and updates state`() = runTest {
    // 1. Create a temporary real image file
    // We need a real bitmap so BitmapFactory doesn't return null
    val context = ApplicationProvider.getApplicationContext<Context>()
    val file: File = File(context.cacheDir, "test_image.jpg")
    val outputStream = FileOutputStream(file)

    // Create a 500x500 bitmap (larger than your 256 limit to trigger resizing logic)
    val originalBitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888)
    originalBitmap.eraseColor(android.graphics.Color.RED)

    // Write it to the file
    originalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    outputStream.flush()
    outputStream.close()

    // 2. Create a URI pointing to this file
    val uri = Uri.fromFile(file)

    // 3. Call the function
    viewModel.setProfilePicture(context, uri)
    advanceUntilIdle()

    // 4. Assertions

    val resultBytes = viewModel.uiState.value.profilePicture

    Assert.assertNotNull("Profile picture bytes should not be null", resultBytes)
    Assert.assertTrue("Byte array should contain data", resultBytes!!.isNotEmpty())
  }
}
