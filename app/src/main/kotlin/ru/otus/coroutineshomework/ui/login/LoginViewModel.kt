package ru.otus.coroutineshomework.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
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

    private val loginApi = LoginApi()

    private val stateFlow = MutableStateFlow<LoginViewState>(LoginViewState.Login())
    val state: StateFlow<LoginViewState> = stateFlow.asStateFlow()

    /**
     * Login to the network
     * @param name user name
     * @param password user password
     */
    fun login(name: String, password: String) {
        viewModelScope.launch {
            loginFlow(name, password).collect { state ->
                stateFlow.value = state
            }
        }
    }

    private fun loginFlow(name: String, password: String): Flow<LoginViewState> = flow {
        emit(LoginViewState.LoggingIn)

        val result = try {
            withContext(Dispatchers.IO) {
                val credentials = Credentials(name, password)
                LoginViewState.Content(loginApi.login(credentials))
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            LoginViewState.Login(exception)
        }

        emit(result)
    }

    /**
     * Logout from the network
     */
    fun logout() {
        viewModelScope.launch {
            stateFlow.value = LoginViewState.LoggingOut

            try {
                withContext(Dispatchers.IO) {
                    loginApi.logout()
                }
                stateFlow.value = LoginViewState.Login()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                stateFlow.value = LoginViewState.Login(exception)
            }
        }
    }
}
