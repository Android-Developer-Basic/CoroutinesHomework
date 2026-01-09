package ru.otus.coroutineshomework.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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

    fun loginFlow(name: String, password: String): Flow<LoginViewState> = flow {
        emit(LoginViewState.LoggingIn)
        try {
            val user = withContext(Dispatchers.IO) {
                LoginApi().login(Credentials(name, password))
            }
            delay(3000L)
            emit(LoginViewState.Content(user = user))
        } catch (e: Exception) {
            emit(LoginViewState.Login(error = e))
        }
    }

    /**
     * Login to the network
     * @param name user name
     * @param password user password
     */
    fun login(name: String, password: String) {
        viewModelScope.launch {
            loginFlow(name = name, password = password).collect { viewState ->
                _state.value = viewState
            }
        }
    }

    /**
     * Logout from the network
     */
    fun logout() {
        viewModelScope.launch {
            _state.value = LoginViewState.LoggingOut
            withContext(Dispatchers.IO) {
                LoginApi().logout()
            }
            delay(3000L)
            _state.value = LoginViewState.Login()
        }
    }
}
