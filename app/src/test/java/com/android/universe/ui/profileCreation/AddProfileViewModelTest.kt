package com.android.universe.ui.profileCreation

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.di.DispatcherProvider
import com.android.universe.model.image.ImageBitmapManager
import com.android.universe.model.user.FakeUserRepository
import com.android.universe.ui.common.InputLimits
import com.android.universe.ui.common.ValidationState
import com.android.universe.utils.MainCoroutineRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
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
  private lateinit var imageManager: ImageBitmapManager

  @Before
  fun setup() {
    repository = FakeUserRepository()

    imageManager = mockk<ImageBitmapManager>()

    val testDispatcher = UnconfinedTestDispatcher()
    val dispatcherProvider =
        object : DispatcherProvider {
          override val main: CoroutineDispatcher = testDispatcher
          override val default: CoroutineDispatcher = testDispatcher
          override val io: CoroutineDispatcher = testDispatcher
          override val unconfined: CoroutineDispatcher = testDispatcher
        }

    viewModel =
        AddProfileViewModel(
            repository = repository,
            imageManager = imageManager,
            dispatcherProvider = dispatcherProvider)
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
    viewModel.setUsername("invalid@name")
    viewModel.setFirstName("John")
    viewModel.setLastName("Doe")
    viewModel.setDay("12")
    viewModel.setMonth("8")
    viewModel.setYear("1990")

    viewModel.addProfile("0")
    advanceUntilIdle()

    assertEquals(0, repository.getAllUsers().size)
  }

  @Test
  fun addProfileRejectsTooLongUsername() = runTest {
    viewModel.setUsername("a".repeat(InputLimits.USERNAME + 2))
    viewModel.setFirstName("John")
    viewModel.setLastName("Doe")
    viewModel.setDay("12")
    viewModel.setMonth("8")
    viewModel.setYear("1990")

    viewModel.addProfile("0")
    advanceUntilIdle()

    assertEquals(0, repository.getAllUsers().size)
  }

  @Test
  fun addProfileRejectsInvalidNameCharacters() = runTest {
    viewModel.setUsername("john_doe")
    viewModel.setFirstName("John1")
    viewModel.setLastName("Doe")
    viewModel.setDay("12")
    viewModel.setMonth("8")
    viewModel.setYear("1990")

    viewModel.addProfile("0")
    advanceUntilIdle()

    assertEquals(0, repository.getAllUsers().size)
  }

  @Test
  fun addProfileRejectsTooLongFirstName() = runTest {
    viewModel.setUsername("john_doe")
    viewModel.setFirstName("A".repeat(InputLimits.FIRST_NAME + 5))
    viewModel.setLastName("Doe")
    viewModel.setDay("12")
    viewModel.setMonth("8")
    viewModel.setYear("1990")

    viewModel.addProfile("0")
    advanceUntilIdle()

    assertEquals(0, repository.getAllUsers().size)
  }

  @Test
  fun addProfileRejectsTooLongLastName() = runTest {
    viewModel.setUsername("john_doe")
    viewModel.setFirstName("John")
    viewModel.setLastName("B".repeat(InputLimits.LAST_NAME + 10))
    viewModel.setDay("12")
    viewModel.setMonth("8")
    viewModel.setYear("1990")

    viewModel.addProfile("0")
    advanceUntilIdle()

    assertEquals(0, repository.getAllUsers().size)
  }

  @Test
  fun addProfileRejectsTooLongDescription() = runTest {
    viewModel.setUsername("john_doe")
    viewModel.setFirstName("John")
    viewModel.setLastName("Doe")
    viewModel.setDay("12")
    viewModel.setMonth("8")
    viewModel.setYear("1990")
    viewModel.setDescription("A".repeat(InputLimits.DESCRIPTION + 20))

    viewModel.addProfile("0")
    advanceUntilIdle()

    assertEquals(0, repository.getAllUsers().size)
  }

  @Test
  fun addProfileTrimsAndAddsCleanedNames() = runTest {
    viewModel.setUsername("john_doe")
    viewModel.setFirstName("   Jean   Luc   ")
    viewModel.setLastName("  De    Silva ")
    viewModel.setDay("15")
    viewModel.setMonth("5")
    viewModel.setYear("1995")
    viewModel.setDescription("  asds  ad  ")

    viewModel.addProfile("0")
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
    viewModel.setProfilePicture(null)
    advanceUntilIdle()

    assertNull(viewModel.uiState.value.profilePicture)
  }

  @Test
  fun `setProfilePicture with valid uri updates state with compressed bytes`() = runTest {
    val mockUri = mockk<Uri>()
    val expectedBytes = byteArrayOf(1, 2, 3, 4)

    coEvery { imageManager.resizeAndCompressImage(mockUri) } returns expectedBytes

    viewModel.setProfilePicture(mockUri)
    advanceUntilIdle()

    val resultBytes = viewModel.uiState.value.profilePicture
    assertNotNull("Profile picture bytes should not be null", resultBytes)
    assertTrue("Byte array should match mock data", resultBytes.contentEquals(expectedBytes))
  }

  @Test
  fun `setProfilePicture handles null result from manager`() = runTest {
    val mockUri = mockk<Uri>()

    coEvery { imageManager.resizeAndCompressImage(mockUri) } returns null

    viewModel.setProfilePicture(mockUri)
    advanceUntilIdle()

    assertNull(
        "Profile picture should be null if manager fails", viewModel.uiState.value.profilePicture)
  }

  @Test
  fun `deleteProfilePicture clears image state`() = runTest {
    viewModel.deleteProfilePicture()

    assertNull(viewModel.uiState.value.profilePicture)
  }
}
