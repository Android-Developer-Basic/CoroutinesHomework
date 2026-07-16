package ru.otus.coroutineshomework.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.otus.coroutineshomework.ui.login.data.Credentials

class LoginViewModel : ViewModel() {

    private val stateFlow = MutableStateFlow<LoginViewState>(LoginViewState.Login())
    val state: StateFlow<LoginViewState> = stateFlow.asStateFlow()

    private fun loginFlow(name: String, password: String): Flow<LoginViewState> = flow {
        emit(LoginViewState.LoggingIn)

        val user = withContext(Dispatchers.IO) {
            LoginApi().login(Credentials(name, password))
        }

        emit(LoginViewState.Content(user))
    }.catch { e ->
        emit(LoginViewState.Login(e as Exception?))
    }

    /**
     * Login to the network
     * @param name user name
     * @param password user password
     */
    fun login(name: String, password: String) {
        viewModelScope.launch {
            loginFlow(name,password).collect { state ->
                stateFlow.value = state
            }
        }
    }

    /**
     * Logout from the network
     */
    private fun logoutFlow(): Flow<LoginViewState> = flow {
        emit( LoginViewState.LoggingOut)
        withContext(Dispatchers.IO) {
           LoginApi().logout()
        }
        emit(LoginViewState.Login(null))
    }
    fun logout() = viewModelScope.launch {
        logoutFlow().collect { state ->
            stateFlow.value = state
        }
    }

}
