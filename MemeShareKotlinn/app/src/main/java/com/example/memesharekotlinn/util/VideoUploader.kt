package com.example.memesharekotlinn.util
import android.app.Activity
import android.net.Uri
import android.util.Log
import android.widget.Toast
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object VideoUploader {

    private const val TAG = "VideoUploader"

    fun uploadVideo(
        videoName: String,
        videoUri: Uri,
        activity: Activity,
        roomId: Int,
        userId: Int,
        caption: String,
        uploadUrl: String
    ) {
        try {
            val inputStream = activity.contentResolver.openInputStream(videoUri)
                ?: throw IOException("Input stream null! uri=$videoUri")

            // Geçici dosya oluştur
            val tempFile = File.createTempFile(videoName, ".mp4", activity.cacheDir)
            FileOutputStream(tempFile).use { out ->
                val buffer = ByteArray(4096)
                var len: Int
                while (inputStream.read(buffer).also { len = it } > 0) {
                    out.write(buffer, 0, len)
                }
            }
            inputStream.close()

            Log.d(TAG, "Temp file: ${tempFile.absolutePath}, size: ${tempFile.length()}")

            val client = OkHttpClient()

            val videoBody = RequestBody.create(MediaType.parse("video/mp4"), tempFile)

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("room_id", roomId.toString())
                .addFormDataPart("user_id", userId.toString())
                .addFormDataPart("caption", caption)
                .addFormDataPart("video_file", tempFile.name, videoBody)
                .build()

            val request = Request.Builder()
                .url(uploadUrl)
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "Bağlantı hatası", e)
                    activity.runOnUiThread {
                        Toast.makeText(
                            activity,
                            "Bağlantı hatası: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val responseStr = response.body()!!.string()
                        Log.d(TAG, "Yanıt: $responseStr")

                        val success = responseStr.contains("\"success\":true")
                        activity.runOnUiThread {
                            Toast.makeText(
                                activity,
                                if (success) "✅ Video başarıyla yüklendi" else "⚠️ Video yükleme başarısız",
                                Toast.LENGTH_SHORT
                            ).show()

                            Toast.makeText(
                                activity,
                                "Yanıt: $responseStr",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Yanıt çözümleme hatası", e)
                    }
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Video yükleme sırasında hata oluştu", e)
            activity.runOnUiThread {
                Toast.makeText(activity, "Hata: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
