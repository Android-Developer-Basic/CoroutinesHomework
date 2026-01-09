package ru.otus.coroutineshomework.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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

            _state.value = LoginViewState.LoggingIn

            try {
                val user = withContext(Dispatchers.IO) {
                    LoginApi().login(Credentials(name, password))
                }
                delay(3000L)
                _state.value = LoginViewState.Content(user = user)
            } catch (e: Exception) {
                _state.value = LoginViewState.Login(error = e)
            }
        }
    }

    /**
     * Logout from the network
     */
    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            LoginApi().logout()
            withContext(Dispatchers.Main) {
                _state.value = LoginViewState.LoggingOut
                delay(3000L)
                _state.value = LoginViewState.Login()
            }
        }
    }
}
