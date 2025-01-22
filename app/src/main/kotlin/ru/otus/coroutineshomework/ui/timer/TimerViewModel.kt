package ru.otus.coroutineshomework.ui.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class TimerViewModel : ViewModel() {

    private val _timeFlow = MutableStateFlow(Duration.ZERO)
    val timeFlow: StateFlow<Duration> = _timeFlow

    private var running = false
    private var timerJob: Job? = null

    fun startTimer() {
        if (!running) {
            running = true
            timerJob = viewModelScope.launch(Dispatchers.Default) {
                while (running && isActive) {
                    delay(50L)
                    _timeFlow.emit(_timeFlow.value + 50.milliseconds)
                }
            }
        }
    }

    fun stopTimer() {
        running = false
        timerJob?.cancel()
        timerJob = null
        _timeFlow.value = Duration.ZERO
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}