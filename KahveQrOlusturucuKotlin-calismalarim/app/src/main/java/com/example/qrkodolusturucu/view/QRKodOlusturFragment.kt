package com.example.qrkodolusturucu.view

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.qrkodolusturucu.data.numara_kayit.NumaraKayitSp
import com.example.qrkodolusturucu.databinding.FragmentQRKodOlusturBinding
import com.example.qrkodolusturucu.services.Services
import com.example.qrkodolusturucu.services.ServicesImpl
import com.example.qrkodolusturucu.usecases.DogrulamaKoduOlustur
import com.example.qrkodolusturucu.viewmodel.QRKodOlusturVM
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
@AndroidEntryPoint
class QRKodOlusturFragment : Fragment() {

    private val qrKodOlusturVM: QRKodOlusturVM by viewModels()
    private lateinit var binding: FragmentQRKodOlusturBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentQRKodOlusturBinding.inflate(inflater, container, false)

        // Doğrulama kodu değişimini takip et ve QR kodu oluştur
        qrKodOlusturVM.dogrulamaKoduLiveData.observe(viewLifecycleOwner) { yeniKod ->
            Log.d("QRKodOlusturFragment", "Yeni doğrulama kodu alındı: $yeniKod")
            qrKodOlusturVM.QRKodOlustur(yeniKod) // **BURADA QR KODU OLUŞTUR**
            qrKodOlusturVM.koduOlustur(yeniKod) // **SONRA KAYDETME İŞLEMİNİ BAŞLAT**
        }

        qrKodOlusturVM.qrKodLiveData.observe(viewLifecycleOwner) { bitmap ->
            binding.idIVQrcode.setImageBitmap(bitmap)
        }

        qrKodOlusturVM.kalanSureLiveData.observe(viewLifecycleOwner) { kalanSure ->
            binding.tvKalanSure.text = "Yenileme için kalan süre: $kalanSure"
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        qrKodOlusturVM.yeniKodUret()
    }
}
