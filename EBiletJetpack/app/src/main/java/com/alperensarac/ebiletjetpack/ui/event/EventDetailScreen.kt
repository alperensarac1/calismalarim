package com.alperensarac.ebiletjetpack.ui.event


import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.alperensarac.ebiletjetpack.data.api.ApiClient
import com.alperensarac.ebiletjetpack.data.model.ApiResponse
import com.alperensarac.ebiletjetpack.data.model.Event
import com.alperensarac.ebiletjetpack.data.model.Ticket
import com.alperensarac.ebiletjetpack.data.session.SessionManager
import com.alperensarac.ebiletjetpack.ui.components.AppBackground
import com.alperensarac.ebiletjetpack.ui.components.AppBlue
import com.alperensarac.ebiletjetpack.ui.components.AppGreen
import com.alperensarac.ebiletjetpack.ui.components.AppSmallWhiteButton
import com.alperensarac.ebiletjetpack.ui.components.AppTextDark
import com.alperensarac.ebiletjetpack.ui.components.AppTextGray
import com.alperensarac.ebiletjetpack.ui.components.AppTopBar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/*
    EventDetailScreen

    Etkinlik detay ekranı.

    HomeScreen'den buraya sadece eventId gelir.

    Neden sadece eventId?
    Çünkü detay ekranında en güncel veriyi backend'den almak daha doğru.
    Örneğin:
    - Kontenjan değişmiş olabilir.
    - Etkinlik pasife alınmış olabilir.
    - Fiyat güncellenmiş olabilir.

    Kullanılan API'ler:

    1. events/event_detail.php
       Etkinlik detayını getirir.

    2. tickets/ticket_buy.php
       Kullanıcı için bilet oluşturur.
*/
@Composable
fun EventDetailScreen(
    eventId: Int,
    onBack: () -> Unit,
    onTicketBought: () -> Unit
) {
    val context = LocalContext.current

    val sessionManager = remember {
        SessionManager(context)
    }

    /*
        Backend'den gelen etkinlik.
    */
    var event by remember { mutableStateOf<Event?>(null) }

    /*
        Genel durum mesajı.
    */
    var statusMessage by remember { mutableStateOf("Etkinlik detayı yükleniyor...") }

    /*
        Detay yükleniyor mu?
    */
    var isLoading by remember { mutableStateOf(false) }

    /*
        Bilet satın alma isteği devam ediyor mu?
    */
    var isBuying by remember { mutableStateOf(false) }

    /*
        Bu fonksiyon etkinlik detayını backend'den çeker.

        Compose içinde aynı kodu hem ilk açılışta hem de bilet aldıktan sonra
        tekrar kullanmak isteyebiliriz. Bu yüzden local fonksiyon olarak yazıyoruz.
    */
    fun loadEventDetail() {
        isLoading = true
        statusMessage = "Etkinlik detayı yükleniyor..."

        ApiClient.apiService
            .getEventDetail(
                apiToken = sessionManager.getApiToken(),
                eventId = eventId
            )
            .enqueue(object : Callback<ApiResponse<Event>> {

                override fun onResponse(
                    call: Call<ApiResponse<Event>>,
                    response: Response<ApiResponse<Event>>
                ) {
                    isLoading = false

                    if (!response.isSuccessful) {
                        statusMessage = "Sunucu hatası: ${response.code()}"
                        return
                    }

                    val apiResponse = response.body()

                    if (apiResponse == null) {
                        statusMessage = "Boş sunucu cevabı"
                        return
                    }

                    if (!apiResponse.success) {
                        statusMessage = apiResponse.message
                        return
                    }

                    val loadedEvent = apiResponse.data

                    if (loadedEvent == null) {
                        statusMessage = "Etkinlik bilgisi alınamadı"
                        return
                    }

                    event = loadedEvent
                    statusMessage = "Etkinlik detayı getirildi."
                }

                override fun onFailure(
                    call: Call<ApiResponse<Event>>,
                    t: Throwable
                ) {
                    isLoading = false
                    statusMessage = "Bağlantı hatası: ${t.localizedMessage}"
                }
            })
    }

    /*
        Ekran ilk açıldığında detay çekilir.
    */
    LaunchedEffect(eventId) {
        if (eventId <= 0) {
            statusMessage = "Etkinlik bilgisi alınamadı"
            return@LaunchedEffect
        }

        loadEventDetail()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        AppTopBar(
            title = "Etkinlik Detayı",
            subtitle = event?.title,
            actions = {
                AppSmallWhiteButton(
                    text = "Geri",
                    onClick = onBack
                )
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = statusMessage,
                color = AppTextGray
            )

            if (isLoading && event == null) {
                LoadingCard()
            }

            val currentEvent = event

            if (currentEvent != null) {
                EventDetailContent(
                    event = currentEvent,
                    isBuying = isBuying,
                    onBuyTicket = {
                        val remainingQuota = currentEvent.remainingQuota ?: 0

                        if (remainingQuota <= 0) {
                            Toast.makeText(
                                context,
                                "Bu etkinlik için kontenjan kalmamış",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@EventDetailContent
                        }

                        isBuying = true
                        statusMessage = "Bilet oluşturuluyor..."

                        ApiClient.apiService
                            .buyTicket(
                                apiToken = sessionManager.getApiToken(),
                                eventId = currentEvent.id
                            )
                            .enqueue(object : Callback<ApiResponse<Ticket>> {

                                override fun onResponse(
                                    call: Call<ApiResponse<Ticket>>,
                                    response: Response<ApiResponse<Ticket>>
                                ) {
                                    isBuying = false

                                    if (!response.isSuccessful) {
                                        statusMessage = "Sunucu hatası: ${response.code()}"
                                        return
                                    }

                                    val apiResponse = response.body()

                                    if (apiResponse == null) {
                                        statusMessage = "Boş sunucu cevabı"
                                        return
                                    }

                                    if (!apiResponse.success) {
                                        statusMessage = apiResponse.message

                                        Toast.makeText(
                                            context,
                                            apiResponse.message,
                                            Toast.LENGTH_LONG
                                        ).show()

                                        return
                                    }

                                    val ticket = apiResponse.data
                                    val ticketCode = ticket?.ticketCode ?: "-"

                                    Toast.makeText(
                                        context,
                                        "Bilet alındı: $ticketCode",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    statusMessage = "Bilet başarıyla oluşturuldu."

                                    /*
                                        Kullanıcı biletini QR ile görebilsin diye
                                        Biletlerim ekranına yönlendiriyoruz.
                                    */
                                    onTicketBought()
                                }

                                override fun onFailure(
                                    call: Call<ApiResponse<Ticket>>,
                                    t: Throwable
                                ) {
                                    isBuying = false
                                    statusMessage = "Bağlantı hatası: ${t.localizedMessage}"
                                }
                            })
                    }
                )
            }
        }
    }
}

/*
    Detay yüklenirken gösterilen basit kart.
*/
@Composable
private fun LoadingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Yükleniyor...",
                color = AppTextGray
            )
        }
    }
}

