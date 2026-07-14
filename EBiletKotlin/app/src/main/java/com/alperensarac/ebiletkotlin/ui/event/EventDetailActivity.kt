package com.alperensarac.ebiletkotlin.ui.event

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alperensarac.ebiletkotlin.data.api.ApiClient
import com.alperensarac.ebiletkotlin.data.model.ApiResponse
import com.alperensarac.ebiletkotlin.data.model.Event
import com.alperensarac.ebiletkotlin.data.model.Ticket
import com.alperensarac.ebiletkotlin.data.session.SessionManager
import com.alperensarac.ebiletkotlin.databinding.ActivityEventDetailBinding
import com.alperensarac.ebiletkotlin.ui.ticket.MyTicketsActivity
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/*
    EventDetailActivity

    Bu ekran etkinlik detaylarını gösterir ve bilet satın alma işlemini yapar.

    HomeActivity'den buraya event_id gönderilecek.

    Intent ile gelen veri:
    - event_id

    Bu ekran açılınca:
    1. event_id alınır
    2. event_detail.php API'si çağrılır
    3. Gelen etkinlik bilgileri ekrana basılır
    4. Kullanıcı "Bilet Al" derse ticket_buy.php API'si çağrılır
*/
class EventDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEventDetailBinding

    private lateinit var sessionManager: SessionManager

    /*
        HomeActivity'den gelen etkinlik id.
    */
    private var eventId: Int = 0

    /*
        API'den gelen etkinlik bilgisini burada tutuyoruz.
        Bilet alırken bazı kontrollerde kullanacağız.
    */
    private var currentEvent: Event? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEventDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        /*
            Kullanıcı token yoksa bu ekran çalışmamalı.
            Normalde HomeActivity bunu garanti ediyor.
            Yine de güvenlik için kontrol ediyoruz.
        */
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Lütfen tekrar giriş yapın", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        /*
            HomeActivity'den gelen event_id değerini alıyoruz.
        */
        eventId = intent.getIntExtra(EXTRA_EVENT_ID, 0)

        if (eventId <= 0) {
            Toast.makeText(this, "Etkinlik bilgisi alınamadı", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupClickListeners()

        /*
            Ekran açılınca etkinlik detayını getiriyoruz.
        */
        loadEventDetail()
    }

    private fun setupClickListeners() {
        /*
            Geri butonu ekranı kapatır.
        */
        binding.btnBack.setOnClickListener {
            finish()
        }

        /*
            Bilet Al butonu.
        */
        binding.btnBuyTicket.setOnClickListener {
            buyTicket()
        }
    }

    /*
        Backend'den etkinlik detayını getirir.

        API:
        events/event_detail.php

        POST:
        - api_token
        - event_id
    */
    private fun loadEventDetail() {
        setStatus("Etkinlik detayı yükleniyor...")
        setLoading(true)

        val apiToken = sessionManager.getApiToken()

        ApiClient.apiService.getEventDetail(
            apiToken = apiToken,
            eventId = eventId
        ).enqueue(object : Callback<ApiResponse<Event>> {

            override fun onResponse(
                call: Call<ApiResponse<Event>>,
                response: Response<ApiResponse<Event>>
            ) {
                setLoading(false)

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

                val event = apiResponse.data

                if (event == null) {
                    setStatus("Etkinlik bilgisi alınamadı")
                    return
                }

                currentEvent = event

                bindEventToUI(event)

                setStatus("Etkinlik detayı getirildi.")
            }

            override fun onFailure(
                call: Call<ApiResponse<Event>>,
                t: Throwable
            ) {
                setLoading(false)
                setStatus("Bağlantı hatası: ${t.localizedMessage}")
            }
        })
    }

    /*
        API'den gelen Event nesnesini ekrandaki alanlara basar.
    */
    private fun bindEventToUI(event: Event) {
        binding.tvTopTitle.text = "Etkinlik Detayı"

        binding.tvEventTitle.text = event.title

        binding.tvDescription.text = event.description ?: "Açıklama bulunmuyor."


        /*
            Detay API'sinde city ve district nested olarak geliyor.
            Listeleme API'sinde cityName/districtName geliyordu.

            Bu yüzden iki ihtimali de kontrol ediyoruz.
        */
        val cityName = event.city?.name ?: event.cityName ?: "-"
        val districtName = event.district?.name ?: event.districtName ?: "-"

        binding.tvLocation.text = "Konum: $cityName / $districtName"

        val venueName = event.venue?.name ?: "Sahne bilgisi yok"
        val venueAddress = event.venue?.address ?: "Adres bilgisi yok"

        binding.tvVenue.text = "Sahne: $venueName"
        binding.tvAddress.text = "Adres: $venueAddress"

        binding.tvDate.text = "Tarih: ${event.eventDate ?: "-"}"
        binding.tvPrice.text = "${event.basePrice?.toInt() ?: 0} TL"
        binding.tvQuota.text = "Kalan: ${event.remainingQuota ?: 0}"

        if ((event.remainingQuota ?: 0) <= 0) {
            binding.btnBuyTicket.isEnabled = false
            binding.btnBuyTicket.text = "Kontenjan Doldu"
            binding.btnBuyTicket.setBackgroundColor(0xFF94A3B8.toInt())
        } else {
            binding.btnBuyTicket.isEnabled = true
            binding.btnBuyTicket.text = "Bilet Al"
        }

        /*
            Poster yükleme.

            Backend'den poster_url şu şekilde gelebilir:
            uploads/events/kadikoy_akustik.jpg

            Glide tam URL istediği için baseUrl ile birleştiriyoruz.
        */
        val baseUrl = "http://10.0.2.2/event_ticket_api/"
        val posterUrl = event.posterUrl

        if (!posterUrl.isNullOrEmpty()) {
            val finalPosterUrl = if (posterUrl.startsWith("http")) {
                posterUrl
            } else {
                baseUrl + posterUrl
            }

            Glide.with(this)
                .load(finalPosterUrl)
                .centerCrop()
                .into(binding.imgPoster)
        } else {
            binding.imgPoster.setImageDrawable(null)
        }
    }

    /*
        Bilet satın alma işlemi.

        API:
        tickets/ticket_buy.php

        POST:
        - api_token
        - event_id
    */
    private fun buyTicket() {
        val event = currentEvent

        if (event == null) {
            Toast.makeText(this, "Etkinlik bilgisi henüz yüklenmedi", Toast.LENGTH_SHORT).show()
            return
        }

        if ((event.remainingQuota ?: 0) <= 0) {
            Toast.makeText(this, "Bu etkinlik için kontenjan kalmamış", Toast.LENGTH_SHORT).show()
            return
        }

        setStatus("Bilet oluşturuluyor...")
        setBuying(true)

        val apiToken = sessionManager.getApiToken()

        ApiClient.apiService.buyTicket(
            apiToken = apiToken,
            eventId = event.id
        ).enqueue(object : Callback<ApiResponse<Ticket>> {

            override fun onResponse(
                call: Call<ApiResponse<Ticket>>,
                response: Response<ApiResponse<Ticket>>
            ) {
                setBuying(false)

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
                    Toast.makeText(
                        this@EventDetailActivity,
                        apiResponse.message,
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }

                val ticket = apiResponse.data

                if (ticket == null) {
                    setStatus("Bilet oluşturuldu fakat detay alınamadı")
                    Toast.makeText(
                        this@EventDetailActivity,
                        "Bilet oluşturuldu",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }

                /*
                    Bilet başarıyla oluşturuldu.

                    Şimdilik kullanıcıya ticket_code gösteriyoruz.
                    Bir sonraki adımda Biletlerim ekranı ve QR ekranı yapacağız.
                */
                setStatus("Bilet başarıyla oluşturuldu.")

                Toast.makeText(
                    this@EventDetailActivity,
                    "Bilet alındı: ${ticket.ticketCode}",
                    Toast.LENGTH_LONG
                ).show()

                /*
                    Bilet alındıktan sonra ekrandaki kontenjanı 1 düşürmek için
                    etkinlik detayını tekrar çekiyoruz.

                    Çünkü backend sold_count değerini artırdı.
                */
                loadEventDetail()

                /*
                    Bir sonraki adımda burada MyTicketsActivity'ye yönlendirebiliriz:

                    val intent = Intent(this@EventDetailActivity, MyTicketsActivity::class.java)
                    startActivity(intent)
                */
                val intent = Intent(this@EventDetailActivity, MyTicketsActivity::class.java)
                startActivity(intent)
            }

            override fun onFailure(
                call: Call<ApiResponse<Ticket>>,
                t: Throwable
            ) {
                setBuying(false)
                setStatus("Bağlantı hatası: ${t.localizedMessage}")
            }
        })
    }

    /*
        Etkinlik detayı yüklenirken butonları kontrol eder.
    */
    private fun setLoading(isLoading: Boolean) {
        binding.btnBuyTicket.isEnabled = !isLoading
    }

    /*
        Bilet alma sırasında butonu kapatır.
    */
    private fun setBuying(isBuying: Boolean) {
        binding.btnBuyTicket.isEnabled = !isBuying

        binding.btnBuyTicket.text = if (isBuying) {
            "Bilet Oluşturuluyor..."
        } else {
            "Bilet Al"
        }
    }

    private fun setStatus(message: String) {
        binding.tvStatus.text = message
    }

    companion object {
        /*
            HomeActivity'den event_id gönderirken bu key'i kullanacağız.

            Kullanım:
            intent.putExtra(EventDetailActivity.EXTRA_EVENT_ID, event.id)
        */
        const val EXTRA_EVENT_ID = "event_id"
    }
}