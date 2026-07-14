package com.alperensarac.ebiletkotlin.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.alperensarac.ebiletkotlin.data.api.ApiClient
import com.alperensarac.ebiletkotlin.data.model.ApiResponse
import com.alperensarac.ebiletkotlin.data.model.City
import com.alperensarac.ebiletkotlin.data.model.District
import com.alperensarac.ebiletkotlin.data.model.Event
import com.alperensarac.ebiletkotlin.data.session.SessionManager
import com.alperensarac.ebiletkotlin.databinding.ActivityHomeBinding
import com.alperensarac.ebiletkotlin.ui.auth.LoginActivity
import com.alperensarac.ebiletkotlin.ui.event.EventDetailActivity
import com.alperensarac.ebiletkotlin.ui.scanner.TicketScannerActivity
import com.alperensarac.ebiletkotlin.ui.ticket.MyTicketsActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/*
    HomeActivity

    Ana ekran.

    Görevleri:
    - Kullanıcının giriş yapıp yapmadığını kontrol eder
    - Şehirleri API'den çeker
    - Şehir seçilince ilçeleri API'den çeker
    - İlçe seçilince etkinlikleri listelemeye hazır hale getirir
    - Etkinlikleri RecyclerView içinde gösterir
*/
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    private lateinit var sessionManager: SessionManager

    /*
        Spinner içinde göstereceğimiz şehir ve ilçe listeleri.
    */
    private val cityList = mutableListOf<City>()
    private val districtList = mutableListOf<District>()

    /*
        Seçili şehir ve ilçe.
        API isteği atarken bunların id değerlerini kullanacağız.
    */
    private var selectedCity: City? = null
    private var selectedDistrict: District? = null

    /*
        RecyclerView adapter.
    */
    private lateinit var eventAdapter: EventAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        /*
            Giriş yapılmamışsa LoginActivity'ye gönderiyoruz.
            Çünkü HomeActivity token olmadan çalışamaz.
        */
        if (!sessionManager.isLoggedIn()) {
            goToLogin()
            return
        }

        setupHeader()
        setupRecyclerView()
        setupClickListeners()

        /*
            Ekran açılır açılmaz şehirleri getiriyoruz.
        */
        loadCities()
    }

    /*
        Üstteki kullanıcı bilgisi alanını hazırlar.
    */
    private fun setupHeader() {
        binding.tvWelcome.text = "Hoş geldin, ${sessionManager.getFullName()}"

        val roleText = when (sessionManager.getRole()) {
            "admin" -> "Admin hesabı"
            "staff" -> "Görevli hesabı"
            else -> "Etkinlikleri keşfet"
        }

        binding.tvRoleInfo.text = roleText

        /*
            QR kontrol butonu sadece staff ve admin için görünür.
        */
        binding.btnScanner.visibility = if (sessionManager.isStaffOrAdmin()) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    /*
        RecyclerView kurulumu.
    */
    private fun setupRecyclerView() {
        eventAdapter = EventAdapter(
            events = mutableListOf(),
            onEventClick = { event ->

                /*
                    Etkinlik detay ekranına geçiş.

                    Burada tüm event nesnesini göndermiyoruz.
                    Sadece event.id gönderiyoruz.

                    Neden?

                    Çünkü detay ekranında en güncel bilgiyi backend'den çekmek daha doğru.
                    Örneğin kontenjan değişmiş olabilir.
                */
                val intent = Intent(this, EventDetailActivity::class.java)
                intent.putExtra(EventDetailActivity.EXTRA_EVENT_ID, event.id)
                startActivity(intent)
            }
        )

        binding.rvEvents.layoutManager = LinearLayoutManager(this)
        binding.rvEvents.adapter = eventAdapter
    }

    /*
        Buton ve spinner tıklama/ seçim işlemleri.
    */
    private fun setupClickListeners() {
        /*
            Çıkış yapma.
        */
        binding.btnLogout.setOnClickListener {
            sessionManager.logout()
            goToLogin()
        }
        binding.btnScanner.setOnClickListener {
            val intent = Intent(this, TicketScannerActivity::class.java)
            startActivity(intent)
        }
        /*
            Şehir spinner seçimi.

            Kullanıcı şehir seçince ilçeleri getiriyoruz.
        */
        binding.spinnerCities.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (cityList.isEmpty()) return

                    selectedCity = cityList[position]

                    /*
                        Şehir değiştiğinde eski ilçe ve etkinlik listesini temizliyoruz.
                    */
                    selectedDistrict = null
                    districtList.clear()
                    eventAdapter.updateList(emptyList())

                    loadDistricts(selectedCity!!.id)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    selectedCity = null
                }
            }

        /*
            İlçe spinner seçimi.
        */
        binding.spinnerDistricts.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (districtList.isEmpty()) return

                    selectedDistrict = districtList[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    selectedDistrict = null
                }
            }

        /*
            Etkinlikleri listeleme butonu.
        */
        binding.btnListEvents.setOnClickListener {
            val city = selectedCity
            val district = selectedDistrict

            if (city == null) {
                Toast.makeText(this, "Lütfen şehir seçiniz", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (district == null) {
                Toast.makeText(this, "Lütfen ilçe seçiniz", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loadEvents(city.id, district.id)
        }
        binding.btnMyTickets.setOnClickListener {
            val intent = Intent(this, MyTicketsActivity::class.java)
            startActivity(intent)
        }
    }

    /*
        Şehirleri backend'den getirir.
    */
    private fun loadCities() {
        setStatus("Şehirler yükleniyor...")

        val apiToken = sessionManager.getApiToken()

        ApiClient.apiService.getCities(apiToken)
            .enqueue(object : Callback<ApiResponse<List<City>>> {

                override fun onResponse(
                    call: Call<ApiResponse<List<City>>>,
                    response: Response<ApiResponse<List<City>>>
                ) {
                    if (!response.isSuccessful) {
                        setStatus("Şehirler alınamadı. Sunucu hatası: ${response.code()}")
                        return
                    }

                    val apiResponse = response.body()

                    if (apiResponse == null) {
                        setStatus("Şehirler alınamadı. Boş cevap döndü.")
                        return
                    }

                    if (!apiResponse.success) {
                        setStatus(apiResponse.message)
                        return
                    }

                    val cities = apiResponse.data ?: emptyList()

                    cityList.clear()
                    cityList.addAll(cities)

                    setupCitySpinner()

                    if (cityList.isEmpty()) {
                        setStatus("Aktif şehir bulunamadı.")
                    } else {
                        setStatus("Şehir seçiniz.")
                    }
                }

                override fun onFailure(
                    call: Call<ApiResponse<List<City>>>,
                    t: Throwable
                ) {
                    setStatus("Bağlantı hatası: ${t.localizedMessage}")
                }
            })
    }

    /*
        Şehir spinner'ına şehir adlarını basar.
    */
    private fun setupCitySpinner() {
        val cityNames = cityList.map { it.name }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            cityNames
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerCities.adapter = adapter
    }

    /*
        Seçilen şehre göre ilçeleri getirir.
    */
    private fun loadDistricts(cityId: Int) {
        setStatus("İlçeler yükleniyor...")

        val apiToken = sessionManager.getApiToken()

        ApiClient.apiService.getDistrictsByCity(
            apiToken = apiToken,
            cityId = cityId
        ).enqueue(object : Callback<ApiResponse<List<District>>> {

            override fun onResponse(
                call: Call<ApiResponse<List<District>>>,
                response: Response<ApiResponse<List<District>>>
            ) {
                if (!response.isSuccessful) {
                    setStatus("İlçeler alınamadı. Sunucu hatası: ${response.code()}")
                    return
                }

                val apiResponse = response.body()

                if (apiResponse == null) {
                    setStatus("İlçeler alınamadı. Boş cevap döndü.")
                    return
                }

                if (!apiResponse.success) {
                    setStatus(apiResponse.message)
                    return
                }

                val districts = apiResponse.data ?: emptyList()

                districtList.clear()
                districtList.addAll(districts)

                setupDistrictSpinner()

                if (districtList.isEmpty()) {
                    setStatus("Bu şehir için aktif ilçe bulunamadı.")
                } else {
                    setStatus("İlçe seçip etkinlikleri listeleyebilirsin.")
                }
            }

            override fun onFailure(
                call: Call<ApiResponse<List<District>>>,
                t: Throwable
            ) {
                setStatus("Bağlantı hatası: ${t.localizedMessage}")
            }
        })
    }

    /*
        İlçe spinner'ına ilçe adlarını basar.
    */
    private fun setupDistrictSpinner() {
        val districtNames = districtList.map { it.name }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            districtNames
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerDistricts.adapter = adapter
    }

    /*
        Seçili şehir + ilçeye göre etkinlikleri getirir.
    */
    private fun loadEvents(cityId: Int, districtId: Int) {
        setStatus("Etkinlikler yükleniyor...")

        val apiToken = sessionManager.getApiToken()

        ApiClient.apiService.getEventsByLocation(
            apiToken = apiToken,
            cityId = cityId,
            districtId = districtId
        ).enqueue(object : Callback<ApiResponse<List<Event>>> {

            override fun onResponse(
                call: Call<ApiResponse<List<Event>>>,
                response: Response<ApiResponse<List<Event>>>
            ) {
                if (!response.isSuccessful) {
                    setStatus("Etkinlikler alınamadı. Sunucu hatası: ${response.code()}")
                    return
                }

                val apiResponse = response.body()

                if (apiResponse == null) {
                    setStatus("Etkinlikler alınamadı. Boş cevap döndü.")
                    return
                }

                if (!apiResponse.success) {
                    setStatus(apiResponse.message)
                    return
                }

                val events = apiResponse.data ?: emptyList()

                eventAdapter.updateList(events)

                if (events.isEmpty()) {
                    setStatus("Bu konum için etkinlik bulunamadı.")
                } else {
                    setStatus("${events.size} etkinlik listelendi.")
                }
            }

            override fun onFailure(
                call: Call<ApiResponse<List<Event>>>,
                t: Throwable
            ) {
                setStatus("Bağlantı hatası: ${t.localizedMessage}")
            }
        })
    }

    /*
        Kullanıcıya ekranda durum mesajı gösterir.
    */
    private fun setStatus(message: String) {
        binding.tvStatus.text = message
    }

    /*
        Login ekranına geçiş.
    */
    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}