package ru.otus.coroutineshomework.ui.timer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ru.otus.coroutineshomework.databinding.FragmentTimerBinding
import java.util.Locale
import kotlin.properties.Delegates
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class TimerFragment : Fragment() {

    private val timerIncrement = 10.milliseconds
    private var _binding: FragmentTimerBinding? = null
    private val binding get() = _binding!!

    private var timeFlow = MutableStateFlow<Duration>(0.milliseconds)

    private var started by Delegates.observable(false) { _, _, newValue ->
        setButtonsState(newValue)
        if (newValue) {
            startTimer()
        } else {
            stopTimer()
        }
    }

    private var job: Job? = null
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
        savedInstanceState?.let {
            val initialTime = it.getLong(TIME).milliseconds
            timeFlow = MutableStateFlow<Duration>(initialTime)
            started = it.getBoolean(STARTED)
        }
        setButtonsState(started)

        with(binding) {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    timeFlow.collect {
                        time.text = it.toDisplayString()
                    }
                }
            }

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
        outState.putLong(TIME, timeFlow.value.inWholeMilliseconds)
        outState.putBoolean(STARTED, started)
    }

    private fun startTimer() {
        resetTimer()

        job = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            while (started) {
                timeFlow.emit(timeFlow.value + timerIncrement)
                delay(timerIncrement)
            }
        }
    }

    private fun stopTimer() {
        job?.cancel()
    }

    private fun resetTimer() {
        if (job != null) {
            job?.cancel()
        }

        timeFlow.value = 0.milliseconds
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TIME = "time"
        private const val STARTED = "started"

        private fun Duration.toDisplayString(): String {
            val minutes = this.inWholeMinutes.toInt()
            val seconds = (this.inWholeSeconds % 60).toInt()
            val millis = (this.inWholeMilliseconds % 1000).toInt()

            return String.format(
                Locale.getDefault(),
                "%02d:%02d.%03d",
                minutes, seconds, millis
            )
        }
    }
}