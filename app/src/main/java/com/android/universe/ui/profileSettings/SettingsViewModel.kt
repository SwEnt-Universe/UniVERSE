package com.android.universe.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.universe.model.Tag
import com.android.universe.model.tagsCanton
import com.android.universe.model.tagsInterest
import com.android.universe.model.tagsMusic
import com.android.universe.model.tagsSport
import com.android.universe.model.tagsTransport
import com.android.universe.model.user.UserProfile
import com.android.universe.model.user.UserRepositoryProvider
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
    val tempSelectedTags: List<String> = emptyList(),
    val emailError: String? = null,
    val passwordError: String? = null,
    val firstNameError: String? = null,
    val lastNameError: String? = null,
    val descriptionError: String? = null,
    val dayError: String? = null,
    val monthError: String? = null,
    val yearError: String? = null,
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
        _uiState.value =
            _uiState.value.copy(
                firstName = userProfile.firstName,
                lastName = userProfile.lastName,
                country = userProfile.country,
                description = userProfile.description ?: "",
                day = userProfile.dateOfBirth.dayOfMonth.toString(),
                month = userProfile.dateOfBirth.monthValue.toString(),
                year = userProfile.dateOfBirth.year.toString(),
                selectedTags = userProfile.tags)
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(errorMsg = "Failed to load user: ${e.message}")
      }
    }
  }

  fun clearErrorMsg() {
    _uiState.value = _uiState.value.copy(errorMsg = null)
  }

  fun updateField(field: String, value: String) {
    when (field) {
      "email" -> _uiState.value = _uiState.value.copy(email = value, emailError = null)
      "password" -> _uiState.value = _uiState.value.copy(password = value, passwordError = null)
      "firstName" -> _uiState.value = _uiState.value.copy(firstName = value, firstNameError = null)
      "lastName" -> _uiState.value = _uiState.value.copy(lastName = value, lastNameError = null)
      "description" ->
          _uiState.value = _uiState.value.copy(description = value, descriptionError = null)
      "country" -> _uiState.value = _uiState.value.copy(country = value)
      "day" -> _uiState.value = _uiState.value.copy(day = value, dayError = null)
      "month" -> _uiState.value = _uiState.value.copy(month = value, monthError = null)
      "year" -> _uiState.value = _uiState.value.copy(year = value, yearError = null)
    }
  }

  fun openModal(field: String) {
    _uiState.value =
        _uiState.value.copy(
            showModal = true,
            currentField = field,
            password = if (field == "password") "" else _uiState.value.password, // Reset password
            tempSelectedTags =
                when (field) {
                  "interest_tags" ->
                      _uiState.value.selectedTags.filter { it.name in tagsInterest }.map { it.name }
                  "sport_tags" ->
                      _uiState.value.selectedTags.filter { it.name in tagsSport }.map { it.name }
                  "music_tags" ->
                      _uiState.value.selectedTags.filter { it.name in tagsMusic }.map { it.name }
                  "transport_tags" ->
                      _uiState.value.selectedTags
                          .filter { it.name in tagsTransport }
                          .map { it.name }
                  "canton_tags" ->
                      _uiState.value.selectedTags.filter { it.name in tagsCanton }.map { it.name }
                  else -> emptyList()
                })
  }

  fun closeModal() {
    _uiState.value = _uiState.value.copy(showModal = false, currentField = "", modalError = null)
  }

  fun toggleCountryDropdown(show: Boolean) {
    _uiState.value = _uiState.value.copy(showCountryDropdown = show)
  }

  fun addTag(tag: String) {
    if (!_uiState.value.tempSelectedTags.contains(tag)) {
      _uiState.value = _uiState.value.copy(tempSelectedTags = _uiState.value.tempSelectedTags + tag)
    } else {
      Log.e("SettingsViewModel", "Tag '$tag' is already selected")
    }
  }

  fun removeTag(tag: String) {
    if (_uiState.value.tempSelectedTags.contains(tag)) {
      _uiState.value = _uiState.value.copy(tempSelectedTags = _uiState.value.tempSelectedTags - tag)
    } else {
      Log.e("SettingsViewModel", "Tag '$tag' is not selected")
    }
  }

  fun saveModal(username: String) {
    val state = _uiState.value
    val modalError =
        when (state.currentField) {
          "email" ->
              when {
                state.email.isEmpty() -> "Email cannot be empty"
                !state.email.contains("@") -> "Invalid email format"
                else -> null
              }
          "password" ->
              when {
                state.password.isNotEmpty() && state.password.length < 6 ->
                    "Password must be at least 6 characters"
                else -> null
              }
          "firstName" -> if (state.firstName.isEmpty()) "First name cannot be empty" else null
          "lastName" -> if (state.lastName.isEmpty()) "Last name cannot be empty" else null
          "description" -> if (state.description.length > 200) "Description too long" else null
          "country" -> if (state.country.isEmpty()) "Country cannot be empty" else null
          "date" -> {
            try {
              LocalDate.of(state.year.toInt(), state.month.toInt(), state.day.toInt())
              null
            } catch (e: Exception) {
              "Invalid date"
            }
          }
          else -> null
        }

    if (modalError != null) {
      _uiState.value = _uiState.value.copy(modalError = modalError)
      return
    }

    if (state.currentField in
        listOf("interest_tags", "sport_tags", "music_tags", "transport_tags", "canton_tags")) {
      val tagList =
          when (state.currentField) {
            "interest_tags" -> tagsInterest
            "sport_tags" -> tagsSport
            "music_tags" -> tagsMusic
            "transport_tags" -> tagsTransport
            "canton_tags" -> tagsCanton
            else -> emptyList()
          }
      _uiState.value =
          _uiState.value.copy(
              selectedTags =
                  _uiState.value.selectedTags.filter { it.name !in tagList } +
                      _uiState.value.tempSelectedTags.map { Tag(it) })
    }

    _uiState.value = _uiState.value.copy(showModal = false, currentField = "", modalError = null)
    saveProfile(username)
  }

  fun saveProfile(username: String) {
    viewModelScope.launch {
      val state = _uiState.value
      val emailError =
          when {
            state.email.isEmpty() -> "Email cannot be empty"
            !state.email.contains("@") -> "Invalid email format"
            else -> null
          }
      val passwordError =
          when {
            state.password.isNotEmpty() && state.password.length < 6 ->
                "Password must be at least 6 characters"
            else -> null
          }
      val firstNameError = if (state.firstName.isEmpty()) "First name cannot be empty" else null
      val lastNameError = if (state.lastName.isEmpty()) "Last name cannot be empty" else null
      val descriptionError = if (state.description.length > 200) "Description too long" else null
      val dayError =
          when {
            state.day.isEmpty() -> "Day cannot be empty"
            state.day.toIntOrNull()?.let { it !in 1..31 } == true -> "Invalid day"
            else -> null
          }
      val monthError =
          when {
            state.month.isEmpty() -> "Month cannot be empty"
            state.month.toIntOrNull()?.let { it !in 1..12 } == true -> "Invalid month"
            else -> null
          }
      val yearError =
          when {
            state.year.isEmpty() -> "Year cannot be empty"
            state.year.toIntOrNull()?.let { it !in 1900..LocalDate.now().year } == true ->
                "Invalid year"
            else -> null
          }

      if (emailError != null ||
          passwordError != null ||
          firstNameError != null ||
          lastNameError != null ||
          descriptionError != null ||
          dayError != null ||
          monthError != null ||
          yearError != null) {
        _uiState.value =
            _uiState.value.copy(
                emailError = emailError,
                passwordError = passwordError,
                firstNameError = firstNameError,
                lastNameError = lastNameError,
                descriptionError = descriptionError,
                dayError = dayError,
                monthError = monthError,
                yearError = yearError)
        return@launch
      }

      try {
        val updatedProfile =
            UserProfile(
                username = username,
                firstName = state.firstName,
                lastName = state.lastName,
                country = state.country,
                description = state.description,
                dateOfBirth =
                    LocalDate.of(state.year.toInt(), state.month.toInt(), state.day.toInt()),
                tags = state.selectedTags)
        userRepository.repository.updateUser(username, updatedProfile)

        if (state.email != FirebaseAuth.getInstance().currentUser?.email) {
          FirebaseAuth.getInstance().currentUser?.updateEmail(state.email)?.addOnFailureListener { e
            ->
            _uiState.value = _uiState.value.copy(errorMsg = "Failed to update email: ${e.message}")
          }
        }
        if (state.password.isNotEmpty()) {
          FirebaseAuth.getInstance()
              .currentUser
              ?.updatePassword(state.password)
              ?.addOnFailureListener { e ->
                _uiState.value =
                    _uiState.value.copy(errorMsg = "Failed to update password: ${e.message}")
              }
        }
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(errorMsg = "Failed to save profile: ${e.message}")
      }
    }
  }
}
