package ru.otus.coroutineshomework.ui.timer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import ru.otus.coroutineshomework.databinding.FragmentTimerBinding
import java.time.Instant
import java.util.Locale
import kotlin.properties.Delegates
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class TimerFragment : Fragment() {

    private var _binding: FragmentTimerBinding? = null
    private val binding get() = _binding!!

    private val timeFlow = MutableStateFlow(Duration.ZERO)

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
            lifecycleScope.launch {
                timeFlow.collect {
                    time.text = this@TimerFragment.timeFlow.value.toDisplayString()
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

    private lateinit var timerStartedAt: Instant

    private fun getTimeDuration(): Duration {
        return (Instant.now().toEpochMilli() - timerStartedAt.toEpochMilli()).toDuration(
            DurationUnit.MILLISECONDS
        )
    }

    private fun doTimerLoops() =
        flow {
            while (started) {
                emit(getTimeDuration())
                delay(16) // 1/60 Hz
            }
        }

    private fun startTimer() {
        CoroutineScope(Dispatchers.Main).launch {
            doTimerLoops().collect { duration -> timeFlow.value = duration }
        }
        timerStartedAt = Instant.now()
    }

    private fun stopTimer() {
        timeFlow.value = getTimeDuration()
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
            this.inWholeSeconds.toInt(),
            this.inWholeMilliseconds.toInt().mod(1000)
        )
    }
}