package com.example.memesharekotlin.viewmodel;


import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.memesharekotlin.model.GonderiModel;
import com.example.memesharekotlin.model.ImageUploadRequest;
import com.example.memesharekotlin.model.SimpleResponse;
import com.example.memesharekotlin.model.UploadResponse;
import com.example.memesharekotlin.service.ApiClient;
import com.example.memesharekotlin.util.VideoUploader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class OdaViewModel extends AndroidViewModel {

    public MutableLiveData<String> uploadResult = new MutableLiveData<>();
    private final Application app;

    public OdaViewModel(@NonNull Application application) {
        super(application);
        this.app = application;
    }

    public void uploadImage(Uri uri, int roomId, int userId, String caption) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(app.getContentResolver(), uri);

            if (bitmap == null) {
                uploadResult.setValue("Görsel alınamadı. Bitmap null.");
                return;
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            boolean success = bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);

            if (!success) {
                uploadResult.setValue("Görsel sıkıştırılamadı.");
                return;
            }

            String base64Image = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
            ImageUploadRequest request = new ImageUploadRequest(roomId, userId, base64Image, caption);

            ApiClient.getService().uploadImageBase64(request).enqueue(new Callback<UploadResponse>() {
                @Override
                public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().success) {
                        uploadResult.setValue("Görsel yüklendi");
                    } else {
                        uploadResult.setValue("Görsel yükleme hatası");
                    }
                }

                @Override
                public void onFailure(Call<UploadResponse> call, Throwable t) {
                    uploadResult.setValue("Bağlantı hatası: " + t.getMessage());
                }
            });

        } catch (IOException e) {
            uploadResult.setValue("Görsel okunamadı: " + e.getMessage());
        } catch (Exception ex) {
            uploadResult.setValue("Bilinmeyen hata: " + ex.getMessage());
        }
    }





    private MutableLiveData<List<GonderiModel>> gonderiler = new MutableLiveData<>();

    public LiveData<List<GonderiModel>> getAllMedia(int roomId) {
        ApiClient.getService().getAllMedia(roomId).enqueue(new Callback<List<GonderiModel>>() {
            @Override
            public void onResponse(Call<List<GonderiModel>> call, Response<List<GonderiModel>> response) {
                if (response.isSuccessful()) {
                    gonderiler.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<GonderiModel>> call, Throwable t) {
                gonderiler.setValue(null);
            }
        });

        return gonderiler;
    }

    private String getRealPathFromUri(Uri uri) {
        String[] proj = { MediaStore.Video.Media.DATA };
        Cursor cursor = app.getContentResolver().query(uri, proj, null, null, null);
        if (cursor != null) {
            int index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(index);
            cursor.close();
            return path;
        }
        return null;
    }
    private MutableLiveData<SimpleResponse> odaOlusturmaSonucu = new MutableLiveData<>();

    public LiveData<SimpleResponse> getOdaOlusturmaSonucu() {
        return odaOlusturmaSonucu;
    }

    public void createRoom(int userId) {
        ApiClient.getService().createRoom(userId).enqueue(new Callback<SimpleResponse>() {
            @Override
            public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    odaOlusturmaSonucu.setValue(response.body());
                } else {
                    SimpleResponse hata = new SimpleResponse();
                    hata.success = false;
                    hata.message = "Sunucu yanıtı başarısız.";
                    odaOlusturmaSonucu.setValue(hata);
                }
            }

            @Override
            public void onFailure(Call<SimpleResponse> call, Throwable t) {
                SimpleResponse hata = new SimpleResponse();
                hata.success = false;
                hata.message = "Bağlantı hatası: " + t.getMessage();
                odaOlusturmaSonucu.setValue(hata);
                System.out.println(hata.message);
            }
        });
    }
}

