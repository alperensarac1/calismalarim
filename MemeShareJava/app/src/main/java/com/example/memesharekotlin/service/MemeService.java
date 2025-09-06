package com.example.memesharekotlin.service;

import com.example.memesharekotlin.model.GonderiModel;
import com.example.memesharekotlin.model.ImageUploadRequest;
import com.example.memesharekotlin.model.KullaniciResponse;
import com.example.memesharekotlin.model.OdaModel;
import com.example.memesharekotlin.model.SimpleResponse;
import com.example.memesharekotlin.model.UploadResponse;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface MemeService {

    @POST("media-upload-image.php")
    Call<UploadResponse> uploadImageBase64(@Body ImageUploadRequest request);

    @Multipart
    @POST("media-upload-video.php")
    Call<UploadResponse> uploadVideo(
            @Part("room_id") RequestBody roomId,
            @Part("user_id") RequestBody userId,
            @Part("caption") RequestBody caption,
            @Part MultipartBody.Part video_file
    );


    @GET("media-get-all.php")
    Call<List<GonderiModel>> getAllMedia(@Query("room_id") int roomId);

    @GET("rooms-join.php")
    Call<SimpleResponse> joinRoom(@Query("user_id") int userId, @Query("room_code") String roomCode);

        @FormUrlEncoded
        @POST("users-register.php")
        Call<KullaniciResponse> registerUser(
                @Field("username") String username,
                @Field("password") String password
        );

        @FormUrlEncoded
        @POST("users-login.php")
        Call<KullaniciResponse> loginUser(
                @Field("username") String username,
                @Field("password") String password
        );
    @FormUrlEncoded
    @POST("rooms-create.php")
    Call<SimpleResponse> createRoom(
            @Field("user_id") int userId
    );

    @GET("rooms-get-joined.php")
    Call<List<OdaModel>> getJoinedRooms(@Query("user_id") int userId);


}
