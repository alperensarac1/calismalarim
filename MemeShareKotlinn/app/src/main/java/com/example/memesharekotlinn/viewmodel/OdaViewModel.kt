package com.example.memesharekotlinn.viewmodel

import android.app.Application
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.memesharekotlinn.model.GonderiModel
import com.example.memesharekotlinn.model.ImageUploadRequest
import com.example.memesharekotlinn.model.SimpleResponse
import com.example.memesharekotlinn.model.UploadResponse
import com.example.memesharekotlinn.service.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.IOException

class OdaViewModel(application: Application) : AndroidViewModel(application) {

    val uploadResult = MutableLiveData<String>()
    private val app: Application = application

    fun uploadImage(uri: Uri, roomId: Int, userId: Int, caption: String) {
        try {
            // (API düzeyine göre) daha modern alternatif: ImageDecoder
            val bitmap: Bitmap? = MediaStore.Images.Media.getBitmap(app.contentResolver, uri)

            if (bitmap == null) {
                uploadResult.value = "Görsel alınamadı. Bitmap null."
                return
            }

            val baos = ByteArrayOutputStream()
            val compressed = bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            if (!compressed) {
                uploadResult.value = "Görsel sıkıştırılamadı."
                return
            }

            val base64Image = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
            val request = ImageUploadRequest(roomId, userId, base64Image, caption)

            ApiClient.getService().uploadImageBase64(request)
                .enqueue(object : Callback<UploadResponse> {
                    override fun onResponse(
                        call: Call<UploadResponse>,
                        response: Response<UploadResponse>
                    ) {
                        val body = response.body()
                        if (response.isSuccessful && body != null && body.success) {
                            uploadResult.value = "Görsel yüklendi"
                        } else {
                            uploadResult.value = "Görsel yükleme hatası"
                        }
                    }

                    override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                        uploadResult.value = "Bağlantı hatası: ${t.message}"
                    }
                })

        } catch (e: IOException) {
            uploadResult.value = "Görsel okunamadı: ${e.message}"
        } catch (ex: Exception) {
            uploadResult.value = "Bilinmeyen hata: ${ex.message}"
        }
    }

    // ---- Gönderiler ----
    private val gonderiler = MutableLiveData<List<GonderiModel>?>()

    fun getAllMedia(roomId: Int): LiveData<List<GonderiModel>?> {
        ApiClient.getService().getAllMedia(roomId)
            .enqueue(object : Callback<List<GonderiModel>> {
                override fun onResponse(
                    call: Call<List<GonderiModel>>,
                    response: Response<List<GonderiModel>>
                ) {
                    gonderiler.value = if (response.isSuccessful) response.body() else null
                }

                override fun onFailure(call: Call<List<GonderiModel>>, t: Throwable) {
                    gonderiler.value = null
                }
            })
        return gonderiler
    }

    // (Gerekirse) dosya yolu çözümleme
    private fun getRealPathFromUri(uri: Uri): String? {
        val proj = arrayOf(MediaStore.Video.Media.DATA)
        val cursor: Cursor? = app.contentResolver.query(uri, proj, null, null, null)
        return cursor?.use {
            val idx = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            it.moveToFirst()
            it.getString(idx)
        }
    }

    // ---- Oda oluşturma ----
    private val _odaOlusturmaSonucu = MutableLiveData<SimpleResponse>()
    val odaOlusturmaSonucu: LiveData<SimpleResponse> get() = _odaOlusturmaSonucu

    fun createRoom(userId: Int) {
        ApiClient.getService().createRoom(userId)
            .enqueue(object : Callback<SimpleResponse> {
                override fun onResponse(
                    call: Call<SimpleResponse>,
                    response: Response<SimpleResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        _odaOlusturmaSonucu.value = response.body()
                    } else {
                        _odaOlusturmaSonucu.value = SimpleResponse(success = false, message = "Sunucu yanıtı başaeısız",roomCode = "1",userId)
                    }
                }

                override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {

                    println("Bağlantı hatası: ${t.message}")
                }
            })
    }
}
