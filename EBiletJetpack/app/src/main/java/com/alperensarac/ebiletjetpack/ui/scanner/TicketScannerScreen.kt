package com.alperensarac.ebiletjetpack.ui.scanner

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.alperensarac.ebiletjetpack.data.api.ApiClient
import com.alperensarac.ebiletjetpack.data.model.ApiResponse
import com.alperensarac.ebiletjetpack.data.model.Ticket
import com.alperensarac.ebiletjetpack.data.session.SessionManager
import com.alperensarac.ebiletjetpack.ui.components.AppBackground
import com.alperensarac.ebiletjetpack.ui.components.AppBlue
import com.alperensarac.ebiletjetpack.ui.components.AppGreen
import com.alperensarac.ebiletjetpack.ui.components.AppSmallWhiteButton
import com.alperensarac.ebiletjetpack.ui.components.AppTextDark
import com.alperensarac.ebiletjetpack.ui.components.AppTextGray
import com.alperensarac.ebiletjetpack.ui.components.AppTopBar
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/*
    TicketScannerScreen

    Görevli / admin QR kontrol ekranıdır.

    Bu ekranın görevleri:
    1. Kullanıcının staff veya admin olup olmadığını kontrol etmek
    2. QR kod okutmak
    3. Manuel bilet kodu ile kontrol yapabilmek
    4. check/ticket_check.php API'sine ticket_code göndermek
    5. Backend'den gelen sonucu ekranda göstermek

    Backend:
    check/ticket_check.php

    POST:
    api_token
    ticket_code

    Başarılı kontrol sonucu genelde:
    success = true
    message = "Bilet onaylandı..."
    data.result = "approved"

    Hatalı sonuçlar:
    success = false
    message = "Bu bilet daha önce kullanılmıştır."
    veya:
    invalid / already_used / cancelled / passive_event
*/
@Composable
fun TicketScannerScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    /*
        Kullanıcı token / rol bilgileri.
    */
    val sessionManager = remember {
        SessionManager(context)
    }

    /*
        Manuel bilet kodu alanı.
    */
    var ticketCode by remember {
        mutableStateOf("")
    }

    /*
        API isteği devam ediyor mu?
    */
    var isChecking by remember {
        mutableStateOf(false)
    }

    /*
        Genel durum mesajı.
    */
    var statusMessage by remember {
        mutableStateOf("")
    }

    /*
        Sonuç kartı state'leri.
    */
    var resultTitle by remember {
        mutableStateOf("Henüz kontrol yapılmadı")
    }

    var resultMessage by remember {
        mutableStateOf("QR kod okutulduğunda veya kod elle girildiğinde sonuç burada görünecek.")
    }

    var isSuccessResult by remember {
        mutableStateOf<Boolean?>(null)
    }

    /*
        Detay bilgi textleri.
    */
    var ticketInfo by remember { mutableStateOf("") }
    var userInfo by remember { mutableStateOf("") }
    var eventInfo by remember { mutableStateOf("") }
    var locationInfo by remember { mutableStateOf("") }

    /*
        QR okuyucu launcher.

        JourneyApps ScanContract kullanıyoruz.
        QR okutma sonucu result.contents ile gelir.
    */
    val qrLauncher = rememberLauncherForActivityResult(
        contract = ScanContract()
    ) { result ->
        val qrContent = result.contents

        if (qrContent.isNullOrBlank()) {
            statusMessage = "QR okuma iptal edildi veya boş sonuç döndü."
            return@rememberLauncherForActivityResult
        }

        val cleanQrContent = qrContent.trim()

        /*
            Okunan değeri manuel input içine de yazıyoruz.
            Böylece görevli hangi kodun okunduğunu görür.
        */
        ticketCode = cleanQrContent

        /*
            Okunan QR içeriğini backend'e gönderiyoruz.
        */
        checkTicketCode(
            ticketCode = cleanQrContent,
            sessionManager = sessionManager,
            onLoadingChange = {
                isChecking = it
            },
            onStatusChange = {
                statusMessage = it
            },
            onClearResult = {
                resultTitle = "Kontrol ediliyor..."
                resultMessage = "Bilet bilgisi backend üzerinden doğrulanıyor."
                isSuccessResult = null
                ticketInfo = ""
                userInfo = ""
                eventInfo = ""
                locationInfo = ""
            },
            onFailed = { title, message ->
                resultTitle = title
                resultMessage = message
                isSuccessResult = false
                ticketInfo = ""
                userInfo = ""
                eventInfo = ""
                locationInfo = ""
            },
            onSuccess = { title, message, ticket ->
                resultTitle = title
                resultMessage = message
                isSuccessResult = true

                ticketInfo = buildTicketInfo(ticket)
                userInfo = buildUserInfo(ticket)
                eventInfo = buildEventInfo(ticket)
                locationInfo = buildLocationInfo(ticket)
            }
        )
    }

    /*
        Eğer normal kullanıcı bu ekrana somehow gelirse,
        ekranı kapatmak yerine içeride yetki uyarısı gösteriyoruz.
        HomeScreen zaten normal kullanıcıya QR Kontrol butonunu göstermiyor.
        Bu ekstra güvenlik kontrolüdür.
    */
    val hasPermission = sessionManager.isStaffOrAdmin()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        AppTopBar(
            title = "QR Bilet Kontrol",
            subtitle = if (hasPermission) {
                "Görevli: ${sessionManager.getFullName()}"
            } else {
                "Yetkisiz erişim"
            },
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (!hasPermission) {
                PermissionDeniedCard()
                return@Column
            }

            StaffInfoCard(
                fullName = sessionManager.getFullName(),
                role = sessionManager.getRole(),
                isChecking = isChecking,
                onScanQr = {
                    val options = ScanOptions().apply {
                        /*
                            Sadece QR okutmak istiyoruz.
                        */
                        setDesiredBarcodeFormats(ScanOptions.QR_CODE)

                        /*
                            Kamera ekranında görünecek açıklama.
                        */
                        setPrompt("Bilet QR kodunu kamera alanına getir")

                        /*
                            Okuyunca bip sesi.
                        */
                        setBeepEnabled(true)

                        /*
                            Dikey/yatay kilitleme yapmıyoruz.
                        */
                        setOrientationLocked(false)

                        /*
                            Barkod görselini kaydetmeye gerek yok.
                        */
                        setBarcodeImageEnabled(false)
                    }

                    qrLauncher.launch(options)
                }
            )

            ManualCheckCard(
                ticketCode = ticketCode,
                onTicketCodeChange = {
                    ticketCode = it
                },
                isChecking = isChecking,
                onCheckClick = {
                    val cleanCode = ticketCode.trim()

                    if (cleanCode.isEmpty()) {
                        Toast.makeText(
                            context,
                            "Bilet kodu zorunludur",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@ManualCheckCard
                    }

                    checkTicketCode(
                        ticketCode = cleanCode,
                        sessionManager = sessionManager,
                        onLoadingChange = {
                            isChecking = it
                        },
                        onStatusChange = {
                            statusMessage = it
                        },
                        onClearResult = {
                            resultTitle = "Kontrol ediliyor..."
                            resultMessage = "Bilet bilgisi backend üzerinden doğrulanıyor."
                            isSuccessResult = null
                            ticketInfo = ""
                            userInfo = ""
                            eventInfo = ""
                            locationInfo = ""
                        },
                        onFailed = { title, message ->
                            resultTitle = title
                            resultMessage = message
                            isSuccessResult = false
                            ticketInfo = ""
                            userInfo = ""
                            eventInfo = ""
                            locationInfo = ""
                        },
                        onSuccess = { title, message, ticket ->
                            resultTitle = title
                            resultMessage = message
                            isSuccessResult = true

                            ticketInfo = buildTicketInfo(ticket)
                            userInfo = buildUserInfo(ticket)
                            eventInfo = buildEventInfo(ticket)
                            locationInfo = buildLocationInfo(ticket)
                        }
                    )
                }
            )

            ResultCard(
                title = resultTitle,
                message = resultMessage,
                isSuccess = isSuccessResult,
                ticketInfo = ticketInfo,
                userInfo = userInfo,
                eventInfo = eventInfo,
                locationInfo = locationInfo
            )

            if (statusMessage.isNotEmpty()) {
                Text(
                    text = statusMessage,
                    color = AppTextGray
                )
            }
        }
    }
}

