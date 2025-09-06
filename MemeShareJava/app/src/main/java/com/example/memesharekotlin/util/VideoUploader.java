package com.example.memesharekotlin.util;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.example.memesharekotlin.VideoUploadTestActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class VideoUploader {


    public static void uploadVideo(String videoName, Uri videoUri, Activity activity,int roomId,int userId,String caption,String UPLOAD_URL) {
        try {
            InputStream inputStream = activity.getContentResolver().openInputStream(videoUri);
            if (inputStream == null) {
                Log.e("VideoUploader", "InputStream NULL: " + videoUri.toString());
                throw new IOException("Input stream null!");
            }

            File tempFile = File.createTempFile(videoName, ".mp4", activity.getCacheDir());
            OutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[4096];
            int len;
            while ((len = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }
            inputStream.close();
            outputStream.close();

            OkHttpClient client = new OkHttpClient();

            RequestBody roomIdBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(roomId));
            RequestBody userIdBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(userId));
            RequestBody captionBody = RequestBody.create(MediaType.parse("text/plain"), caption);

            RequestBody videoBody = RequestBody.create(MediaType.parse("video/mp4"), tempFile);
            Log.d("VideoUploader", "Temp file: " + tempFile.getAbsolutePath() + ", size: " + tempFile.length());
            MultipartBody.Part videoPart = MultipartBody.Part.createFormData("video_file", tempFile.getName(), videoBody);

            MultipartBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addPart(MultipartBody.Part.createFormData("room_id", String.valueOf(roomId)))
                    .addPart(MultipartBody.Part.createFormData("user_id", String.valueOf(userId)))
                    .addPart(MultipartBody.Part.createFormData("caption", caption))
                    .addFormDataPart("video_file", tempFile.getName(), videoBody)
                    .build();

            Request request = new Request.Builder()
                    .url(UPLOAD_URL)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    activity.runOnUiThread(() -> Toast.makeText(activity.getApplicationContext(), "Bağlantı hatası: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    Log.e("VideoUploader", "Bağlantı hatası", e);
                }

                @Override
                public void onResponse(Call call, Response response) {
                    try {
                        String responseStr = response.body().string();
                        Log.d("VideoUploader", "Yanıt: " + responseStr);
                        if (responseStr.contains("\"success\":true")) {
                            activity.runOnUiThread(() -> Toast.makeText(activity, "✅ Video başarıyla yüklendi", Toast.LENGTH_SHORT).show());
                        } else {
                            activity.runOnUiThread(() -> Toast.makeText(activity, "⚠️ Video yükleme başarısız", Toast.LENGTH_SHORT).show());
                        }

                        activity.runOnUiThread(() -> Toast.makeText(activity.getApplicationContext(), "Yanıt: " + responseStr, Toast.LENGTH_LONG).show());
                    } catch (Exception e) {
                        Log.e("Video Uploader", "Yanıt çözümleme hatası", e);
                    }
                }
            });

        } catch (Exception e) {
            Log.e("Video Uploader", "Video yükleme sırasında hata oluştu", e);
        }
    }
}

