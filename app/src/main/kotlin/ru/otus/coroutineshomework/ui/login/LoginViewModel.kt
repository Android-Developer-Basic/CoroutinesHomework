package ru.otus.coroutineshomework.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.otus.coroutineshomework.ui.login.data.Credentials

class LoginViewModel : ViewModel() {

    private val _state = MutableStateFlow<LoginViewState>(LoginViewState.Login())
    val state: StateFlow<LoginViewState> = _state.asStateFlow()

    private fun loginFlow(credentials: Credentials): Flow<LoginViewState> =
        flow {
            val result = withContext(Dispatchers.IO) {
                runCatching { LoginApi().login(credentials) }
            }
            result.onSuccess { emit(LoginViewState.Content(it)) }
            result.onFailure { emit(LoginViewState.Login(it as Exception)) }
        }

    /**
     * Login to the network
     * @param name user name
     * @param password user password
     */
    fun login(name: String, password: String) {
        _state.value = LoginViewState.LoggingIn
        viewModelScope.launch {
            loginFlow(Credentials(name, password)).collect {
                _state.value = it
            }
        }
    }

    /**
     * Logout from the network
     */
    fun logout() {
        _state.value = LoginViewState.LoggingOut
        viewModelScope.launch(Dispatchers.IO) {
            LoginApi().logout()
        }
        _state.value = LoginViewState.Login()
    }
}
