package ru.otus.coroutineshomework.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.otus.coroutineshomework.ui.login.data.Credentials

class LoginViewModel : ViewModel() {

    private val _stateFlow = MutableStateFlow<LoginViewState>(LoginViewState.Login())
    val state: StateFlow<LoginViewState> = _stateFlow // Экспортируем как StateFlow

    private val loginApi = LoginApi() // Экземпляр LoginApi для выполнения операций

    /**
     * Login to the network
     * @param name user name
     * @param password user password
     */
    fun login(name: String, password: String) {
        viewModelScope.launch {
            // Сначала устанавливаем состояние "вход в процесс"
            _stateFlow.emit(LoginViewState.LoggingIn)

            try {
                // Выполняем сетевой запрос в фоновом потоке
                val user = withContext(Dispatchers.IO) {
                    loginApi.login(Credentials(name, password)) // Запрос на вход
                }

                // Успешный вход
                _stateFlow.emit(LoginViewState.Content(user))
            } catch (e: Exception) {
                // Ошибка входа
                _stateFlow.emit(LoginViewState.Login(e))
            }
        }
    }

    /**
     * Logout from the network
     */
    fun logout() {
        viewModelScope.launch {
            _stateFlow.emit(LoginViewState.LoggingOut) // Состояние "выход в процессе"

            try {
                // Выполняем сетевой запрос на выход
                withContext(Dispatchers.IO) {
                    loginApi.logout() // Запрос на выход
                }

                // Успешный выход
                _stateFlow.emit(LoginViewState.Login()) // Возвращаемся в начальное состояние
            } catch (e: Exception) {
                // Ошибка выхода
                val currentState = _stateFlow.value
                _stateFlow.emit(
                    when (currentState) {
                        is LoginViewState.Content -> currentState // Если был залогинен, оставляем Content
                        else -> LoginViewState.Login(e) // В противном случае — возвращаемся к Login с ошибкой
                    }
                )
            }
        }
    }
}
