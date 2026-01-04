package com.example.qryoklamajetpack.view
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.provider.Settings
import android.text.InputType
import android.util.Log
import android.widget.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.qryoklamajetpack.data.Prefs
import com.example.qryoklamajetpack.service.ApiClient
import com.example.qryoklamajetpack.util.LocationHelper
import com.google.android.gms.location.LocationServices
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

@Composable
fun ScanScreen(
    onOpenAttendance: () -> Unit,
    onOpenExam: () -> Unit
) {
    val ctx = LocalContext.current
    val activity = ctx as Activity

    val prefs = remember { Prefs(ctx) }
    val fused = remember { LocationServices.getFusedLocationProviderClient(ctx) }

    var hasCam by remember { mutableStateOf(false) }
    var hasLoc by remember { mutableStateOf(LocationHelper.hasLocationPermission(ctx)) }

    // Kamera izni
    val camPerm = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCam = granted
        if (!granted) Toast.makeText(ctx, "Kamera izni gerekli", Toast.LENGTH_LONG).show()
    }

    // Konum izinleri
    val locPerms = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val fine = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarse = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLoc = fine || coarse
        if (!hasLoc) {
            Toast.makeText(ctx, "Konum izni gerekli", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        camPerm.launch(Manifest.permission.CAMERA)
        locPerms.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    // debounce
    var lastText by remember { mutableStateOf<String?>(null) }
    var lastTs by remember { mutableLongStateOf(0L) }
    val debounceMs = 1200L

    // scanner referansı
    var scannerRef by remember { mutableStateOf<com.journeyapps.barcodescanner.DecoratedBarcodeView?>(null) }

    Box(Modifier.fillMaxSize()) {

        if (hasCam) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    com.journeyapps.barcodescanner.DecoratedBarcodeView(context).also { scanner ->
                        scannerRef = scanner
                        scanner.decodeContinuous(object : BarcodeCallback {
                            override fun barcodeResult(result: BarcodeResult?) {
                                val txt = result?.text ?: return
                                val now = System.currentTimeMillis()

                                if (lastText == txt && (now - lastTs) < debounceMs) return
                                lastText = txt
                                lastTs = now

                                scanner.pause()

                                // Konumu al -> Yoklamayı gönder
                                LocationHelper.getCurrentLocation(
                                    activity = activity,
                                    fused = fused,
                                    onLoc = { lat, lng ->
                                        sendAttendance(
                                            activity = activity,
                                            prefs = prefs,
                                            qrPayloadRaw = txt,
                                            lat = lat,
                                            lng = lng,
                                            onDone = { scanner.resume() }
                                        )
                                    },
                                    onFail = { msg ->
                                        Toast.makeText(activity, msg, Toast.LENGTH_LONG).show()
                                        scanner.resume()
                                    }
                                )
                            }
                        })
                        scanner.resume()
                    }
                }
            )
        } else {
            // Kamera yoksa basit placeholder
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Kamera izni gerekli.")
            }
        }

        // Üst: Sınav yeri sorgula
        Button(
            onClick = {
                scannerRef?.pause()
                onOpenExam()
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 50.dp)
        ) { Text("Sınav Yeri Sorgula") }

        // Sağ alt: Kod Gir
        FloatingActionButton(
            onClick = {
                scannerRef?.pause()
                showCodeInputDialog(ctx) { code ->
                    LocationHelper.getCurrentLocation(
                        activity = activity,
                        fused = fused,
                        onLoc = { lat, lng ->
                            sendAttendanceByCode(
                                activity = activity,
                                prefs = prefs,
                                joinCode = code,
                                lat = lat,
                                lng = lng,
                                onDone = { scannerRef?.resume() }
                            )
                        },
                        onFail = { msg ->
                            Toast.makeText(activity, msg, Toast.LENGTH_LONG).show()
                            scannerRef?.resume()
                        }
                    )
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) { Text("Kod") }

        // Sol alt: Yoklama Göster
        FloatingActionButton(
            onClick = {
                scannerRef?.pause()
                onOpenAttendance()
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 24.dp)
        ) { Text("Liste") }
    }
}

/* ------------------- Dialog (Kod Gir) ------------------- */