/*
    API çağrısını ayrı fonksiyona aldık.

    Neden?
    - QR okutunca da aynı API çalışıyor.
    - Manuel kod girince de aynı API çalışıyor.
    - Kod tekrarı azalıyor.
*/
private fun checkTicketCode(
    ticketCode: String,
    sessionManager: SessionManager,
    onLoadingChange: (Boolean) -> Unit,
    onStatusChange: (String) -> Unit,
    onClearResult: () -> Unit,
    onFailed: (String, String) -> Unit,
    onSuccess: (String, String, Ticket) -> Unit
) {
    onLoadingChange(true)
    onStatusChange("Bilet kontrol ediliyor...")
    onClearResult()

    ApiClient.apiService
        .checkTicket(
            apiToken = sessionManager.getApiToken(),
            ticketCode = ticketCode
        )
        .enqueue(object : Callback<ApiResponse<Ticket>> {

            override fun onResponse(
                call: Call<ApiResponse<Ticket>>,
                response: Response<ApiResponse<Ticket>>
            ) {
                onLoadingChange(false)

                if (!response.isSuccessful) {
                    onStatusChange("Kontrol tamamlandı.")
                    onFailed(
                        "Sunucu Hatası",
                        "HTTP hata kodu: ${response.code()}"
                    )
                    return
                }

                val apiResponse = response.body()

                if (apiResponse == null) {
                    onStatusChange("Kontrol tamamlandı.")
                    onFailed(
                        "Boş Cevap",
                        "Sunucudan boş cevap döndü."
                    )
                    return
                }

                /*
                    success false ise:
                    - invalid
                    - already_used
                    - cancelled
                    - passive_event
                    gibi durumlar olabilir.
                */
                if (!apiResponse.success) {
                    onStatusChange("Kontrol tamamlandı.")
                    onFailed(
                        "Giriş Reddedildi",
                        apiResponse.message
                    )
                    return
                }

                val ticket = apiResponse.data

                if (ticket == null) {
                    /*
                        Backend success true dönmüş ama data boşsa yine de mesajı gösteriyoruz.
                    */
                    onStatusChange("Kontrol tamamlandı.")
                    onFailed(
                        "Bilet Kontrol Edildi",
                        apiResponse.message
                    )
                    return
                }

                val result = ticket.result ?: "approved"

                if (result == "approved") {
                    onStatusChange("Kontrol tamamlandı.")
                    onSuccess(
                        "Giriş Onaylandı",
                        apiResponse.message,
                        ticket
                    )
                } else {
                    onStatusChange("Kontrol tamamlandı.")
                    onFailed(
                        "Giriş Reddedildi",
                        apiResponse.message
                    )
                }
            }

            override fun onFailure(
                call: Call<ApiResponse<Ticket>>,
                t: Throwable
            ) {
                onLoadingChange(false)
                onStatusChange("Kontrol tamamlandı.")

                onFailed(
                    "Bağlantı Hatası",
                    t.localizedMessage ?: "Sunucuya ulaşılamadı."
                )
            }
        })
}

