package com.example.qryoklamajava.view;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;

import com.example.qryoklamajava.R;
import com.example.qryoklamajava.data.Prefs;
import com.example.qryoklamajava.databinding.ActivityScanBinding;
import com.example.qryoklamajava.service.ApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ScanActivity extends AppCompatActivity {

    ActivityScanBinding binding;
    private FusedLocationProviderClient fused;
    private Prefs prefs;

    private boolean firstResume = true;

    private final ActivityResultLauncher<String> camPerm =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (!granted) {
                    Toast.makeText(this, "Kamera izni gerekli", Toast.LENGTH_LONG).show();
                } else {
                    startScanningIfReady();
                }
            });

    private final ActivityResultLauncher<String[]> locPerms =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), (Map<String,Boolean> result) -> {
                boolean fine = Boolean.TRUE.equals(result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false));
                boolean coarse = Boolean.TRUE.equals(result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false));
                if (!fine && !coarse) {
                    if (isPermissionPermanentlyDenied(Manifest.permission.ACCESS_FINE_LOCATION) ||
                            isPermissionPermanentlyDenied(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        showGoToSettingsDialog("Konum izni kalıcı olarak reddedilmiş görünüyor. Lütfen Ayarlar > Uygulamalar > İzinler kısmından açın.");
                    } else {
                        Toast.makeText(this, "Konum izni gerekli", Toast.LENGTH_LONG).show();
                    }
                } else {
                    ensureLocationEnabled();
                }
            });

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScanBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        prefs = new Prefs(this);
        fused = LocationServices.getFusedLocationProviderClient(this);

        camPerm.launch(Manifest.permission.CAMERA);
        requestLocationPermissions();

        binding.barcodeScanner.decodeContinuous(callback);

        binding.btnKodGir.setOnClickListener(v ->
                showInputDialog(this, "Kod Gönder", "6 haneli kod", "Gönder", value ->
                        getCurrentLocation((lat, lng) -> sendAttendanceByCode(value, lat, lng))
                )
        );

        binding.btnYoklamaGoster.setOnClickListener(v -> {
            binding.barcodeScanner.pause();
            binding.barcodeScanner.setVisibility(View.GONE);
            binding.container.setVisibility(View.VISIBLE);
            binding.container.bringToFront();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new StudentAttendanceFragment())
                    .addToBackStack("attendance")
                    .commit();
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                    binding.container.setVisibility(View.GONE);
                    binding.barcodeScanner.setVisibility(View.VISIBLE);
                    binding.barcodeScanner.resume();
                } else {
                    setEnabled(false);
                    onBackPressed();
                }
            }
        });
        binding.btnSinavYeriSorgula.setOnClickListener(view -> {

            binding.container.setVisibility(View.VISIBLE);

            String ogrNo = prefs.getStudentNo();
            SinavWebView fragment = SinavWebView.newInstance(ogrNo);

            int containerId = binding.container.getId();

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(containerId, fragment)
                    .addToBackStack(null)
                    .commit();

        });


    }

    private String lastText = null;
    private long lastTs = 0;
    private static final long SCAN_DEBOUNCE_MS = 1200;

    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override public void barcodeResult(BarcodeResult result) {
            if (result == null || result.getText() == null) return;
            long now = System.currentTimeMillis();
            String txt = result.getText();

            if (lastText != null && lastText.equals(txt) && (now - lastTs) < SCAN_DEBOUNCE_MS) {
                return;
            }
            lastText = txt; lastTs = now;

            binding.barcodeScanner.pause();

            getCurrentLocation((lat, lng) -> {
                sendAttendance(txt, lat, lng);
                Log.d("QR_LATLNG", lat + ", " + lng);
            });
        }
    };

    private void startScanningIfReady() {
        binding.barcodeScanner.resume();
    }

    private interface LocCb { void onLoc(double lat, double lng); }

    private boolean hasLocationPermission(){
        boolean fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        return fine || coarse;
    }

    private void requestLocationPermissions(){
        if (!hasLocationPermission()) {
            locPerms.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        } else {
            ensureLocationEnabled();
        }
    }

    private void ensureLocationEnabled(){
        if (!isLocationEnabled()) {
            new AlertDialog.Builder(this)
                    .setTitle("Konum Kapalı")
                    .setMessage("Konum servisleri kapalı görünüyor. Açmak ister misiniz?")
                    .setPositiveButton("Aç", (d, w) -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                    .setNegativeButton("İptal", null)
                    .show();
        }
    }

    private boolean isLocationEnabled(){
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            boolean gps = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean net = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            return gps || net;
        } catch (Exception e){
            return true;
        }
    }

    private boolean isPermissionPermanentlyDenied(String permission){
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean denied = ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED;
            boolean showRationale = shouldShowRequestPermissionRationale(permission);
            return denied && !showRationale;
        }
        return false;
    }

    private void showGoToSettingsDialog(String msg){
        new AlertDialog.Builder(this)
                .setTitle("İzin Gerekli")
                .setMessage(msg)
                .setPositiveButton("Ayarları Aç", (d, w) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.fromParts("package", getPackageName(), null));
                    startActivity(intent);
                })
                .setNegativeButton("İptal", null)
                .show();
    }

    private JSONObject parseQrPayload(String raw) throws Exception {
        String s = raw == null ? "" : raw.trim();

        if ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("“") && s.endsWith("”"))) {
            s = s.substring(1, s.length() - 1);
        }

        s = s.replace("\\\"", "\"").replace("\\\\", "\\");

        if (s.startsWith("http")) {
            Uri u = Uri.parse(s);
            String q = u.getQueryParameter("qr");
            if (q != null && q.trim().startsWith("{")) {
                s = q.trim();
            }
        }

        if (!s.startsWith("{") && s.matches("^[A-Za-z0-9+/=\\s]+$")) {
            try {
                byte[] decoded = android.util.Base64.decode(s, android.util.Base64.DEFAULT);
                String b64 = new String(decoded, java.nio.charset.StandardCharsets.UTF_8).trim();
                if (b64.startsWith("{")) s = b64;
            } catch (Exception ignore) {}
        }

        if (!s.startsWith("{")) throw new Exception("Geçersiz QR: " + (s.length() > 60 ? s.substring(0,60)+"..." : s));
        return new JSONObject(s);
    }

    /** Sunucudan gelen hata gövdesi JSON değilse HTML etiketlerini ayıklar. */
    private String humanizeServerError(String resp) {
        if (resp == null || resp.isEmpty()) return "Bilinmeyen hata";
        if (resp.trim().startsWith("{") || resp.trim().startsWith("[")) return resp;
        return resp.replaceAll("(?s)<[^>]*>", " ")
                .replace("&quot;", "\"")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private void getCurrentLocation(LocCb cb){
        if (!hasLocationPermission()){
            requestLocationPermissions();
            Toast.makeText(this, "Konum izni gerekli", Toast.LENGTH_SHORT).show();
            binding.barcodeScanner.resume();
            return;
        }

        try {
            fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(loc -> {
                        if (isLocationUsable(loc)) {
                            // Güncel ve yeterince doğru konum
                            cb.onLoc(loc.getLatitude(), loc.getLongitude());
                        } else {
                            // getCurrentLocation başarısız veya eski/hatalı, lastLocation'a düş
                            fused.getLastLocation()
                                    .addOnSuccessListener(last -> {
                                        if (isLocationUsable(last)) {
                                            cb.onLoc(last.getLatitude(), last.getLongitude());
                                        } else {
                                            Toast.makeText(
                                                    this,
                                                    "Konum alınamadı veya çok eski.\nLütfen GPS'i açıp birkaç saniye bekleyin.",
                                                    Toast.LENGTH_LONG
                                            ).show();
                                            binding.barcodeScanner.resume();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(
                                                this,
                                                "Konum (last) hatası: " + e.getMessage(),
                                                Toast.LENGTH_SHORT
                                        ).show();
                                        binding.barcodeScanner.resume();
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(
                                this,
                                "Konum hatası: " + e.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                        binding.barcodeScanner.resume();
                    });
        } catch (SecurityException se){
            Toast.makeText(this, "Konum izni yok", Toast.LENGTH_SHORT).show();
            binding.barcodeScanner.resume();
        }
    }

    private String prettyServerError(String resp) {
        // HTML vs. içerebilir; önce sadeleştir
        String clean = humanizeServerError(resp);
        try {
            if (clean != null && (clean.trim().startsWith("{") || clean.trim().startsWith("["))) {
                JSONObject obj = new JSONObject(clean);
                StringBuilder sb = new StringBuilder();
                if (obj.has("ok"))        sb.append("ok: ").append(obj.optBoolean("ok")).append("\n");
                if (obj.has("error"))     sb.append("error: ").append(obj.optString("error")).append("\n");
                if (obj.has("message"))   sb.append("message: ").append(obj.optString("message")).append("\n");
                if (obj.has("file"))      sb.append("file: ").append(obj.optString("file")).append("\n");
                if (obj.has("line"))      sb.append("line: ").append(obj.optInt("line")).append("\n");
                if (obj.has("path"))      sb.append("path: ").append(obj.optString("path")).append("\n");
                if (obj.has("fields"))    sb.append("fields: ").append(obj.opt("fields")).append("\n");
                if (obj.has("details"))   sb.append("details: ").append(obj.optString("details")).append("\n");
                // Eğer yukarıdakiler yoksa tüm objeyi yaz
                if (sb.length() == 0) sb.append(obj.toString(2));
                return sb.toString().trim();
            }
        } catch (Exception ignore) { /* JSON değilse olduğu gibi döneriz */ }
        return clean != null ? clean : "Bilinmeyen hata";
    }

    /** Basit uyarı dialog’u */
    private void showErrorDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Tamam", null)
                .show();
    }

    private void sendAttendance(String qrPayloadRaw, double lat, double lng){
        String baseUrl = "https://alperensaracdeneme.com";
        String url = baseUrl + "/qryoklama/api/index.php?p=attendance/mark";

        String studentNo = prefs.getStudentNo();
        if (studentNo == null || studentNo.trim().isEmpty()) {
            Toast.makeText(this, "Öğrenci no bulunamadı", Toast.LENGTH_SHORT).show();
            binding.barcodeScanner.resume();
            return;
        }
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        String deviceInfo = android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL + " / SDK" + android.os.Build.VERSION.SDK_INT;

        try {
            JSONObject qrJson = parseQrPayload(qrPayloadRaw);

            JSONObject body = new JSONObject();
            body.put("student_no", studentNo);
            body.put("method", "QR");
            body.put("qr_payload", qrJson);
            body.put("lat", lat);
            body.put("lng", lng);
            body.put("device_id", deviceId);
            body.put("device_info", deviceInfo);

            ApiClient.postJson(url, body.toString(), new Callback() {
                @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> {
                        Toast.makeText(ScanActivity.this, "Ağ hatası: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        binding.barcodeScanner.resume();
                    });
                }
                @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    final String resp = response.body() != null ? response.body().string() : "";
                    Log.d("ATTENDANCE_QR_RESP", "code=" + response.code() + " body=" + resp);

                    runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            Toast.makeText(ScanActivity.this, "Yoklama alındı ✅", Toast.LENGTH_LONG).show();
                        } else {
                            String pretty = prettyServerError(resp);
                            showErrorDialog("Sunucu Hatası (" + response.code() + ")", pretty);
                        }
                        binding.barcodeScanner.resume();
                    });
                }

            });

        } catch (Exception e){
            Toast.makeText(this, "QR parse hatası: " + e.getMessage(), Toast.LENGTH_LONG).show();
            binding.barcodeScanner.resume();
        }
    }

    private void sendAttendanceByCode(String joinCode, double lat, double lng){
        String baseUrl = "https://alperensaracdeneme.com";
        String url = baseUrl + "/qryoklama/api/index.php?p=attendance/mark";

        String studentNo = prefs.getStudentNo();
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        String deviceInfo = android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL + " / SDK" + android.os.Build.VERSION.SDK_INT;

        try {
            JSONObject body = new JSONObject();
            body.put("student_no", studentNo);
            body.put("method", "CODE");
            body.put("join_code", joinCode);
            body.put("lat", lat);
            body.put("lng", lng);
            body.put("device_id", deviceId);
            body.put("device_info", deviceInfo);

            ApiClient.postJson(url, body.toString(), new Callback() {
                @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> {
                        Toast.makeText(ScanActivity.this, "Ağ hatası: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        binding.barcodeScanner.resume();
                    });
                }
                @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    final String resp = response.body() != null ? response.body().string() : "";
                    Log.d("ATTENDANCE_CODE_RESP", "code=" + response.code() + " body=" + resp);

                    runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            Toast.makeText(ScanActivity.this, "Yoklama alındı ✅", Toast.LENGTH_LONG).show();
                        } else {
                            String pretty = prettyServerError(resp);
                            showErrorDialog("Sunucu Hatası (" + response.code() + ")", pretty);
                        }
                        binding.barcodeScanner.resume();
                    });
                }

            });
        } catch (Exception e){
            Toast.makeText(this, "Beklenmeyen hata: " + e.getMessage(), Toast.LENGTH_LONG).show();
            binding.barcodeScanner.resume();
        }
    }

    @Override protected void onResume() {
        super.onResume();
        binding.barcodeScanner.resume();
    }

    @Override protected void onPause() {
        super.onPause();
        binding.barcodeScanner.pause();
    }

    public static void showInputDialog(
            Context context,
            String titleText,
            String hintText,
            String buttonText,
            OnSubmitListener onSubmit
    ) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        TextView tvTitle = new TextView(context);
        tvTitle.setText(titleText);
        tvTitle.setTextSize(18);
        tvTitle.setPadding(0, 0, 0, 20);
        layout.addView(tvTitle);

        EditText etInput = new EditText(context);
        etInput.setHint(hintText);
        etInput.setInputType(InputType.TYPE_CLASS_TEXT);
        layout.addView(etInput);

        Button btnAction = new Button(context);
        btnAction.setText(buttonText);
        layout.addView(btnAction);

        builder.setView(layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        btnAction.setOnClickListener(v -> {
            String input = etInput.getText().toString().trim();
            if (!input.isEmpty()) {
                onSubmit.onSubmit(input);
                dialog.dismiss();
            } else {
                Toast.makeText(context, "Lütfen bir değer girin!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    /**
     * Konumun kullanılabilir olup olmadığını kontrol eder:
     * - null olmamalı
     * - latitude/longitude 0,0 olmamalı
     * - yaşı 2 dakikadan (120 sn) küçük olmalı
     * - accuracy varsa 100 metreden kötü olmamalı
     */
    private boolean isLocationUsable(Location loc) {
        if (loc == null) return false;

        double lat = loc.getLatitude();
        double lng = loc.getLongitude();
        if (lat == 0.0 && lng == 0.0) {
            return false;
        }

        long now = System.currentTimeMillis();
        long ageMs = now - loc.getTime();
        // 2 dakikadan eski ise kabul etme
        if (ageMs > 120_000) { // 120.000 ms = 2 dk
            Log.w("LOC_CHECK", "Konum çok eski, ageMs=" + ageMs);
            return false;
        }

        if (loc.hasAccuracy()) {
            float acc = loc.getAccuracy();
            // 100 metreden daha kötü doğruluk varsa reddet
            if (acc > 100f) {
                Log.w("LOC_CHECK", "Konum doğruluğu kötü, acc=" + acc);
                return false;
            }
        }

        return true;
    }

    public interface OnSubmitListener { void onSubmit(String value); }
}
