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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.otus.coroutineshomework.databinding.FragmentTimerBinding
import java.util.Locale
import kotlin.properties.Delegates
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class TimerFragment : Fragment() {

    private var _binding: FragmentTimerBinding? = null
    private val binding get() = _binding!!

    private var time: Duration by Delegates.observable(Duration.ZERO) { _, _, newValue ->
        binding.time.text = newValue.toDisplayString()
    }

    private var timeFlow = MutableStateFlow(Duration.ZERO)
    private var timerJob: Job? = null

    private var timeDelay = 10.milliseconds

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
        savedInstanceState?.let {
            timeFlow.value = it.getLong(TIME).milliseconds
            started = it.getBoolean(STARTED)
        }
        setButtonsState(started)
        with(binding) {
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                     timeFlow.collect {
                        time.text = it.toDisplayString()
                    }
                }
            }
            time.text = this@TimerFragment.timeFlow.value.toDisplayString()
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
        timerJob = lifecycleScope.launch {
            while (started) {
                delay(timeDelay)
                timeFlow.emit(timeFlow.value + timeDelay )
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        stopTimer()
    }

    companion object {
        private const val TIME = "time"
        private const val STARTED = "started"

        private fun Duration.toDisplayString(): String = String.format(
            Locale.getDefault(),
            "%02d:%02d.%03d",
            this.inWholeMinutes.toInt(),
            this.inWholeSeconds.toInt(),
            this.inWholeMilliseconds.toInt()
        )
    }
}