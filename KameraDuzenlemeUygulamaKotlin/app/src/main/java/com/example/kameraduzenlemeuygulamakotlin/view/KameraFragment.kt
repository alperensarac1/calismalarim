package com.example.kameraduzenlemeuygulamakotlin.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.fragment.app.Fragment
import com.example.kameraduzenlemeuygulamakotlin.R
import com.example.kameraduzenlemeuygulamakotlin.databinding.FragmentKameraBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class KameraFragment : Fragment() {

    private lateinit var binding: FragmentKameraBinding
    private lateinit var imageCapture: ImageCapture

    private val cameraPermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private val permissionRequestCode = 101

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentKameraBinding.inflate(inflater, container, false)

        // Kamera izinlerini kontrol et
        if (arePermissionsGranted()) {
            setupCamera()
        } else {
            requestPermissions(cameraPermissions, permissionRequestCode)
        }

        // Fotoğraf çekme butonuna tıklama işlemi
        binding.takePhotoButton.setOnClickListener {
            takePhoto()
        }

        // Yazı ekleme butonuna tıklama işlemi
        binding.addTextButton.setOnClickListener {
            showTextInputDialog()
        }

        // Ekran görüntüsü kaydetme butonuna tıklama işlemi
        binding.saveScreenshotButton.setOnClickListener {
            saveScreenshot()
        }

        return binding.root
    }

    // Kamera ve depolama izinlerinin kontrol edilmesi
    private fun arePermissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    // Kamera kurulumu
    private fun setupCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(binding.previewView.surfaceProvider)

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    // Fotoğraf çekme işlemi
    private fun takePhoto() {
        val photoFile = File(requireContext().externalCacheDir, "${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = File(photoFile.absolutePath)
                    // Fotoğraf çekildikten sonra yapılacak işlemler
                    // PreviewView'i gizleyip, ImageView'i göster
                    showPhoto(savedUri)
                }

                override fun onError(exception: ImageCaptureException) {
                    exception.printStackTrace()
                }
            })
    }

    // Fotoğrafı ekranda gösterme işlemi
    private fun showPhoto(photoUri: File) {
        // PreviewView'i gizle
        binding.previewView.visibility = View.GONE
        // ImageView'i göster
        binding.imageView.visibility = View.VISIBLE

        // Fotoğrafı Bitmap olarak al
        val bitmap = BitmapFactory.decodeFile(photoUri.absolutePath)

        // Fotoğrafın Exif verilerini al
        val exif = ExifInterface(photoUri.absolutePath)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)

        // Yön verilerine göre fotoğrafı döndür
        val rotatedBitmap = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
            else -> bitmap // Yön bilgisi yoksa orijinal halini kullan
        }

        // Döndürülmüş fotoğrafı ImageView'de göster
        binding.imageView.setImageBitmap(rotatedBitmap)
    }

    // Fotoğrafı döndürme işlemi
    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = android.graphics.Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    // Yazı eklemek için AlertDialog gösterme
    private fun showTextInputDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val editText = EditText(requireContext())
        builder.setTitle("Yazı Ekle")
            .setMessage("Eklemek istediğiniz yazıyı girin")
            .setView(editText)
            .setPositiveButton("Ekle") { _, _ ->
                val text = editText.text.toString()
                addTextToImage(text)
            }
            .setNegativeButton("İptal") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    // Fotoğraf üzerine yazı ekleme işlemi
    private fun addTextToImage(text: String) {
        // ImageView'deki fotoğrafı alıyoruz
        val bitmap = (binding.imageView.drawable as? BitmapDrawable)?.bitmap

        // Eğer fotoğraf varsa, üzerinde yazı ekleme işlemi yapıyoruz
        if (bitmap != null) {
            // Fotoğrafı alıp, üzerine yazıyı eklemek için yeni bir Bitmap oluşturuyoruz
            val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = Canvas(mutableBitmap)

            // Yazı stilini belirliyoruz
            val paint = Paint()
            paint.color = Color.RED
            paint.textSize = 80f
            paint.isAntiAlias = true
            paint.textAlign = Paint.Align.CENTER

            // Yazıyı ekliyoruz. 100f, 100f başlangıç noktasıdır.
            canvas.drawText(text, 100f, 100f, paint)

            // Fotoğrafın üzerine yazıyı ekledikten sonra, ImageView'de gösteriyoruz
            binding.imageView.setImageBitmap(mutableBitmap)
        } else {
            Log.e("Error", "Bitmap is null!")
        }
    }

    private fun saveScreenshot() {
        // ImageView'deki fotoğrafı alıyoruz
        binding.imageView.isDrawingCacheEnabled = true
        val bitmap = Bitmap.createBitmap(binding.imageView.drawingCache)
        binding.imageView.isDrawingCacheEnabled = false

        if (bitmap != null) {
            try {
                // MediaStore API kullanarak fotoğrafı kaydediyoruz
                val contentResolver = requireContext().contentResolver
                val contentValues = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, "screenshot_${System.currentTimeMillis()}.jpg")  // Dosya ismi
                    put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)  // "Pictures" dizinine kaydediyoruz
                }

                // Yeni dosya URI'sini alıyoruz
                val imageUri = contentResolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                // URI ile dosyayı yazma işlemi
                imageUri?.let {
                    val outputStream = contentResolver.openOutputStream(it)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream!!)
                    outputStream?.flush()
                    outputStream?.close()

                    // Kaydedilen dosyanın yolunu loglamak
                    Log.d("Screenshot", "Ekran görüntüsü kaydedildi: $it")
                }

            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            // Bitmap null olduğu durum
            Log.e("Screenshot", "Bitmap oluşturulamadı!")
        }
    }



    // İzin isteklerine yanıt verme
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // İzinler verildiyse kamera kurulumu yapılır
                setupCamera()
            } else {
                // Kullanıcı izin vermezse, izinleri nasıl vereceğiyle ilgili bilgilendiren bir dialog gösterilir
                showPermissionDeniedDialog()
            }
        }
    }

    // İzin reddedildiğinde kullanıcıyı bilgilendiren bir dialog
    private fun showPermissionDeniedDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Kamera ve depolama izinlerini vermeniz gerekiyor. İzinleri vermek için ayarlara gitmek ister misiniz?")
            .setCancelable(false)
            .setPositiveButton("Evet") { _, _ ->
                // İzinler için ayarlara yönlendir
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireContext().packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Hayır") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }
}
