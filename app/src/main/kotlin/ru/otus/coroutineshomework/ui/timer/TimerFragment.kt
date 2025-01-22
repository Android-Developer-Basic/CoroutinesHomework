package ru.otus.coroutineshomework.ui.timer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import ru.otus.coroutineshomework.databinding.FragmentTimerBinding
import java.util.Locale
import kotlin.time.Duration

class TimerFragment : Fragment() {

    private lateinit var viewModel: TimerViewModel

    private var _binding: FragmentTimerBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = viewModels<TimerViewModel>().value
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTimerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.timeFlow.collect { duration ->
                    binding.time.text = duration.toDisplayString()
                }
            }
        }

        binding.btnStart.setOnClickListener {
            viewModel.startTimer()
        }
        binding.btnStop.setOnClickListener {
            viewModel.stopTimer()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        private fun Duration.toDisplayString(): String {
            val totalMillis = inWholeMilliseconds
            val minutes = totalMillis / 60000 % 60
            val seconds = totalMillis / 1000 % 60
            val millis = totalMillis % 1000
            return String.format(Locale.getDefault(), "%02d:%02d.%03d", minutes, seconds, millis)
        }
    }
}

