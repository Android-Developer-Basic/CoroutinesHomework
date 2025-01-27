package ru.otus.coroutineshomework.ui.network

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.SelectClause1
import kotlinx.coroutines.withContext
import kotlin.random.Random

class NetworkViewModel : ViewModel() {

    private val _running: MutableLiveData<Boolean> = MutableLiveData(false)
    val running: LiveData<Boolean> = _running

    private val _result: MutableLiveData<Long?> = MutableLiveData()
    val result: LiveData<Long?> = _result

    fun startTest(numberOfThreads: Int) {
        _result.postValue(0L)
        val deferredList = ArrayList<Deferred<Result<Long>>>()

        viewModelScope.launch {
            _running.postValue(true)

            repeat(numberOfThreads) {
                deferredList.add(async { emulateBlockingNetworkRequest() })
            }
            val middleValue =
                deferredList.awaitAll().mapNotNull { it.getOrNull() }.average().toLong()

            _running.postValue(false)
            _result.postValue(middleValue)
        }
    }

    private companion object {
        private const val TAG = "NetworkViewModel"
        private const val MIN_DELAY = 1000L
        private const val MAX_DELAY = 5000L

        suspend fun emulateBlockingNetworkRequest(): Result<Long> = withContext(Dispatchers.IO) {
            val delay = Random.nextLong(MIN_DELAY, MAX_DELAY)
            var progress = 0L
            while (isActive && progress < delay) {
                if (5 == Random.nextInt(1, 20)) {
                    log("emulateBlockingNetworkRequest: Network request failed")
                    return@withContext Result.failure(Exception("Network request failed"))
                }
                log("emulateBlockingNetworkRequest: progress = $progress, delay = $delay")
                Thread.sleep(100)
                progress += 100
            }
            log("emulateBlockingNetworkRequest: Network request completed")
            return@withContext Result.success(delay)
        }

        private fun log(message: String) {
            Log.i(TAG, "Thread: ${Thread.currentThread().name}. $message")
        }
    }
}

