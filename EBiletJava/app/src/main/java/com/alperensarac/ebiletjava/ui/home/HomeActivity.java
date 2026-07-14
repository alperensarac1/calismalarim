package com.alperensarac.ebiletjava.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alperensarac.ebiletjava.R;
import com.alperensarac.ebiletjava.data.api.ApiClient;
import com.alperensarac.ebiletjava.data.model.ApiResponse;
import com.alperensarac.ebiletjava.data.model.City;
import com.alperensarac.ebiletjava.data.model.District;
import com.alperensarac.ebiletjava.data.model.Event;
import com.alperensarac.ebiletjava.data.session.SessionManager;
import com.alperensarac.ebiletjava.ui.auth.LoginActivity;
import com.alperensarac.ebiletjava.ui.event.EventDetailActivity;
import com.alperensarac.ebiletjava.ui.scanner.TicketScannerActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    /*
        Üst bar view'ları.
    */
    private TextView tvWelcome;
    private TextView tvRoleInfo;
    private Button btnMyTickets;
    private Button btnLogout;
    private Button btnScanner;

    /*
        Filtre view'ları.
    */
    private Spinner spinnerCities;
    private Spinner spinnerDistricts;
    private Button btnListEvents;
    private TextView tvStatus;

    /*
        RecyclerView.
    */
    private RecyclerView rvEvents;
    private EventAdapter eventAdapter;

    /*
        Session bilgileri.
    */
    private SessionManager sessionManager;

    /*
        Spinner için şehir ve ilçe listeleri.
    */
    private final List<City> cityList = new ArrayList<>();
    private final List<District> districtList = new ArrayList<>();

    /*
        Seçili şehir ve ilçe.
    */
    private City selectedCity = null;
    private District selectedDistrict = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sessionManager = new SessionManager(this);

        /*
            Bu ekran token olmadan çalışmamalı.
        */
        if (!sessionManager.isLoggedIn()) {
            goToLogin();
            return;
        }

        initViews();
        setupHeader();
        setupRecyclerView();
        setupClickListeners();

        /*
            Ekran açılır açılmaz şehirleri backend'den çekiyoruz.
        */
        loadCities();
    }

    /*
        XML view'larını Java değişkenlerine bağlar.
    */
    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvRoleInfo = findViewById(R.id.tvRoleInfo);
        btnMyTickets = findViewById(R.id.btnMyTickets);
        btnLogout = findViewById(R.id.btnLogout);
        btnScanner = findViewById(R.id.btnScanner);

        spinnerCities = findViewById(R.id.spinnerCities);
        spinnerDistricts = findViewById(R.id.spinnerDistricts);
        btnListEvents = findViewById(R.id.btnListEvents);
        tvStatus = findViewById(R.id.tvStatus);

        rvEvents = findViewById(R.id.rvEvents);
    }

    /*
        Üst bar kullanıcı bilgilerini doldurur.
    */
    private void setupHeader() {
        tvWelcome.setText("Hoş geldin, " + sessionManager.getFullName());

        String role = sessionManager.getRole();
        String roleText;

        if ("admin".equals(role)) {
            roleText = "Admin hesabı";
        } else if ("staff".equals(role)) {
            roleText = "Görevli hesabı";
        } else {
            roleText = "Etkinlikleri keşfet";
        }

        tvRoleInfo.setText(roleText);

    /*
        QR Kontrol butonu sadece staff ve admin hesaplarda görünür.
    */
        if (sessionManager.isStaffOrAdmin()) {
            btnScanner.setVisibility(View.VISIBLE);
        } else {
            btnScanner.setVisibility(View.GONE);
        }
    }

    /*
        RecyclerView kurulumunu yapar.
    */
    private void setupRecyclerView() {
        eventAdapter = new EventAdapter(new EventAdapter.OnEventClickListener() {
            @Override
            public void onEventClick(Event event) {
            /*
                Etkinlik kartına tıklanınca detay ekranına geçiyoruz.

                Burada sadece event_id gönderiyoruz.
                Detay ekranı geri kalan bilgileri backend'den çekecek.
            */
                Intent intent = new Intent(HomeActivity.this, EventDetailActivity.class);
                intent.putExtra(EventDetailActivity.EXTRA_EVENT_ID, event.getId());
                startActivity(intent);
            }
        });

        rvEvents.setLayoutManager(new LinearLayoutManager(this));
        rvEvents.setAdapter(eventAdapter);
    }

    /*
        Buton ve spinner olaylarını ayarlar.
    */
    private void setupClickListeners() {

        /*
            Biletlerim ekranı henüz yok.
            Şimdilik Toast gösteriyoruz.
        */
        btnMyTickets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(
                        HomeActivity.this,
                        "Biletlerim ekranı sonraki adımda eklenecek",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
        btnScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, TicketScannerActivity.class);
                startActivity(intent);
            }
        });

        /*
            Çıkış yapma.
        */
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sessionManager.logout();
                goToLogin();
            }
        });

        /*
            Şehir seçimi.

            Kullanıcı şehir seçince:
            - selectedCity güncellenir
            - eski ilçeler temizlenir
            - eski etkinlikler temizlenir
            - seçilen şehre ait ilçeler backend'den çekilir
        */
        spinnerCities.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(
                    AdapterView<?> parent,
                    View view,
                    int position,
                    long id
            ) {
                if (cityList.isEmpty()) {
                    return;
                }

                selectedCity = cityList.get(position);

                selectedDistrict = null;
                districtList.clear();
                eventAdapter.updateList(new ArrayList<Event>());

                loadDistricts(selectedCity.getId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCity = null;
            }
        });

        /*
            İlçe seçimi.
        */
        spinnerDistricts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(
                    AdapterView<?> parent,
                    View view,
                    int position,
                    long id
            ) {
                if (districtList.isEmpty()) {
                    return;
                }

                selectedDistrict = districtList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedDistrict = null;
            }
        });

        /*
            Etkinlikleri listele.
        */
        btnListEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedCity == null) {
                    Toast.makeText(
                            HomeActivity.this,
                            "Lütfen şehir seçiniz",
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }

                if (selectedDistrict == null) {
                    Toast.makeText(
                            HomeActivity.this,
                            "Lütfen ilçe seçiniz",
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }

                loadEvents(selectedCity.getId(), selectedDistrict.getId());
            }
        });
    }

    /*
        Şehirleri backend'den çeker.

        API:
        locations/cities_list.php

        POST:
        api_token
    */
    private void loadCities() {
        setStatus("Şehirler yükleniyor...");

        String apiToken = sessionManager.getApiToken();

        ApiClient.getApiService()
                .getCities(apiToken)
                .enqueue(new Callback<ApiResponse<List<City>>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<List<City>>> call,
                            Response<ApiResponse<List<City>>> response
                    ) {
                        if (!response.isSuccessful()) {
                            setStatus("Şehirler alınamadı. Sunucu hatası: " + response.code());
                            return;
                        }

                        ApiResponse<List<City>> apiResponse = response.body();

                        if (apiResponse == null) {
                            setStatus("Şehirler alınamadı. Boş cevap döndü.");
                            return;
                        }

                        if (!apiResponse.isSuccess()) {
                            setStatus(apiResponse.getMessage());
                            return;
                        }

                        List<City> cities = apiResponse.getData();

                        cityList.clear();

                        if (cities != null) {
                            cityList.addAll(cities);
                        }

                        setupCitySpinner();

                        if (cityList.isEmpty()) {
                            setStatus("Aktif şehir bulunamadı.");
                        } else {
                            setStatus("Şehir seçiniz.");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<City>>> call, Throwable t) {
                        setStatus("Bağlantı hatası: " + t.getLocalizedMessage());
                    }
                });
    }

    /*
        Şehir Spinner'ını doldurur.

        City.java içinde toString() name döndürdüğü için
        Spinner ekranda şehir adını gösterir.
    */
    private void setupCitySpinner() {
        ArrayAdapter<City> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                cityList
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerCities.setAdapter(adapter);
    }

    /*
        Seçilen şehre göre ilçeleri backend'den çeker.

        API:
        locations/districts_by_city.php

        POST:
        api_token
        city_id
    */
    private void loadDistricts(int cityId) {
        setStatus("İlçeler yükleniyor...");

        String apiToken = sessionManager.getApiToken();

        ApiClient.getApiService()
                .getDistrictsByCity(apiToken, cityId)
                .enqueue(new Callback<ApiResponse<List<District>>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<List<District>>> call,
                            Response<ApiResponse<List<District>>> response
                    ) {
                        if (!response.isSuccessful()) {
                            setStatus("İlçeler alınamadı. Sunucu hatası: " + response.code());
                            return;
                        }

                        ApiResponse<List<District>> apiResponse = response.body();

                        if (apiResponse == null) {
                            setStatus("İlçeler alınamadı. Boş cevap döndü.");
                            return;
                        }

                        if (!apiResponse.isSuccess()) {
                            setStatus(apiResponse.getMessage());
                            return;
                        }

                        List<District> districts = apiResponse.getData();

                        districtList.clear();

                        if (districts != null) {
                            districtList.addAll(districts);
                        }

                        setupDistrictSpinner();

                        if (districtList.isEmpty()) {
                            setStatus("Bu şehir için aktif ilçe bulunamadı.");
                        } else {
                            setStatus("İlçe seçip etkinlikleri listeleyebilirsin.");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<District>>> call, Throwable t) {
                        setStatus("Bağlantı hatası: " + t.getLocalizedMessage());
                    }
                });
    }

    /*
        İlçe Spinner'ını doldurur.

        District.java içinde toString() name döndürdüğü için
        Spinner ekranda ilçe adını gösterir.
    */
    private void setupDistrictSpinner() {
        ArrayAdapter<District> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                districtList
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerDistricts.setAdapter(adapter);
    }

    /*
        Şehir + ilçe seçimine göre etkinlikleri backend'den çeker.

        API:
        events/events_by_location.php

        POST:
        api_token
        city_id
        district_id
    */
    private void loadEvents(int cityId, int districtId) {
        setStatus("Etkinlikler yükleniyor...");

        String apiToken = sessionManager.getApiToken();

        ApiClient.getApiService()
                .getEventsByLocation(apiToken, cityId, districtId)
                .enqueue(new Callback<ApiResponse<List<Event>>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<List<Event>>> call,
                            Response<ApiResponse<List<Event>>> response
                    ) {
                        if (!response.isSuccessful()) {
                            setStatus("Etkinlikler alınamadı. Sunucu hatası: " + response.code());
                            return;
                        }

                        ApiResponse<List<Event>> apiResponse = response.body();

                        if (apiResponse == null) {
                            setStatus("Etkinlikler alınamadı. Boş cevap döndü.");
                            return;
                        }

                        if (!apiResponse.isSuccess()) {
                            setStatus(apiResponse.getMessage());
                            return;
                        }

                        List<Event> events = apiResponse.getData();

                        if (events == null) {
                            events = new ArrayList<>();
                        }

                        eventAdapter.updateList(events);

                        if (events.isEmpty()) {
                            setStatus("Bu konum için etkinlik bulunamadı.");
                        } else {
                            setStatus(events.size() + " etkinlik listelendi.");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Event>>> call, Throwable t) {
                        setStatus("Bağlantı hatası: " + t.getLocalizedMessage());
                    }
                });
    }

    /*
        Durum mesajını ekrana basar.
    */
    private void setStatus(String message) {
        tvStatus.setText(message);
    }

    /*
        Login ekranına döner.
    */
    private void goToLogin() {
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
