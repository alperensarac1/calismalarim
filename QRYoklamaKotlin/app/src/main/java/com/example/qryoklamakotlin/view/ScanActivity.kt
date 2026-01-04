package com.example.qryoklamakotlin.view


import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.qryoklamakotlin.R
import com.example.qryoklamakotlin.data.Prefs
import com.example.qryoklamakotlin.databinding.ActivityScanBinding
import com.example.qryoklamakotlin.service.ApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class ScanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanBinding
    private lateinit var fused: FusedLocationProviderClient
    private lateinit var prefs: Prefs

    private var firstResume = true

    /* -------------------------------- Permissions -------------------------------- */

    private val camPerm: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                toast("Kamera izni gerekli")
            } else {
                startScanningIfReady()
            }
        }

    private val locPerms: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val fine = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
            val coarse = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (!fine && !coarse) {
                if (isPermissionPermanentlyDenied(Manifest.permission.ACCESS_FINE_LOCATION) ||
                    isPermissionPermanentlyDenied(Manifest.permission.ACCESS_COARSE_LOCATION)
                ) {
                    showGoToSettingsDialog(
                        "Konum izni kalıcı olarak reddedilmiş görünüyor. " +
                                "Lütfen Ayarlar > Uygulamalar > İzinler kısmından açın."
                    )
                } else {
                    toast("Konum izni gerekli")
                }
            } else {
                ensureLocationEnabled()
            }
        }

    /* -------------------------------- Lifecycle -------------------------------- */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        prefs = Prefs(this)
        fused = LocationServices.getFusedLocationProviderClient(this)

        camPerm.launch(Manifest.permission.CAMERA)
        requestLocationPermissions()

        binding.barcodeScanner.decodeContinuous(barcodeCallback)

        binding.btnKodGir.setOnClickListener {
            showInputDialog(
                this,
                "Kod Gönder",
                "6 haneli kod",
                "Gönder"
            ) { value ->
                getCurrentLocation { lat, lng ->
                    sendAttendanceByCode(value, lat, lng)
                }
            }
        }

        binding.btnYoklamaGoster.setOnClickListener {
            binding.barcodeScanner.pause()
            binding.barcodeScanner.visibility = View.GONE
            binding.container.visibility = View.VISIBLE

            supportFragmentManager.beginTransaction()
                .replace(R.id.container, StudentAttendanceFragment())
                .addToBackStack("attendance")
                .commit()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                    binding.container.visibility = View.GONE
                    binding.barcodeScanner.visibility = View.VISIBLE
                    binding.barcodeScanner.resume()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        binding.btnSinavYeriSorgula.setOnClickListener {
            binding.container.visibility = View.VISIBLE
            val ogrNo = prefs.getStudentNo()
            val fragment = SinavWebView.newInstance(ogrNo)

            supportFragmentManager.beginTransaction()
                .replace(binding.container.id, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    /* -------------------------------- QR Callback -------------------------------- */

    private var lastText: String? = null
    private var lastTs = 0L
    private val SCAN_DEBOUNCE_MS = 1200L

    private val barcodeCallback = BarcodeCallback { result: BarcodeResult? ->
        val txt = result?.text ?: return@BarcodeCallback
        val now = System.currentTimeMillis()

        if (lastText == txt && now - lastTs < SCAN_DEBOUNCE_MS) return@BarcodeCallback

        lastText = txt
        lastTs = now

        binding.barcodeScanner.pause()

        getCurrentLocation { lat, lng ->
            sendAttendance(txt, lat, lng)
            Log.d("QR_LATLNG", "$lat, $lng")
        }
    }

    private fun startScanningIfReady() {
        binding.barcodeScanner.resume()
    }

    /* -------------------------------- Location -------------------------------- */

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarse = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fine || coarse
    }

    private fun requestLocationPermissions() {
        if (!hasLocationPermission()) {
            locPerms.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            ensureLocationEnabled()
        }
    }

    private fun ensureLocationEnabled() {
        if (!isLocationEnabled()) {
            AlertDialog.Builder(this)
                .setTitle("Konum Kapalı")
                .setMessage("Konum servisleri kapalı görünüyor. Açmak ister misiniz?")
                .setPositiveButton("Aç") { _, _ ->
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                .setNegativeButton("İptal", null)
                .show()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return try {
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) {
            true
        }
    }

    private fun isPermissionPermanentlyDenied(permission: String): Boolean {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            val denied =
                ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
            val rationale = shouldShowRequestPermissionRationale(permission)
            return denied && !rationale
        }
        return false
    }

    private fun getCurrentLocation(cb: (Double, Double) -> Unit) {
        if (!hasLocationPermission()) {
            requestLocationPermissions()
            toast("Konum izni gerekli")
            binding.barcodeScanner.resume()
            return
        }

        try {
            fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { loc ->
                    if (isLocationUsable(loc)) {
                        cb(loc.latitude, loc.longitude)
                    } else {
                        fused.lastLocation.addOnSuccessListener { last ->
                            if (isLocationUsable(last)) {
                                cb(last.latitude, last.longitude)
                            } else {
                                toast("Konum alınamadı veya çok eski.")
                                binding.barcodeScanner.resume()
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    toast("Konum hatası: ${it.message}")
                    binding.barcodeScanner.resume()
                }
        } catch (e: SecurityException) {
            toast("Konum izni yok")
            binding.barcodeScanner.resume()
        }
    }

    private fun isLocationUsable(loc: Location?): Boolean {
        if (loc == null) return false
        if (loc.latitude == 0.0 && loc.longitude == 0.0) return false

        val age = System.currentTimeMillis() - loc.time
        if (age > 120_000) return false

        if (loc.hasAccuracy() && loc.accuracy > 100f) return false
        return true
    }

    /* -------------------------------- Network -------------------------------- */

    private fun sendAttendance(qrPayloadRaw: String, lat: Double, lng: Double) {
        val studentNo = prefs.getStudentNo()
        if (studentNo.isNullOrBlank()) {
            toast("Öğrenci no bulunamadı")
            binding.barcodeScanner.resume()
            return
        }

        val url =
            "https://alperensaracdeneme.com/qryoklama/api/index.php?p=attendance/mark"

        val deviceId =
            Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val deviceInfo =
            "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL} / SDK${android.os.Build.VERSION.SDK_INT}"

        try {
            val qrJson = parseQrPayload(qrPayloadRaw)

            val body = JSONObject().apply {
                put("student_no", studentNo)
                put("method", "QR")
                put("qr_payload", qrJson)
                put("lat", lat)
                put("lng", lng)
                put("device_id", deviceId)
                put("device_info", deviceInfo)
            }

            ApiClient.postJson(url, body.toString(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        toast("Ağ hatası: ${e.message}")
                        binding.barcodeScanner.resume()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val resp = response.body?.string().orEmpty()
                    runOnUiThread {
                        if (response.isSuccessful) {
                            toast("Yoklama alındı ✅")
                        } else {
                            showErrorDialog(
                                "Sunucu Hatası (${response.code})",
                                prettyServerError(resp)
                            )
                        }
                        binding.barcodeScanner.resume()
                    }
                }
            })
        } catch (e: Exception) {
            toast("QR parse hatası: ${e.message}")
            binding.barcodeScanner.resume()
        }
    }

    /* -------------------------------- Utils -------------------------------- */

    private fun parseQrPayload(raw: String): JSONObject {
        var s = raw.trim()

        if (s.startsWith("http")) {
            val u = Uri.parse(s)
            u.getQueryParameter("qr")?.let { s = it }
        }

        if (!s.startsWith("{")) throw Exception("Geçersiz QR")
        return JSONObject(s)
    }

    private fun prettyServerError(resp: String?): String {
        return resp?.replace(Regex("(?s)<[^>]*>"), " ")?.trim()
            ?: "Bilinmeyen hata"
    }

    private fun showErrorDialog(title: String, msg: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(msg)
            .setPositiveButton("Tamam", null)
            .show()
    }

    private fun showGoToSettingsDialog(msg: String) {
        AlertDialog.Builder(this)
            .setTitle("İzin Gerekli")
            .setMessage(msg)
            .setPositiveButton("Ayarları Aç") { _, _ ->
                startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", packageName, null)
                    )
                )
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()

    override fun onResume() {
        super.onResume()
        binding.barcodeScanner.resume()

        if (firstResume) {
            firstResume = false
            getCurrentLocation { lat, lng ->
                Log.d("APP_START_LOC", "$lat, $lng")
            }
        }
    }

    override fun onPause() {
        super.onPause()
        binding.barcodeScanner.pause()
    }

    /* -------------------------------- Dialog -------------------------------- */

    interface OnSubmitListener {
        fun onSubmit(value: String)
    }

    companion object {
        fun showInputDialog(
            context: Context,
            title: String,
            hint: String,
            button: String,
            onSubmit: (String) -> Unit
        ) {
            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(50, 40, 50, 10)
            }

            val et = EditText(context).apply {
                this.hint = hint
                inputType = InputType.TYPE_CLASS_TEXT
            }

            layout.addView(TextView(context).apply { text = title })
            layout.addView(et)

            val dialog = AlertDialog.Builder(context)
                .setView(layout)
                .setPositiveButton(button) { _, _ ->
                    val v = et.text.toString().trim()
                    if (v.isNotEmpty()) onSubmit(v)
                }
                .create()

            dialog.show()
        }
    }
    private fun sendAttendanceByCode(joinCode: String, lat: Double, lng: Double) {
        val baseUrl = "https://alperensaracdeneme.com"
        val url = "$baseUrl/qryoklama/api/index.php?p=attendance/mark"

        val studentNo = prefs.getStudentNo()
        val deviceId = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ANDROID_ID
        )
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
                    runOnUiThread {
                        Toast.makeText(
                            this@ScanActivity,
                            "Ağ hatası: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        binding.barcodeScanner.resume()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val resp = response.body?.string().orEmpty()
                    Log.d(
                        "ATTENDANCE_CODE_RESP",
                        "code=${response.code} body=$resp"
                    )

                    runOnUiThread {
                        if (response.isSuccessful) {
                            Toast.makeText(
                                this@ScanActivity,
                                "Yoklama alındı ✅",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            val pretty = prettyServerError(resp)
                            showErrorDialog(
                                "Sunucu Hatası (${response.code})",
                                pretty
                            )
                        }
                        binding.barcodeScanner.resume()
                    }
                }
            })

        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Beklenmeyen hata: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            binding.barcodeScanner.resume()
        }
    }

}
