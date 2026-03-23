package com.example.surusuygulamakotlin.model

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ReportApi {

    @Multipart
    @POST("upload-report.php")
    suspend fun uploadReport(
        @Part video: MultipartBody.Part,
        @Part("plates_json") platesJson: RequestBody,
        @Part("client_sent_at") clientSentAt: RequestBody,

        @Part("device_lat") deviceLat: RequestBody?,
        @Part("device_lng") deviceLng: RequestBody?,
        @Part("device_acc") deviceAcc: RequestBody?,
        @Part("device_loc_at") deviceLocAt: RequestBody?
    ): ApiResp<UploadData>

}
