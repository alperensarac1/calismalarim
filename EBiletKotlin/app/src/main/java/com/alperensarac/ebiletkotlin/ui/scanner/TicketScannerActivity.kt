package com.alperensarac.ebiletkotlin.ui.scanner

import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.alperensarac.ebiletkotlin.data.api.ApiClient
import com.alperensarac.ebiletkotlin.data.model.ApiResponse
import com.alperensarac.ebiletkotlin.data.model.Ticket
import com.alperensarac.ebiletkotlin.data.session.SessionManager
import com.alperensarac.ebiletkotlin.databinding.ActivityTicketScannerBinding
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/*
    TicketScannerActivity

    Görevli / admin kullanıcının QR kod ile bilet kontrol ettiği ekrandır.

    Bu ekran normal kullanıcıya açık değildir.
    HomeActivity zaten role kontrolü yapacak.
    Yine de burada ekstra kontrol yapıyoruz.

    Kullanılan kütüphane:
    com.journeyapps:zxing-android-embedded

    QR okutma sonucu bize text olarak döner.
    Biz bu text'i ticket_code kabul edip backend'e göndeririz.

    Backend:
    check/ticket_check.php

    POST:
    - api_token
    - ticket_code
*/
class TicketScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTicketScannerBinding

    private lateinit var sessionManager: SessionManager

    /*
        QR scanner result launcher.

        Modern Android'de startActivityForResult yerine
        Activity Result API kullanıyoruz.
    */
    private lateinit var qrLauncher: ActivityResultLauncher<ScanOptions>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTicketScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        /*
            Bu ekran sadece staff veya admin için açık.
        */
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Lütfen tekrar giriş yapın", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (!sessionManager.isStaffOrAdmin()) {
            Toast.makeText(this, "Bu ekran için görevli yetkisi gerekir", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupScannerLauncher()
        setupUi()
        setupClickListeners()
    }

    /*
        QR okutma sonucunu dinleyen launcher.
    */
    private fun setupScannerLauncher() {
        qrLauncher = registerForActivityResult(ScanContract()) { result ->

            /*
                result.contents:
                QR içinde okunan metindir.

                Kullanıcı geri basarsa veya iptal ederse null gelebilir.
            */
            val qrContent = result.contents

            if (qrContent.isNullOrEmpty()) {
                setStatus("QR okuma iptal edildi veya boş sonuç döndü.")
                return@registerForActivityResult
            }

            /*
                Okunan QR kodu ekrandaki manuel input'a da basıyoruz.
                Böylece görevli hangi kodun okunduğunu görebilir.
            */
            binding.etTicketCode.setText(qrContent)

            /*
                Okunan kodu backend'e gönderiyoruz.
            */
            checkTicket(qrContent)
        }
    }

    private fun setupUi() {
        binding.tvStaffInfo.text =
            "Görevli: ${sessionManager.getFullName()} | Rol: ${sessionManager.getRole()}"

        clearResult()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnScanQr.setOnClickListener {
            openQrScanner()
        }

        binding.btnManualCheck.setOnClickListener {
            val ticketCode = binding.etTicketCode.text.toString().trim()

            binding.tilTicketCode.error = null

            if (ticketCode.isEmpty()) {
                binding.tilTicketCode.error = "Bilet kodu zorunludur"
                return@setOnClickListener
            }

            checkTicket(ticketCode)
        }
    }

    /*
        QR scanner ekranını açar.
    */
    private fun openQrScanner() {
        val options = ScanOptions()

        /*
            QR ve barkod formatlarından sadece QR kod okutmak istiyoruz.
        */
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)

        options.setPrompt("Bilet QR kodunu kamera alanına getir")
        options.setBeepEnabled(true)
        options.setOrientationLocked(false)

        /*
            true:
            Tarama sonucu otomatik yakalanır.
        */
        options.setBarcodeImageEnabled(false)

        qrLauncher.launch(options)
    }

    /*
        Bilet kodunu backend'e gönderip kontrol eder.
    */
    private fun checkTicket(ticketCode: String) {
        setLoading(true)
        setStatus("Bilet kontrol ediliyor...")

        clearResult()

        val apiToken = sessionManager.getApiToken()

        ApiClient.apiService.checkTicket(
            apiToken = apiToken,
            ticketCode = ticketCode
        ).enqueue(object : Callback<ApiResponse<Ticket>> {

            override fun onResponse(
                call: Call<ApiResponse<Ticket>>,
                response: Response<ApiResponse<Ticket>>
            ) {
                setLoading(false)

                if (!response.isSuccessful) {
                    showFailedResult(
                        title = "Sunucu Hatası",
                        message = "HTTP hata kodu: ${response.code()}"
                    )
                    return
                }

                val apiResponse = response.body()

                if (apiResponse == null) {
                    showFailedResult(
                        title = "Boş Cevap",
                        message = "Sunucudan boş cevap döndü."
                    )
                    return
                }

                /*
                    Burada dikkat:

                    Backend success false döndürdüğünde bile
                    mesajı ve extra sonucu anlamlıdır.
                    Ancak Retrofit ApiResponse<Ticket> içinde extra Any? olduğu için
                    detaylı result bilgisini güvenli parse etmiyoruz.

                    Yine de message değerini net gösteriyoruz.
                */
                if (!apiResponse.success) {
                    showFailedResult(
                        title = "Giriş Reddedildi",
                        message = apiResponse.message
                    )
                    return
                }

                val ticket = apiResponse.data

                if (ticket == null) {
                    showSuccessResult(
                        title = "Bilet Onaylandı",
                        message = apiResponse.message
                    )
                    return
                }

                showTicketResult(
                    apiMessage = apiResponse.message,
                    ticket = ticket
                )
            }

            override fun onFailure(call: Call<ApiResponse<Ticket>>, t: Throwable) {
                setLoading(false)

                showFailedResult(
                    title = "Bağlantı Hatası",
                    message = t.localizedMessage ?: "Sunucuya ulaşılamadı."
                )
            }
        })
    }

    /*
        Başarılı bilet sonucunu ekrana basar.
    */
    private fun showTicketResult(apiMessage: String, ticket: Ticket) {
        /*
            Backend başarılı onayda result = approved döndürüyor.
        */
        val result = ticket.result ?: "approved"

        if (result == "approved") {
            showSuccessResult(
                title = "Giriş Onaylandı",
                message = apiMessage
            )
        } else {
            showFailedResult(
                title = "Giriş Reddedildi",
                message = apiMessage
            )
        }

        binding.tvTicketInfo.text = """
            Bilet ID: ${ticket.ticketId ?: "-"}
            Bilet Kodu: ${ticket.ticketCode ?: "-"}
            Durum: ${ticket.ticketStatus ?: ticket.status ?: "-"}
        """.trimIndent()

        binding.tvUserInfo.text = """
            Kullanıcı: ${ticket.user?.fullName ?: "-"}
            E-posta: ${ticket.user?.email ?: "-"}
            Telefon: ${ticket.user?.phone ?: "-"}
        """.trimIndent()

        binding.tvEventInfo.text = """
            Etkinlik: ${ticket.event?.title ?: ticket.eventTitle ?: "-"}
            Tarih: ${ticket.event?.eventDate ?: "-"}
        """.trimIndent()

        binding.tvLocationInfo.text = """
            Konum: ${ticket.location?.cityName ?: "-"} / ${ticket.location?.districtName ?: "-"}
            Sahne: ${ticket.location?.venueName ?: "-"}
            Adres: ${ticket.location?.venueAddress ?: "-"}
        """.trimIndent()
    }

    /*
        Başarılı görsel sonuç.
    */
    private fun showSuccessResult(title: String, message: String) {
        binding.tvResultTitle.text = title
        binding.tvResultMessage.text = message

        binding.tvResultTitle.setTextColor(0xFF166534.toInt())
        binding.resultCard.setBackgroundColor(0xFFDCFCE7.toInt())

        setStatus("Kontrol tamamlandı.")
    }

    /*
        Başarısız görsel sonuç.
    */
    private fun showFailedResult(title: String, message: String) {
        binding.tvResultTitle.text = title
        binding.tvResultMessage.text = message

        binding.tvResultTitle.setTextColor(0xFF991B1B.toInt())
        binding.resultCard.setBackgroundColor(0xFFFEE2E2.toInt())

        binding.tvTicketInfo.text = ""
        binding.tvUserInfo.text = ""
        binding.tvEventInfo.text = ""
        binding.tvLocationInfo.text = ""

        setStatus("Kontrol tamamlandı.")
    }

    /*
        Sonuç alanını ilk hale getirir.
    */
    private fun clearResult() {
        binding.tvResultTitle.text = "Henüz kontrol yapılmadı"
        binding.tvResultMessage.text = "QR kod okutulduğunda sonuç burada görünecek."

        binding.tvResultTitle.setTextColor(0xFF0F172A.toInt())
        binding.resultCard.setBackgroundColor(0xFFFFFFFF.toInt())

        binding.tvTicketInfo.text = ""
        binding.tvUserInfo.text = ""
        binding.tvEventInfo.text = ""
        binding.tvLocationInfo.text = ""
    }

    /*
        API isteği sırasında butonları kapatır.
    */
    private fun setLoading(isLoading: Boolean) {
        binding.btnScanQr.isEnabled = !isLoading
        binding.btnManualCheck.isEnabled = !isLoading

        binding.btnScanQr.text = if (isLoading) {
            "Kontrol Ediliyor..."
        } else {
            "QR Kod Okut"
        }
    }

    private fun setStatus(message: String) {
        binding.tvStatus.text = message
    }
}