package com.example.memesharekotlinn.service

import com.example.memesharekotlinn.model.GonderiModel
import com.example.memesharekotlinn.model.ImageUploadRequest
import com.example.memesharekotlinn.model.KullaniciResponse
import com.example.memesharekotlinn.model.OdaModel
import com.example.memesharekotlinn.model.SimpleResponse
import com.example.memesharekotlinn.model.UploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface MemeService {

    @POST("media-upload-image.php")
    fun uploadImageBase64(@Body request: ImageUploadRequest): Call<UploadResponse>

    @Multipart
    @POST("media-upload-video.php")
    fun uploadVideo(
        @Part("room_id") roomId: RequestBody,
        @Part("user_id") userId: RequestBody,
        @Part("caption") caption: RequestBody,
        @Part video_file: MultipartBody.Part
    ): Call<UploadResponse>

    @GET("media-get-all.php")
    fun getAllMedia(@Query("room_id") roomId: Int): Call<List<GonderiModel>>

    @GET("rooms-join.php")
    fun joinRoom(
        @Query("user_id") userId: Int,
        @Query("room_code") roomCode: String
    ): Call<SimpleResponse>

    @FormUrlEncoded
    @POST("users-register.php")
    fun registerUser(
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<KullaniciResponse>

    @FormUrlEncoded
    @POST("users-login.php")
    fun loginUser(
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<KullaniciResponse>

    @FormUrlEncoded
    @POST("rooms-create.php")
    fun createRoom(
        @Field("user_id") userId: Int
    ): Call<SimpleResponse>

    @GET("rooms-get-joined.php")
    fun getJoinedRooms(@Query("user_id") userId: Int): Call<List<OdaModel>>
}