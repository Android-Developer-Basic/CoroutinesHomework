package ru.otus.coroutineshomework.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.otus.coroutineshomework.ui.login.data.Credentials

class LoginViewModel : ViewModel() {
    private val api = LoginApi()
    private val _state = MutableStateFlow<LoginViewState>(LoginViewState.Login())
    val state: StateFlow<LoginViewState> = _state

    /**
     * Login to the network
     * @param name user name
     * @param password user password
     */
    fun login(name: String, password: String) {
        // TODO: Implement login
        loginFlow(name, password)
            .onEach { _state.value = it}
            .launchIn(viewModelScope)
    }

    /**
     * Logout from the network
     */
    fun logout() {
        // TODO: Implement logout
        logoutFlow()
            .onEach { _state.value = it}
            .launchIn(viewModelScope)
    }

    private fun loginFlow(name: String, password: String): Flow<LoginViewState> = flow {
        emit(LoginViewState.LoggingIn)
        val user = api.login(Credentials(name, password))
        emit(LoginViewState.Content(user))
    }.catch { e ->
        emit(LoginViewState.Login(e as Exception?))
    }.flowOn(Dispatchers.IO)

    private fun logoutFlow(): Flow<LoginViewState> = flow {
        emit(LoginViewState.LoggingOut)
        api.logout()
        emit(LoginViewState.Login())
    }.flowOn(Dispatchers.IO)
}
