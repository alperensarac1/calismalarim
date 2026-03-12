package com.example.yardimuygulamakotlin.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.yardimuygulamakotlin.R
import com.example.yardimuygulamakotlin.entity.Session
import com.example.yardimuygulamakotlin.model.RegisterBody
import com.example.yardimuygulamakotlin.repo.AuthRepo
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private val repo = AuthRepo()

    private lateinit var etAd: EditText
    private lateinit var etSoyad: EditText
    private lateinit var etYas: EditText
    private lateinit var etPhone: EditText
    private lateinit var etPass: EditText

    private lateinit var rgRole: RadioGroup
    private lateinit var rbHasta: RadioButton
    private lateinit var rbYardimci: RadioButton

    private lateinit var btnRegister: Button
    private lateinit var btnGoLogin: Button
    private lateinit var progress: ProgressBar
    private lateinit var tvInfo: TextView

    // ✅ Konumdan tespit gösterimi
    private lateinit var tvDetected: TextView
    private lateinit var btnDetect: Button

    private var detectedCity: String? = null
    private var detectedDistrict: String? = null

    private val requestLocationPerm = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val fine = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarse = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fine || coarse) {
            detectCityDistrict()
        } else {
            tvDetected.text = "Konum izni verilmedi. Şehir/ilçe otomatik alınamadı."
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etAd = view.findViewById(R.id.etAd)
        etSoyad = view.findViewById(R.id.etSoyad)
        etYas = view.findViewById(R.id.etYas)
        etPhone = view.findViewById(R.id.etPhone)
        etPass = view.findViewById(R.id.etPass)

        rgRole = view.findViewById(R.id.rgRole)
        rbHasta = view.findViewById(R.id.rbHasta)
        rbYardimci = view.findViewById(R.id.rbYardimci)

        btnRegister = view.findViewById(R.id.btnRegister)
        btnGoLogin = view.findViewById(R.id.btnGoLogin)
        progress = view.findViewById(R.id.progress)
        tvInfo = view.findViewById(R.id.tvInfo)

        tvDetected = view.findViewById(R.id.tvDetected)
        btnDetect = view.findViewById(R.id.btnDetect)

        btnGoLogin.setOnClickListener {
            parentFragmentManager.popBackStack() // login'e dön
        }

        btnDetect.setOnClickListener {
            ensureLocationAndDetect()
        }

        btnRegister.setOnClickListener {
            doRegister()
        }

        // ✅ ekran açılır açılmaz otomatik tespit
        ensureLocationAndDetect()
    }

    private fun doRegister() {
        val ad = etAd.text.toString().trim()
        val soyad = etSoyad.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val pass = etPass.text.toString().trim()
        val yas = etYas.text.toString().trim().takeIf { it.isNotEmpty() }?.toIntOrNull()

        val role = if (rbYardimci.isChecked) "YARDIMCI" else "HASTA"

        if (ad.isEmpty() || soyad.isEmpty() || phone.isEmpty() || pass.isEmpty()) {
            tvInfo.text = "Ad, soyad, telefon, şifre zorunlu"
            return
        }

        val city = detectedCity
        val district = detectedDistrict
        if (city.isNullOrBlank() || district.isNullOrBlank()) {
            tvInfo.text = "Şehir/ilçe tespit edilemedi. Konum iznini/GPS'i kontrol et."
            return
        }

        val body = RegisterBody(
            role = role,
            ad = ad,
            soyad = soyad,
            yas = yas,
            telefon = phone,
            il = city,
            ilce = district,
            sifre = pass
        )

        progress.visibility = View.VISIBLE
        tvInfo.text = ""

        lifecycleScope.launch(Dispatchers.IO) {
            val res = repo.register(body)
            withContext(Dispatchers.Main) {
                progress.visibility = View.GONE
                if (res?.ok == true && res.user != null) {
                    val u = res.user!!
                    Session.save(requireContext(), u.id, u.role)
                    goHomeByRole(u.id, u.role)
                } else {
                    tvInfo.text = res?.error ?: "Kayıt başarısız"
                }
            }
        }
    }

    private fun goHomeByRole(userId: Long, role: String) {
        val f: Fragment = if (role == "YARDIMCI") {
            HelperOpenListFragment.newInstance(helperId = userId)
        } else {
            PatientHelpFragment.newInstance(patientId = userId)
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.container, f)
            .commit()
    }

    private fun ensureLocationAndDetect() {
        val fineGranted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            detectCityDistrict()
        } else {
            requestLocationPerm.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun detectCityDistrict() {
        tvDetected.text = "Konumdan şehir/ilçe tespit ediliyor..."
        lifecycleScope.launch(Dispatchers.IO) {
            val loc = getCurrentLocationSuspend()
            if (loc == null) {
                withContext(Dispatchers.Main) {
                    tvDetected.text = "Konum alınamadı (GPS açık mı?)"
                }
                return@launch
            }

            val (lat, lng) = loc
            val (city, district) = reverseGeocode(lat, lng)

            withContext(Dispatchers.Main) {
                detectedCity = city
                detectedDistrict = district

                if (!city.isNullOrBlank() && !district.isNullOrBlank()) {
                    tvDetected.text = "Tespit edilen: $city / $district"
                } else {
                    tvDetected.text = "Şehir/ilçe tespit edilemedi. Tekrar dene."
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getCurrentLocationSuspend(): Pair<Double, Double>? {
        val client = LocationServices.getFusedLocationProviderClient(requireContext())
        return suspendCancellableCoroutine { cont ->
            val tokenSource = CancellationTokenSource()
            client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.token)
                .addOnSuccessListener { l ->
                    if (l != null) cont.resume(Pair(l.latitude, l.longitude))
                    else cont.resume(null)
                }
                .addOnFailureListener { cont.resume(null) }
        }
    }

    private fun reverseGeocode(lat: Double, lng: Double): Pair<String?, String?> {
        return try {
            val geocoder = Geocoder(requireContext(), Locale("tr", "TR"))
            val list = geocoder.getFromLocation(lat, lng, 1)
            val a = list?.firstOrNull()

            val city = a?.adminArea // İl (İstanbul)
            // İlçe: bazı cihazlarda subAdminArea, bazılarında locality
            val district = a?.subAdminArea ?: a?.locality

            Pair(city, district)
        } catch (_: Exception) {
            Pair(null, null)
        }
    }
}