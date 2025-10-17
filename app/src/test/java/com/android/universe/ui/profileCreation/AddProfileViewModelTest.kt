package com.android.universe.ui.profileCreation

import com.android.universe.model.user.FakeUserRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddProfileViewModelTest {


  private val testDispatcher = UnconfinedTestDispatcher()

  private lateinit var repository: FakeUserRepository
  private lateinit var viewModel: AddProfileViewModel

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
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
    assertEquals("", state.username)
    assertNull(state.errorMsg)
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
  fun setErrorMsgUpdateTheState() = runTest {
    val initialState = viewModel.uiState.value
    assertNull(initialState.errorMsg)

    viewModel.setErrorMsg("Error")

    val updatedState = viewModel.uiState.value
    assertEquals("Error", updatedState.errorMsg)
  }

  @Test
  fun clearErrorMsgUpdateTheState() = runTest {
    val initialState = viewModel.uiState.value
    assertNull(initialState.errorMsg)

    viewModel.setErrorMsg("Error")

    val updatedState1 = viewModel.uiState.value
    assertEquals("Error", updatedState1.errorMsg)

    viewModel.clearErrorMsg()

    val updatedState2 = viewModel.uiState.value
    assertNull(updatedState2.errorMsg)
  }

  @Test
  fun addProfileEmptyFirstName() = runTest {
    val repository = FakeUserRepository()
    val testDispatcher = StandardTestDispatcher(testScheduler)
    val viewModel = AddProfileViewModel(repository, testDispatcher)

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
    assertEquals("First name cannot be empty", viewModel.uiState.value.errorMsg)
  }

  @Test
  fun addProfileEmptyLastName() = runTest {
    val repository = FakeUserRepository()
    val testDispatcher = StandardTestDispatcher(testScheduler)
    val viewModel = AddProfileViewModel(repository, testDispatcher)

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
    assertEquals("Last name cannot be empty", viewModel.uiState.value.errorMsg)
  }

  @Test
  fun addProfileEmptyDay() = runTest {
    val repository = FakeUserRepository()
    val testDispatcher = StandardTestDispatcher(testScheduler)
    val viewModel = AddProfileViewModel(repository, testDispatcher)

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
    assertEquals("Day is not a number", viewModel.uiState.value.errorMsg)
  }

  @Test
  fun addProfileNonNumericDay() = runTest {
    val repository = FakeUserRepository()
    val testDispatcher = StandardTestDispatcher(testScheduler)
    val viewModel = AddProfileViewModel(repository, testDispatcher)

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
    assertEquals("Day is not a number", viewModel.uiState.value.errorMsg)
  }

  @Test
  fun addProfileEmptyMonth() = runTest {
    val repository = FakeUserRepository()
    val testDispatcher = StandardTestDispatcher(testScheduler)
    val viewModel = AddProfileViewModel(repository, testDispatcher)

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
    assertEquals("Month is not a number", viewModel.uiState.value.errorMsg)
  }

  @Test
  fun addProfileNonNumericMonth() = runTest {
    val repository = FakeUserRepository()
    val testDispatcher = StandardTestDispatcher(testScheduler)
    val viewModel = AddProfileViewModel(repository, testDispatcher)

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
    assertEquals("Month is not a number", viewModel.uiState.value.errorMsg)
  }

  @Test
  fun addProfileEmptyYear() = runTest {
    val repository = FakeUserRepository()
    val testDispatcher = StandardTestDispatcher(testScheduler)
    val viewModel = AddProfileViewModel(repository, testDispatcher)

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
    assertEquals("Year is not a number", viewModel.uiState.value.errorMsg)
  }

  @Test
  fun addProfileNonNumericYear() = runTest {
    val repository = FakeUserRepository()
    val testDispatcher = StandardTestDispatcher(testScheduler)
    val viewModel = AddProfileViewModel(repository, testDispatcher)

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
    assertEquals("Year is not a number", viewModel.uiState.value.errorMsg)
  }

  @Test
  fun addProfileEmptyCountry() = runTest {
    val repository = FakeUserRepository()
    val testDispatcher = StandardTestDispatcher(testScheduler)
    val viewModel = AddProfileViewModel(repository, testDispatcher)

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
    assertEquals("Country cannot be empty", viewModel.uiState.value.errorMsg)
  }

  @Test
  fun addProfileInvalidCountry() = runTest {
    val repository = FakeUserRepository()
    val testDispatcher = StandardTestDispatcher(testScheduler)
    val viewModel = AddProfileViewModel(repository, testDispatcher)

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
    assertEquals("Invalid country", viewModel.uiState.value.errorMsg)
  }

  @Test
  fun addProfileInvalidDate1() = runTest {
    val repository = FakeUserRepository()
    val testDispatcher = StandardTestDispatcher(testScheduler)
    val viewModel = AddProfileViewModel(repository, testDispatcher)

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
    assertEquals("Invalid date", viewModel.uiState.value.errorMsg)
  }

  @Test
  fun addProfileInvalidDate2() = runTest {
    val repository = FakeUserRepository()
    val testDispatcher = StandardTestDispatcher(testScheduler)
    val viewModel = AddProfileViewModel(repository, testDispatcher)

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
    assertEquals("Invalid date", viewModel.uiState.value.errorMsg)
  }

  @Test
  fun addProfileInvalidDate3() = runTest {
    val repository = FakeUserRepository()
    val testDispatcher = StandardTestDispatcher(testScheduler)
    val viewModel = AddProfileViewModel(repository, testDispatcher)

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
    assertEquals("Invalid date", viewModel.uiState.value.errorMsg)
  }

  @Test
  fun addProfileInvalidDate4() = runTest {
    val repository = FakeUserRepository()
    val testDispatcher = StandardTestDispatcher(testScheduler)
    val viewModel = AddProfileViewModel(repository, testDispatcher)

    viewModel.setUsername("john_doe")
    viewModel.setFirstName("John")
    viewModel.setLastName("Doe")
    viewModel.setCountry("United States")
    viewModel.setDay("12")
    viewModel.setMonth("11")
    viewModel.setYear("2025")

    viewModel.addProfile("0")
    advanceUntilIdle()

    val users = repository.getAllUsers()
    assertEquals(0, users.size)
    assertEquals("At least 13 years old required", viewModel.uiState.value.errorMsg)
  }

  @Test
  fun addProfileEmptyUsername() = runTest {
    val repository = FakeUserRepository()
    val testDispatcher = StandardTestDispatcher(testScheduler)
    val viewModel = AddProfileViewModel(repository, testDispatcher)

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
    assertEquals("Username cannot be empty", viewModel.uiState.value.errorMsg)
  }

  @Test
  fun addProfileValid() = runTest {
    val repository = FakeUserRepository()
    val testDispatcher = StandardTestDispatcher(testScheduler)
    val testRepositoryDispatcher = StandardTestDispatcher(testScheduler)
    val viewModel = AddProfileViewModel(repository, testDispatcher, testRepositoryDispatcher)

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

    assertNull(viewModel.uiState.value.errorMsg)
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
      assertEquals(
          "Invalid username format, allowed characters are letters, numbers, dots, underscores, or dashes",
          result)
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
    assertEquals("Username is too long", result)
  }

  // ---------- FIRST NAME TESTS ----------

  @Test
  fun firstNameCleansMultipleSpaces() = runTest {
    viewModel.setFirstName("   John    Michael   Doe   ")
    assertEquals(" John Michael Doe ", viewModel.uiState.value.firstName)
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
      assertEquals(
          "Invalid name format, allowed characters are letters, apostrophes, hyphens, and spaces",
          result)
    }
  }

  // ---------- LAST NAME TESTS ----------

  @Test
  fun lastNameCleansProperly() = runTest {
    viewModel.setLastName("   Van   der   Waals   ")
    assertEquals(" Van der Waals ", viewModel.uiState.value.lastName)
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
      assertEquals(
          "Invalid name format, allowed characters are letters, apostrophes, hyphens, and spaces",
          result)
    }
  }

  // ---------- DESCRIPTION TESTS ----------

  @Test
  fun descriptionCleansSpaces() = runTest {
    viewModel.setDescription("   Hello    there   world!   ")
    assertEquals(" Hello there world! ", viewModel.uiState.value.description)
  }

  @Test
  fun descriptionTrimsToPlusOneLimit() = runTest {
    val longDesc = "A".repeat(InputLimits.DESCRIPTION + 50)
    viewModel.setDescription(longDesc)
    assertEquals(InputLimits.DESCRIPTION + 1, viewModel.uiState.value.description?.length)
  }

  @Test
  fun descriptionTooLongFailsValidation() = runTest {
    viewModel.setDescription("A".repeat(InputLimits.DESCRIPTION + 5))
    val result = viewModel.uiState.value.descriptionError
    assertEquals("Description is too long", result)
  }

  // ---------- ADD PROFILE INTEGRATION TESTS ----------

  @Test
  fun addProfileRejectsInvalidUsernameCharacters() = runTest {
    val repository = FakeUserRepository()
    val dispatcher = StandardTestDispatcher(testScheduler)
    val vm = AddProfileViewModel(repository, dispatcher)

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
    assertEquals("Invalid username format", vm.uiState.value.errorMsg)
  }

  @Test
  fun addProfileRejectsTooLongUsername() = runTest {
    val repository = FakeUserRepository()
    val dispatcher = StandardTestDispatcher(testScheduler)
    val vm = AddProfileViewModel(repository, dispatcher)

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
    assertEquals("Username is too long", vm.uiState.value.errorMsg)
  }

  @Test
  fun addProfileRejectsInvalidNameCharacters() = runTest {
    val repository = FakeUserRepository()
    val dispatcher = StandardTestDispatcher(testScheduler)
    val vm = AddProfileViewModel(repository, dispatcher)

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
    assertEquals("Invalid first name format", vm.uiState.value.errorMsg)
  }

  @Test
  fun addProfileRejectsTooLongFirstName() = runTest {
    val repository = FakeUserRepository()
    val dispatcher = StandardTestDispatcher(testScheduler)
    val vm = AddProfileViewModel(repository, dispatcher)

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
    assertEquals("First name is too long", vm.uiState.value.errorMsg)
  }

  @Test
  fun addProfileRejectsTooLongLastName() = runTest {
    val repository = FakeUserRepository()
    val dispatcher = StandardTestDispatcher(testScheduler)
    val vm = AddProfileViewModel(repository, dispatcher)

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
    assertEquals("Last name is too long", vm.uiState.value.errorMsg)
  }

  @Test
  fun addProfileRejectsTooLongDescription() = runTest {
    val repository = FakeUserRepository()
    val dispatcher = StandardTestDispatcher(testScheduler)
    val vm = AddProfileViewModel(repository, dispatcher)

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
    assertEquals("Description is too long", vm.uiState.value.errorMsg)
  }

  @Test
  fun addProfileTrimsAndAddsCleanedNames() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
    val testRepositoryDispatcher = StandardTestDispatcher(testScheduler)
    val vm = AddProfileViewModel(repository, dispatcher, testRepositoryDispatcher)

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
    assertEquals("asds ad", user.description)
  }
}
