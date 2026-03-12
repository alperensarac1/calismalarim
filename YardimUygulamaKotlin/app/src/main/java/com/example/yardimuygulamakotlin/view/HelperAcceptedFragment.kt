package com.example.yardimuygulamakotlin.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.yardimuygulamakotlin.R
import com.example.yardimuygulamakotlin.model.AcceptedHelpItem
import com.example.yardimuygulamakotlin.repo.HelperRepo
import com.example.yardimuygulamakotlin.service.Poller
import com.example.yardimuygulamakotlin.util.TimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class HelperAcceptedFragment : Fragment(R.layout.fragment_helper_accepted) {

    private var helperId: Long = 0L
    private val repo = HelperRepo()
    private var hadAcceptedBefore = false
    private lateinit var tvTimer: TextView
    private lateinit var tvPatient: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvService: TextView
    private lateinit var tvRoom: TextView
    private lateinit var btnDial: Button

    private var current: AcceptedHelpItem? = null
    private var poller: Poller? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        helperId = requireArguments().getLong("helper_id")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tvTimer = view.findViewById(R.id.tvTimer)
        tvPatient = view.findViewById(R.id.tvPatient)
        tvPhone = view.findViewById(R.id.tvPhone)
        tvService = view.findViewById(R.id.tvService)
        tvRoom = view.findViewById(R.id.tvRoom)
        btnDial = view.findViewById(R.id.btnDial)

        btnDial.setOnClickListener {
            val phone = current?.patient_phone ?: return@setOnClickListener
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
        }

        poller = Poller(viewLifecycleOwner.lifecycleScope, intervalMs = 3000) {
            fetchAccepted()
        }
    }

    override fun onStart() {
        super.onStart()
        poller?.start()
    }

    override fun onStop() {
        super.onStop()
        poller?.stop()
    }

    private suspend fun fetchAccepted() {
        val res = repo.myAccepted(helperId)
        val first = res?.items?.firstOrNull()

        withContext(Dispatchers.Main) {
            if (res?.ok == true && first != null) {
                hadAcceptedBefore = true
                current = first

                tvPatient.text = "Hasta: ${first.patient_name} (${first.patient_age ?: "-"})"
                tvPhone.text = "Telefon: ${first.patient_phone}"
                tvService.text = "Servis: ${first.servis_adi}"
                tvRoom.text = "Oda: ${first.oda_no}"

                tvTimer.text = "Kalan süre: ${TimeUtils.formatRemainingSeconds(first.remaining_seconds)}"

            } else {
                // Daha önce ACCEPTED vardı ama artık yok -> confirmed veya timeout
                if (hadAcceptedBefore) {
                    android.widget.Toast.makeText(
                        requireContext(),
                        "Hasta onayladı veya süre doldu. Listeye dönülüyor.",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
                parentFragmentManager.popBackStack() // open list'e geri
            }
        }
    }

    private fun formatRemainingSeconds(sec: Int): String {
        val s = if (sec < 0) 0 else sec
        val mm = s / 60
        val ss = s % 60
        return String.format(Locale.US, "%02d:%02d", mm, ss)
    }

    companion object {
        fun newInstance(helperId: Long): HelperAcceptedFragment {
            val f = HelperAcceptedFragment()
            f.arguments = Bundle().apply { putLong("helper_id", helperId) }
            return f
        }
    }
}