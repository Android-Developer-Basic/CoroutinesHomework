package ru.otus.coroutineshomework.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.otus.coroutineshomework.ui.login.data.Credentials

class LoginViewModel : ViewModel() {

    private val _state = MutableLiveData<LoginViewState>(LoginViewState.Login())
    val state: LiveData<LoginViewState> = _state
    val loginApi = LoginApi()
    /**
     * Login to the network
     * @param name user name
     * @param password user password
     */
    fun login(name: String, password: String) {
        _state.value = LoginViewState.LoggingIn



        viewModelScope.launch {
            val credentials = Credentials(name, password)

            try {
                val user = withContext(Dispatchers.IO) {
                    loginApi.login(credentials)
                }
                _state.value = LoginViewState.Content(user)
            } catch (e: Exception){
                _state.value = LoginViewState.Login(e)
            }
        }
    }

    /**
     * Logout from the network
     */
    fun logout() {
        _state.value = LoginViewState.LoggingOut

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                loginApi.logout()
            }
            _state.value = LoginViewState.Login()
        }
    }
}
