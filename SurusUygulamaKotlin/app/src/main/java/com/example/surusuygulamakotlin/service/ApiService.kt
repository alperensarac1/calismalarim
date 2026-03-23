package com.example.surusuygulamakotlin.service

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @Multipart
    @POST("upload_report.php")
    suspend fun uploadReport(
        @Part video: MultipartBody.Part,
        @Part("plates_json") platesJson: RequestBody,
        @Part("client_sent_at") clientSentAt: RequestBody
    ): Response<UploadResp>
}


data class UploadResp(
    val ok: Boolean,
    val message: String? = null,
    val data: UploadData? = null
)

data class UploadData(
    val video_url: String? = null
)
