package com.example.pdfconverterkotlin.data.remote


import com.example.pdfconverterkotlin.data.model.CreateJobResponse
import com.example.pdfconverterkotlin.data.model.JobStatusResponse
import com.example.pdfconverterkotlin.data.model.ListJobsResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface PdfApiService {

    // Tek dosyalı job:
    // pdf_to_word, word_to_pdf, jpg_to_pdf gibi
    @Multipart
    @POST("create_job.php")
    suspend fun createSingleFileJob(
        @Part("job_type") jobType: RequestBody,
        @Part("user_id") userId: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<CreateJobResponse>

    // Çoklu dosyalı job:
    // pdf_merge gibi işlemler için
    @Multipart
    @POST("create_job.php")
    suspend fun createMultiFileJob(
        @Part("job_type") jobType: RequestBody,
        @Part("user_id") userId: RequestBody,
        @Part files: List<MultipartBody.Part>
    ): Response<CreateJobResponse>

    // Job durumunu öğren
    @GET("job_status.php")
    suspend fun getJobStatus(
        @Query("job_id") jobId: Int
    ): Response<JobStatusResponse>

    // Kullanıcıya ait job listesi
    @GET("list_jobs.php")
    suspend fun listJobs(
        @Query("user_id") userId: Int
    ): Response<ListJobsResponse>
}