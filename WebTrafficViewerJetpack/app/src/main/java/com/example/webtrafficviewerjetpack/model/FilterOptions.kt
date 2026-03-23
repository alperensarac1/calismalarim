package com.example.webtrafficviewerjetpack.model

data class FilterOptions(
    val enableFilter: Boolean = true,
    val onlyApiRequests: Boolean = false,
    val enableJsHook: Boolean = true,
    val showOnlyGet: Boolean = false,
    val showOnlyPost: Boolean = false,
    val searchQuery: String = ""
)