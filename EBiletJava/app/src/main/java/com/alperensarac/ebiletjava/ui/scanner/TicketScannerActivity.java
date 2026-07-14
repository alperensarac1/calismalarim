package com.alperensarac.ebiletjava.ui.scanner;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.alperensarac.ebiletjava.R;
import com.alperensarac.ebiletjava.data.api.ApiClient;
import com.alperensarac.ebiletjava.data.model.ApiResponse;
import com.alperensarac.ebiletjava.data.model.Ticket;
import com.alperensarac.ebiletjava.data.session.SessionManager;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
    TicketScannerActivity.java

    Görevli / admin kullanıcıların bilet kontrol ekranıdır.

    Bu ekran:
    - QR kod okutur
    - QR içindeki ticket_code değerini alır
    - check/ticket_check.php API'sine gönderir
    - Backend bileti kontrol eder
    - Aktif bileti used yapar
    - Sonucu ekranda gösterir

    Normal kullanıcı bu ekrana giremez.
*/
public class TicketScannerActivity extends AppCompatActivity {

    /*
        XML view değişkenleri.
    */
    private Button btnBack;
    private Button btnScanQr;
    private Button btnManualCheck;

    private EditText etTicketCode;

    private TextView tvStaffInfo;
    private TextView tvResultTitle;
    private TextView tvResultMessage;
    private TextView tvTicketInfo;
    private TextView tvUserInfo;
    private TextView tvEventInfo;
    private TextView tvLocationInfo;
    private TextView tvStatus;

    private LinearLayout resultCard;

    /*
        Session bilgileri.
    */
    private SessionManager sessionManager;

