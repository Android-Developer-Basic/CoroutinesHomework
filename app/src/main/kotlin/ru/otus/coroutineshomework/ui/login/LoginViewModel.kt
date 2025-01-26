package ru.otus.coroutineshomework.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.otus.coroutineshomework.ui.login.data.Credentials

class LoginViewModel : ViewModel() {

    private val _state = MutableStateFlow<LoginViewState>(LoginViewState.Login())
    val state: StateFlow<LoginViewState> = _state
    private val loginApi = LoginApi()

    /**
     * Login to the network
     * @param name user name
     * @param password user password
     */
    fun login(name: String, password: String) {
        viewModelScope.launch {
            loginFlow(Credentials(name, password)).collect {
                _state.value = it
            }
        }
    }

    private fun loginFlow(creds: Credentials): Flow<LoginViewState> = flow {
        emit(LoginViewState.LoggingIn)
        try {
            val user = withContext(Dispatchers.IO) {
                return@withContext loginApi.login(creds)
            }
            emit(LoginViewState.Content(user))
        } catch (e: Exception) {
            emit(LoginViewState.Login(e))
        }
    }

    /**
     * Logout from the network
     */
    fun logout() {
        _state.value = LoginViewState.LoggingOut

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    return@withContext loginApi.logout()
                }
                _state.value = LoginViewState.Login()
            } catch (e: Exception) {
                _state.value = LoginViewState.Login(e)
            }
        }

    }
}
