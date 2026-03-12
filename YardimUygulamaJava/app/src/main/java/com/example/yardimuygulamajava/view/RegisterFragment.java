package com.example.yardimuygulamajava.view;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


import com.example.yardimuygulamajava.R;
import com.example.yardimuygulamajava.entity.Session;
import com.example.yardimuygulamajava.repo.AuthRepo;
import com.example.yardimuygulamajava.service.ApiOk;
import com.example.yardimuygulamajava.service.RegisterBody;
import com.example.yardimuygulamajava.service.User;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterFragment extends Fragment {

    private final AuthRepo repo = new AuthRepo();
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    private TextView tvDetected, tvInfo;
    private ProgressBar progress;

    private String detectedCity = null;
    private String detectedDistrict = null;

    private ActivityResultLauncher<String[]> requestLocationPerm;

    public RegisterFragment() { super(R.layout.fragment_register); }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestLocationPerm = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                (Map<String, Boolean> result) -> {
                    boolean fine = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION));
                    boolean coarse = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION));
                    if (fine || coarse) detectCityDistrict();
                    else if (tvDetected != null) tvDetected.setText("Konum izni verilmedi. Şehir/ilçe otomatik alınamadı.");
                }
        );
    }

    @Override public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        EditText etAd = view.findViewById(R.id.etAd);
        EditText etSoyad = view.findViewById(R.id.etSoyad);
        EditText etYas = view.findViewById(R.id.etYas);
        EditText etPhone = view.findViewById(R.id.etPhone);
        EditText etPass = view.findViewById(R.id.etPass);

        RadioButton rbHasta = view.findViewById(R.id.rbHasta);
        RadioButton rbYardimci = view.findViewById(R.id.rbYardimci);

        Button btnRegister = view.findViewById(R.id.btnRegister);
        Button btnGoLogin = view.findViewById(R.id.btnGoLogin);

        progress = view.findViewById(R.id.progress);
        tvInfo = view.findViewById(R.id.tvInfo);

        tvDetected = view.findViewById(R.id.tvDetected);
        Button btnDetect = view.findViewById(R.id.btnDetect);

        btnGoLogin.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        btnDetect.setOnClickListener(v -> ensureLocationAndDetect());

        btnRegister.setOnClickListener(v -> {
            String ad = etAd.getText().toString().trim();
            String soyad = etSoyad.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String pass = etPass.getText().toString().trim();

            Integer yas = null;
            String ys = etYas.getText().toString().trim();
            if (!ys.isEmpty()) { try { yas = Integer.parseInt(ys); } catch (Exception ignored) {} }

            String role = rbYardimci.isChecked() ? "YARDIMCI" : "HASTA";

            if (ad.isEmpty() || soyad.isEmpty() || phone.isEmpty() || pass.isEmpty()) {
                tvInfo.setText("Ad, soyad, telefon, şifre zorunlu");
                return;
            }
            if (detectedCity == null || detectedDistrict == null) {
                tvInfo.setText("Şehir/ilçe tespit edilemedi. Konum iznini/GPS'i kontrol et.");
                return;
            }

            progress.setVisibility(View.VISIBLE);
            tvInfo.setText("");

            RegisterBody body = new RegisterBody(role, ad, soyad, yas, phone, detectedCity, detectedDistrict, pass);

            repo.register(body).enqueue(new Callback<ApiOk<Object>>() {
                @Override public void onResponse(Call<ApiOk<Object>> call, Response<ApiOk<Object>> resp) {
                    progress.setVisibility(View.GONE);
                    ApiOk<Object> res = resp.body();
                    if (resp.isSuccessful() && res != null && res.getOk() && res.user != null) {
                        Session.save(requireContext(), res.user.id, res.user.role);
                        if ("YARDIMCI".equals(res.user.role)) go(HelperOpenListFragment.newInstance(res.user.id));
                        else go(PatientHelpFragment.newInstance(res.user.id));
                    } else {
                        tvInfo.setText(res != null ? (res.error != null ? res.error : "Kayıt başarısız") : "Kayıt başarısız");
                    }
                }

                @Override public void onFailure(Call<ApiOk<Object>> call, Throwable t) {
                    progress.setVisibility(View.GONE);
                    tvInfo.setText("Bağlantı hatası: " + t.getMessage());
                }
            });
        });

        // ekran açılınca otomatik tespit
        ensureLocationAndDetect();
    }

    private void go(Fragment f) {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, f)
                .commit();
    }

    private void ensureLocationAndDetect() {
        boolean fineGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarseGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (fineGranted || coarseGranted) detectCityDistrict();
        else requestLocationPerm.launch(new String[]{ Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION });
    }

    private void detectCityDistrict() {
        tvDetected.setText("Konumdan şehir/ilçe tespit ediliyor...");

        getCurrentLocation((lat, lng) -> {
            if (lat == null || lng == null) {
                tvDetected.setText("Konum alınamadı (GPS açık mı?)");
                return;
            }
            io.execute(() -> {
                String[] cd = reverseGeocode(lat, lng);
                requireActivity().runOnUiThread(() -> {
                    detectedCity = cd[0];
                    detectedDistrict = cd[1];
                    if (detectedCity != null && detectedDistrict != null) {
                        tvDetected.setText("Tespit edilen: " + detectedCity + " / " + detectedDistrict);
                    } else {
                        tvDetected.setText("Şehir/ilçe tespit edilemedi. Tekrar dene.");
                    }
                });
            });
        });
    }

    interface LocCb { void onResult(Double lat, Double lng); }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation(LocCb cb) {
        CancellationTokenSource token = new CancellationTokenSource();
        LocationServices.getFusedLocationProviderClient(requireContext())
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, token.getToken())
                .addOnSuccessListener(loc -> {
                    if (loc != null) cb.onResult(loc.getLatitude(), loc.getLongitude());
                    else cb.onResult(null, null);
                })
                .addOnFailureListener(e -> cb.onResult(null, null));
    }

    private String[] reverseGeocode(double lat, double lng) {
        try {
            Geocoder geocoder = new Geocoder(requireContext(), new Locale("tr","TR"));
            List<Address> list = geocoder.getFromLocation(lat, lng, 1);
            Address a = (list != null && !list.isEmpty()) ? list.get(0) : null;
            if (a == null) return new String[]{null, null};

            String city = a.getAdminArea();
            String district = a.getSubAdminArea();
            if (district == null || district.isEmpty()) district = a.getLocality();
            return new String[]{city, district};
        } catch (Exception e) {
            return new String[]{null, null};
        }
    }

    @Override public void onDestroy() {
        super.onDestroy();
        io.shutdownNow();
    }
}