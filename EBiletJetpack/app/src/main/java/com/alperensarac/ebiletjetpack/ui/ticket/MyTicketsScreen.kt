package com.alperensarac.ebiletjetpack.ui.ticket

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
    MyTicketsScreen

    Kullanıcının satın aldığı biletleri listeler.

    Backend:
    tickets/my_tickets.php

    POST:
    api_token

    XML tarafındaki RecyclerView karşılığı Compose'ta LazyColumn'dır.
*/
@Composable
fun MyTicketsScreen(
    onBack: () -> Unit,
    onOpenTicketDetail: (Int) -> Unit
) {
    val context = LocalContext.current

    val sessionManager = remember {
        SessionManager(context)
    }

    var tickets by remember { mutableStateOf<List<Ticket>>(emptyList()) }

    var statusMessage by remember {
        mutableStateOf("Biletler yükleniyor...")
    }

    var isLoading by remember {
        mutableStateOf(false)
    }

    /*
        Biletleri backend'den çeken fonksiyon.
    */
    fun loadMyTickets() {
        isLoading = true
        statusMessage = "Biletler yükleniyor..."

        ApiClient.apiService
            .getMyTickets(sessionManager.getApiToken())
            .enqueue(object : Callback<ApiResponse<List<Ticket>>> {

                override fun onResponse(
                    call: Call<ApiResponse<List<Ticket>>>,
                    response: Response<ApiResponse<List<Ticket>>>
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

                    tickets = apiResponse.data ?: emptyList()

                    statusMessage = if (tickets.isEmpty()) {
                        "Henüz satın alınmış biletin yok."
                    } else {
                        "${tickets.size} bilet listelendi."
                    }
                }

                override fun onFailure(
                    call: Call<ApiResponse<List<Ticket>>>,
                    t: Throwable
                ) {
                    isLoading = false
                    statusMessage = "Bağlantı hatası: ${t.localizedMessage}"
                }
            })
    }

    /*
        Ekran açılınca biletleri yükle.
    */
    LaunchedEffect(Unit) {
        loadMyTickets()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        AppTopBar(
            title = "Biletlerim",
            subtitle = "Satın aldığın biletler",
            actions = {
                AppSmallWhiteButton(
                    text = "Geri",
                    onClick = onBack
                )
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = if (isLoading) "Yükleniyor..." else statusMessage,
                    color = AppTextGray
                )
            }

            items(tickets) { ticket ->
                TicketCard(
                    ticket = ticket,
                    onClick = {
                        val ticketId = ticket.ticketId

                        if (ticketId != null && ticketId > 0) {
                            onOpenTicketDetail(ticketId)
                        }
                    }
                )
            }
        }
    }
}

/*
    Tek bilet kartı.

    XML item_ticket.xml yerine Compose Card kullanıyoruz.
*/
@Composable
private fun TicketCard(
    ticket: Ticket,
    onClick: () -> Unit
) {
    val event = ticket.event

    val posterUrl = event?.posterUrl?.let { poster ->
        if (poster.startsWith("http")) {
            poster
        } else {
            ApiClient.BASE_URL + poster
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp)
        ) {
            AsyncImage(
                model = posterUrl,
                contentDescription = ticket.eventTitle ?: event?.title ?: "Bilet",
                modifier = Modifier
                    .width(95.dp)
                    .height(120.dp)
                    .background(
                        color = Color(0xFFE2E8F0),
                        shape = RoundedCornerShape(12.dp)
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = event?.title ?: ticket.eventTitle ?: "Etkinlik bilgisi yok",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppTextDark
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Tarih: ${event?.eventDate ?: "-"}",
                    color = Color(0xFF475569)
                )

                Spacer(modifier = Modifier.height(4.dp))

                val venueName = ticket.location?.venueName ?: "-"

                Text(
                    text = "Sahne: $venueName",
                    color = Color(0xFF475569)
                )

                Spacer(modifier = Modifier.height(4.dp))

                val cityName = ticket.location?.cityName ?: "-"
                val districtName = ticket.location?.districtName ?: "-"

                Text(
                    text = "$cityName / $districtName",
                    color = AppTextGray
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${ticket.price?.toInt() ?: 0} TL",
                        color = AppGreen,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    TicketStatusBadge(
                        status = ticket.status ?: ticket.ticketStatus ?: "-"
                    )
                }
            }
        }
    }
}

/*
    Bilet durum etiketi.

    Backend status:
    active
    used
    cancelled
*/
@Composable
private fun TicketStatusBadge(
    status: String
) {
    val label: String
    val bgColor: Color
    val textColor: Color

    when (status) {
        "active" -> {
            label = "Aktif"
            bgColor = Color(0xFFDCFCE7)
            textColor = Color(0xFF166534)
        }

        "used" -> {
            label = "Kullanıldı"
            bgColor = Color(0xFFE2E8F0)
            textColor = Color(0xFF475569)
        }

        "cancelled" -> {
            label = "İptal"
            bgColor = Color(0xFFFEE2E2)
            textColor = Color(0xFF991B1B)
        }

        else -> {
            label = status
            bgColor = Color(0xFFEFF6FF)
            textColor = AppBlue
        }
    }

    Box(
        modifier = Modifier
            .background(
                color = bgColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(
                horizontal = 9.dp,
                vertical = 6.dp
            )
    ) {
        Text(
            text = label,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}