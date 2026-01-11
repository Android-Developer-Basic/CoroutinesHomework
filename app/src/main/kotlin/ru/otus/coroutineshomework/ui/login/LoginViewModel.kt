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
    private lateinit var userCred: User

    /**
     * Login to the network
     * @param name user name
     * @param password user password
     */
    fun login(name: String, password: String) {

        //_state.value = LoginViewState.LoggingIn
        stateFlow.value = LoginViewState.LoggingIn
        viewModelScope.launch {
            loginFlow(name, password).collect {
                stateFlow.value = it
            }
        }
    }

    /**
     * Logout from the network
     */
    fun logout() {

        //_state.value = LoginViewState.LoggingOut
        stateFlow.value = LoginViewState.LoggingOut
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                loginApi.logout()
            }
            //_state.value = LoginViewState.Login()
            stateFlow.value = LoginViewState.Login()
        }
    }

    private fun loginFlow(name: String, password: String): Flow<LoginViewState> = flow {
        emit(LoginViewState.LoggingIn)
        try {
            userCred = loginApi.login(Credentials(name, password))
            emit(LoginViewState.Content(userCred))
        } catch (e: IllegalArgumentException) {
            emit(LoginViewState.Login(e))

        }
    }.flowOn(Dispatchers.IO)
}

