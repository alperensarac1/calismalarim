package com.example.pdfconverterjetpack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pdfconverterjetpack.data.model.JobItem
import com.example.pdfconverterjetpack.data.repository.PdfRepository
import com.example.pdfconverterjetpack.ui.state.MainUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel : ViewModel() {

    private val repository = PdfRepository()

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _jobList = MutableStateFlow<List<JobItem>>(emptyList())
    val jobList: StateFlow<List<JobItem>> = _jobList.asStateFlow()

    // Aynı anda birden fazla polling dönmesin diye tutuyoruz
    private var pollingJob: Job? = null

    /**
     * Tek dosya ile job oluşturur.
     * Örnek:
     * - jpg_to_pdf
     * - pdf_to_word
     * - word_to_pdf
     */
    fun createSingleFileJob(
        userId: Int,
        jobType: String,
        file: File
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                message = "Dosya yükleniyor ve job oluşturuluyor...",
                errorText = null,
                resultFileUrl = null
            )

            val result = repository.createSingleFileJob(
                userId = userId,
                jobType = jobType,
                file = file
            )

            result.onSuccess { response ->
                val jobId = response.job_id

                if (response.success && jobId != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentJobId = jobId,
                        message = response.message ?: "Job oluşturuldu"
                    )

                    startPolling(jobId)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorText = response.message ?: "Job oluşturulamadı"
                    )
                }
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorText = e.message ?: "Bilinmeyen hata"
                )
            }
        }
    }

    /**
     * Çoklu dosya ile job oluşturur.
     * Şu an pdf_merge için kullanacağız.
     */
    fun createMultiFileJob(
        userId: Int,
        jobType: String,
        files: List<File>
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                message = "Dosyalar yükleniyor ve merge job oluşturuluyor...",
                errorText = null,
                resultFileUrl = null
            )

            val result = repository.createMultiFileJob(
                userId = userId,
                jobType = jobType,
                files = files
            )

            result.onSuccess { response ->
                val jobId = response.job_id

                if (response.success && jobId != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentJobId = jobId,
                        message = response.message ?: "Merge job oluşturuldu"
                    )

                    startPolling(jobId)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorText = response.message ?: "Job oluşturulamadı"
                    )
                }
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorText = e.message ?: "Bilinmeyen hata"
                )
            }
        }
    }

    /**
     * Job tamamlanana kadar backend'i belirli aralıklarla sorgular.
     */
    fun startPolling(jobId: Int) {
        pollingJob?.cancel()

        pollingJob = viewModelScope.launch {
            while (true) {
                val result = repository.getJobStatus(jobId)

                result.onSuccess { statusResponse ->
                    val status = statusResponse.status

                    _uiState.value = _uiState.value.copy(
                        currentJobId = statusResponse.job_id,
                        currentJobStatus = status,
                        message = "Job durumu: $status",
                        resultFileUrl = statusResponse.result_file_url,
                        errorText = statusResponse.error_message
                    )

                    if (status == "done" || status == "failed") {
                        return@launch
                    }
                }.onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        errorText = e.message ?: "Durum sorgulama hatası"
                    )
                }

                delay(3000)
            }
        }
    }

    /**
     * Kullanıcının geçmiş işlemlerini yükler.
     */
    fun loadJobs(userId: Int) {
        viewModelScope.launch {
            val result = repository.listJobs(userId)

            result.onSuccess { response ->
                if (response.success) {
                    _jobList.value = response.jobs ?: emptyList()
                } else {
                    _jobList.value = emptyList()
                }
            }.onFailure {
                _jobList.value = emptyList()
            }
        }
    }

    /**
     * Ekrandaki hata/mesaj temizliği için yardımcı fonksiyon.
     */
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(
            message = null,
            errorText = null
        )
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}