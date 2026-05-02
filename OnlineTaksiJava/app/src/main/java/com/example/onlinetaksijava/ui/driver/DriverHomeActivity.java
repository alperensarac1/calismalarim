package com.example.onlinetaksijava.ui.driver;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.onlinetaksijava.data.local.SessionManager;
import com.example.onlinetaksijava.data.remote.api.ApiClient;
import com.example.onlinetaksijava.data.remote.model.AvailableRideItem;
import com.example.onlinetaksijava.data.remote.model.AvailableRideListResponse;
import com.example.onlinetaksijava.data.remote.model.DriverProfileResponse;
import com.example.onlinetaksijava.data.remote.model.RideListResponse;
import com.example.onlinetaksijava.data.remote.model.RideResponse;
import com.example.onlinetaksijava.data.remote.socket.SocketManager;
import com.example.onlinetaksijava.data.repository.DriverRepository;
import com.example.onlinetaksijava.databinding.ActivityDriverHomeBinding;
import com.example.onlinetaksijava.location.DriverLocationTracker;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverHomeActivity extends AppCompatActivity implements SocketManager.SocketEventListener {

    private ActivityDriverHomeBinding binding;
    private SessionManager sessionManager;
    private DriverRepository driverRepository;
    private DriverLocationTracker locationTracker;
    private AvailableRideAdapter rideAdapter;

    private RideResponse activeRide;
    private final List<AvailableRideItem> availableRideItems = new ArrayList<>();

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    startLocation();
                } else {
                    Toast.makeText(this, "İzin gerekli", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDriverHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        driverRepository = new DriverRepository(ApiClient.create(this));
        locationTracker = new DriverLocationTracker(this);

        setupRecycler();

        binding.btnConnectSocket.setOnClickListener(v -> connectSocket());
        binding.btnOnline.setOnClickListener(v -> setDriverOnline(true));
        binding.btnOffline.setOnClickListener(v -> setDriverOnline(false));
        binding.btnLoadAvailableRides.setOnClickListener(v -> loadAvailableRides());
        binding.btnLoadActiveRide.setOnClickListener(v -> loadActiveRide());
        binding.btnStartLocation.setOnClickListener(v -> checkPermissionAndStart());
        binding.btnStopLocation.setOnClickListener(v -> {
            locationTracker.stop();
            setLog("Konum güncellemesi durduruldu");
        });

        binding.btnArriving.setOnClickListener(v -> updateActiveRideStatus("DRIVER_ARRIVING", "Şoför müşteriye doğru yola çıktı."));
        binding.btnArrived.setOnClickListener(v -> updateActiveRideStatus("DRIVER_ARRIVED", "Şoför alım noktasına ulaştı."));
        binding.btnStartRide.setOnClickListener(v -> updateActiveRideStatus("RIDE_STARTED", "Müşteri araca bindi."));
        binding.btnCompleteRide.setOnClickListener(v -> updateActiveRideStatus("RIDE_COMPLETED", "Yolculuk tamamlandı."));
    }

    private void setupRecycler() {
        rideAdapter = new AvailableRideAdapter(item -> acceptRide(item.getId()));
        binding.rvAvailableRides.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAvailableRides.setAdapter(rideAdapter);
    }

    private void connectSocket() {
        String token = sessionManager.getToken();
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Token yok", Toast.LENGTH_SHORT).show();
            return;
        }

        SocketManager.setListener(this);
        SocketManager.connect(token);
    }

    private void setDriverOnline(boolean isOnline) {
        driverRepository.setOnline(isOnline).enqueue(new Callback<DriverProfileResponse>() {
            @Override
            public void onResponse(Call<DriverProfileResponse> call, Response<DriverProfileResponse> response) {
                if (response.isSuccessful()) {
                    setLog(isOnline ? "Online oldun" : "Offline oldun");
                    Toast.makeText(DriverHomeActivity.this,
                            isOnline ? "Online oldun" : "Offline oldun",
                            Toast.LENGTH_SHORT).show();
                } else {
                    setLog("Online/offline güncellenemedi");
                }
            }

            @Override
            public void onFailure(Call<DriverProfileResponse> call, Throwable t) {
                setLog("Online/offline hata: " + t.getMessage());
            }
        });
    }

    private void loadAvailableRides() {
        driverRepository.getAvailableRides().enqueue(new Callback<AvailableRideListResponse>() {
            @Override
            public void onResponse(Call<AvailableRideListResponse> call, Response<AvailableRideListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    availableRideItems.clear();
                    if (response.body().getRides() != null) {
                        availableRideItems.addAll(response.body().getRides());
                    }
                    rideAdapter.submitList(new ArrayList<>(availableRideItems));
                    setLog("Açık ride listesi güncellendi");
                } else {
                    setLog("Ride listesi alınamadı");
                }
            }

            @Override
            public void onFailure(Call<AvailableRideListResponse> call, Throwable t) {
                setLog("Ride listesi hata: " + t.getMessage());
            }
        });
    }

    private void loadActiveRide() {
        driverRepository.getMyActiveRides().enqueue(new Callback<RideListResponse>() {
            @Override
            public void onResponse(Call<RideListResponse> call, Response<RideListResponse> response) {
                if (response.isSuccessful() && response.body() != null &&
                        response.body().getRides() != null &&
                        !response.body().getRides().isEmpty()) {

                    activeRide = response.body().getRides().get(0);
                    renderActiveRide();
                    setLog("Aktif ride yüklendi");
                } else {
                    activeRide = null;
                    renderActiveRide();
                    setLog("Aktif ride yok");
                }
            }

            @Override
            public void onFailure(Call<RideListResponse> call, Throwable t) {
                setLog("Aktif ride hata: " + t.getMessage());
            }
        });
    }

    private void acceptRide(int rideId) {
        driverRepository.acceptRide(rideId).enqueue(new Callback<RideResponse>() {
            @Override
            public void onResponse(Call<RideResponse> call, Response<RideResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    activeRide = response.body();
                    renderActiveRide();

                    List<AvailableRideItem> filtered = new ArrayList<>();
                    for (AvailableRideItem item : availableRideItems) {
                        if (item.getId() != rideId) {
                            filtered.add(item);
                        }
                    }
                    availableRideItems.clear();
                    availableRideItems.addAll(filtered);
                    rideAdapter.submitList(new ArrayList<>(availableRideItems));

                    Toast.makeText(DriverHomeActivity.this, "Ride kabul edildi", Toast.LENGTH_SHORT).show();
                    setLog("Ride kabul edildi. id=" + rideId);
                } else {
                    setLog("Ride kabul edilemedi");
                }
            }

            @Override
            public void onFailure(Call<RideResponse> call, Throwable t) {
                setLog("Ride kabul hata: " + t.getMessage());
            }
        });
    }

    private void updateActiveRideStatus(String status, String note) {
        if (activeRide == null) {
            Toast.makeText(this, "Aktif ride yok", Toast.LENGTH_SHORT).show();
            return;
        }

        driverRepository.updateRideStatus(activeRide.getId(), status, note).enqueue(new Callback<RideResponse>() {
            @Override
            public void onResponse(Call<RideResponse> call, Response<RideResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    activeRide = response.body();
                    renderActiveRide();
                    setLog("Status güncellendi: " + status);

                    if ("RIDE_COMPLETED".equals(status)) {
                        Toast.makeText(DriverHomeActivity.this, "Yolculuk tamamlandı", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    setLog("Status güncellenemedi");
                }
            }

            @Override
            public void onFailure(Call<RideResponse> call, Throwable t) {
                setLog("Status hata: " + t.getMessage());
            }
        });
    }

    private void renderActiveRide() {
        if (activeRide == null) {
            binding.tvActiveRideInfo.setText("Aktif ride yok");
            return;
        }

        binding.tvActiveRideInfo.setText(
                "Ride ID: " + activeRide.getId() + "\n" +
                        "Pickup: " + activeRide.getPickup_address() + "\n" +
                        "Dropoff: " + activeRide.getDropoff_address() + "\n" +
                        "Durum: " + activeRide.getStatus()
        );
    }

    private void checkPermissionAndStart() {
        String permission = Manifest.permission.ACCESS_FINE_LOCATION;

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            startLocation();
        } else {
            permissionLauncher.launch(permission);
        }
    }

    private void startLocation() {
        locationTracker.start((lat, lng) -> runOnUiThread(() -> {
            binding.tvLocation.setText("Lat: " + lat + "  Lng: " + lng);

            driverRepository.updateLocation(lat, lng).enqueue(new Callback<DriverProfileResponse>() {
                @Override
                public void onResponse(Call<DriverProfileResponse> call, Response<DriverProfileResponse> response) {
                    setLog("Konum gönderildi");
                }

                @Override
                public void onFailure(Call<DriverProfileResponse> call, Throwable t) {
                    setLog("Konum gönderme hata: " + t.getMessage());
                }
            });
        }));
    }

    @Override
    public void onConnected() {
        runOnUiThread(() -> {
            setLog("Socket bağlandı");
            Toast.makeText(this, "Socket bağlandı", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDisconnected() {
        runOnUiThread(() -> setLog("Socket bağlantısı kapandı"));
    }

    @Override
    public void onMessage(String message) {
        runOnUiThread(() -> {
            setLog(message);

            try {
                JSONObject json = new JSONObject(message);
                String event = json.optString("event");

                if ("NEW_RIDE_REQUEST".equals(event)) {
                    JSONObject data = json.optJSONObject("data");
                    if (data != null) {
                        AvailableRideItem incoming = jsonToRideItem(data);

                        boolean exists = false;
                        for (AvailableRideItem item : availableRideItems) {
                            if (item.getId() == incoming.getId()) {
                                exists = true;
                                break;
                            }
                        }

                        if (!exists) {
                            availableRideItems.add(0, incoming);
                            rideAdapter.submitList(new ArrayList<>(availableRideItems));
                        }

                        Toast.makeText(this, "Yeni ride geldi!", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {
                setLog("Socket parse hatası: " + e.getMessage());
            }
        });
    }

    @Override
    public void onError(String errorMessage) {
        runOnUiThread(() -> setLog("Socket hata: " + errorMessage));
    }

    private AvailableRideItem jsonToRideItem(JSONObject data) {
        try {
            JSONObject wrapper = new JSONObject();
            wrapper.put("id", data.optInt("ride_id"));
            wrapper.put("customer_id", data.optInt("customer_id"));
            wrapper.put("pickup_lat", data.optDouble("pickup_lat"));
            wrapper.put("pickup_lng", data.optDouble("pickup_lng"));
            wrapper.put("pickup_address", data.optString("pickup_address"));
            wrapper.put("dropoff_lat", data.optDouble("dropoff_lat"));
            wrapper.put("dropoff_lng", data.optDouble("dropoff_lng"));
            wrapper.put("dropoff_address", data.optString("dropoff_address"));
            wrapper.put("status", data.optString("status"));
            wrapper.put("estimated_fare", data.optDouble("estimated_fare"));

            com.google.gson.Gson gson = new com.google.gson.Gson();
            return gson.fromJson(wrapper.toString(), AvailableRideItem.class);
        } catch (Exception e) {
            return new com.google.gson.Gson().fromJson("{}", AvailableRideItem.class);
        }
    }

    private void setLog(String text) {
        binding.tvLog.setText(text);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SocketManager.setListener(null);
        locationTracker.stop();
    }
}
