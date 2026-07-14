package com.alperensarac.ebiletjetpack.ui.ticket

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
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
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/*
    TicketDetailScreen

    Tek biletin detayını ve QR kodunu gösterir.

    Backend:
    tickets/ticket_detail.php

    POST:
    api_token
    ticket_id

    QR kod:
    - Önce qr_code_text kullanılır.
    - Boşsa ticket_code kullanılır.
*/
@Composable
fun TicketDetailScreen(
    ticketId: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val sessionManager = remember {
        SessionManager(context)
    }

    var ticket by remember { mutableStateOf<Ticket?>(null) }

    var statusMessage by remember {
        mutableStateOf("Bilet detayı yükleniyor...")
    }

    var isLoading by remember {
        mutableStateOf(false)
    }

    /*
        Bilet detayını yükleyen fonksiyon.
    */
    fun loadTicketDetail() {
        isLoading = true
        statusMessage = "Bilet detayı yükleniyor..."

        ApiClient.apiService
            .getTicketDetail(
                apiToken = sessionManager.getApiToken(),
                ticketId = ticketId
            )
            .enqueue(object : Callback<ApiResponse<Ticket>> {

                override fun onResponse(
                    call: Call<ApiResponse<Ticket>>,
                    response: Response<ApiResponse<Ticket>>
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

                    val loadedTicket = apiResponse.data

                    if (loadedTicket == null) {
                        statusMessage = "Bilet bilgisi alınamadı"
                        return
                    }

                    ticket = loadedTicket
                    statusMessage = "Bilet detayı getirildi."
                }

                override fun onFailure(
                    call: Call<ApiResponse<Ticket>>,
                    t: Throwable
                ) {
                    isLoading = false
                    statusMessage = "Bağlantı hatası: ${t.localizedMessage}"
                }
            })
    }

    LaunchedEffect(ticketId) {
        if (ticketId <= 0) {
            statusMessage = "Bilet bilgisi alınamadı"
            return@LaunchedEffect
        }

        loadTicketDetail()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        AppTopBar(
            title = "Bilet Detayı",
            subtitle = ticket?.ticketCode,
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isLoading) "Yükleniyor..." else statusMessage,
                color = AppTextGray,
                modifier = Modifier.fillMaxWidth()
            )

            val currentTicket = ticket

            if (currentTicket != null) {
                TicketDetailCard(ticket = currentTicket)
            }
        }
    }
}

/*
    Bilet detay kartı.
*/
@Composable
private fun TicketDetailCard(
    ticket: Ticket
) {
    val event: Event? = ticket.event

    val eventTitle = event?.title ?: ticket.eventTitle ?: "Etkinlik"

    val status = ticket.status ?: ticket.ticketStatus ?: "-"

    val statusLabel: String
    val statusBg: Color
    val statusColor: Color

    when (status) {
        "active" -> {
            statusLabel = "Aktif Bilet"
            statusBg = Color(0xFFDCFCE7)
            statusColor = Color(0xFF166534)
        }

        "used" -> {
            statusLabel = "Kullanıldı"
            statusBg = Color(0xFFE2E8F0)
            statusColor = Color(0xFF475569)
        }

        "cancelled" -> {
            statusLabel = "İptal Edildi"
            statusBg = Color(0xFFFEE2E2)
            statusColor = Color(0xFF991B1B)
        }

        else -> {
            statusLabel = status
            statusBg = Color(0xFFEFF6FF)
            statusColor = AppBlue
        }
    }

    val qrText = ticket.qrCodeText ?: ticket.ticketCode
    val qrBitmap = remember(qrText) {
        if (!qrText.isNullOrEmpty()) {
            generateQrBitmap(qrText, 800, 800)
        } else {
            null
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = eventTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AppTextDark
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .background(
                        color = statusBg,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(
                        horizontal = 12.dp,
                        vertical = 8.dp
                    )
            ) {
                Text(
                    text = statusLabel,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (qrBitmap != null) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "Bilet QR Kodu",
                    modifier = Modifier
                        .size(260.dp)
                        .background(Color.White)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .background(Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "QR oluşturulamadı",
                        color = AppTextGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = ticket.ticketCode ?: "-",
                color = AppTextGray
            )

            Spacer(modifier = Modifier.height(20.dp))

            DetailLine(
                title = "Tarih",
                value = event?.eventDate ?: "-"
            )

            val venueName =
                ticket.venue?.name
                    ?: ticket.location?.venueName
                    ?: event?.venue?.name
                    ?: "-"

            DetailLine(
                title = "Sahne",
                value = venueName
            )

            val cityName =
                ticket.city?.name
                    ?: ticket.location?.cityName
                    ?: event?.city?.name
                    ?: "-"

            val districtName =
                ticket.district?.name
                    ?: ticket.location?.districtName
                    ?: event?.district?.name
                    ?: "-"

            DetailLine(
                title = "Konum",
                value = "$cityName / $districtName"
            )

            DetailLine(
                title = "Fiyat",
                value = "${ticket.price?.toInt() ?: 0} TL",
                valueColor = AppGreen
            )

            val usedAtText = if (!ticket.usedAt.isNullOrEmpty()) {
                "Kullanım zamanı: ${ticket.usedAt}"
            } else {
                "Bilet henüz kullanılmadı."
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = usedAtText,
                color = AppTextGray,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/*
    Detay satırı.
*/
@Composable
private fun DetailLine(
    title: String,
    value: String,
    valueColor: Color = AppTextDark
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = title,
            color = AppTextGray,
            style = MaterialTheme.typography.bodySmall
        )

        Text(
            text = value,
            color = valueColor,
            fontWeight = FontWeight.Bold
        )
    }
}

/*
    QR kod Bitmap üretir.

    ZXing:
    - MultiFormatWriter ile QR matrix oluştururuz.
    - Matrix içindeki true noktalar siyah, false noktalar beyaz olur.
*/
private fun generateQrBitmap(
    text: String,
    width: Int,
    height: Int
): Bitmap {
    return try {
        val bitMatrix = MultiFormatWriter().encode(
            text,
            BarcodeFormat.QR_CODE,
            width,
            height
        )

        val bitmap = Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.RGB_565
        )

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(
                    x,
                    y,
                    if (bitMatrix[x, y]) {
                        AndroidColor.BLACK
                    } else {
                        AndroidColor.WHITE
                    }
                )
            }
        }

        bitmap
    } catch (e: Exception) {
        val bitmap = Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.RGB_565
        )

        bitmap.eraseColor(AndroidColor.WHITE)
        bitmap
    }
}