/*
    Etkinlik detay içeriği.

    Bu composable sadece UI basar.
    API isteği yapmaz.

    Böyle ayırmamızın sebebi:
    - Ana screen state ve API yönetir.
    - Content sadece ekrana veri basar.
    - Kod okunabilir olur.
*/
@Composable
private fun EventDetailContent(
    event: Event,
    isBuying: Boolean,
    onBuyTicket: () -> Unit
) {
    val posterUrl = event.posterUrl?.let { poster ->
        if (poster.startsWith("http")) {
            poster
        } else {
            ApiClient.BASE_URL + poster
        }
    }

    AsyncImage(
        model = posterUrl,
        contentDescription = event.title,
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .background(
                color = Color(0xFFE2E8F0),
                shape = RoundedCornerShape(18.dp)
            )
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AppTextDark
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = event.description ?: "Açıklama bulunmuyor.",
                color = Color(0xFF475569)
            )

            Spacer(modifier = Modifier.height(18.dp))

            DetailLine(
                title = "Tarih",
                value = event.eventDate ?: "-"
            )

            val cityName = event.city?.name ?: event.cityName ?: "-"
            val districtName = event.district?.name ?: event.districtName ?: "-"

            DetailLine(
                title = "Konum",
                value = "$cityName / $districtName"
            )

            DetailLine(
                title = "Sahne",
                value = event.venue?.name ?: "-"
            )

            DetailLine(
                title = "Adres",
                value = event.venue?.address ?: "-"
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${event.basePrice?.toInt() ?: 0} TL",
                    color = AppGreen,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .background(
                            color = Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(
                            horizontal = 12.dp,
                            vertical = 8.dp
                        )
                ) {
                    Text(
                        text = "Kalan: ${event.remainingQuota ?: 0}",
                        color = AppBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            val remainingQuota = event.remainingQuota ?: 0
            val canBuy = remainingQuota > 0 && !isBuying

            Button(
                onClick = onBuyTicket,
                enabled = canBuy,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (remainingQuota > 0) {
                        AppGreen
                    } else {
                        Color(0xFF94A3B8)
                    },
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = when {
                        isBuying -> "Bilet Oluşturuluyor..."
                        remainingQuota <= 0 -> "Kontenjan Doldu"
                        else -> "Bilet Al"
                    }
                )
            }
        }
    }
}

/*
    Detay satırı.

    Örnek:
    Tarih: 2026-07-10 20:30:00
*/
@Composable
private fun DetailLine(
    title: String,
    value: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {
        Text(
            text = title,
            color = AppTextGray,
            style = MaterialTheme.typography.bodySmall
        )

        Text(
            text = value,
            color = AppTextDark,
            fontWeight = FontWeight.Bold
        )
    }
}