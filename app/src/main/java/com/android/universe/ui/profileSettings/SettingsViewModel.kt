package com.android.universe.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.universe.model.Tag
import com.android.universe.model.user.UserProfile
import com.android.universe.model.user.UserRepositoryProvider
import com.android.universe.ui.profileSettings.sanitize
import com.android.universe.ui.profileSettings.validateAll
import com.android.universe.ui.profileSettings.validateDateTriple
import com.android.universe.ui.profileSettings.validateDescription
import com.android.universe.ui.profileSettings.validateEmail
import com.android.universe.ui.profileSettings.validateName
import com.android.universe.ui.profileSettings.validateNonEmpty
import com.android.universe.ui.profileSettings.validatePassword
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
  val email: String = "preview@example.com",
  val password: String = "",
  val firstName: String = "",
  val lastName: String = "",
  val country: String = "",
  val description: String = "",
  val day: String = "",
  val month: String = "",
  val year: String = "",
  val selectedTags: List<Tag> = emptyList(),
  val tempSelectedTags: List<Tag> = emptyList(),
  val tempValue: String = "",
  val tempDay: String = "",
  val tempMonth: String = "",
  val tempYear: String = "",
  val emailError: String? = null,
  val passwordError: String? = null,
  val firstNameError: String? = null,
  val lastNameError: String? = null,
  val descriptionError: String? = null,
  val dayError: String? = null,
  val monthError: String? = null,
  val yearError: String? = null,
  val tempDayError: String? = null,
  val tempMonthError: String? = null,
  val tempYearError: String? = null,
  val modalError: String? = null,
  val showModal: Boolean = false,
  val currentField: String = "",
  val showCountryDropdown: Boolean = false,
  val errorMsg: String? = null
)

