package com.example.pdfconverterkotlin.ui


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pdfconverterkotlin.data.repository.PdfRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import androidx.lifecycle.viewModelScope
import com.example.pdfconverterkotlin.data.model.JobItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val repository = PdfRepository()

    private val _uiState = MutableLiveData(MainUiState())
    val uiState: LiveData<MainUiState> = _uiState

    // Geçmiş job listesi burada tutulacak
    private val _jobList = MutableLiveData<List<JobItem>>(emptyList())
    val jobList: LiveData<List<JobItem>> = _jobList

    private var pollingJob: Job? = null

    fun createSingleFileJob(
        userId: Int,
        jobType: String,
        file: File
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value?.copy(
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
                    _uiState.value = _uiState.value?.copy(
                        isLoading = false,
                        currentJobId = jobId,
                        message = response.message ?: "Job oluşturuldu"
                    )

                    startPolling(jobId)
                } else {
                    _uiState.value = _uiState.value?.copy(
                        isLoading = false,
                        errorText = response.message ?: "Job oluşturulamadı"
                    )
                }
            }.onFailure { e ->
                _uiState.value = _uiState.value?.copy(
                    isLoading = false,
                    errorText = e.message ?: "Bilinmeyen hata"
                )
            }
        }
    }

    fun createMultiFileJob(
        userId: Int,
        jobType: String,
        files: List<File>
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value?.copy(
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
                    _uiState.value = _uiState.value?.copy(
                        isLoading = false,
                        currentJobId = jobId,
                        message = response.message ?: "Merge job oluşturuldu"
                    )

                    startPolling(jobId)
                } else {
                    _uiState.value = _uiState.value?.copy(
                        isLoading = false,
                        errorText = response.message ?: "Job oluşturulamadı"
                    )
                }
            }.onFailure { e ->
                _uiState.value = _uiState.value?.copy(
                    isLoading = false,
                    errorText = e.message ?: "Bilinmeyen hata"
                )
            }
        }
    }

    fun startPolling(jobId: Int) {
        pollingJob?.cancel()

        pollingJob = viewModelScope.launch {
            while (true) {
                val result = repository.getJobStatus(jobId)

                result.onSuccess { statusResponse ->
                    val status = statusResponse.status

                    _uiState.postValue(
                        _uiState.value?.copy(
                            currentJobId = statusResponse.job_id,
                            currentJobStatus = status,
                            message = "Job durumu: $status",
                            resultFileUrl = statusResponse.result_file_url,
                            errorText = statusResponse.error_message
                        )
                    )

                    if (status == "done" || status == "failed") {
                        return@launch
                    }
                }.onFailure { e ->
                    _uiState.postValue(
                        _uiState.value?.copy(
                            errorText = e.message ?: "Durum sorgulama hatası"
                        )
                    )
                }

                delay(3000)
            }
        }
    }

    /**
     * Kullanıcının geçmiş job listesini backend'den çeker.
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

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}