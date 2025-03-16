package ru.otus.coroutineshomework.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.otus.coroutineshomework.ui.login.data.Credentials

class LoginViewModel(private val loginApi: LoginApi = LoginApi()) : ViewModel() {

    private val _state = MutableStateFlow<LoginViewState>(LoginViewState.Login())
    val state: StateFlow<LoginViewState> = _state

    /**
     * Login to the network
     * @param name user name
     * @param password user password
     */
    fun login(name: String, password: String) {
        viewModelScope.launch {
            _state.emit(LoginViewState.LoggingIn)
            try {
                val user = withContext(Dispatchers.IO){
                    loginApi.login(Credentials(name, password))
                }
                _state.emit(LoginViewState.Content(user))
            }
            catch (ex: Exception){
                _state.emit(LoginViewState.Login(error = ex))
            }
        }
    }

    /**
     * Logout from the network
     */
    fun logout() {
        viewModelScope.launch {
            _state.emit(LoginViewState.LoggingOut)
            try {
                withContext(Dispatchers.IO){
                    loginApi.logout()
                }
                _state.emit(LoginViewState.Login())
            }
            catch (ex: Exception){
                _state.emit(LoginViewState.Login(error = ex))
            }
        }
    }
}