    /*
        QR okutma sonucunu almak için Activity Result Launcher.
    */
    private ActivityResultLauncher<ScanOptions> qrLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_scanner);

        sessionManager = new SessionManager(this);

        /*
            Kullanıcı giriş yapmamışsa ekranı kapatıyoruz.
        */
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Lütfen tekrar giriş yapın", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        /*
            Bu ekran sadece staff/admin için açık.
        */
        if (!sessionManager.isStaffOrAdmin()) {
            Toast.makeText(this, "Bu ekran için görevli yetkisi gerekir", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initViews();
        setupScannerLauncher();
        setupUi();
        setupClickListeners();
    }

    /*
        XML view'larını Java değişkenlerine bağlar.
    */
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnScanQr = findViewById(R.id.btnScanQr);
        btnManualCheck = findViewById(R.id.btnManualCheck);

        etTicketCode = findViewById(R.id.etTicketCode);

        tvStaffInfo = findViewById(R.id.tvStaffInfo);
        tvResultTitle = findViewById(R.id.tvResultTitle);
        tvResultMessage = findViewById(R.id.tvResultMessage);
        tvTicketInfo = findViewById(R.id.tvTicketInfo);
        tvUserInfo = findViewById(R.id.tvUserInfo);
        tvEventInfo = findViewById(R.id.tvEventInfo);
        tvLocationInfo = findViewById(R.id.tvLocationInfo);
        tvStatus = findViewById(R.id.tvStatus);

        resultCard = findViewById(R.id.resultCard);
    }

    /*
        QR scanner launcher kurulur.

        JourneyApps ZXing kullanıyoruz.
        Kullanıcı QR okutunca result.getContents() bize QR içindeki text'i verir.
    */
    private void setupScannerLauncher() {
        qrLauncher = registerForActivityResult(new ScanContract(), result -> {
            String qrContent = result.getContents();

            if (qrContent == null || qrContent.trim().isEmpty()) {
                setStatus("QR okuma iptal edildi veya boş sonuç döndü.");
                return;
            }

            qrContent = qrContent.trim();

            /*
                Okunan kodu manuel alana da yazıyoruz.
                Böylece görevli hangi kodun okunduğunu görür.
            */
            etTicketCode.setText(qrContent);

            /*
                Okunan QR içeriğini backend'e gönderiyoruz.
            */
            checkTicket(qrContent);
        });
    }

    /*
        Ekran ilk açıldığında kullanıcı bilgilerini basar.
    */
    private void setupUi() {
        tvStaffInfo.setText(
                "Görevli: " + sessionManager.getFullName()
                        + " | Rol: " + sessionManager.getRole()
        );

        clearResult();
    }

    /*
        Buton olayları.
    */
    private void setupClickListeners() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnScanQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openQrScanner();
            }
        });

        btnManualCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ticketCode = etTicketCode.getText() == null
                        ? ""
                        : etTicketCode.getText().toString().trim();

                etTicketCode.setError(null);

                if (ticketCode.isEmpty()) {
                    etTicketCode.setError("Bilet kodu zorunludur");
                    etTicketCode.requestFocus();
                    return;
                }

                checkTicket(ticketCode);
            }
        });
    }

    /*
        QR okuyucu ekranını açar.
    */
    private void openQrScanner() {
        ScanOptions options = new ScanOptions();

        /*
            Sadece QR kod okutmak istiyoruz.
        */
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);

        /*
            Kamera ekranında görünecek açıklama.
        */
        options.setPrompt("Bilet QR kodunu kamera alanına getir");

        /*
            Okuyunca bip sesi.
        */
        options.setBeepEnabled(true);

        /*
            Cihaz yönünü kilitlemiyoruz.
        */
        options.setOrientationLocked(false);

        /*
            Barkod görüntüsünü kaydetmeye gerek yok.
        */
        options.setBarcodeImageEnabled(false);

        qrLauncher.launch(options);
    }

    /*
        Bilet kodunu backend'e gönderip kontrol eder.

        API:
        check/ticket_check.php

        POST:
        api_token
        ticket_code
    */
    private void checkTicket(String ticketCode) {
        setLoading(true);
        setStatus("Bilet kontrol ediliyor...");
        clearResult();

        String apiToken = sessionManager.getApiToken();

        ApiClient.getApiService()
                .checkTicket(apiToken, ticketCode)
                .enqueue(new Callback<ApiResponse<Ticket>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<Ticket>> call,
                            Response<ApiResponse<Ticket>> response
                    ) {
                        setLoading(false);

                        if (!response.isSuccessful()) {
                            showFailedResult(
                                    "Sunucu Hatası",
                                    "HTTP hata kodu: " + response.code()
                            );
                            return;
                        }

                        ApiResponse<Ticket> apiResponse = response.body();

                        if (apiResponse == null) {
                            showFailedResult(
                                    "Boş Cevap",
                                    "Sunucudan boş cevap döndü."
                            );
                            return;
                        }

                        /*
                            Backend success false döndürürse:
                            - invalid
                            - already_used
                            - cancelled
                            - passive_event

                            gibi durumlarda message bilgisini gösteriyoruz.
                        */
                        if (!apiResponse.isSuccess()) {
                            showFailedResult(
                                    "Giriş Reddedildi",
                                    apiResponse.getMessage()
                            );
                            return;
                        }

                        Ticket ticket = apiResponse.getData();

                        if (ticket == null) {
                            showSuccessResult(
                                    "Bilet Onaylandı",
                                    apiResponse.getMessage()
                            );
                            return;
                        }

                        showTicketResult(apiResponse.getMessage(), ticket);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Ticket>> call, Throwable t) {
                        setLoading(false);

                        showFailedResult(
                                "Bağlantı Hatası",
                                t.getLocalizedMessage() == null
                                        ? "Sunucuya ulaşılamadı."
                                        : t.getLocalizedMessage()
                        );
                    }
                });
    }

    /*
        Backend'den başarılı bilet data geldiğinde sonucu ekrana basar.
    */
    private void showTicketResult(String apiMessage, Ticket ticket) {
        String result = ticket.getResult();

        if (result == null || result.isEmpty()) {
            result = "approved";
        }

        if ("approved".equals(result)) {
            showSuccessResult("Giriş Onaylandı", apiMessage);
        } else {
            showFailedResult("Giriş Reddedildi", apiMessage);
        }

        /*
            Bilet bilgileri.
        */
        String ticketId = ticket.getTicketId() == null
                ? "-"
                : String.valueOf(ticket.getTicketId());

        String ticketCode = ticket.getTicketCode() == null
                ? "-"
                : ticket.getTicketCode();

        String ticketStatus = "-";

        if (ticket.getTicketStatus() != null) {
            ticketStatus = ticket.getTicketStatus();
        } else if (ticket.getStatus() != null) {
            ticketStatus = ticket.getStatus();
        }

        tvTicketInfo.setText(
                "Bilet ID: " + ticketId + "\n"
                        + "Bilet Kodu: " + ticketCode + "\n"
                        + "Durum: " + ticketStatus
        );

        /*
            Kullanıcı bilgileri.
        */
        String userName = "-";
        String userEmail = "-";
        String userPhone = "-";

        if (ticket.getUser() != null) {
            if (ticket.getUser().getFullName() != null) {
                userName = ticket.getUser().getFullName();
            }

            if (ticket.getUser().getEmail() != null) {
                userEmail = ticket.getUser().getEmail();
            }

            if (ticket.getUser().getPhone() != null) {
                userPhone = ticket.getUser().getPhone();
            }
        }

        tvUserInfo.setText(
                "Kullanıcı: " + userName + "\n"
                        + "E-posta: " + userEmail + "\n"
                        + "Telefon: " + userPhone
        );

        /*
            Etkinlik bilgileri.
        */
        String eventTitle = "-";
        String eventDate = "-";

        if (ticket.getEvent() != null) {
            if (ticket.getEvent().getTitle() != null) {
                eventTitle = ticket.getEvent().getTitle();
            }

            if (ticket.getEvent().getEventDate() != null) {
                eventDate = ticket.getEvent().getEventDate();
            }
        } else if (ticket.getEventTitle() != null) {
            eventTitle = ticket.getEventTitle();
        }

        tvEventInfo.setText(
                "Etkinlik: " + eventTitle + "\n"
                        + "Tarih: " + eventDate
        );

        /*
            Konum bilgileri.
        */
        String cityName = "-";
        String districtName = "-";
        String venueName = "-";
        String venueAddress = "-";

        if (ticket.getLocation() != null) {
            if (ticket.getLocation().getCityName() != null) {
                cityName = ticket.getLocation().getCityName();
            }

            if (ticket.getLocation().getDistrictName() != null) {
                districtName = ticket.getLocation().getDistrictName();
            }

            if (ticket.getLocation().getVenueName() != null) {
                venueName = ticket.getLocation().getVenueName();
            }

            if (ticket.getLocation().getVenueAddress() != null) {
                venueAddress = ticket.getLocation().getVenueAddress();
            }
        }

        tvLocationInfo.setText(
                "Konum: " + cityName + " / " + districtName + "\n"
                        + "Sahne: " + venueName + "\n"
                        + "Adres: " + venueAddress
        );
    }

    /*
        Başarılı sonuç görünümü.
    */
    private void showSuccessResult(String title, String message) {
        tvResultTitle.setText(title);
        tvResultMessage.setText(message);

        tvResultTitle.setTextColor(0xFF166534);
        resultCard.setBackgroundColor(0xFFDCFCE7);

        setStatus("Kontrol tamamlandı.");
    }

    /*
        Başarısız sonuç görünümü.
    */
    private void showFailedResult(String title, String message) {
        tvResultTitle.setText(title);
        tvResultMessage.setText(message);

        tvResultTitle.setTextColor(0xFF991B1B);
        resultCard.setBackgroundColor(0xFFFEE2E2);

        tvTicketInfo.setText("");
        tvUserInfo.setText("");
        tvEventInfo.setText("");
        tvLocationInfo.setText("");

        setStatus("Kontrol tamamlandı.");
    }

    /*
        Sonuç alanını ilk haline döndürür.
    */
    private void clearResult() {
        tvResultTitle.setText("Henüz kontrol yapılmadı");
        tvResultMessage.setText("QR kod okutulduğunda sonuç burada görünecek.");

        tvResultTitle.setTextColor(0xFF0F172A);
        resultCard.setBackgroundColor(0xFFFFFFFF);

        tvTicketInfo.setText("");
        tvUserInfo.setText("");
        tvEventInfo.setText("");
        tvLocationInfo.setText("");
    }

    /*
        API isteği sırasında butonları pasifleştirir.
    */
    private void setLoading(boolean isLoading) {
        btnScanQr.setEnabled(!isLoading);
        btnManualCheck.setEnabled(!isLoading);

        if (isLoading) {
            btnScanQr.setText("Kontrol Ediliyor...");
        } else {
            btnScanQr.setText("QR Kod Okut");
        }
    }

    private void setStatus(String message) {
        tvStatus.setText(message);
    }
}
