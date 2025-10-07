package com.android.universe.ui.authentification

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SignInScreenUIState(
    val errorMsg: String? = null,
    val isLoading: Boolean = false,
    val isLoginSuccess: Boolean = false
)

class SignInScreenViewModel(): ViewModel() {

}