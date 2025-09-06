package com.example.memesharekotlin;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class VideoUploadTestActivity extends AppCompatActivity {

    private static final int VIDEO_PICK_REQUEST = 101;
    private static final String TAG = "UPLOAD_RAW";
    private final String UPLOAD_URL = "https://alperensaracdeneme.com/meme/media-upload-video.php";

    private int roomId = 10;
    private int userId = 2;
    private String caption = "Test videosu yüklemesi";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Button btn = new Button(this);
        btn.setText("Video Seç ve Yükle");
        setContentView(btn);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

        btn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            intent.setType("video/*");
            startActivityForResult(intent, VIDEO_PICK_REQUEST);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VIDEO_PICK_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri videoUri = data.getData();
            Log.d(TAG, "Video URI: " + videoUri);
            uploadVideo("deneme",videoUri);
        }
    }

    private void uploadVideo(String videoName,Uri videoUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(videoUri);
            File tempFile = File.createTempFile(videoName, ".mp4", getCacheDir());
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

            RequestBody videoBody = RequestBody.create(MediaType.parse("video/*"), tempFile);
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
                    runOnUiThread(() -> Toast.makeText(VideoUploadTestActivity.this, "Bağlantı hatası: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    Log.e(TAG, "Bağlantı hatası", e);
                }

                @Override
                public void onResponse(Call call, Response response) {
                    try {
                        String responseStr = response.body().string();
                        Log.d(TAG, "Sunucu cevabı: " + responseStr);
                        runOnUiThread(() -> Toast.makeText(VideoUploadTestActivity.this, "Yanıt: " + responseStr, Toast.LENGTH_LONG).show());
                    } catch (Exception e) {
                        Log.e(TAG, "Yanıt çözümleme hatası", e);
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Video yükleme sırasında hata oluştu", e);
        }
    }
}
