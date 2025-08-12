package com.example.sozlukjetpack.util

data class EntriesUiState(
    val loading: Boolean = false,
    val error: String? = null
)
// ---- Ortak küçük bileşenler ----


data class AddEntryUiState(
    val loading: Boolean = false,
    val error: String? = null
)
data class EntryDetailUiState(
    val loadingEntry: Boolean = false,
    val loadingComments: Boolean = false,
    val posting: Boolean = false,
    val error: String? = null
)