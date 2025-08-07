package com.example.jetpackkameraduzenlemeuyg

import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

@Composable
fun KameraScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var userText by remember { mutableStateOf("") }
    var showPreviewDialog by remember { mutableStateOf(false) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val cameraPermission = android.Manifest.permission.CAMERA
    val permissionState = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, cameraPermission) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        permissionState.value = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Kamera izni gereklidir", Toast.LENGTH_SHORT).show()
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        imageBitmap = bitmap
    }

    // Önizleme dialog'u
    if (showPreviewDialog && previewBitmap != null) {
        AlertDialog(
            onDismissRequest = { showPreviewDialog = false },
            title = { Text("Kaydetmeden Önce Önizleme") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        bitmap = previewBitmap!!.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .border(2.dp, Color.Gray, RectangleShape)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        saveBitmapToGallery(context, previewBitmap!!)
                        showPreviewDialog = false
                        Toast.makeText(context, "Fotoğraf kaydedildi", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Evet, Kaydet")
                }
            },
            dismissButton = {
                Button(onClick = { showPreviewDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap!!.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(300.dp)
                    .border(2.dp, Color.Gray, RectangleShape)
            )
        } else {
            Box(
                modifier = modifier
                    .size(300.dp)
                    .border(2.dp, Color.Gray, RectangleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Fotoğraf Yok", color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = userText,
            onValueChange = { userText = it },
            label = { Text("Fotoğrafa yazılacak metin") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(
                onClick = {
                    if (permissionState.value) {
                        cameraLauncher.launch()
                    } else {
                        launcher.launch(cameraPermission)
                    }
                }
            ) {
                Text(text = "Fotoğraf Çek")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = {
                    imageBitmap?.let {
                        val withText = drawTextOnBitmap(it, userText)
                        previewBitmap = withText
                        showPreviewDialog = true
                    }
                },
                enabled = imageBitmap != null
            ) {
                Text(text = "Kaydet")
            }
        }
    }
}
fun saveBitmapToGallery(context: Context, bitmap: Bitmap) {
    val filename = "photo_${System.currentTimeMillis()}.jpg"
    val fos: OutputStream?

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val imageUri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        fos = imageUri?.let { context.contentResolver.openOutputStream(it) }
    } else {
        val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val image = File(imagesDir, filename)
        fos = FileOutputStream(image)
    }

    fos?.use {
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
    }
}
fun drawTextOnBitmap(bitmap: Bitmap, text: String): Bitmap {
    val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(result)

    val paint = Paint()
    paint.textSize = 50f
    paint.isAntiAlias = true
    paint.setShadowLayer(1f, 0f, 1f,0xFFFFFFF)

    val x = 20f
    val y = bitmap.height - 40f

    canvas.drawText(text, x, y, paint)
    return result
}
