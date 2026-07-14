package com.alperensarac.ebiletkotlin.ui.ticket

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alperensarac.ebiletkotlin.data.api.ApiClient
import com.alperensarac.ebiletkotlin.data.model.ApiResponse
import com.alperensarac.ebiletkotlin.data.model.Ticket
import com.alperensarac.ebiletkotlin.data.session.SessionManager
import com.alperensarac.ebiletkotlin.databinding.ActivityTicketDetailBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/*
    TicketDetailActivity

    Kullanıcıya tek biletin detayını ve QR kodunu gösterir.

    Backend:
    tickets/ticket_detail.php

    POST:
    - api_token
    - ticket_id

    QR kod içinde backend'den gelen qr_code_text değeri bulunur.
    Görevli bu QR kodu okuttuğunda ticket_check.php çalışacak.
*/
class TicketDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTicketDetailBinding

    private lateinit var sessionManager: SessionManager

    private var ticketId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTicketDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Lütfen tekrar giriş yapın", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        ticketId = intent.getIntExtra(EXTRA_TICKET_ID, 0)

        if (ticketId <= 0) {
            Toast.makeText(this, "Bilet bilgisi alınamadı", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        loadTicketDetail()
    }

    /*
        Bilet detayını backend'den alır.
    */
    private fun loadTicketDetail() {
        setStatus("Bilet detayı yükleniyor...")

        val apiToken = sessionManager.getApiToken()

        ApiClient.apiService.getTicketDetail(
            apiToken = apiToken,
            ticketId = ticketId
        ).enqueue(object : Callback<ApiResponse<Ticket>> {

            override fun onResponse(
                call: Call<ApiResponse<Ticket>>,
                response: Response<ApiResponse<Ticket>>
            ) {
                if (!response.isSuccessful) {
                    setStatus("Sunucu hatası: ${response.code()}")
                    return
                }

                val apiResponse = response.body()

                if (apiResponse == null) {
                    setStatus("Boş sunucu cevabı")
                    return
                }

                if (!apiResponse.success) {
                    setStatus(apiResponse.message)
                    return
                }

                val ticket = apiResponse.data

                if (ticket == null) {
                    setStatus("Bilet bilgisi alınamadı")
                    return
                }

                bindTicketToUI(ticket)

                setStatus("Bilet detayı getirildi.")
            }

            override fun onFailure(
                call: Call<ApiResponse<Ticket>>,
                t: Throwable
            ) {
                setStatus("Bağlantı hatası: ${t.localizedMessage}")
            }
        })
    }

    /*
        Ticket verisini ekrana basar.
    */
    private fun bindTicketToUI(ticket: Ticket) {
        val event = ticket.event

        binding.tvEventTitle.text = event?.title ?: ticket.eventTitle ?: "Etkinlik"

        val status = ticket.status ?: ticket.ticketStatus ?: "-"

        val statusText = when (status) {
            "active" -> "Aktif Bilet"
            "used" -> "Kullanıldı"
            "cancelled" -> "İptal Edildi"
            else -> status
        }

        binding.tvTicketStatus.text = statusText

        when (status) {
            "active" -> {
                binding.tvTicketStatus.setBackgroundColor(0xFFDCFCE7.toInt())
                binding.tvTicketStatus.setTextColor(0xFF166534.toInt())
            }

            "used" -> {
                binding.tvTicketStatus.setBackgroundColor(0xFFE2E8F0.toInt())
                binding.tvTicketStatus.setTextColor(0xFF475569.toInt())
            }

            "cancelled" -> {
                binding.tvTicketStatus.setBackgroundColor(0xFFFEE2E2.toInt())
                binding.tvTicketStatus.setTextColor(0xFF991B1B.toInt())
            }
        }

        binding.tvTicketCode.text = ticket.ticketCode ?: "-"

        binding.tvDate.text = "Tarih: ${event?.eventDate ?: "-"}"

        /*
            Bilet detay PHP cevabında venue/city/district ayrı nested gelebilir.
            Bizim Ticket modelimizde şimdilik location nested alanı daha çok my_tickets cevabı için var.
            Bu yüzden eksik görünürse birazdan model/backend uyumunu genişleteceğiz.

            ticket_detail.php cevabında event, city, district, venue ayrı dönüyordu.
            Ticket modelinde city/district/venue yoksa onları eklememiz gerekir.

            Aşağıdaki satırlar location üzerinden çalışır.
        */
        binding.tvVenue.text = "Sahne: ${ticket.location?.venueName ?: event?.venue?.name ?: "-"}"

        val cityName = ticket.location?.cityName ?: event?.city?.name ?: "-"
        val districtName = ticket.location?.districtName ?: event?.district?.name ?: "-"
        binding.tvLocation.text = "Konum: $cityName / $districtName"

        binding.tvPrice.text = "Fiyat: ${ticket.price?.toInt() ?: 0} TL"

        if (!ticket.usedAt.isNullOrEmpty()) {
            binding.tvUsedAt.text = "Kullanım zamanı: ${ticket.usedAt}"
        } else {
            binding.tvUsedAt.text = "Bilet henüz kullanılmadı."
        }

        /*
            QR kod üretimi.

            QR içine qr_code_text yazıyoruz.
            Eğer qr_code_text boşsa ticket_code kullanıyoruz.
        */
        val qrText = ticket.qrCodeText ?: ticket.ticketCode

        if (qrText.isNullOrEmpty()) {
            setStatus("QR kod oluşturulamadı. Bilet kodu boş.")
            binding.imgQrCode.setImageDrawable(null)
        } else {
            val qrBitmap = generateQrBitmap(qrText, 800, 800)
            binding.imgQrCode.setImageBitmap(qrBitmap)
        }
    }

    /*
        QR kod Bitmap üretir.

        ZXing core kütüphanesini kullanıyoruz.

        text:
        QR içine yazılacak değer.

        width / height:
        Üretilecek Bitmap boyutu.
    */
    private fun generateQrBitmap(text: String, width: Int, height: Int): Bitmap {
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(
            text,
            BarcodeFormat.QR_CODE,
            width,
            height
        )

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                val color = if (bitMatrix[x, y]) {
                    Color.BLACK
                } else {
                    Color.WHITE
                }

                bitmap.setPixel(x, y, color)
            }
        }

        return bitmap
    }

    private fun setStatus(message: String) {
        binding.tvStatus.text = message
    }

    companion object {
        const val EXTRA_TICKET_ID = "ticket_id"
    }
}