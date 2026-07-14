package com.alperensarac.ebiletjava.ui.event;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alperensarac.ebiletjava.R;
import com.alperensarac.ebiletjava.data.api.ApiClient;
import com.alperensarac.ebiletjava.data.model.ApiResponse;
import com.alperensarac.ebiletjava.data.model.Event;
import com.alperensarac.ebiletjava.data.model.Ticket;
import com.alperensarac.ebiletjava.data.session.SessionManager;
import com.bumptech.glide.Glide;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventDetailActivity extends AppCompatActivity {

    /*
        HomeActivity'den event_id gönderirken kullanılacak key.
    */
    public static final String EXTRA_EVENT_ID = "event_id";

    /*
        XML view değişkenleri.
    */
    private Button btnBack;
    private Button btnBuyTicket;

    private TextView tvTopTitle;
    private TextView tvStatus;
    private TextView tvEventTitle;
    private TextView tvDescription;
    private TextView tvDate;
    private TextView tvLocation;
    private TextView tvVenue;
    private TextView tvAddress;
    private TextView tvPrice;
    private TextView tvQuota;

    private ImageView imgPoster;

    /*
        Session bilgisi.
    */
    private SessionManager sessionManager;

    /*
        HomeActivity'den gelen etkinlik ID.
    */
    private int eventId = 0;

    /*
        Backend'den gelen güncel etkinlik nesnesi.
        Bilet alırken bunu kullanacağız.
    */
    private Event currentEvent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        sessionManager = new SessionManager(this);

        /*
            Bu ekran giriş yapılmadan kullanılmamalı.
        */
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Lütfen tekrar giriş yapın", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupClickListeners();

        /*
            HomeActivity'den gelen event_id değerini alıyoruz.
        */
        eventId = getIntent().getIntExtra(EXTRA_EVENT_ID, 0);

        if (eventId <= 0) {
            Toast.makeText(this, "Etkinlik bilgisi alınamadı", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        /*
            Ekran açılınca etkinlik detayını backend'den çekiyoruz.
        */
        loadEventDetail();
    }

    /*
        XML view'larını Java değişkenlerine bağlar.
    */
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnBuyTicket = findViewById(R.id.btnBuyTicket);

        tvTopTitle = findViewById(R.id.tvTopTitle);
        tvStatus = findViewById(R.id.tvStatus);
        tvEventTitle = findViewById(R.id.tvEventTitle);
        tvDescription = findViewById(R.id.tvDescription);
        tvDate = findViewById(R.id.tvDate);
        tvLocation = findViewById(R.id.tvLocation);
        tvVenue = findViewById(R.id.tvVenue);
        tvAddress = findViewById(R.id.tvAddress);
        tvPrice = findViewById(R.id.tvPrice);
        tvQuota = findViewById(R.id.tvQuota);

        imgPoster = findViewById(R.id.imgPoster);
    }

    /*
        Buton tıklama olaylarını ayarlar.
    */
    private void setupClickListeners() {
        /*
            Geri butonu.
        */
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        /*
            Bilet Al butonu.
        */
        btnBuyTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buyTicket();
            }
        });
    }

    /*
        Etkinlik detayını backend'den getirir.

        API:
        events/event_detail.php

        POST:
        api_token
        event_id
    */
    private void loadEventDetail() {
        setStatus("Etkinlik detayı yükleniyor...");
        setLoading(true);

        String apiToken = sessionManager.getApiToken();

        ApiClient.getApiService()
                .getEventDetail(apiToken, eventId)
                .enqueue(new Callback<ApiResponse<Event>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<Event>> call,
                            Response<ApiResponse<Event>> response
                    ) {
                        setLoading(false);

                        if (!response.isSuccessful()) {
                            setStatus("Sunucu hatası: " + response.code());
                            return;
                        }

                        ApiResponse<Event> apiResponse = response.body();

                        if (apiResponse == null) {
                            setStatus("Boş sunucu cevabı");
                            return;
                        }

                        if (!apiResponse.isSuccess()) {
                            setStatus(apiResponse.getMessage());
                            return;
                        }

                        Event event = apiResponse.getData();

                        if (event == null) {
                            setStatus("Etkinlik bilgisi alınamadı");
                            return;
                        }

                        currentEvent = event;

                        bindEventToUI(event);

                        setStatus("Etkinlik detayı getirildi.");
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Event>> call, Throwable t) {
                        setLoading(false);
                        setStatus("Bağlantı hatası: " + t.getLocalizedMessage());
                    }
                });
    }

    /*
        Event nesnesini ekrana basar.
    */
    private void bindEventToUI(Event event) {
        tvTopTitle.setText("Etkinlik Detayı");

        tvEventTitle.setText(event.getTitle());

        if (event.getDescription() == null || event.getDescription().isEmpty()) {
            tvDescription.setText("Açıklama bulunmuyor.");
        } else {
            tvDescription.setText(event.getDescription());
        }

        String eventDate = event.getEventDate() == null ? "-" : event.getEventDate();
        tvDate.setText("Tarih: " + eventDate);

        /*
            Şehir / ilçe bilgisi iki farklı şekilde gelebilir:
            - city_name / district_name
            - city / district nested object
        */
        String cityName = "-";
        String districtName = "-";

        if (event.getCity() != null && event.getCity().getName() != null) {
            cityName = event.getCity().getName();
        } else if (event.getCityName() != null) {
            cityName = event.getCityName();
        }

        if (event.getDistrict() != null && event.getDistrict().getName() != null) {
            districtName = event.getDistrict().getName();
        } else if (event.getDistrictName() != null) {
            districtName = event.getDistrictName();
        }

        tvLocation.setText("Konum: " + cityName + " / " + districtName);

        String venueName = "Sahne bilgisi yok";
        String venueAddress = "Adres bilgisi yok";

        if (event.getVenue() != null) {
            if (event.getVenue().getName() != null) {
                venueName = event.getVenue().getName();
            }

            if (event.getVenue().getAddress() != null) {
                venueAddress = event.getVenue().getAddress();
            }
        }

        tvVenue.setText("Sahne: " + venueName);
        tvAddress.setText("Adres: " + venueAddress);

        int price = 0;

        if (event.getBasePrice() != null) {
            price = event.getBasePrice().intValue();
        }

        tvPrice.setText(price + " TL");

        int remainingQuota = 0;

        if (event.getRemainingQuota() != null) {
            remainingQuota = event.getRemainingQuota();
        }

        tvQuota.setText("Kalan: " + remainingQuota);

        /*
            Kontenjan yoksa butonu pasifleştiriyoruz.
        */
        if (remainingQuota <= 0) {
            btnBuyTicket.setEnabled(false);
            btnBuyTicket.setText("Kontenjan Doldu");
            btnBuyTicket.setBackgroundColor(0xFF94A3B8);
        } else {
            btnBuyTicket.setEnabled(true);
            btnBuyTicket.setText("Bilet Al");
            btnBuyTicket.setBackgroundColor(0xFF16A34A);
        }

        /*
            Poster yükleme.

            Backend poster_url örnek:
            uploads/events/kadikoy_akustik.jpg

            Tam URL:
            http://10.0.2.2/event_ticket_api/uploads/events/kadikoy_akustik.jpg
        */
        String posterUrl = event.getPosterUrl();

        if (posterUrl != null && !posterUrl.isEmpty()) {
            String finalPosterUrl;

            if (posterUrl.startsWith("http")) {
                finalPosterUrl = posterUrl;
            } else {
                finalPosterUrl = ApiClient.getBaseUrl() + posterUrl;
            }

            Glide.with(this)
                    .load(finalPosterUrl)
                    .centerCrop()
                    .into(imgPoster);
        } else {
            imgPoster.setImageDrawable(null);
        }
    }

    /*
        Bilet satın alma işlemi.

        API:
        tickets/ticket_buy.php

        POST:
        api_token
        event_id
    */
    private void buyTicket() {
        if (currentEvent == null) {
            Toast.makeText(this, "Etkinlik bilgisi henüz yüklenmedi", Toast.LENGTH_SHORT).show();
            return;
        }

        int remainingQuota = 0;

        if (currentEvent.getRemainingQuota() != null) {
            remainingQuota = currentEvent.getRemainingQuota();
        }

        if (remainingQuota <= 0) {
            Toast.makeText(this, "Bu etkinlik için kontenjan kalmamış", Toast.LENGTH_SHORT).show();
            return;
        }

        setStatus("Bilet oluşturuluyor...");
        setBuying(true);

        String apiToken = sessionManager.getApiToken();

        ApiClient.getApiService()
                .buyTicket(apiToken, currentEvent.getId())
                .enqueue(new Callback<ApiResponse<Ticket>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<Ticket>> call,
                            Response<ApiResponse<Ticket>> response
                    ) {
                        setBuying(false);

                        if (!response.isSuccessful()) {
                            setStatus("Sunucu hatası: " + response.code());
                            return;
                        }

                        ApiResponse<Ticket> apiResponse = response.body();

                        if (apiResponse == null) {
                            setStatus("Boş sunucu cevabı");
                            return;
                        }

                        if (!apiResponse.isSuccess()) {
                            setStatus(apiResponse.getMessage());
                            Toast.makeText(
                                    EventDetailActivity.this,
                                    apiResponse.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        Ticket ticket = apiResponse.getData();

                        if (ticket == null) {
                            setStatus("Bilet oluşturuldu fakat detay alınamadı");
                            Toast.makeText(
                                    EventDetailActivity.this,
                                    "Bilet oluşturuldu",
                                    Toast.LENGTH_SHORT
                            ).show();

                            /*
                                Yine de kontenjanı güncellemek için detay tekrar çekilir.
                            */
                            loadEventDetail();
                            return;
                        }

                        String ticketCode = ticket.getTicketCode() == null
                                ? "-"
                                : ticket.getTicketCode();

                        setStatus("Bilet başarıyla oluşturuldu.");

                        Toast.makeText(
                                EventDetailActivity.this,
                                "Bilet alındı: " + ticketCode,
                                Toast.LENGTH_LONG
                        ).show();

                        /*
                            Bilet alındıktan sonra kontenjan güncellenmiş olabilir.
                            Bu yüzden etkinlik detayını tekrar çekiyoruz.
                        */
                        loadEventDetail();

                        /*
                            Bir sonraki adımda MyTicketsActivity yapınca
                            burada otomatik Biletlerim ekranına geçeceğiz.
                        */
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Ticket>> call, Throwable t) {
                        setBuying(false);
                        setStatus("Bağlantı hatası: " + t.getLocalizedMessage());
                    }
                });
    }

    /*
        Etkinlik detayı yüklenirken bilet al butonunu kapatır.
    */
    private void setLoading(boolean isLoading) {
        btnBuyTicket.setEnabled(!isLoading);
    }

    /*
        Bilet alma isteği sırasında butonu kapatır.
    */
    private void setBuying(boolean isBuying) {
        btnBuyTicket.setEnabled(!isBuying);

        if (isBuying) {
            btnBuyTicket.setText("Bilet Oluşturuluyor...");
        } else {
            btnBuyTicket.setText("Bilet Al");
        }
    }

    /*
        Durum mesajını ekrana basar.
    */
    private void setStatus(String message) {
        tvStatus.setText(message);
    }
}
