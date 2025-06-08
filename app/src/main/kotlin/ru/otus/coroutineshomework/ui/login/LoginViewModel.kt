package ru.otus.coroutineshomework.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.otus.coroutineshomework.ui.login.data.Credentials

class LoginViewModel : ViewModel() {

    private val _stateFlow = MutableStateFlow<LoginViewState>(LoginViewState.Login())
    val stateFlow: StateFlow<LoginViewState> = _stateFlow.asStateFlow()
    val loginApi = LoginApi()

    fun login(name: String, password: String){
        viewModelScope.launch {
            loginFlow(name, password).collect {
                _stateFlow.emit(it)
            }
        }
    }

    fun logout(){
        viewModelScope.launch {
            logoutFlow().collect {
                _stateFlow.emit(it)
            }
        }
    }

    fun loginFlow(name: String, password: String) = flow<LoginViewState> {
        emit(LoginViewState.LoggingIn)
        val credentials = Credentials(name, password)
        try {
            val user = loginApi.login(credentials)
            emit(LoginViewState.Content(user))
        } catch (e: Exception){
            emit(LoginViewState.Login(e))
        }
    }.flowOn(Dispatchers.IO)

    fun logoutFlow() = flow<LoginViewState> {
        emit(LoginViewState.LoggingOut)
        loginApi.logout()
        emit(LoginViewState.Login())
    }.flowOn(Dispatchers.IO)
}
