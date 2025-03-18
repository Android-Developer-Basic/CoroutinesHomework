package ru.otus.coroutineshomework.ui.login

import android.util.Log
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

    private val service = LoginApi()
    /**
     * Login to the network
     * @param name user name
     * @param password user password
     */
    fun login(name: String, password: String) {
        viewModelScope.launch {
            Log.i(TAG, "Logging in $name...")
            _state.value = LoginViewState.LoggingIn
            try {
                withContext(Dispatchers.IO) {
                    val loginResponse = service.login(Credentials(name, password))
                    Log.i(TAG, "Successfully logged-in user with id: ${loginResponse.id}")
                    _state.postValue(LoginViewState.Content(loginResponse))
               }
            } catch (t: Exception) {
                Log.w(TAG, "Login error", t)
                _state.value = LoginViewState.Login(t)
            }
        }
    }

    /**
     * Logout from the network
     */
    fun logout() {
        viewModelScope.launch {
            _state.value = LoginViewState.LoggingOut
            withContext(Dispatchers.IO) {
                service.logout()
            }
            _state.value = LoginViewState.Login()
        }
    }
    companion object {
        const val TAG = "LoginViewModel"
    }
}
