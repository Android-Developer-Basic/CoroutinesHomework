package ru.otus.coroutineshomework.ui.network

sealed class ResultNetwork<out T> {
    data class Success<T>(val value: T) : ResultNetwork<T>()
    data class Failure(val exception: Throwable) : ResultNetwork<Nothing>()
}