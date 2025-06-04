package ru.otus.coroutineshomework.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.otus.coroutineshomework.ui.login.data.Credentials

class LoginViewModel : ViewModel() {

    private val _state = MutableLiveData<LoginViewState>(LoginViewState.Login())
    val state: LiveData<LoginViewState> = _state

    /**
     * Login to the network
     * @param name user name
     * @param password user password
     */
    fun login(name: String, password: String) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                updateState(LoginViewState.LoggingIn)

                try {
                    val user = LoginApi().login(Credentials(name, password))

                    updateState(LoginViewState.Content(user))
                } catch (ex: Exception) {
                    updateState(LoginViewState.Login(ex))
                }
            }
        }
    }

    /**
     * Logout from the network
     */
    fun logout() {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                updateState(LoginViewState.LoggingOut)

                LoginApi().logout()

                updateState(LoginViewState.Login())
            }
        }
    }

    private suspend fun updateState(state: LoginViewState) {
        withContext(Dispatchers.Main) {
            _state.value = state
        }
    }
}