private fun showCodeInputDialog(context: Context, onSubmit: (String) -> Unit) {
    val layout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(50, 40, 50, 10)
    }

    val tvTitle = TextView(context).apply {
        text = "Kod Gönder"
        textSize = 18f
        setPadding(0, 0, 0, 20)
    }

    val etInput = EditText(context).apply {
        hint = "6 haneli kod"
        inputType = InputType.TYPE_CLASS_TEXT
    }

    val btnAction = Button(context).apply { text = "Gönder" }

    layout.addView(tvTitle)
    layout.addView(etInput)
    layout.addView(btnAction)

    val dialog = AlertDialog.Builder(context).setView(layout).create()
    dialog.show()

    btnAction.setOnClickListener {
        val input = etInput.text.toString().trim()
        if (input.isNotEmpty()) {
            onSubmit(input)
            dialog.dismiss()
        } else {
            Toast.makeText(context, "Lütfen bir değer girin!", Toast.LENGTH_SHORT).show()
        }
    }
}

private fun sendAttendanceByCode(
    activity: Activity,
    prefs: Prefs,
    joinCode: String,
    lat: Double,
    lng: Double,
    onDone: () -> Unit
) {
    val url = "https://alperensaracdeneme.com/qryoklama/api/index.php?p=attendance/mark"
    val studentNo = prefs.getStudentNo()

    if (studentNo.isNullOrBlank()) {
        Toast.makeText(activity, "Öğrenci no bulunamadı", Toast.LENGTH_LONG).show()
        onDone()
        return
    }

    val deviceId = Settings.Secure.getString(activity.contentResolver, Settings.Secure.ANDROID_ID)
    val deviceInfo =
        "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL} / SDK${android.os.Build.VERSION.SDK_INT}"

    try {
        val body = JSONObject().apply {
            put("student_no", studentNo)
            put("method", "CODE")
            put("join_code", joinCode)
            put("lat", lat)
            put("lng", lng)
            put("device_id", deviceId)
            put("device_info", deviceInfo)
        }

        ApiClient.postJson(url, body.toString(), object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity.runOnUiThread {
                    Toast.makeText(activity, "Ağ hatası: ${e.message}", Toast.LENGTH_LONG).show()
                    onDone()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val resp = response.body?.string().orEmpty()
                Log.d("ATTENDANCE_CODE_RESP", "code=${response.code} body=$resp")

                activity.runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(activity, "Yoklama alındı ✅", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(activity, "Sunucu Hatası (${response.code})", Toast.LENGTH_LONG).show()
                    }
                    onDone()
                }
            }
        })
    } catch (e: Exception) {
        Toast.makeText(activity, "Beklenmeyen hata: ${e.message}", Toast.LENGTH_LONG).show()
        onDone()
    }
}

private fun sendAttendance(
    activity: Activity,
    prefs: Prefs,
    qrPayloadRaw: String,
    lat: Double,
    lng: Double,
    onDone: () -> Unit
) {
    val url = "https://alperensaracdeneme.com/qryoklama/api/index.php?p=attendance/mark"
    val studentNo = prefs.getStudentNo()

    if (studentNo.isNullOrBlank()) {
        Toast.makeText(activity, "Öğrenci no bulunamadı", Toast.LENGTH_LONG).show()
        onDone()
        return
    }

    val deviceId = Settings.Secure.getString(activity.contentResolver, Settings.Secure.ANDROID_ID)
    val deviceInfo =
        "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL} / SDK${android.os.Build.VERSION.SDK_INT}"

    try {
        // Eğer sende parseQrPayload gibi bir fonksiyon varsa burada çağırabilirsin.
        // Şimdilik: ham string’i gönderiyoruz veya JSON parse ediyorsun.
        val body = JSONObject().apply {
            put("student_no", studentNo)
            put("method", "QR")
            put("qr_raw", qrPayloadRaw) // backend bekliyorsa buna göre değiştir
            put("lat", lat)
            put("lng", lng)
            put("device_id", deviceId)
            put("device_info", deviceInfo)
        }

        ApiClient.postJson(url, body.toString(), object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity.runOnUiThread {
                    Toast.makeText(activity, "Ağ hatası: ${e.message}", Toast.LENGTH_LONG).show()
                    onDone()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val resp = response.body?.string().orEmpty()
                Log.d("ATTENDANCE_QR_RESP", "code=${response.code} body=$resp")

                activity.runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(activity, "Yoklama alındı ✅", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(activity, "Sunucu Hatası (${response.code})", Toast.LENGTH_LONG).show()
                    }
                    onDone()
                }
            }
        })
    } catch (e: Exception) {
        Toast.makeText(activity, "Beklenmeyen hata: ${e.message}", Toast.LENGTH_LONG).show()
        onDone()
    }
}