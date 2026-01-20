package com.example.eticaretjetpack.util


sealed class UiResult<out T> {
    object Idle : UiResult<Nothing>()
    object Loading : UiResult<Nothing>()
    data class Success<T>(val data: T) : UiResult<T>()
    data class Error(val message: String) : UiResult<Nothing>()
}