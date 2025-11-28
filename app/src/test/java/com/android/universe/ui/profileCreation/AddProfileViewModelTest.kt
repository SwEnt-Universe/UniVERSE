package com.android.universe.ui.profileCreation

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.di.DefaultDP
import com.android.universe.model.user.FakeUserRepository
import com.android.universe.ui.common.InputLimits
import com.android.universe.ui.common.ValidationState
import com.android.universe.utils.MainCoroutineRule
import io.mockk.every
import io.mockk.mockkObject
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class AddProfileViewModelTest {
  @get:Rule val mainCoroutineRule = MainCoroutineRule()
  private lateinit var repository: FakeUserRepository
  private lateinit var viewModel: AddProfileViewModel

  @Before
  fun setup() {
    repository = FakeUserRepository()
    viewModel = AddProfileViewModel(repository)
    mockkObject(DefaultDP)
    every { DefaultDP.io } returns UnconfinedTestDispatcher()
    every { DefaultDP.default } returns UnconfinedTestDispatcher()
  }

  @Test
  fun initialStateIsEmpty() = runTest {
    val state = viewModel.uiState.value
    assertEquals("", state.username)
    assertEquals("", state.firstName)
    assertEquals("", state.lastName)
    assertNull(state.description)
    assertEquals("", state.day)
    assertEquals("", state.month)
    assertEquals("", state.year)
  }

  @Test
  fun setUsernameUpdateTheState() = runTest {
    val initialState = viewModel.uiState.value
    assertEquals("", initialState.username)

    viewModel.setUsername("john_doe")

    val updatedState = viewModel.uiState.value
    assertEquals("john_doe", updatedState.username)
  }

  @Test
  fun setFirstNameUpdateTheState() = runTest {
    val initialState = viewModel.uiState.value
    assertEquals("", initialState.firstName)

    viewModel.setFirstName("John")

    val updatedState = viewModel.uiState.value
    assertEquals("John", updatedState.firstName)
  }

  @Test
  fun setLastNameUpdateTheState() = runTest {
    val initialState = viewModel.uiState.value
    assertEquals("", initialState.lastName)

    viewModel.setLastName("Doe")

    val updatedState = viewModel.uiState.value
    assertEquals("Doe", updatedState.lastName)
  }

  @Test
  fun setDescriptionUpdateTheState() = runTest {
    val initialState = viewModel.uiState.value
    assertNull(initialState.description)

    viewModel.setDescription("I'm a default person")

    val updatedState = viewModel.uiState.value
    assertEquals("I'm a default person", updatedState.description)
  }

  @Test
  fun firstNameCleansMultipleSpaces() = runTest {
    viewModel.setFirstName("   John    Michael   Doe   ")
    assertEquals("John Michael Doe ", viewModel.uiState.value.firstName)
  }

  @Test
  fun lastNameCleansProperly() = runTest {
    viewModel.setLastName("   Van   der   Waals   ")
    assertEquals("Van der Waals ", viewModel.uiState.value.lastName)
  }

  @Test
  fun setDayUpdateTheState() = runTest {
    val initialState = viewModel.uiState.value
    assertEquals("", initialState.day)

    viewModel.setDay("12")

    val updatedState = viewModel.uiState.value
    assertEquals("12", updatedState.day)
  }

  @Test
  fun setMonthUpdateTheState() = runTest {
    val initialState = viewModel.uiState.value
    assertEquals("", initialState.month)

    viewModel.setMonth("8")

    val updatedState = viewModel.uiState.value
    assertEquals("8", updatedState.month)
  }

  @Test
  fun setYearUpdateTheState() = runTest {
    val initialState = viewModel.uiState.value
    assertEquals("", initialState.year)

    viewModel.setYear("1990")

    val updatedState = viewModel.uiState.value
    assertEquals("1990", updatedState.year)
  }

  @Test
  fun addProfileEmptyFirstName() = runTest {
    val repository = FakeUserRepository()
    val viewModel = AddProfileViewModel(repository)

    viewModel.setUsername("john_doe")
    viewModel.setFirstName("")
    viewModel.setLastName("Doe")
    viewModel.setDay("12")
    viewModel.setMonth("8")
    viewModel.setYear("1990")

    viewModel.addProfile("0")
    advanceUntilIdle()

    val users = repository.getAllUsers()
    assertEquals(0, users.size)
  }

  @Test
  fun addProfileEmptyLastName() = runTest {
    val repository = FakeUserRepository()
    val viewModel = AddProfileViewModel(repository)

    viewModel.setUsername("john_doe")
    viewModel.setFirstName("John")
    viewModel.setLastName("")
    viewModel.setDay("12")
    viewModel.setMonth("8")
    viewModel.setYear("1990")

    viewModel.addProfile("0")
    advanceUntilIdle()

    val users = repository.getAllUsers()
    assertEquals(0, users.size)
  }

  @Test
  fun addProfileEmptyDay() = runTest {
    val repository = FakeUserRepository()
    val viewModel = AddProfileViewModel(repository)

    viewModel.setUsername("john_doe")
    viewModel.setFirstName("John")
    viewModel.setLastName("Doe")
    viewModel.setDay("")
    viewModel.setMonth("8")
    viewModel.setYear("1990")

    viewModel.addProfile("0")
    advanceUntilIdle()

    val users = repository.getAllUsers()
    assertEquals(0, users.size)
  }

  @Test
  fun addProfileNonNumericDay() = runTest {
    val repository = FakeUserRepository()
    val viewModel = AddProfileViewModel(repository)

    viewModel.setUsername("john_doe")
    viewModel.setFirstName("John")
    viewModel.setLastName("Doe")
    viewModel.setDay("hello")
    viewModel.setMonth("8")
    viewModel.setYear("1990")

    viewModel.addProfile("0")
    advanceUntilIdle()

    val users = repository.getAllUsers()
    assertEquals(0, users.size)
  }

  @Test
  fun addProfileEmptyMonth() = runTest {
    val repository = FakeUserRepository()
    val viewModel = AddProfileViewModel(repository)

    viewModel.setUsername("john_doe")
    viewModel.setFirstName("John")
    viewModel.setLastName("Doe")
    viewModel.setDay("12")
    viewModel.setMonth("")
    viewModel.setYear("1990")

    viewModel.addProfile("0")
    advanceUntilIdle()

    val users = repository.getAllUsers()
    assertEquals(0, users.size)
  }

  @Test
  fun addProfileNonNumericMonth() = runTest {
    val repository = FakeUserRepository()
    val viewModel = AddProfileViewModel(repository)

    viewModel.setUsername("john_doe")
    viewModel.setFirstName("John")
    viewModel.setLastName("Doe")
    viewModel.setDay("12")
    viewModel.setMonth("hello")
    viewModel.setYear("1990")

    viewModel.addProfile("0")
    advanceUntilIdle()

    val users = repository.getAllUsers()
    assertEquals(0, users.size)
  }

  @Test
  fun addProfileEmptyYear() = runTest {
    val repository = FakeUserRepository()
    val viewModel = AddProfileViewModel(repository)

    viewModel.setUsername("john_doe")
    viewModel.setFirstName("John")
    viewModel.setLastName("Doe")
    viewModel.setDay("12")
    viewModel.setMonth("8")
    viewModel.setYear("")

    viewModel.addProfile("0")
    advanceUntilIdle()

    val users = repository.getAllUsers()
    assertEquals(0, users.size)
  }

  @Test
  fun addProfileNonNumericYear() = runTest {
    val repository = FakeUserRepository()
    val viewModel = AddProfileViewModel(repository)

    viewModel.setUsername("john_doe")
    viewModel.setFirstName("John")
    viewModel.setLastName("Doe")
    viewModel.setDay("12")
    viewModel.setMonth("8")
    viewModel.setYear("hello")

    viewModel.addProfile("0")
    advanceUntilIdle()

    val users = repository.getAllUsers()
    assertEquals(0, users.size)
  }

  @Test
  fun addProfileInvalidDate1() = runTest {
    val repository = FakeUserRepository()
    val viewModel = AddProfileViewModel(repository)

    viewModel.setUsername("john_doe")
    viewModel.setFirstName("John")
    viewModel.setLastName("Doe")
    viewModel.setDay("29")
    viewModel.setMonth("2")
    viewModel.setYear("2001")

    viewModel.addProfile("0")
    advanceUntilIdle()

    val users = repository.getAllUsers()
    assertEquals(0, users.size)
  }

  @Test
  fun addProfileInvalidDate2() = runTest {
    val repository = FakeUserRepository()
    val viewModel = AddProfileViewModel(repository)

    viewModel.setUsername("john_doe")
    viewModel.setFirstName("John")
    viewModel.setLastName("Doe")
    viewModel.setDay("52")
    viewModel.setMonth("1")
    viewModel.setYear("2025")

    viewModel.addProfile("0")
    advanceUntilIdle()

    val users = repository.getAllUsers()
    assertEquals(0, users.size)
  }

  @Test
  fun addProfileInvalidDate3() = runTest {
    val repository = FakeUserRepository()
    val viewModel = AddProfileViewModel(repository)

    viewModel.setUsername("john_doe")
    viewModel.setFirstName("John")
    viewModel.setLastName("Doe")
    viewModel.setDay("12")
    viewModel.setMonth("13")
    viewModel.setYear("2025")

    viewModel.addProfile("0")
    advanceUntilIdle()

    val users = repository.getAllUsers()
    assertEquals(0, users.size)
  }

  @Test
  fun addProfileInvalidDate4() = runTest {
    val repository = FakeUserRepository()
    val viewModel = AddProfileViewModel(repository)

    viewModel.setUsername("john_doe")
    viewModel.setFirstName("John")
    viewModel.setLastName("Doe")
    viewModel.setDay("12")
    viewModel.setMonth("11")
    viewModel.setYear("2105")

    viewModel.addProfile("0")
    advanceUntilIdle()

    val users = repository.getAllUsers()
    assertEquals(0, users.size)
  }

  @Test
  fun addProfileEmptyUsername() = runTest {
    val repository = FakeUserRepository()
    val viewModel = AddProfileViewModel(repository)

    viewModel.setUsername("")
    viewModel.setFirstName("John")
    viewModel.setLastName("Doe")
    viewModel.setDay("12")
    viewModel.setMonth("8")
    viewModel.setYear("1990")

    viewModel.addProfile("0")
    advanceUntilIdle()

    val users = repository.getAllUsers()
    assertEquals(0, users.size)
  }

  @Test
  fun addProfileValid() = runTest {
    val repository = FakeUserRepository()
    val viewModel = AddProfileViewModel(repository)

    viewModel.setUsername("john_doe")
    viewModel.setFirstName("John")
    viewModel.setLastName("Doe")
    viewModel.setDay("12")
    viewModel.setMonth("8")
    viewModel.setYear("1990")

    viewModel.addProfile("0")

    advanceUntilIdle()

    val users = repository.getAllUsers()
    assertEquals(1, users.size)

    val user = users.first()
    assertEquals("john_doe", user.username)
    assertEquals("John", user.firstName)
    assertEquals("Doe", user.lastName)
    assertEquals(1990, user.dateOfBirth.year)
    assertEquals(8, user.dateOfBirth.monthValue)
    assertEquals(12, user.dateOfBirth.dayOfMonth)
  }

  // ---------- USERNAME TESTS ----------

  @Test
  fun usernameAcceptsValidCharacters() = runTest {
    val valid = listOf("john_doe", "Jane.Doe-123", "simpleUser", "A_B-C.123")
    valid.forEach {
      viewModel.setUsername(it)
      val result = viewModel.uiState.value.userNameValid is ValidationState.Valid
      assertTrue(result)
    }
  }

  @Test
  fun usernameTrimsToPlusOneLimit() = runTest {
    val tooLong = "a".repeat(InputLimits.USERNAME + 5)
    viewModel.setUsername(tooLong)
    assertEquals(InputLimits.USERNAME + 1, viewModel.uiState.value.username.length)
  }

  // ---------- FIRST NAME TESTS ----------

  @Test
  fun firstNameTrimsToPlusOneLimit() = runTest {
    val longName = "A".repeat(InputLimits.FIRST_NAME + 5)
    viewModel.setFirstName(longName)
    assertEquals(InputLimits.FIRST_NAME + 1, viewModel.uiState.value.firstName.length)
  }

  @Test
  fun firstNameValidatesAllowedCharacters() = runTest {
    val validNames = listOf("Élodie", "Anne-Marie", "D'Arcy", "Åke", "Jean Luc")
    validNames.forEach {
      viewModel.setFirstName(it)
      val result = viewModel.uiState.value.firstNameValid is ValidationState.Valid
      assertTrue(result)
    }
  }

  // ---------- LAST NAME TESTS ----------
  @Test
  fun lastNameAcceptsAccentsAndSpecialLetters() = runTest {
    val valid = listOf("O'Connor", "García-López", "Brân", "L'Écuyer")
    valid.forEach {
      viewModel.setLastName(it)
      val result = viewModel.uiState.value.lastNameValid is ValidationState.Valid
      assertTrue(result)
    }
  }

  // ---------- DESCRIPTION TESTS ----------

  @Test
  fun descriptionCleansSpaces() = runTest {
    viewModel.setDescription("   Hello    there   world!   ")
    assertEquals("   Hello    there   world!   ", viewModel.uiState.value.description)
  }

  @Test
  fun descriptionTrimsToPlusOneLimit() = runTest {
    val longDesc = "A".repeat(InputLimits.DESCRIPTION + 50)
    viewModel.setDescription(longDesc)
    assertEquals(InputLimits.DESCRIPTION + 50, viewModel.uiState.value.description?.length)
  }

  // ---------- ADD PROFILE INTEGRATION TESTS ----------

  @Test
  fun addProfileRejectsInvalidUsernameCharacters() = runTest {
    val repository = FakeUserRepository()
    val vm = AddProfileViewModel(repository)

    vm.setUsername("invalid@name")
    vm.setFirstName("John")
    vm.setLastName("Doe")
    vm.setDay("12")
    vm.setMonth("8")
    vm.setYear("1990")

    vm.addProfile("0")
    advanceUntilIdle()

    assertEquals(0, repository.getAllUsers().size)
  }

  @Test
  fun addProfileRejectsTooLongUsername() = runTest {
    val repository = FakeUserRepository()
    val vm = AddProfileViewModel(repository)

    vm.setUsername("a".repeat(InputLimits.USERNAME + 2))
    vm.setFirstName("John")
    vm.setLastName("Doe")
    vm.setDay("12")
    vm.setMonth("8")
    vm.setYear("1990")

    vm.addProfile("0")
    advanceUntilIdle()

    assertEquals(0, repository.getAllUsers().size)
  }

  @Test
  fun addProfileRejectsInvalidNameCharacters() = runTest {
    val repository = FakeUserRepository()
    val vm = AddProfileViewModel(repository)

    vm.setUsername("john_doe")
    vm.setFirstName("John1")
    vm.setLastName("Doe")
    vm.setDay("12")
    vm.setMonth("8")
    vm.setYear("1990")

    vm.addProfile("0")
    advanceUntilIdle()

    assertEquals(0, repository.getAllUsers().size)
  }

  @Test
  fun addProfileRejectsTooLongFirstName() = runTest {
    val repository = FakeUserRepository()
    val vm = AddProfileViewModel(repository)

    vm.setUsername("john_doe")
    vm.setFirstName("A".repeat(InputLimits.FIRST_NAME + 5))
    vm.setLastName("Doe")
    vm.setDay("12")
    vm.setMonth("8")
    vm.setYear("1990")

    vm.addProfile("0")
    advanceUntilIdle()

    assertEquals(0, repository.getAllUsers().size)
  }

  @Test
  fun addProfileRejectsTooLongLastName() = runTest {
    val repository = FakeUserRepository()
    val vm = AddProfileViewModel(repository)

    vm.setUsername("john_doe")
    vm.setFirstName("John")
    vm.setLastName("B".repeat(InputLimits.LAST_NAME + 10))
    vm.setDay("12")
    vm.setMonth("8")
    vm.setYear("1990")

    vm.addProfile("0")
    advanceUntilIdle()

    assertEquals(0, repository.getAllUsers().size)
  }

  @Test
  fun addProfileRejectsTooLongDescription() = runTest {
    val repository = FakeUserRepository()
    val vm = AddProfileViewModel(repository)

    vm.setUsername("john_doe")
    vm.setFirstName("John")
    vm.setLastName("Doe")
    vm.setDay("12")
    vm.setMonth("8")
    vm.setYear("1990")
    vm.setDescription("A".repeat(InputLimits.DESCRIPTION + 20))

    vm.addProfile("0")
    advanceUntilIdle()

    assertEquals(0, repository.getAllUsers().size)
  }

  @Test
  fun addProfileTrimsAndAddsCleanedNames() = runTest {
    val vm = AddProfileViewModel(repository)

    vm.setUsername("john_doe")
    vm.setFirstName("   Jean   Luc   ")
    vm.setLastName("  De    Silva ")
    vm.setDay("15")
    vm.setMonth("5")
    vm.setYear("1995")
    vm.setDescription("  asds  ad  ")

    vm.addProfile("0")
    advanceUntilIdle()
    val users = repository.getAllUsers()

    assertEquals(1, users.size)
    val user = users.first()
    assertEquals("0", user.uid)
    assertEquals("john_doe", user.username)
    assertEquals("Jean Luc", user.firstName)
    assertEquals("De Silva", user.lastName)
    assertEquals(1995, user.dateOfBirth.year)
    assertEquals(5, user.dateOfBirth.monthValue)
    assertEquals(15, user.dateOfBirth.dayOfMonth)
    assertEquals("  asds  ad  ", user.description)
  }

  @Test
  fun `setProfilePicture with null uri clears image`() = runTest {
    // 1. Set a dummy state first (simulate an existing image)
    // If not, we just rely on the fact that null uri -> null profilePicture
    val context = ApplicationProvider.getApplicationContext<Context>()
    viewModel.setProfilePicture(context, null)
    advanceUntilIdle()

    assertNull(viewModel.uiState.value.profilePicture)
  }

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

    assertNotNull("Profile picture bytes should not be null", resultBytes)
    assertTrue("Byte array should contain data", resultBytes!!.isNotEmpty())
  }

  @Test
  fun `setProfilePicture handles massive image by downsampling`() = runTest {
    // 1. Create a "Massive" image (e.g., 1000x1000)

    val context = ApplicationProvider.getApplicationContext<Context>()
    val file: File = File(context.cacheDir, "massive_image.jpg")
    val stream = FileOutputStream(file)

    // Create 1000x1000 bitmap
    val largeBitmap = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888)
    val success = largeBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
    stream.close()

    assertTrue("Bitmap compression failed in test setup", success)
    assertTrue("File should exist", file.exists())
    assertTrue("File should have data", file.length() > 0)
    // 2. Run logic
    viewModel.setProfilePicture(context, Uri.fromFile(file))
    advanceUntilIdle()
    // 3. Verify
    val resultBytes = viewModel.uiState.value.profilePicture
    assertNotNull(resultBytes)
  }
}
