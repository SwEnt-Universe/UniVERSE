package com.android.universe.ui.profileCreation

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.universe.model.user.FakeUserRepository
import com.android.universe.ui.common.ErrorMessages
import com.android.universe.ui.common.InputLimits
import com.android.universe.utils.MainCoroutineRule
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
  }

  @Test
  fun initialStateIsEmpty() = runTest {
    val state = viewModel.uiState.value
    assertEquals("", state.username)
    assertEquals("", state.firstName)
    assertEquals("", state.lastName)
    assertNull(state.description)
    assertEquals("", state.country)
    assertEquals("", state.day)
    assertEquals("", state.month)
    assertEquals("", state.year)
    assertNull(state.usernameError)
    assertNull(state.firstNameError)
    assertNull(state.lastNameError)
    assertNull(state.countryError)
    assertNull(state.descriptionError)
    assertNull(state.yearError)
    assertNull(state.monthError)
    assertNull(state.dayError)
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
  fun setCountryUpdateTheState() = runTest {
    val initialState = viewModel.uiState.value
    assertEquals("", initialState.country)

    viewModel.setCountry("United States")

    val updatedState = viewModel.uiState.value
    assertEquals("United States", updatedState.country)
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
    viewModel.setCountry("United States")
    viewModel.setDay("12")
    viewModel.setMonth("8")
    viewModel.setYear("1990")

    viewModel.addProfile("0")
    advanceUntilIdle()

    val users = repository.getAllUsers()
    assertEquals(0, users.size)
    assertEquals(ErrorMessages.FIRSTNAME_EMPTY, viewModel.uiState.value.firstNameError)
  }

  @Test
  fun addProfileEmptyLastName() = runTest {
    val repository = FakeUserRepository()
    val viewModel = AddProfileViewModel(repository)

    viewModel.setUsername("john_doe")
    viewModel.setFirstName("John")
    viewModel.setLastName("")
    viewModel.setCountry("United States")
    viewModel.setDay("12")
    viewModel.setMonth("8")
    viewModel.setYear("1990")

    viewModel.addProfile("0")
    advanceUntilIdle()

    val users = repository.getAllUsers()
    assertEquals(0, users.size)
    assertEquals(ErrorMessages.LASTNAME_EMPTY, viewModel.uiState.value.lastNameError)
  }

  @Test
  fun addProfileEmptyDay() = runTest {
    val repository = FakeUserRepository()
    val viewModel = AddProfileViewModel(repository)

    viewModel.setUsername("john_doe")
    viewModel.setFirstName("John")
    viewModel.setLastName("Doe")
    viewModel.setCountry("United States")
    viewModel.setDay("")
    viewModel.setMonth("8")
    viewModel.setYear("1990")

    viewModel.addProfile("0")
    advanceUntilIdle()

    val users = repository.getAllUsers()
    assertEquals(0, users.size)
    assertEquals(ErrorMessages.DAY_EMPTY, viewModel.uiState.value.dayError)
  }

  @Test
  fun addProfileNonNumericDay() = runTest {
    val repository = FakeUserRepository()
    val viewModel = AddProfileViewModel(repository)

    viewModel.setUsername("john_doe")
    viewModel.setFirstName("John")
    viewModel.setLastName("Doe")
    viewModel.setCountry("United States")
    viewModel.setDay("hello")
    viewModel.setMonth("8")
    viewModel.setYear("1990")

    viewModel.addProfile("0")
    advanceUntilIdle()

    val users = repository.getAllUsers()
    assertEquals(0, users.size)
    assertEquals(ErrorMessages.DAY_EMPTY, viewModel.uiState.value.dayError)
  }

  @Test
  fun addProfileEmptyMonth() = runTest {
    val repository = FakeUserRepository()
    val viewModel = AddProfileViewModel(repository)

    viewModel.setUsername("john_doe")
    viewModel.setFirstName("John")
    viewModel.setLastName("Doe")
    viewModel.setCountry("United States")
    viewModel.setDay("12")
    viewModel.setMonth("")
    viewModel.setYear("1990")

    viewModel.addProfile("0")
    advanceUntilIdle()

    val users = repository.getAllUsers()
    assertEquals(0, users.size)
    assertEquals(ErrorMessages.MONTH_EMPTY, viewModel.uiState.value.monthError)
  }

  @Test
  fun addProfileNonNumericMonth() = runTest {
    val repository = FakeUserRepository()
    val viewModel = AddProfileViewModel(repository)

    viewModel.setUsername("john_doe")
    viewModel.setFirstName("John")
    viewModel.setLastName("Doe")
    viewModel.setCountry("United States")
    viewModel.setDay("12")
    viewModel.setMonth("hello")
    viewModel.setYear("1990")

    viewModel.addProfile("0")
    advanceUntilIdle()

    val users = repository.getAllUsers()
    assertEquals(0, users.size)
    assertEquals(ErrorMessages.MONTH_EMPTY, viewModel.uiState.value.monthError)
  }

  @Test
  fun addProfileEmptyYear() = runTest {
    val repository = FakeUserRepository()
    val viewModel = AddProfileViewModel(repository)

    viewModel.setUsername("john_doe")
    viewModel.setFirstName("John")
    viewModel.setLastName("Doe")
    viewModel.setCountry("United States")
    viewModel.setDay("12")
    viewModel.setMonth("8")
    viewModel.setYear("")

    viewModel.addProfile("0")
    advanceUntilIdle()

    val users = repository.getAllUsers()
    assertEquals(0, users.size)
    assertEquals(ErrorMessages.YEAR_EMPTY, viewModel.uiState.value.yearError)
  }

  @Test
  fun addProfileNonNumericYear() = runTest {
    val repository = FakeUserRepository()
    val viewModel = AddProfileViewModel(repository)

    viewModel.setUsername("john_doe")
    viewModel.setFirstName("John")
    viewModel.setLastName("Doe")
    viewModel.setCountry("United States")
    viewModel.setDay("12")
    viewModel.setMonth("8")
    viewModel.setYear("hello")

    viewModel.addProfile("0")
    advanceUntilIdle()

    val users = repository.getAllUsers()
    assertEquals(0, users.size)
    assertEquals(ErrorMessages.YEAR_EMPTY, viewModel.uiState.value.yearError)
  }

  @Test
  fun addProfileEmptyCountry() = runTest {
    val repository = FakeUserRepository()
    val viewModel = AddProfileViewModel(repository)

    viewModel.setUsername("john_doe")
    viewModel.setFirstName("John")
    viewModel.setLastName("Doe")
    viewModel.setCountry("")
    viewModel.setDay("12")
    viewModel.setMonth("8")
    viewModel.setYear("1990")

    viewModel.addProfile("0")
    advanceUntilIdle()

    val users = repository.getAllUsers()
    assertEquals(0, users.size)
    assertEquals(ErrorMessages.COUNTRY_EMPTY, viewModel.uiState.value.countryError)
  }

  @Test
  fun addProfileInvalidCountry() = runTest {
    val repository = FakeUserRepository()
    val viewModel = AddProfileViewModel(repository)

    viewModel.setUsername("john_doe")
    viewModel.setFirstName("John")
    viewModel.setLastName("Doe")
    viewModel.setCountry("Lune")
    viewModel.setDay("12")
    viewModel.setMonth("8")
    viewModel.setYear("1990")

    viewModel.addProfile("0")
    advanceUntilIdle()

    val users = repository.getAllUsers()
    assertEquals(0, users.size)
    assertEquals(ErrorMessages.COUNTRY_INVALID, viewModel.uiState.value.countryError)
  }

  @Test
  fun addProfileInvalidDate1() = runTest {
    val repository = FakeUserRepository()
    val viewModel = AddProfileViewModel(repository)

    viewModel.setUsername("john_doe")
    viewModel.setFirstName("John")
    viewModel.setLastName("Doe")
    viewModel.setCountry("United States")
    viewModel.setDay("29")
    viewModel.setMonth("2")
    viewModel.setYear("2001")

    viewModel.addProfile("0")
    advanceUntilIdle()

    val users = repository.getAllUsers()
    assertEquals(0, users.size)
    assertEquals(ErrorMessages.DATE_INVALID_LOGICAL, viewModel.uiState.value.dayError)
  }

  @Test
  fun addProfileInvalidDate2() = runTest {
    val repository = FakeUserRepository()
    val viewModel = AddProfileViewModel(repository)

    viewModel.setUsername("john_doe")
    viewModel.setFirstName("John")
    viewModel.setLastName("Doe")
    viewModel.setCountry("United States")
    viewModel.setDay("52")
    viewModel.setMonth("1")
    viewModel.setYear("2025")

    viewModel.addProfile("0")
    advanceUntilIdle()

    val users = repository.getAllUsers()
    assertEquals(0, users.size)
    assertEquals(ErrorMessages.DAY_OUT_OF_RANGE, viewModel.uiState.value.dayError)
  }

  @Test
  fun addProfileInvalidDate3() = runTest {
    val repository = FakeUserRepository()
    val viewModel = AddProfileViewModel(repository)

    viewModel.setUsername("john_doe")
    viewModel.setFirstName("John")
    viewModel.setLastName("Doe")
    viewModel.setCountry("United States")
    viewModel.setDay("12")
    viewModel.setMonth("13")
    viewModel.setYear("2025")

    viewModel.addProfile("0")
    advanceUntilIdle()

    val users = repository.getAllUsers()
    assertEquals(0, users.size)
    assertEquals(ErrorMessages.MONTH_OUT_OF_RANGE, viewModel.uiState.value.monthError)
  }

  @Test
  fun addProfileInvalidDate4() = runTest {
    val repository = FakeUserRepository()
    val viewModel = AddProfileViewModel(repository)

    viewModel.setUsername("john_doe")
    viewModel.setFirstName("John")
    viewModel.setLastName("Doe")
    viewModel.setCountry("United States")
    viewModel.setDay("12")
    viewModel.setMonth("11")
    viewModel.setYear("2105")

    viewModel.addProfile("0")
    advanceUntilIdle()

    val users = repository.getAllUsers()
    assertEquals(0, users.size)
    assertEquals(
        ErrorMessages.YEAR_OUT_OF_RANGE.format(InputLimits.MIN_BIRTH_YEAR, LocalDate.now().year),
        viewModel.uiState.value.yearError)
  }

  @Test
  fun addProfileEmptyUsername() = runTest {
    val repository = FakeUserRepository()
    val viewModel = AddProfileViewModel(repository)

    viewModel.setUsername("")
    viewModel.setFirstName("John")
    viewModel.setLastName("Doe")
    viewModel.setCountry("United States")
    viewModel.setDay("12")
    viewModel.setMonth("8")
    viewModel.setYear("1990")

    viewModel.addProfile("0")
    advanceUntilIdle()

    val users = repository.getAllUsers()
    assertEquals(0, users.size)
    assertEquals(ErrorMessages.USERNAME_EMPTY, viewModel.uiState.value.usernameError)
  }

  @Test
  fun addProfileValid() = runTest {
    val repository = FakeUserRepository()
    val viewModel = AddProfileViewModel(repository)

    viewModel.setUsername("john_doe")
    viewModel.setFirstName("John")
    viewModel.setLastName("Doe")
    viewModel.setCountry("United States")
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
    assertEquals("US", user.country)
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
      val result = viewModel.uiState.value.usernameError
      assertEquals(null, result)
    }
  }

  @Test
  fun usernameRejectsInvalidCharacters() = runTest {
    val invalid = listOf("john doe", "user@", "name!", "john#doe")
    invalid.forEach {
      viewModel.setUsername(it)
      val result = viewModel.uiState.value.usernameError
      assertEquals(ErrorMessages.USERNAME_INVALID_FORMAT, result)
    }
  }

  @Test
  fun usernameTrimsToPlusOneLimit() = runTest {
    val tooLong = "a".repeat(InputLimits.USERNAME + 5)
    viewModel.setUsername(tooLong)
    assertEquals(InputLimits.USERNAME + 1, viewModel.uiState.value.username.length)
  }

  @Test
  fun usernameFailsValidationWhenTooLong() = runTest {
    val tooLong = "a".repeat(InputLimits.USERNAME + 1)
    viewModel.setUsername(tooLong)
    val result = viewModel.uiState.value.usernameError
    assertEquals(ErrorMessages.USERNAME_TOO_LONG.format(InputLimits.USERNAME), result)
  }

  // ---------- FIRST NAME TESTS ----------

  @Test
  fun firstNameCleansMultipleSpaces() = runTest {
    viewModel.setFirstName("   John    Michael   Doe   ")
    assertEquals("John Michael Doe", viewModel.uiState.value.firstName)
  }

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
      val result = viewModel.uiState.value.firstNameError
      assertEquals(null, result)
    }
  }

  @Test
  fun firstNameRejectsNumbersOrSymbols() = runTest {
    val invalid = listOf("John1", "Jane@", "Bob!", "Al1en")
    invalid.forEach {
      viewModel.setFirstName(it)
      val result = viewModel.uiState.value.firstNameError
      assertEquals(ErrorMessages.FIRSTNAME_INVALID_FORMAT, result)
    }
  }

  // ---------- LAST NAME TESTS ----------

  @Test
  fun lastNameCleansProperly() = runTest {
    viewModel.setLastName("   Van   der   Waals   ")
    assertEquals("Van der Waals", viewModel.uiState.value.lastName)
  }

  @Test
  fun lastNameAcceptsAccentsAndSpecialLetters() = runTest {
    val valid = listOf("O'Connor", "García-López", "Brân", "L'Écuyer")
    valid.forEach {
      viewModel.setLastName(it)
      val result = viewModel.uiState.value.lastNameError
      assertEquals(null, result)
    }
  }

  @Test
  fun lastNameRejectsInvalidCharacters() = runTest {
    val invalid = listOf("Doe!", "Smith@", "Brown#", "O%Neil")
    invalid.forEach {
      viewModel.setLastName(it)
      val result = viewModel.uiState.value.lastNameError
      assertEquals(ErrorMessages.LASTNAME_INVALID_FORMAT, result)
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

  @Test
  fun descriptionTooLongFailsValidation() = runTest {
    viewModel.setDescription("A".repeat(InputLimits.DESCRIPTION + 5))
    val result = viewModel.uiState.value.descriptionError
    assertEquals(ErrorMessages.DESCRIPTION_TOO_LONG.format(InputLimits.DESCRIPTION), result)
  }

  // ---------- ADD PROFILE INTEGRATION TESTS ----------

  @Test
  fun addProfileRejectsInvalidUsernameCharacters() = runTest {
    val repository = FakeUserRepository()
    val vm = AddProfileViewModel(repository)

    vm.setUsername("invalid@name")
    vm.setFirstName("John")
    vm.setLastName("Doe")
    vm.setCountry("United States")
    vm.setDay("12")
    vm.setMonth("8")
    vm.setYear("1990")

    vm.addProfile("0")
    advanceUntilIdle()

    assertEquals(0, repository.getAllUsers().size)
    assertEquals(ErrorMessages.USERNAME_INVALID_FORMAT, vm.uiState.value.usernameError)
  }

  @Test
  fun addProfileRejectsTooLongUsername() = runTest {
    val repository = FakeUserRepository()
    val vm = AddProfileViewModel(repository)

    vm.setUsername("a".repeat(InputLimits.USERNAME + 2))
    vm.setFirstName("John")
    vm.setLastName("Doe")
    vm.setCountry("United States")
    vm.setDay("12")
    vm.setMonth("8")
    vm.setYear("1990")

    vm.addProfile("0")
    advanceUntilIdle()

    assertEquals(0, repository.getAllUsers().size)
    assertEquals(
        ErrorMessages.USERNAME_TOO_LONG.format(InputLimits.USERNAME),
        vm.uiState.value.usernameError)
  }

  @Test
  fun addProfileRejectsInvalidNameCharacters() = runTest {
    val repository = FakeUserRepository()
    val vm = AddProfileViewModel(repository)

    vm.setUsername("john_doe")
    vm.setFirstName("John1")
    vm.setLastName("Doe")
    vm.setCountry("United States")
    vm.setDay("12")
    vm.setMonth("8")
    vm.setYear("1990")

    vm.addProfile("0")
    advanceUntilIdle()

    assertEquals(0, repository.getAllUsers().size)
    assertEquals(ErrorMessages.FIRSTNAME_INVALID_FORMAT, vm.uiState.value.firstNameError)
  }

  @Test
  fun addProfileRejectsTooLongFirstName() = runTest {
    val repository = FakeUserRepository()
    val vm = AddProfileViewModel(repository)

    vm.setUsername("john_doe")
    vm.setFirstName("A".repeat(InputLimits.FIRST_NAME + 5))
    vm.setLastName("Doe")
    vm.setCountry("United States")
    vm.setDay("12")
    vm.setMonth("8")
    vm.setYear("1990")

    vm.addProfile("0")
    advanceUntilIdle()

    assertEquals(0, repository.getAllUsers().size)
    assertEquals(
        ErrorMessages.FIRSTNAME_TOO_LONG.format(InputLimits.FIRST_NAME),
        vm.uiState.value.firstNameError)
  }

  @Test
  fun addProfileRejectsTooLongLastName() = runTest {
    val repository = FakeUserRepository()
    val vm = AddProfileViewModel(repository)

    vm.setUsername("john_doe")
    vm.setFirstName("John")
    vm.setLastName("B".repeat(InputLimits.LAST_NAME + 10))
    vm.setCountry("United States")
    vm.setDay("12")
    vm.setMonth("8")
    vm.setYear("1990")

    vm.addProfile("0")
    advanceUntilIdle()

    assertEquals(0, repository.getAllUsers().size)
    assertEquals(
        ErrorMessages.LASTNAME_TOO_LONG.format(InputLimits.LAST_NAME),
        vm.uiState.value.lastNameError)
  }

  @Test
  fun addProfileRejectsTooLongDescription() = runTest {
    val repository = FakeUserRepository()
    val vm = AddProfileViewModel(repository)

    vm.setUsername("john_doe")
    vm.setFirstName("John")
    vm.setLastName("Doe")
    vm.setCountry("United States")
    vm.setDay("12")
    vm.setMonth("8")
    vm.setYear("1990")
    vm.setDescription("A".repeat(InputLimits.DESCRIPTION + 20))

    vm.addProfile("0")
    advanceUntilIdle()

    assertEquals(0, repository.getAllUsers().size)
    assertEquals(
        ErrorMessages.DESCRIPTION_TOO_LONG.format(InputLimits.DESCRIPTION),
        vm.uiState.value.descriptionError)
  }

  @Test
  fun addProfileTrimsAndAddsCleanedNames() = runTest {
    val vm = AddProfileViewModel(repository)

    vm.setUsername("john_doe")
    vm.setFirstName("   Jean   Luc   ")
    vm.setLastName("  De    Silva ")
    vm.setCountry("United States")
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
    assertEquals("US", user.country)
    assertEquals(1995, user.dateOfBirth.year)
    assertEquals(5, user.dateOfBirth.monthValue)
    assertEquals(15, user.dateOfBirth.dayOfMonth)
    assertEquals("  asds  ad  ", user.description)
  }
}