/*
    Yetkisiz erişim kartı.
*/
@Composable
private fun PermissionDeniedCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFEE2E2)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Bu ekran için görevli yetkisi gerekir",
                color = Color(0xFF991B1B),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "QR bilet kontrol ekranı sadece staff veya admin hesabıyla kullanılabilir.",
                color = Color(0xFF7F1D1D)
            )
        }
    }
}

/*
    Görevli bilgi ve QR okutma kartı.
*/
@Composable
private fun StaffInfoCard(
    fullName: String,
    role: String,
    isChecking: Boolean,
    onScanQr: () -> Unit
) {
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
                text = "Görevli Kontrol Paneli",
                color = AppTextDark,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Görevli: $fullName | Rol: $role",
                color = AppTextGray
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onScanQr,
                enabled = !isChecking,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppGreen,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (isChecking) {
                        "Kontrol Ediliyor..."
                    } else {
                        "QR Kod Okut"
                    }
                )
            }
        }
    }
}

/*
    Manuel bilet kodu kontrol kartı.
*/
@Composable
private fun ManualCheckCard(
    ticketCode: String,
    onTicketCodeChange: (String) -> Unit,
    isChecking: Boolean,
    onCheckClick: () -> Unit
) {
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
                text = "Manuel Kod Kontrolü",
                color = AppTextDark,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "QR okunamazsa bilet kodunu elle girebilirsin.",
                color = AppTextGray
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = ticketCode,
                onValueChange = onTicketCodeChange,
                label = {
                    Text(text = "Bilet kodu")
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onCheckClick,
                enabled = !isChecking,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppBlue,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (isChecking) {
                        "Kontrol Ediliyor..."
                    } else {
                        "Kodu Kontrol Et"
                    }
                )
            }
        }
    }
}

