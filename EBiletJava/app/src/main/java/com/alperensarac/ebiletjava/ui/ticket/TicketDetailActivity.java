package com.alperensarac.ebiletjava.ui.ticket;


import android.graphics.Bitmap;
import android.graphics.Color;
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
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
    TicketDetailActivity.java

    Tek biletin detayını ve QR kodunu gösterir.

    Backend:
    tickets/ticket_detail.php

    POST:
    api_token
    ticket_id

    QR kod içine backend'den gelen qr_code_text yazılır.
    Eğer qr_code_text boşsa ticket_code kullanılır.
*/
public class TicketDetailActivity extends AppCompatActivity {

    public static final String EXTRA_TICKET_ID = "ticket_id";

    private Button btnBack;

    private TextView tvStatus;
    private TextView tvEventTitle;
    private TextView tvTicketStatus;
    private TextView tvTicketCode;
    private TextView tvDate;
    private TextView tvVenue;
    private TextView tvLocation;
    private TextView tvPrice;
    private TextView tvUsedAt;

    private ImageView imgQrCode;

    private SessionManager sessionManager;

    private int ticketId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_detail);

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Lütfen tekrar giriş yapın", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupClickListeners();

        ticketId = getIntent().getIntExtra(EXTRA_TICKET_ID, 0);

        if (ticketId <= 0) {
            Toast.makeText(this, "Bilet bilgisi alınamadı", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadTicketDetail();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);

        tvStatus = findViewById(R.id.tvStatus);
        tvEventTitle = findViewById(R.id.tvEventTitle);
        tvTicketStatus = findViewById(R.id.tvTicketStatus);
        tvTicketCode = findViewById(R.id.tvTicketCode);
        tvDate = findViewById(R.id.tvDate);
        tvVenue = findViewById(R.id.tvVenue);
        tvLocation = findViewById(R.id.tvLocation);
        tvPrice = findViewById(R.id.tvPrice);
        tvUsedAt = findViewById(R.id.tvUsedAt);

        imgQrCode = findViewById(R.id.imgQrCode);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    /*
        Backend'den bilet detayı çeker.
    */
    private void loadTicketDetail() {
        setStatus("Bilet detayı yükleniyor...");

        String apiToken = sessionManager.getApiToken();

        ApiClient.getApiService()
                .getTicketDetail(apiToken, ticketId)
                .enqueue(new Callback<ApiResponse<Ticket>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<Ticket>> call,
                            Response<ApiResponse<Ticket>> response
                    ) {
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
                            return;
                        }

                        Ticket ticket = apiResponse.getData();

                        if (ticket == null) {
                            setStatus("Bilet bilgisi alınamadı");
                            return;
                        }

                        bindTicketToUI(ticket);

                        setStatus("Bilet detayı getirildi.");
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Ticket>> call, Throwable t) {
                        setStatus("Bağlantı hatası: " + t.getLocalizedMessage());
                    }
                });
    }

    /*
        Ticket bilgisini ekrana basar.
    */
    private void bindTicketToUI(Ticket ticket) {
        Event event = ticket.getEvent();

        /*
            Etkinlik adı.
        */
        String eventTitle = "Etkinlik";

        if (event != null && event.getTitle() != null) {
            eventTitle = event.getTitle();
        } else if (ticket.getEventTitle() != null) {
            eventTitle = ticket.getEventTitle();
        }

        tvEventTitle.setText(eventTitle);

        /*
            Bilet durumu.
        */
        String status = "-";

        if (ticket.getStatus() != null) {
            status = ticket.getStatus();
        } else if (ticket.getTicketStatus() != null) {
            status = ticket.getTicketStatus();
        }

        String statusText;

        if ("active".equals(status)) {
            statusText = "Aktif Bilet";
            tvTicketStatus.setBackgroundColor(0xFFDCFCE7);
            tvTicketStatus.setTextColor(0xFF166534);
        } else if ("used".equals(status)) {
            statusText = "Kullanıldı";
            tvTicketStatus.setBackgroundColor(0xFFE2E8F0);
            tvTicketStatus.setTextColor(0xFF475569);
        } else if ("cancelled".equals(status)) {
            statusText = "İptal Edildi";
            tvTicketStatus.setBackgroundColor(0xFFFEE2E2);
            tvTicketStatus.setTextColor(0xFF991B1B);
        } else {
            statusText = status;
            tvTicketStatus.setBackgroundColor(0xFFEFF6FF);
            tvTicketStatus.setTextColor(0xFF2563EB);
        }

        tvTicketStatus.setText(statusText);

        /*
            Bilet kodu.
        */
        String ticketCode = ticket.getTicketCode() == null ? "-" : ticket.getTicketCode();
        tvTicketCode.setText(ticketCode);

        /*
            Tarih.
        */
        String eventDate = "-";

        if (event != null && event.getEventDate() != null) {
            eventDate = event.getEventDate();
        }

        tvDate.setText("Tarih: " + eventDate);

        /*
            Sahne / konum.

            ticket_detail.php cevabında venue/city/district nested gelebilir.
            my_tickets.php tarafında ise location nested geliyordu.

            İki ihtimali de kontrol ediyoruz.
        */
        String venueName = "-";
        String cityName = "-";
        String districtName = "-";

        if (ticket.getVenue() != null && ticket.getVenue().getName() != null) {
            venueName = ticket.getVenue().getName();
        } else if (ticket.getLocation() != null && ticket.getLocation().getVenueName() != null) {
            venueName = ticket.getLocation().getVenueName();
        } else if (event != null && event.getVenue() != null && event.getVenue().getName() != null) {
            venueName = event.getVenue().getName();
        }

        if (ticket.getCity() != null && ticket.getCity().getName() != null) {
            cityName = ticket.getCity().getName();
        } else if (ticket.getLocation() != null && ticket.getLocation().getCityName() != null) {
            cityName = ticket.getLocation().getCityName();
        } else if (event != null && event.getCity() != null && event.getCity().getName() != null) {
            cityName = event.getCity().getName();
        }

        if (ticket.getDistrict() != null && ticket.getDistrict().getName() != null) {
            districtName = ticket.getDistrict().getName();
        } else if (ticket.getLocation() != null && ticket.getLocation().getDistrictName() != null) {
            districtName = ticket.getLocation().getDistrictName();
        } else if (event != null && event.getDistrict() != null && event.getDistrict().getName() != null) {
            districtName = event.getDistrict().getName();
        }

        tvVenue.setText("Sahne: " + venueName);
        tvLocation.setText("Konum: " + cityName + " / " + districtName);

        /*
            Fiyat.
        */
        int price = 0;

        if (ticket.getPrice() != null) {
            price = ticket.getPrice().intValue();
        }

        tvPrice.setText("Fiyat: " + price + " TL");

        /*
            Kullanım zamanı.
        */
        if (ticket.getUsedAt() != null && !ticket.getUsedAt().isEmpty()) {
            tvUsedAt.setText("Kullanım zamanı: " + ticket.getUsedAt());
        } else {
            tvUsedAt.setText("Bilet henüz kullanılmadı.");
        }

        /*
            QR kod üretimi.

            QR içine önce qr_code_text yazılır.
            Eğer o boşsa ticket_code kullanılır.
        */
        String qrText = ticket.getQrCodeText();

        if (qrText == null || qrText.isEmpty()) {
            qrText = ticket.getTicketCode();
        }

        if (qrText == null || qrText.isEmpty()) {
            setStatus("QR kod oluşturulamadı. Bilet kodu boş.");
            imgQrCode.setImageDrawable(null);
        } else {
            Bitmap qrBitmap = generateQrBitmap(qrText, 800, 800);
            imgQrCode.setImageBitmap(qrBitmap);
        }
    }

    /*
        QR kod Bitmap üretir.

        ZXing core kütüphanesini kullanıyoruz.

        text:
        QR içine yazılacak değer.

        width / height:
        Bitmap boyutu.
    */
    private Bitmap generateQrBitmap(String text, int width, int height) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                    text,
                    BarcodeFormat.QR_CODE,
                    width,
                    height
            );

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (bitMatrix.get(x, y)) {
                        bitmap.setPixel(x, y, Color.BLACK);
                    } else {
                        bitmap.setPixel(x, y, Color.WHITE);
                    }
                }
            }

            return bitmap;

        } catch (Exception e) {
            /*
                QR üretiminde hata olursa boş beyaz bitmap döndürüyoruz.
                Ayrıca kullanıcıya durum mesajı gösteriyoruz.
            */
            setStatus("QR kod üretilemedi: " + e.getMessage());

            Bitmap emptyBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            emptyBitmap.eraseColor(Color.WHITE);
            return emptyBitmap;
        }
    }

    private void setStatus(String message) {
        tvStatus.setText(message);
    }
}
