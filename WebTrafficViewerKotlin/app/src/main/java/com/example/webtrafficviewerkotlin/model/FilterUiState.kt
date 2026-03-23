package com.example.webtrafficviewerkotlin.model

data class FilterUiState(
    val enableFilter: Boolean = true,
    val onlyApiRequests: Boolean = false,
    val enableJsHook: Boolean = true,
    val showOnlyGet: Boolean = false,
    val showOnlyPost: Boolean = false
)