/*
    Sonuç kartı.

    isSuccess:
    null  -> henüz işlem yok / kontrol ediliyor
    true  -> onaylandı
    false -> reddedildi
*/
@Composable
private fun ResultCard(
    title: String,
    message: String,
    isSuccess: Boolean?,
    ticketInfo: String,
    userInfo: String,
    eventInfo: String,
    locationInfo: String
) {
    val backgroundColor = when (isSuccess) {
        true -> Color(0xFFDCFCE7)
        false -> Color(0xFFFEE2E2)
        null -> Color.White
    }

    val titleColor = when (isSuccess) {
        true -> Color(0xFF166534)
        false -> Color(0xFF991B1B)
        null -> AppTextDark
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                color = titleColor,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                color = AppTextGray
            )

            if (ticketInfo.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                InfoBlock(title = "Bilet Bilgisi", value = ticketInfo)
            }

            if (userInfo.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                InfoBlock(title = "Kullanıcı Bilgisi", value = userInfo)
            }

            if (eventInfo.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                InfoBlock(title = "Etkinlik Bilgisi", value = eventInfo)
            }

            if (locationInfo.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                InfoBlock(title = "Konum Bilgisi", value = locationInfo)
            }
        }
    }
}

/*
    Sonuç kartı içindeki bilgi bloğu.
*/
@Composable
private fun InfoBlock(
    title: String,
    value: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            color = AppTextDark,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            color = Color(0xFF334155)
        )
    }
}

/*
    Ticket bilgisinden ekrana basılacak bilet textini üretir.
*/
private fun buildTicketInfo(ticket: Ticket): String {
    val ticketId = ticket.ticketId?.toString() ?: ticket.id?.toString() ?: "-"
    val ticketCode = ticket.ticketCode ?: "-"
    val ticketStatus = ticket.ticketStatus ?: ticket.status ?: "-"

    return "Bilet ID: $ticketId\n" +
            "Bilet Kodu: $ticketCode\n" +
            "Durum: $ticketStatus"
}

/*
    Kullanıcı bilgisini üretir.
*/
private fun buildUserInfo(ticket: Ticket): String {
    val userName = ticket.user?.fullName ?: "-"
    val userEmail = ticket.user?.email ?: "-"
    val userPhone = ticket.user?.phone ?: "-"

    return "Kullanıcı: $userName\n" +
            "E-posta: $userEmail\n" +
            "Telefon: $userPhone"
}

/*
    Etkinlik bilgisini üretir.
*/
private fun buildEventInfo(ticket: Ticket): String {
    val eventTitle = ticket.event?.title ?: ticket.eventTitle ?: "-"
    val eventDate = ticket.event?.eventDate ?: "-"

    return "Etkinlik: $eventTitle\n" +
            "Tarih: $eventDate"
}

/*
    Konum bilgisini üretir.
*/
private fun buildLocationInfo(ticket: Ticket): String {
    val cityName = ticket.location?.cityName ?: ticket.city?.name ?: "-"
    val districtName = ticket.location?.districtName ?: ticket.district?.name ?: "-"
    val venueName = ticket.location?.venueName ?: ticket.venue?.name ?: "-"
    val venueAddress = ticket.location?.venueAddress ?: ticket.venue?.address ?: "-"

    return "Konum: $cityName / $districtName\n" +
            "Sahne: $venueName\n" +
            "Adres: $venueAddress"
}