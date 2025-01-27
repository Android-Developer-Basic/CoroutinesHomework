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
    private val loginApi = LoginApi() // Экземпляр LoginApi для выполнения операций

    /**
     * Login to the network
     * @param name user name
     * @param password user password
     */
    fun login(name: String, password: String) {
        viewModelScope.launch {
            _state.value = LoginViewState.LoggingIn // Устанавливаем состояние загрузки

            try {
                val user = withContext(Dispatchers.IO) {
                    loginApi.login(Credentials(name, password)) // Выполняем сетевой запрос
                }
                _state.value = LoginViewState.Content(user) // Успешный вход
            } catch (e: Exception) {
                _state.value = LoginViewState.Login(e) // Ошибка входа
            }
        }
    }

    /**
     * Logout from the network
     */
    fun logout() {
        viewModelScope.launch {
            val currentState = _state.value
            _state.value = LoginViewState.LoggingOut // Устанавливаем состояние загрузки

            try {
                withContext(Dispatchers.IO) {
                    loginApi.logout() // Выполняем сетевой запрос
                }
                _state.value = LoginViewState.Login() // Успешный выход
            } catch (e: Exception) {
                // В случае ошибки выхода возвращаемся к предыдущему состоянию
                _state.value = when (currentState) {
                    is LoginViewState.Content -> currentState // Если пользователь был залогинен, оставляем Content
                    else -> LoginViewState.Login(e) // Если нет, возвращаемся в Login с ошибкой
                }
            }
        }
    }
}
