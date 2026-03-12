package com.example.yardimuygulamakotlin.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.yardimuygulamakotlin.R
import com.example.yardimuygulamakotlin.repo.PatientRepo
import com.example.yardimuygulamakotlin.service.Poller
import com.example.yardimuygulamakotlin.util.TimeUtils.formatRemainingSeconds
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class PatientHelpFragment : Fragment(R.layout.fragment_patient_help) {

    private var patientId: Long = 0L
    private val repo = PatientRepo()

    private var lastLatLng: Pair<Double, Double>? = null

    private lateinit var tvLocation: TextView
    private lateinit var btnCancel: Button
    private lateinit var etService: EditText
    private lateinit var etRoom: EditText
    private lateinit var btnSend: Button
    private lateinit var btnConfirm: Button
    private lateinit var tvStatus: TextView
    private lateinit var progress: ProgressBar

    private var currentRequestId: Long? = null
    private var poller: Poller? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        patientId = requireArguments().getLong("patient_id")
    }

    // ✅ İzin isteme (uygulama içinde popup çıkarır)
    private val requestLocationPerm = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val fine = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarse = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (fine || coarse) {
            // İzin verildiyse konumu çek
            fetchLocationOnce()
        } else {
            // tvStatus daha init olmadan callback gelebilir, o yüzden güvenli kontrol
            if (this::tvStatus.isInitialized) {
                tvStatus.text = "Konum izni verilmedi. Konum olmadan istek gönderilemez."
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        etService = view.findViewById(R.id.etService)
        etRoom = view.findViewById(R.id.etRoom)
        btnSend = view.findViewById(R.id.btnSend)
        btnConfirm = view.findViewById(R.id.btnConfirm)
        tvStatus = view.findViewById(R.id.tvStatus)
        progress = view.findViewById(R.id.progress)
        tvLocation = view.findViewById(R.id.tvLocation)

        btnCancel = view.findViewById(R.id.btnCancel)
        btnCancel.visibility = View.GONE

        btnSend.setOnClickListener {
            // ✅ önce izin/konum, sonra create
            ensureLocationThenCreate()
        }

        btnConfirm.setOnClickListener { confirm() }
        btnCancel.setOnClickListener { cancelRequest() }

        poller = Poller(viewLifecycleOwner.lifecycleScope, intervalMs = 2500) {
            fetchActive()
        }

        // ✅ Ekran açılır açılmaz izin iste + konum çek
        ensureLocationOnly()
    }

    override fun onStart() {
        super.onStart()
        poller?.start()
    }

    override fun onStop() {
        super.onStop()
        poller?.stop()
    }

    // ✅ sadece konumu hazırla (ekran açılınca)
    private fun ensureLocationOnly() {
        val fineGranted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            fetchLocationOnce()
        } else {
            requestLocationPerm.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // ✅ yardım iste butonuna basınca çalışır: konum yoksa al, sonra createHelp çağır
    private fun ensureLocationThenCreate() {
        val fineGranted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            fetchLocationOnce {
                createHelp()
            }
        } else {
            requestLocationPerm.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun createHelp() {
        val servis = etService.text.toString().trim()
        val oda = etRoom.text.toString().trim()

        if (servis.isEmpty() || oda.isEmpty()) {
            tvStatus.text = "Servis ve oda zorunlu"
            return
        }

        val loc = lastLatLng
        if (loc == null) {
            tvStatus.text = "Konum alınmadan gönderilemez."
            return
        }

        progress.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            val res = repo.createHelp(patientId, servis, oda, loc.first, loc.second)
            withContext(Dispatchers.Main) {
                progress.visibility = View.GONE
                tvStatus.text = if (res?.ok == true) {
                    "Durum: OPEN (yardımcı bekleniyor)"
                } else {
                    res?.error ?: "İstek gönderilemedi"
                }
            }
        }
    }

    private suspend fun fetchActive() {
        val res = repo.myActive(patientId)
        val active = res?.active

        withContext(Dispatchers.Main) {
            if (res?.ok == true && active != null) {
                currentRequestId = active.id

                val showCancel = active.status == "OPEN" || active.status == "ACCEPTED"
                btnCancel.visibility = if (showCancel) View.VISIBLE else View.GONE

                btnConfirm.visibility = if (active.status == "ACCEPTED") View.VISIBLE else View.GONE

                val rem = active.remaining_seconds ?: 0
                tvStatus.text = if (active.status == "ACCEPTED") {
                    "Durum: ACCEPTED (Kalan: ${formatRemainingSeconds(rem)})"
                } else {
                    "Durum: ${active.status}"
                }
            } else {
                tvStatus.text = "Durum: Aktif istek yok"
                btnConfirm.visibility = View.GONE
                btnCancel.visibility = View.GONE
                currentRequestId = null
            }
        }
    }

    private fun confirm() {
        val reqId = currentRequestId ?: return
        progress.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            val res = repo.confirm(reqId, patientId)
            withContext(Dispatchers.Main) {
                progress.visibility = View.GONE
                if (res?.ok == true) {
                    tvStatus.text = "Durum: CONFIRMED (tamamlandı)"
                    btnConfirm.visibility = View.GONE
                    btnCancel.visibility = View.GONE
                    currentRequestId = null
                } else {
                    tvStatus.text = res?.error ?: "Onaylanamadı (süre dolmuş olabilir)"
                }
            }
        }
    }

    private fun cancelRequest() {
        val reqId = currentRequestId ?: return
        progress.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            val res = repo.cancel(reqId, patientId)
            withContext(Dispatchers.Main) {
                progress.visibility = View.GONE
                if (res?.ok == true) {
                    tvStatus.text = "İstek iptal edildi"
                    btnConfirm.visibility = View.GONE
                    btnCancel.visibility = View.GONE
                    currentRequestId = null
                } else {
                    tvStatus.text = res?.error ?: "İptal edilemedi"
                }
            }
        }
    }

    private fun fetchLocationOnce(onReady: (() -> Unit)? = null) {
        progress.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            val loc = getCurrentLocationSuspend() // ✅ daha sağlam
            withContext(Dispatchers.Main) {
                progress.visibility = View.GONE
                if (loc != null) {
                    lastLatLng = loc
                    tvLocation.text = "Konum: ${loc.first}, ${loc.second}"
                    onReady?.invoke()
                } else {
                    tvStatus.text = "Konum alınamadı. GPS açık mı?"
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

    companion object {
        fun newInstance(patientId: Long): PatientHelpFragment {
            val f = PatientHelpFragment()
            f.arguments = Bundle().apply { putLong("patient_id", patientId) }
            return f
        }
    }
}