package ru.otus.coroutineshomework.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.otus.coroutineshomework.ui.login.data.Credentials
import ru.otus.coroutineshomework.ui.login.data.User

class LoginViewModel : ViewModel() {

    private val _state = MutableStateFlow<LoginViewState>(LoginViewState.Login())
    val state: StateFlow<LoginViewState> = _state
    private val loginAIp = LoginApi()

    /**
     * Login to the network
     * @param name user name
     * @param password user password
     */
    fun login(name: String, password: String) {

        viewModelScope.launch(Dispatchers.IO) {
            _state.value = LoginViewState.LoggingIn

            try {
                val userNetworkRequest = loginAIp.login(Credentials(name, password))
                _state.value = LoginViewState.Content(userNetworkRequest)
            } catch (e: Exception) {
                _state.value = LoginViewState.Login(e)
            }
        }
    }

    /**
     * Logout from the network
     */
    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = LoginViewState.LoggingOut
            loginAIp.logout()
            _state.value = LoginViewState.Login()
        }
    }
}
