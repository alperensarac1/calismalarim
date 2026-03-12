package com.example.yardimuygulamajetpack.vm

sealed class UiState<out T> {
    data object Idle : UiState<Nothing>()
    data object Loading : UiState<Nothing>()
    data class Error(val message: String) : UiState<Nothing>()
    data class Data<T>(val value: T) : UiState<T>()
}