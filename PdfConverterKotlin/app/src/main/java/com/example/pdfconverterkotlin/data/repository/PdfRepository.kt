package com.example.pdfconverterkotlin.data.repository

import com.example.pdfconverterkotlin.data.model.CreateJobResponse
import com.example.pdfconverterkotlin.data.model.JobStatusResponse
import com.example.pdfconverterkotlin.data.model.ListJobsResponse
import com.example.pdfconverterkotlin.data.remote.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class PdfRepository {

    private val api = RetrofitClient.api

    suspend fun createSingleFileJob(
        userId: Int,
        jobType: String,
        file: File
    ): Result<CreateJobResponse> {
        return try {
            val jobTypeBody = jobType.toRequestBody("text/plain".toMediaTypeOrNull())
            val userIdBody = userId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            // Dosyanın mime type'ını burada basit tuttuk.
            // İstersen sonra dosya uzantısına göre dinamik yaparız.
            val requestFile: RequestBody =
                file.asRequestBody("application/octet-stream".toMediaTypeOrNull())

            val filePart = MultipartBody.Part.createFormData(
                "file",
                file.name,
                requestFile
            )

            val response = api.createSingleFileJob(
                jobType = jobTypeBody,
                userId = userIdBody,
                file = filePart
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("createSingleFileJob başarısız: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createMultiFileJob(
        userId: Int,
        jobType: String,
        files: List<File>
    ): Result<CreateJobResponse> {
        return try {
            val jobTypeBody = jobType.toRequestBody("text/plain".toMediaTypeOrNull())
            val userIdBody = userId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            val fileParts = files.map { file ->
                val requestBody = file.asRequestBody("application/pdf".toMediaTypeOrNull())

                MultipartBody.Part.createFormData(
                    "files[]",
                    file.name,
                    requestBody
                )
            }

            val response = api.createMultiFileJob(
                jobType = jobTypeBody,
                userId = userIdBody,
                files = fileParts
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("createMultiFileJob başarısız: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getJobStatus(jobId: Int): Result<JobStatusResponse> {
        return try {
            val response = api.getJobStatus(jobId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("getJobStatus başarısız: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listJobs(userId: Int): Result<ListJobsResponse> {
        return try {
            val response = api.listJobs(userId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("listJobs başarısız: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}