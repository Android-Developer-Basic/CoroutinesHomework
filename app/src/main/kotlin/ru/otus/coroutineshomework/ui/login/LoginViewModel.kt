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
    private val loginApi = LoginApi()

    private val _state = MutableLiveData<LoginViewState>(LoginViewState.Login())
    val state: LiveData<LoginViewState> = _state

    fun login(name: String, password: String) {

        _state.value = LoginViewState.LoggingIn

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = loginApi.login(Credentials(name, password))
                withContext(Dispatchers.Main) {
                    _state.value = LoginViewState.Content(user)
                }
            } catch (exception: Exception) {
                withContext(Dispatchers.Main) {
                    _state.value = LoginViewState.Login(exception)
                }
            }
        }
    }

    fun logout() {
        _state.value = LoginViewState.LoggingOut

        viewModelScope.launch(Dispatchers.IO) {
            try {
                loginApi.logout()
                withContext(Dispatchers.Main) {
                    _state.value = LoginViewState.Login()
                }
            } catch (exception: Exception) {
                withContext(Dispatchers.Main) {
                    _state.value = LoginViewState.Login(exception)
                }
            }
        }
    }
}