class SettingsViewModel(
  private val userRepository: UserRepositoryProvider = UserRepositoryProvider
) : ViewModel() {
  private val _uiState = MutableStateFlow(SettingsUiState())
  val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

  init {
    FirebaseAuth.getInstance().currentUser?.email?.let { email ->
      _uiState.value = _uiState.value.copy(email = email)
    }
  }

  fun loadUser(username: String) {
    viewModelScope.launch {
      try {
        val userProfile = userRepository.repository.getUser(username)
        _uiState.value = _uiState.value.copy(
          firstName = userProfile.firstName,
          lastName = userProfile.lastName,
          country = userProfile.country,
          description = userProfile.description ?: "",
          day = userProfile.dateOfBirth.dayOfMonth.toString(),
          month = userProfile.dateOfBirth.monthValue.toString(),
          year = userProfile.dateOfBirth.year.toString(),
          selectedTags = userProfile.tags.toList()
        )
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(errorMsg = "Failed to load user: ${e.message}")
      }
    }
  }

  fun clearErrorMsg() {
    _uiState.value = _uiState.value.copy(errorMsg = null)
  }

  fun updateTemp(key: String, value: String) {
    _uiState.value = when (key) {
      "tempValue" -> _uiState.value.copy(tempValue = value, modalError = null)
      "tempDay" -> _uiState.value.copy(tempDay = value, tempDayError = null)
      "tempMonth" -> _uiState.value.copy(tempMonth = value, tempMonthError = null)
      "tempYear" -> _uiState.value.copy(tempYear = value, tempYearError = null)
      else -> _uiState.value
    }
  }

  fun openModal(field: String) {
    val state = _uiState.value
    _uiState.value = _uiState.value.copy(
      showModal = true,
      currentField = field,
      modalError = null,
      tempDayError = null,
      tempMonthError = null,
      tempYearError = null,
      tempValue = when (field) {
        "email" -> state.email
        "password" -> ""
        "firstName" -> state.firstName
        "lastName" -> state.lastName
        "description" -> state.description
        "country" -> state.country
        else -> ""
      },
      tempDay = if (field == "date") state.day else "",
      tempMonth = if (field == "date") state.month else "",
      tempYear = if (field == "date") state.year else "",
      tempSelectedTags = Tag.Category.entries
        .find { it.fieldName == field }
        ?.let { category -> Tag.filterByCategory(state.selectedTags, category) }
        ?: emptyList()
    )
  }

  fun closeModal() {
    _uiState.value = _uiState.value.copy(
      showModal = false,
      currentField = "",
      modalError = null,
      tempDayError = null,
      tempMonthError = null,
      tempYearError = null
    )
  }

  fun toggleCountryDropdown(show: Boolean) {
    _uiState.value = _uiState.value.copy(showCountryDropdown = show)
  }

  fun addTag(tag: Tag) {
    if (!_uiState.value.tempSelectedTags.contains(tag)) {
      _uiState.value = _uiState.value.copy(tempSelectedTags = _uiState.value.tempSelectedTags + tag)
    } else {
      Log.e("SettingsViewModel", "Tag '${tag.displayName}' is already selected")
    }
  }

  fun removeTag(tag: Tag) {
    if (_uiState.value.tempSelectedTags.contains(tag)) {
      _uiState.value = _uiState.value.copy(tempSelectedTags = _uiState.value.tempSelectedTags - tag)
    } else {
      Log.e("SettingsViewModel", "Tag '${tag.displayName}' is not selected")
    }
  }

  fun saveModal(username: String) {
    val state = _uiState.value
    var modalError: String? = null
    when (state.currentField) {
      "email" -> {
        modalError = validateEmail(state.tempValue)
        if (modalError != null) {
          _uiState.value = _uiState.value.copy(modalError = modalError)
          return
        }
        _uiState.value = _uiState.value.copy(email = state.tempValue, emailError = null)
      }
      "password" -> {
        modalError = validatePassword(state.tempValue)
        if (modalError != null) {
          _uiState.value = _uiState.value.copy(modalError = modalError)
          return
        }
        _uiState.value = _uiState.value.copy(password = state.tempValue, passwordError = null)
      }
      "firstName" -> {
        modalError = validateName("First name", state.tempValue)
        if (modalError != null) {
          _uiState.value = _uiState.value.copy(modalError = modalError)
          return
        }
        _uiState.value = _uiState.value.copy(firstName = state.tempValue, firstNameError = null)
      }
      "lastName" -> {
        modalError = validateName("Last name", state.tempValue)
        if (modalError != null) {
          _uiState.value = _uiState.value.copy(modalError = modalError)
          return
        }
        _uiState.value = _uiState.value.copy(lastName = state.tempValue, lastNameError = null)
      }
      "description" -> {
        modalError = validateDescription(state.tempValue)
        if (modalError != null) {
          _uiState.value = _uiState.value.copy(modalError = modalError)
          return
        }
        _uiState.value = _uiState.value.copy(description = state.tempValue, descriptionError = null)
      }
      "country" -> {
        modalError = validateNonEmpty("Country", state.tempValue)
        if (modalError != null) {
          _uiState.value = _uiState.value.copy(modalError = modalError)
          return
        }
        _uiState.value = _uiState.value.copy(country = state.tempValue)
      }
      "date" -> {
        val (dErr, mErr, yErr) = validateDateTriple(state.tempDay, state.tempMonth, state.tempYear)
        if (dErr != null || mErr != null || yErr != null) {
          _uiState.value = _uiState.value.copy(
            tempDayError = dErr,
            tempMonthError = mErr,
            tempYearError = yErr
          )
          return
        }
        _uiState.value = _uiState.value.copy(
          day = state.tempDay,
          month = state.tempMonth,
          year = state.tempYear,
          dayError = null,
          monthError = null,
          yearError = null
        )
      }
      else -> {
        Tag.Category.entries
          .find { it.fieldName == state.currentField }
          ?.let { category ->
            val tagList = Tag.getTagsForCategory(category)
            _uiState.value = _uiState.value.copy(
              selectedTags = _uiState.value.selectedTags.filter { it !in tagList } + state.tempSelectedTags
            )
          }
      }
    }

    _uiState.value = _uiState.value.copy(
      showModal = false,
      currentField = "",
      modalError = null,
      tempDayError = null,
      tempMonthError = null,
      tempYearError = null
    )
    saveProfile(username)
  }

  fun saveProfile(username: String) {
    viewModelScope.launch {
      val state = _uiState.value
      val errors = validateAll(
        state.email,
        state.password,
        state.firstName,
        state.lastName,
        state.description,
        state.day,
        state.month,
        state.year
      )

      if (errors.email != null ||
        errors.password != null ||
        errors.firstName != null ||
        errors.lastName != null ||
        errors.description != null ||
        errors.day != null ||
        errors.month != null ||
        errors.year != null
      ) {
        _uiState.value = _uiState.value.copy(
          emailError = errors.email,
          passwordError = errors.password,
          firstNameError = errors.firstName,
          lastNameError = errors.lastName,
          descriptionError = errors.description,
          dayError = errors.day,
          monthError = errors.month,
          yearError = errors.year
        )
        return@launch
      }

      try {
        val cleanedFirstName = sanitize(state.firstName)
        val cleanedLastName = sanitize(state.lastName)
        val cleanedDescription = sanitize(state.description).takeIf { it.isNotBlank() }
        val updatedProfile = UserProfile(
          username = username,
          firstName = cleanedFirstName,
          lastName = cleanedLastName,
          country = state.country,
          description = cleanedDescription,
          dateOfBirth = LocalDate.of(state.year.toInt(), state.month.toInt(), state.day.toInt()),
          tags = state.selectedTags.toSet()
        )
        userRepository.repository.updateUser(username, updatedProfile)

        if (state.email != FirebaseAuth.getInstance().currentUser?.email) {
          FirebaseAuth.getInstance().currentUser?.updateEmail(state.email)?.addOnFailureListener { e ->
            _uiState.value = _uiState.value.copy(errorMsg = "Failed to update email: ${e.message}")
          }
        }
        if (state.password.isNotEmpty()) {
          FirebaseAuth.getInstance()
            .currentUser
            ?.updatePassword(state.password)
            ?.addOnFailureListener { e ->
              _uiState.value = _uiState.value.copy(errorMsg = "Failed to update password: ${e.message}")
            }
        }
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(errorMsg = "Failed to save profile: ${e.message}")
      }
    }
  }
}