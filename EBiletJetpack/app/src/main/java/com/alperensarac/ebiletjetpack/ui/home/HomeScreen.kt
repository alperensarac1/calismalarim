package com.alperensarac.ebiletjetpack.ui.home
import android.widget.Toast
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.Button
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
import com.alperensarac.ebiletjetpack.data.model.City
import com.alperensarac.ebiletjetpack.data.model.District
import com.alperensarac.ebiletjetpack.data.model.Event
import com.alperensarac.ebiletjetpack.data.session.SessionManager
import com.alperensarac.ebiletjetpack.ui.components.AppBackground
import com.alperensarac.ebiletjetpack.ui.components.AppBlue
import com.alperensarac.ebiletjetpack.ui.components.AppGreen
import com.alperensarac.ebiletjetpack.ui.components.AppSmallWhiteButton
import com.alperensarac.ebiletjetpack.ui.components.AppTextDark
import com.alperensarac.ebiletjetpack.ui.components.AppTextGray
import com.alperensarac.ebiletjetpack.ui.components.AppTopBar
import com.alperensarac.ebiletjetpack.ui.components.AppWhiteCard
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/*
    HomeScreen

    Ana ekran.

    Görevleri:
    - Kullanıcı bilgilerini göstermek
    - Şehirleri backend'den çekmek
    - Seçilen şehre göre ilçeleri çekmek
    - Şehir + ilçe ile etkinlikleri listelemek
    - Etkinlik kartına tıklanınca EventDetail ekranına gitmek

    Compose karşılıkları:
    XML Spinner yerine:
        ExposedDropdownMenuBox

    XML RecyclerView yerine:
        LazyColumn

    XML ImageView + Glide yerine:
        AsyncImage + Coil
*/
@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    onOpenMyTickets: () -> Unit,
    onOpenScanner: () -> Unit,
    onOpenEventDetail: (Int) -> Unit
) {
    val context = LocalContext.current

    val sessionManager = remember {
        SessionManager(context)
    }

    /*
        Session bilgileri.
    */
    val fullName = remember {
        sessionManager.getFullName()
    }

    val role = remember {
        sessionManager.getRole()
    }

    /*
        Backend'den gelen şehir / ilçe / etkinlik listeleri.
    */
    var cities by remember { mutableStateOf<List<City>>(emptyList()) }
    var districts by remember { mutableStateOf<List<District>>(emptyList()) }
    var events by remember { mutableStateOf<List<Event>>(emptyList()) }

    /*
        Seçili şehir / ilçe.
    */
    var selectedCity by remember { mutableStateOf<City?>(null) }
    var selectedDistrict by remember { mutableStateOf<District?>(null) }

    /*
        Durum mesajı.
    */
    var statusMessage by remember { mutableStateOf("Şehirler yükleniyor...") }

    /*
        Loading state'leri.
    */
    var isCitiesLoading by remember { mutableStateOf(false) }
    var isDistrictsLoading by remember { mutableStateOf(false) }
    var isEventsLoading by remember { mutableStateOf(false) }

    /*
        Ekran ilk açıldığında şehirleri yükle.
    */
    LaunchedEffect(Unit) {
        isCitiesLoading = true
        statusMessage = "Şehirler yükleniyor..."

        ApiClient.apiService
            .getCities(sessionManager.getApiToken())
            .enqueue(object : Callback<ApiResponse<List<City>>> {

                override fun onResponse(
                    call: Call<ApiResponse<List<City>>>,
                    response: Response<ApiResponse<List<City>>>
                ) {
                    isCitiesLoading = false

                    if (!response.isSuccessful) {
                        statusMessage = "Şehirler alınamadı. Sunucu hatası: ${response.code()}"
                        return
                    }

                    val apiResponse = response.body()

                    if (apiResponse == null) {
                        statusMessage = "Şehirler alınamadı. Boş cevap döndü."
                        return
                    }

                    if (!apiResponse.success) {
                        statusMessage = apiResponse.message
                        return
                    }

                    cities = apiResponse.data ?: emptyList()

                    statusMessage = if (cities.isEmpty()) {
                        "Aktif şehir bulunamadı."
                    } else {
                        "Şehir seçiniz."
                    }
                }

                override fun onFailure(
                    call: Call<ApiResponse<List<City>>>,
                    t: Throwable
                ) {
                    isCitiesLoading = false
                    statusMessage = "Bağlantı hatası: ${t.localizedMessage}"
                }
            })
    }

    /*
        selectedCity değiştiğinde ilçeleri yükle.

        Dikkat:
        Kullanıcı yeni şehir seçerse:
        - İlçe sıfırlanır
        - Etkinlik listesi sıfırlanır
        - Yeni ilçeler backend'den gelir
    */
    LaunchedEffect(selectedCity) {
        val city = selectedCity ?: return@LaunchedEffect

        selectedDistrict = null
        districts = emptyList()
        events = emptyList()

        isDistrictsLoading = true
        statusMessage = "İlçeler yükleniyor..."

        ApiClient.apiService
            .getDistrictsByCity(
                apiToken = sessionManager.getApiToken(),
                cityId = city.id
            )
            .enqueue(object : Callback<ApiResponse<List<District>>> {

                override fun onResponse(
                    call: Call<ApiResponse<List<District>>>,
                    response: Response<ApiResponse<List<District>>>
                ) {
                    isDistrictsLoading = false

                    if (!response.isSuccessful) {
                        statusMessage = "İlçeler alınamadı. Sunucu hatası: ${response.code()}"
                        return
                    }

                    val apiResponse = response.body()

                    if (apiResponse == null) {
                        statusMessage = "İlçeler alınamadı. Boş cevap döndü."
                        return
                    }

                    if (!apiResponse.success) {
                        statusMessage = apiResponse.message
                        return
                    }

                    districts = apiResponse.data ?: emptyList()

                    statusMessage = if (districts.isEmpty()) {
                        "Bu şehir için aktif ilçe bulunamadı."
                    } else {
                        "İlçe seçip etkinlikleri listeleyebilirsin."
                    }
                }

                override fun onFailure(
                    call: Call<ApiResponse<List<District>>>,
                    t: Throwable
                ) {
                    isDistrictsLoading = false
                    statusMessage = "Bağlantı hatası: ${t.localizedMessage}"
                }
            })
    }

    /*
        Ekran ana layout.
    */
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        AppTopBar(
            title = "Hoş geldin, $fullName",
            subtitle = when (role) {
                "admin" -> "Admin hesabı"
                "staff" -> "Görevli hesabı"
                else -> "Etkinlikleri keşfet"
            },
            actions = {
                AppSmallWhiteButton(
                    text = "Biletlerim",
                    onClick = onOpenMyTickets
                )

                Spacer(modifier = Modifier.width(8.dp))

                if (sessionManager.isStaffOrAdmin()) {
                    AppSmallWhiteButton(
                        text = "QR Kontrol",
                        onClick = onOpenScanner
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                }

                AppSmallWhiteButton(
                    text = "Çıkış",
                    onClick = {
                        sessionManager.logout()
                        onLogout()
                    }
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
                AppWhiteCard {
                    Text(
                        text = "Konum Seç",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AppTextDark
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Önce şehir, sonra ilçe seçerek etkinlikleri listeleyebilirsin.",
                        color = AppTextGray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CityDropdown(
                        cities = cities,
                        selectedCity = selectedCity,
                        isLoading = isCitiesLoading,
                        onCitySelected = {
                            selectedCity = it
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    DistrictDropdown(
                        districts = districts,
                        selectedDistrict = selectedDistrict,
                        isLoading = isDistrictsLoading,
                        enabled = selectedCity != null,
                        onDistrictSelected = {
                            selectedDistrict = it
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val city = selectedCity
                            val district = selectedDistrict

                            if (city == null) {
                                Toast.makeText(context, "Lütfen şehir seçiniz", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            if (district == null) {
                                Toast.makeText(context, "Lütfen ilçe seçiniz", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isEventsLoading = true
                            statusMessage = "Etkinlikler yükleniyor..."

                            ApiClient.apiService
                                .getEventsByLocation(
                                    apiToken = sessionManager.getApiToken(),
                                    cityId = city.id,
                                    districtId = district.id
                                )
                                .enqueue(object : Callback<ApiResponse<List<Event>>> {

                                    override fun onResponse(
                                        call: Call<ApiResponse<List<Event>>>,
                                        response: Response<ApiResponse<List<Event>>>
                                    ) {
                                        isEventsLoading = false

                                        if (!response.isSuccessful) {
                                            statusMessage = "Etkinlikler alınamadı. Sunucu hatası: ${response.code()}"
                                            return
                                        }

                                        val apiResponse = response.body()

                                        if (apiResponse == null) {
                                            statusMessage = "Etkinlikler alınamadı. Boş cevap döndü."
                                            return
                                        }

                                        if (!apiResponse.success) {
                                            statusMessage = apiResponse.message
                                            return
                                        }

                                        events = apiResponse.data ?: emptyList()

                                        statusMessage = if (events.isEmpty()) {
                                            "Bu konum için etkinlik bulunamadı."
                                        } else {
                                            "${events.size} etkinlik listelendi."
                                        }
                                    }

                                    override fun onFailure(
                                        call: Call<ApiResponse<List<Event>>>,
                                        t: Throwable
                                    ) {
                                        isEventsLoading = false
                                        statusMessage = "Bağlantı hatası: ${t.localizedMessage}"
                                    }
                                })
                        },
                        enabled = !isEventsLoading,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppGreen,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = if (isEventsLoading) {
                                "Etkinlikler Yükleniyor..."
                            } else {
                                "Etkinlikleri Listele"
                            }
                        )
                    }
                }
            }

            item {
                Text(
                    text = statusMessage,
                    color = AppTextGray,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            /*
                Etkinlik kartları.
            */
            items(events) { event ->
                EventCard(
                    event = event,
                    onClick = {
                        onOpenEventDetail(event.id)
                    }
                )
            }
        }
    }
}

/*
    Şehir seçimi dropdown.

    XML Spinner yerine Compose Material3 ExposedDropdownMenuBox kullanıyoruz.
*/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CityDropdown(
    cities: List<City>,
    selectedCity: City?,
    isLoading: Boolean,
    onCitySelected: (City) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            if (!isLoading && cities.isNotEmpty()) {
                expanded = !expanded
            }
        }
    ) {
        OutlinedTextField(
            value = selectedCity?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = {
                Text(text = if (isLoading) "Şehirler yükleniyor..." else "Şehir")
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            cities.forEach { city ->
                DropdownMenuItem(
                    text = {
                        Text(text = city.name)
                    },
                    onClick = {
                        onCitySelected(city)
                        expanded = false
                    }
                )
            }
        }
    }
}

/*
    İlçe seçimi dropdown.
*/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DistrictDropdown(
    districts: List<District>,
    selectedDistrict: District?,
    isLoading: Boolean,
    enabled: Boolean,
    onDistrictSelected: (District) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            if (enabled && !isLoading && districts.isNotEmpty()) {
                expanded = !expanded
            }
        }
    ) {
        OutlinedTextField(
            value = selectedDistrict?.name ?: "",
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = {
                Text(
                    text = when {
                        !enabled -> "Önce şehir seçiniz"
                        isLoading -> "İlçeler yükleniyor..."
                        else -> "İlçe"
                    }
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            districts.forEach { district ->
                DropdownMenuItem(
                    text = {
                        Text(text = district.name)
                    },
                    onClick = {
                        onDistrictSelected(district)
                        expanded = false
                    }
                )
            }
        }
    }
}

/*
    Tek etkinlik kartı.

    XML RecyclerView item_event.xml yerine Compose Card kullanıyoruz.
*/
@Composable
private fun EventCard(
    event: Event,
    onClick: () -> Unit
) {
    val posterUrl = event.posterUrl?.let { poster ->
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
        Column {
            AsyncImage(
                model = posterUrl,
                contentDescription = event.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .background(Color(0xFFE2E8F0))
            )

            Column(
                modifier = Modifier.padding(14.dp)
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppTextDark
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Tarih: ${event.eventDate ?: "-"}",
                    color = Color(0xFF475569)
                )

                Spacer(modifier = Modifier.height(4.dp))

                val venueName = event.venue?.name ?: "-"

                Text(
                    text = "Sahne: $venueName",
                    color = Color(0xFF475569)
                )

                Spacer(modifier = Modifier.height(4.dp))

                val cityName = event.cityName ?: event.city?.name ?: "-"
                val districtName = event.districtName ?: event.district?.name ?: "-"

                Text(
                    text = "$cityName / $districtName",
                    color = AppTextGray
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${event.basePrice?.toInt() ?: 0} TL",
                        color = AppGreen,
                        style = MaterialTheme.typography.titleLarge,
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
                                horizontal = 10.dp,
                                vertical = 7.dp
                            )
                    ) {
                        Text(
                            text = "Kalan: ${event.remainingQuota ?: 0}",
                            color = AppBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}