package com.example.yardimuygulamajava.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.yardimuygulamajava.R;
import com.example.yardimuygulamajava.model.HelpRequestActive;
import com.example.yardimuygulamajava.repo.PatientRepo;
import com.example.yardimuygulamajava.service.ApiOk;
import com.example.yardimuygulamajava.service.Poller;
import com.example.yardimuygulamajava.util.TimeUtils;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientHelpFragment extends Fragment {

    private long patientId;
    private final PatientRepo repo = new PatientRepo();

    private Double lat = null, lng = null;

    private TextView tvLocation, tvStatus;
    private ProgressBar progress;
    private EditText etService, etRoom;
    private Button btnSend, btnConfirm, btnCancel;

    private Long currentRequestId = null;
    private Poller poller;

    private ActivityResultLauncher<String[]> requestLocationPerm;

    public PatientHelpFragment() { super(R.layout.fragment_patient_help); }

    public static PatientHelpFragment newInstance(long patientId) {
        PatientHelpFragment f = new PatientHelpFragment();
        Bundle b = new Bundle();
        b.putLong("patient_id", patientId);
        f.setArguments(b);
        return f;
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        patientId = requireArguments().getLong("patient_id");

        requestLocationPerm = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean fine = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION));
                    boolean coarse = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION));
                    if (fine || coarse) fetchLocationOnce(null);
                    else if (tvStatus != null) tvStatus.setText("Konum izni verilmedi.");
                }
        );
    }

    @Override public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        etService = view.findViewById(R.id.etService);
        etRoom = view.findViewById(R.id.etRoom);
        btnSend = view.findViewById(R.id.btnSend);
        btnConfirm = view.findViewById(R.id.btnConfirm);
        btnCancel = view.findViewById(R.id.btnCancel);
        tvStatus = view.findViewById(R.id.tvStatus);
        tvLocation = view.findViewById(R.id.tvLocation);
        progress = view.findViewById(R.id.progress);

        btnConfirm.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);

        btnSend.setOnClickListener(v -> ensureLocationThenCreate());
        btnConfirm.setOnClickListener(v -> confirm());
        btnCancel.setOnClickListener(v -> cancel());

        // ekran açılınca izin + konum
        ensureLocationOnly();

        poller = new Poller(2500, this::fetchActive);
    }

    @Override public void onStart() {
        super.onStart();
        if (poller != null) poller.start();
    }

    @Override public void onStop() {
        super.onStop();
        if (poller != null) poller.stop();
    }

    private void ensureLocationOnly() {
        boolean fineGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarseGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (fineGranted || coarseGranted) fetchLocationOnce(null);
        else requestLocationPerm.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
    }

    private void ensureLocationThenCreate() {
        boolean fineGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarseGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (fineGranted || coarseGranted) fetchLocationOnce(this::createHelp);
        else requestLocationPerm.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
    }

    interface Ready { void run(); }

    private void fetchLocationOnce(Ready onReady) {
        progress.setVisibility(View.VISIBLE);
        getCurrentLocation((a,b) -> {
            progress.setVisibility(View.GONE);
            if (a == null || b == null) {
                tvStatus.setText("Konum alınamadı. GPS açık mı?");
                return;
            }
            lat = a; lng = b;
            tvLocation.setText("Konum: " + lat + ", " + lng);
            if (onReady != null) onReady.run();
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

    private void createHelp() {
        String servis = etService.getText().toString().trim();
        String oda = etRoom.getText().toString().trim();

        if (servis.isEmpty() || oda.isEmpty()) {
            tvStatus.setText("Servis ve oda zorunlu");
            return;
        }
        if (lat == null || lng == null) {
            tvStatus.setText("Konum alınmadan gönderilemez.");
            return;
        }

        progress.setVisibility(View.VISIBLE);
        repo.createHelp(patientId, servis, oda, lat, lng).enqueue(new Callback<ApiOk<Object>>() {
            @Override public void onResponse(Call<ApiOk<Object>> call, Response<ApiOk<Object>> resp) {
                progress.setVisibility(View.GONE);
                ApiOk<Object> res = resp.body();
                tvStatus.setText(resp.isSuccessful() && res != null && res.getOk()
                        ? "Durum: OPEN (yardımcı bekleniyor)"
                        : (res != null && res.error != null ? res.error : "İstek gönderilemedi"));
            }
            @Override public void onFailure(Call<ApiOk<Object>> call, Throwable t) {
                progress.setVisibility(View.GONE);
                tvStatus.setText("Hata: " + t.getMessage());
            }
        });
    }

    private void fetchActive() {
        repo.myActive(patientId).enqueue(new Callback<ApiOk<HelpRequestActive>>() {
            @Override public void onResponse(Call<ApiOk<HelpRequestActive>> call, Response<ApiOk<HelpRequestActive>> resp) {
                ApiOk<HelpRequestActive> res = resp.body();
                if (resp.isSuccessful() && res != null && res.getOk() && res.active != null) {
                    HelpRequestActive a = res.active;
                    currentRequestId = a.id;

                    boolean showCancel = "OPEN".equals(a.status) || "ACCEPTED".equals(a.status);
                    btnCancel.setVisibility(showCancel ? View.VISIBLE : View.GONE);

                    btnConfirm.setVisibility("ACCEPTED".equals(a.status) ? View.VISIBLE : View.GONE);

                    int rem = a.remaining_seconds != null ? a.remaining_seconds : 0;
                    if ("ACCEPTED".equals(a.status)) {
                        tvStatus.setText("Durum: ACCEPTED (Kalan: " + TimeUtils.formatRemainingSeconds(rem) + ")");
                    } else {
                        tvStatus.setText("Durum: " + a.status);
                    }
                } else {
                    tvStatus.setText("Durum: Aktif istek yok");
                    btnConfirm.setVisibility(View.GONE);
                    btnCancel.setVisibility(View.GONE);
                    currentRequestId = null;
                }
            }

            @Override public void onFailure(Call<ApiOk<HelpRequestActive>> call, Throwable t) {
                tvStatus.setText("Hata: " + t.getMessage());
            }
        });
    }

    private void confirm() {
        if (currentRequestId == null) return;
        progress.setVisibility(View.VISIBLE);
        repo.confirm(currentRequestId, patientId).enqueue(new Callback<ApiOk<Object>>() {
            @Override public void onResponse(Call<ApiOk<Object>> call, Response<ApiOk<Object>> resp) {
                progress.setVisibility(View.GONE);
                ApiOk<Object> res = resp.body();
                if (resp.isSuccessful() && res != null && res.getOk()) {
                    tvStatus.setText("Durum: CONFIRMED (tamamlandı)");
                    btnConfirm.setVisibility(View.GONE);
                    btnCancel.setVisibility(View.GONE);
                    currentRequestId = null;
                } else {
                    tvStatus.setText(res != null && res.error != null ? res.error : "Onaylanamadı");
                }
            }
            @Override public void onFailure(Call<ApiOk<Object>> call, Throwable t) {
                progress.setVisibility(View.GONE);
                tvStatus.setText("Hata: " + t.getMessage());
            }
        });
    }

    private void cancel() {
        if (currentRequestId == null) return;
        progress.setVisibility(View.VISIBLE);
        repo.cancel(currentRequestId, patientId).enqueue(new Callback<ApiOk<Object>>() {
            @Override public void onResponse(Call<ApiOk<Object>> call, Response<ApiOk<Object>> resp) {
                progress.setVisibility(View.GONE);
                ApiOk<Object> res = resp.body();
                if (resp.isSuccessful() && res != null && res.getOk()) {
                    tvStatus.setText("İstek iptal edildi");
                    btnConfirm.setVisibility(View.GONE);
                    btnCancel.setVisibility(View.GONE);
                    currentRequestId = null;
                } else {
                    tvStatus.setText(res != null && res.error != null ? res.error : "İptal edilemedi");
                }
            }
            @Override public void onFailure(Call<ApiOk<Object>> call, Throwable t) {
                progress.setVisibility(View.GONE);
                tvStatus.setText("Hata: " + t.getMessage());
            }
        });
    }
}
