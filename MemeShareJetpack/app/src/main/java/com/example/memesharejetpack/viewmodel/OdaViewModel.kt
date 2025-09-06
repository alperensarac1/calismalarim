package com.example.memesharejetpack.viewmodel

import android.app.Application
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.memesharejetpack.model.ImageUploadRequest
import com.example.memesharejetpack.model.OdaModel
import com.example.memesharejetpack.model.SimpleResponse
import com.example.memesharejetpack.model.UploadResponse
import com.example.memesharejetpack.service.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.IOException

class OdaViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application

    // ---- Upload görsel sonucu (mevcut)
    val uploadResult = MutableLiveData<String>()

    // ---- Joined rooms (Compose listesi için)
    private val _joinedRooms = MutableLiveData<List<OdaModel>>(emptyList())
    val joinedRooms: LiveData<List<OdaModel>> get() = _joinedRooms

    // ---- Oda oluşturma sonucu (mevcut)
    private val _odaOlusturmaSonucu = MutableLiveData<SimpleResponse>()
    val odaOlusturmaSonucu: LiveData<SimpleResponse> get() = _odaOlusturmaSonucu

    // ---- Odaya katılma sonucu (ekledik)
    private val _joinResult = MutableLiveData<SimpleResponse>()
    val joinResult: LiveData<SimpleResponse> get() = _joinResult

    fun fetchJoinedRooms(userId: Int) {
        ApiClient.getService().getJoinedRooms(userId)
            .enqueue(object : Callback<List<OdaModel>> {
                override fun onResponse(
                    call: Call<List<OdaModel>>,
                    response: Response<List<OdaModel>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        _joinedRooms.value = response.body()
                    }
                }
                override fun onFailure(call: Call<List<OdaModel>>, t: Throwable) {
                    // Hata durumunda list boş kalabilir
                }
            })
    }

    fun joinRoom(userId: Int, roomCode: String) {
        ApiClient.getService().joinRoom(userId, roomCode)
            .enqueue(object : Callback<SimpleResponse> {
                override fun onResponse(
                    call: Call<SimpleResponse>,
                    response: Response<SimpleResponse>
                ) {
                    val body = response.body()
                    if (response.isSuccessful && body != null) {
                        _joinResult.value = body!!
                    } else {
                        _joinResult.value = SimpleResponse(success = false, message = "Katılım başarısız", roomId = 1, roomCode = "1")
                    }
                }
                override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {
                    _joinResult.value = SimpleResponse(success = false, message = "Hata: ${t.message}",roomId = 0, roomCode = "0")
                }
            })
    }

    fun createRoom(userId: Int) {
        ApiClient.getService().createRoom(userId)
            .enqueue(object : Callback<SimpleResponse> {
                override fun onResponse(call: Call<SimpleResponse>, response: Response<SimpleResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        _odaOlusturmaSonucu.value = response.body()
                    } else {
                        _odaOlusturmaSonucu.value =
                            SimpleResponse(success = false, message = "Sunucu yanıtı başarısız.",roomId = 1, roomCode = "1")
                    }
                }
                override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {
                    _odaOlusturmaSonucu.value =
                        SimpleResponse(success = false, message = "Bağlantı hatası: ${t.message}",roomId = 1, roomCode = "1")
                }
            })
    }

    // --- (Mevcut) Görsel upload base64
    fun uploadImage(uri: Uri, roomId: Int, userId: Int, caption: String) {
        try {
            val bitmap: Bitmap? = MediaStore.Images.Media.getBitmap(app.contentResolver, uri)
            if (bitmap == null) {
                uploadResult.value = "Görsel alınamadı. Bitmap null."
                return
            }
            val baos = ByteArrayOutputStream()
            val ok = bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            if (!ok) {
                uploadResult.value = "Görsel sıkıştırılamadı."
                return
            }
            val base64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
            val request = ImageUploadRequest(roomId, userId, base64, caption)

            ApiClient.getService().uploadImageBase64(request)
                .enqueue(object : Callback<UploadResponse> {
                    override fun onResponse(call: Call<UploadResponse>, response: Response<UploadResponse>) {
                        val body = response.body()
                        uploadResult.value =
                            if (response.isSuccessful && body != null && body.success) "Görsel yüklendi"
                            else "Görsel yükleme hatası"
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

    // (opsiyonel) Eski yardımcı
    private fun getRealPathFromUri(uri: Uri): String? {
        val proj = arrayOf(MediaStore.Video.Media.DATA)
        val cursor: Cursor? = app.contentResolver.query(uri, proj, null, null, null)
        return cursor?.use {
            val idx = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            it.moveToFirst()
            it.getString(idx)
        }
    }
}
