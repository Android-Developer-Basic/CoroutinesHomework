package ru.otus.coroutineshomework.ui.timer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ru.otus.coroutineshomework.databinding.FragmentTimerBinding
import java.util.Locale
import kotlin.properties.Delegates
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds

class TimerFragment : Fragment() {

    private var _binding: FragmentTimerBinding? = null
    private val binding get() = _binding!!

    private var timeFlow: MutableSharedFlow<Duration> = MutableSharedFlow(replay = 1)
    private var timerJob: Job? = null
    private val time: Duration get() = timeFlow.replayCache.firstOrNull() ?: ZERO


    private var started by Delegates.observable(false) { _, _, newValue ->
        setButtonsState(newValue)
        if (newValue) {
            startTimer()
        } else {
            stopTimer()
        }
    }


    private fun setButtonsState(started: Boolean) {
        with(binding) {
            btnStart.isEnabled = !started
            btnStop.isEnabled = started
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Подписка на timeFlow для обновления UI
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                timeFlow.collect { duration ->
                    binding.time.text = duration.toDisplayString()
                }
            }
        }
        // Восстанавливаем значение времени из Bundle или используем значение по умолчанию
        val initialTime = savedInstanceState?.getLong(TIME)?.milliseconds ?: ZERO
        timeFlow.tryEmit(initialTime)
        savedInstanceState?.let {
            started = it.getBoolean(STARTED)
        }
        setButtonsState(started)
        with(binding) {
            btnStart.setOnClickListener {
                started = true
            }
            btnStop.setOnClickListener {
                started = false
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(TIME, time.inWholeMilliseconds)
        outState.putBoolean(STARTED, started)
    }

    private fun startTimer() {
        timerJob?.cancel() // Остановить предыдущую корутину, если она запущена
        var currentTime = time
        timerJob = viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) { // Проверяем, что корутина активна
                val delta = 10L;
                delay(delta) // Интервал обновления (например, 10 миллисекунд)
                currentTime += delta.milliseconds
                timeFlow.emit(currentTime) // Обновляем значение через emit
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel() // Останавливаем корутину
        timerJob = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TIME = "time"
        private const val STARTED = "started"

        private fun Duration.toDisplayString(): String = String.format(
            Locale.getDefault(),
            "%02d:%02d.%03d",
            this.inWholeMinutes.toInt(),
            this.inWholeSeconds.toInt() % 60,
            this.inWholeMilliseconds.toInt() % 1000
        )
    }
}