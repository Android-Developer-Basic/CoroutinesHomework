package ru.otus.coroutineshomework.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.otus.coroutineshomework.ui.login.data.Credentials
import ru.otus.coroutineshomework.ui.login.data.User

class LoginViewModel : ViewModel() {

    private val _state = MutableLiveData<LoginViewState>(LoginViewState.Login())
    val state: LiveData<LoginViewState> = _state
    private val loginApi = LoginApi()
    private lateinit var user: User

    fun login(name: String, password: String) {
        _state.value = LoginViewState.LoggingIn
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    user = loginApi.login(Credentials(name, password))
                }
                _state.value = LoginViewState.Content(user)
            } catch (e: IllegalArgumentException) {
                withContext(Dispatchers.Main) {
                    _state.value = LoginViewState.Login(e)
                }
            }
        }
    }

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
