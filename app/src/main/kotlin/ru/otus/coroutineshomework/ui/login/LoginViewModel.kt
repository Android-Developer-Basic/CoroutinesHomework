package ru.otus.coroutineshomework.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.otus.coroutineshomework.ui.login.data.Credentials
import ru.otus.coroutineshomework.ui.login.data.User

class LoginViewModel : ViewModel() {

    private val stateFlow = MutableStateFlow<LoginViewState>(LoginViewState.Login())
    val state: StateFlow<LoginViewState> = stateFlow
    private val loginApi = LoginApi()
    private lateinit var user: User

    fun login(name: String, password: String) {
        stateFlow.value = LoginViewState.LoggingIn
        viewModelScope.launch {
            loginFlow(name, password).collect {
                stateFlow.value = it
            }
        }
    }

    fun logout() {
        stateFlow.value = LoginViewState.LoggingOut
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                loginApi.logout()
            }
            stateFlow.value = LoginViewState.Login()
        }
    }

    private fun loginFlow(name: String, password: String): Flow<LoginViewState> = flow {
        emit(LoginViewState.LoggingIn)
        try {
            user = loginApi.login(Credentials(name, password))
            emit(LoginViewState.Content(user))
        } catch (e: IllegalArgumentException) {
            emit(LoginViewState.Login(e))

        }
    }.flowOn(Dispatchers.IO)
}
