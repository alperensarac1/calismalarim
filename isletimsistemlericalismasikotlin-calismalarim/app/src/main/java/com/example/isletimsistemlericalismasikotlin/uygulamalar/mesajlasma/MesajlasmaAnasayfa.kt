package com.example.isletimsistemlericalismasikotlin.uygulamalar.mesajlasma

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.isletimsistemlericalismasikotlin.R
import com.example.isletimsistemlericalismasikotlin.databinding.FragmentMesajlasmaAnasayfaBinding
import com.google.android.material.snackbar.Snackbar

class MesajlasmaAnasayfa : Fragment() {

    private val permissionRequestSendSms = 105
    private lateinit var binding: FragmentMesajlasmaAnasayfaBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMesajlasmaAnasayfaBinding.inflate(inflater, container, false)

        sendSMS()

        binding.btnMesajGonder.setOnClickListener { view ->
            val mesaj = binding.etTelefonMesaj.text.toString()
            val gonderilecekNumara = binding.etTelefonNumara.text.toString()

            /* Intent ile SMS uygulamasını açmak için:
            val smsSayfasinaGecis = Intent(Intent.ACTION_SENDTO, Uri.parse("sms:$gonderilecekNumara"))
            smsSayfasinaGecis.putExtra("sms_body", mesaj)
            startActivity(smsSayfasinaGecis)
            */

            val sms = SmsManager.getDefault()
            val ist = sms.divideMessage(mesaj)
            sms.sendMultipartTextMessage(gonderilecekNumara, null, ist, null, null)

            binding.etTelefonMesaj.text.clear()
            binding.etTelefonNumara.text.clear()

            Snackbar.make(view, "Mesaj Gönderildi", Snackbar.LENGTH_SHORT).show()
        }

        return binding.root
    }

    private fun sendSMS() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(), Manifest.permission.SEND_SMS
                )
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(), arrayOf(Manifest.permission.SEND_SMS), permissionRequestSendSms
                )
            }
        }
    }
}
