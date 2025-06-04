package ru.otus.coroutineshomework.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
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

    private val stateFlow = MutableStateFlow<LoginViewState>(LoginViewState.Login())
    val state: StateFlow<LoginViewState> = stateFlow.asStateFlow()

    fun login(name: String, password: String) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                loginFlow(name, password).collect {
                    updateState(it)
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                updateState(LoginViewState.LoggingOut)

                LoginApi().logout()

                updateState(LoginViewState.Login())
            }
        }
    }

    private fun loginFlow(name: String, password: String): Flow<LoginViewState> = flow {
        emit(LoginViewState.LoggingIn)

        try {
            val user = LoginApi().login(Credentials(name, password))

            emit(LoginViewState.Content(user))
        } catch (ex: Exception) {
            emit(LoginViewState.Login(ex))
        }
    }

    private suspend fun updateState(state: LoginViewState) {
        withContext(Dispatchers.Main) {
            stateFlow.value = state
        }
    }
}
