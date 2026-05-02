package com.example.onlinetaksijava.ui.home;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.onlinetaksijava.data.local.SessionManager;
import com.example.onlinetaksijava.data.remote.api.ApiClient;
import com.example.onlinetaksijava.data.remote.model.CreateRideRequest;
import com.example.onlinetaksijava.data.remote.model.DriverLocationEvent;
import com.example.onlinetaksijava.data.remote.model.RideResponse;
import com.example.onlinetaksijava.data.remote.socket.SocketEventParser;
import com.example.onlinetaksijava.data.remote.socket.SocketManager;
import com.example.onlinetaksijava.data.repository.RideRepository;
import com.example.onlinetaksijava.databinding.ActivityCustomerHomeBinding;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerHomeActivity extends AppCompatActivity
        implements SocketManager.SocketEventListener, OnMapReadyCallback {

    private ActivityCustomerHomeBinding binding;
    private SessionManager sessionManager;
    private RideRepository rideRepository;

    private GoogleMap googleMap;

    private Marker customerMarker;
    private Marker pickupMarker;
    private Marker dropoffMarker;
    private Marker driverMarker;

    private LatLng customerLatLng;
    private LatLng pickupLatLng;
    private LatLng dropoffLatLng;
    private LatLng driverLatLng;

    private Integer activeRideId = null;

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    fetchCustomerLocation();
                } else {
                    appendLog("Müşteri konum izni verilmedi");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomerHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        rideRepository = new RideRepository(ApiClient.create(this));

        binding.tvWelcome.setText("Hoş geldin, " +
                (sessionManager.getFullName() != null ? sessionManager.getFullName() : "Müşteri"));

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(binding.mapContainer.getId());

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        binding.btnConnectSocket.setOnClickListener(v -> {
            String token = sessionManager.getToken();
            if (token == null || token.trim().isEmpty()) {
                Toast.makeText(this, "Token bulunamadı", Toast.LENGTH_SHORT).show();
                return;
            }

            SocketManager.setListener(this);
            SocketManager.connect(token);
        });

        binding.btnPing.setOnClickListener(v -> SocketManager.sendPing());

        binding.btnCreateRide.setOnClickListener(v -> createRideFromForm());

        binding.btnLogout.setOnClickListener(v -> {
            SocketManager.disconnect();
            sessionManager.clearSession();
            finish();
        });

        checkLocationPermissionAndFetch();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        LatLng defaultLocation = new LatLng(41.0082, 28.9784);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 11f));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SocketManager.setListener(null);
    }

    private void createRideFromForm() {
        Double pickupLat = parseDouble(binding.etPickupLat.getText().toString().trim());
        Double pickupLng = parseDouble(binding.etPickupLng.getText().toString().trim());
        String pickupAddress = binding.etPickupAddress.getText().toString().trim();

        Double dropoffLat = parseDouble(binding.etDropoffLat.getText().toString().trim());
        Double dropoffLng = parseDouble(binding.etDropoffLng.getText().toString().trim());
        String dropoffAddress = binding.etDropoffAddress.getText().toString().trim();

        if (pickupLat == null || pickupLng == null || pickupAddress.isEmpty()
                || dropoffLat == null || dropoffLng == null || dropoffAddress.isEmpty()) {
            Toast.makeText(this, "Tüm pickup/dropoff alanlarını doldur", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnCreateRide.setEnabled(false);
        binding.btnCreateRide.setText("Çağırılıyor...");

        CreateRideRequest request = new CreateRideRequest(
                pickupLat,
                pickupLng,
                pickupAddress,
                dropoffLat,
                dropoffLng,
                dropoffAddress
        );

        rideRepository.createRide(request).enqueue(new Callback<RideResponse>() {
            @Override
            public void onResponse(Call<RideResponse> call, Response<RideResponse> response) {
                binding.btnCreateRide.setEnabled(true);
                binding.btnCreateRide.setText("Taksi Çağır");

                if (response.isSuccessful() && response.body() != null) {
                    RideResponse ride = response.body();
                    activeRideId = ride.getId();

                    pickupLatLng = new LatLng(ride.getPickup_lat(), ride.getPickup_lng());
                    dropoffLatLng = new LatLng(ride.getDropoff_lat(), ride.getDropoff_lng());

                    updatePickupMarker(pickupLatLng);
                    updateDropoffMarker(dropoffLatLng);
                    updateCameraForAllPoints();

                    binding.tvRideStatus.setText("Ride Durumu: " + ride.getStatus());

                    appendLog("Ride oluşturuldu. id=" + ride.getId());
                    Toast.makeText(CustomerHomeActivity.this, "Taksi çağrısı oluşturuldu", Toast.LENGTH_SHORT).show();
                } else {
                    appendLog("Ride oluşturma hatası");
                    Toast.makeText(CustomerHomeActivity.this, "Ride oluşturulamadı", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<RideResponse> call, Throwable t) {
                binding.btnCreateRide.setEnabled(true);
                binding.btnCreateRide.setText("Taksi Çağır");
                appendLog("Ride oluşturma hatası: " + t.getMessage());
                Toast.makeText(CustomerHomeActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void checkLocationPermissionAndFetch() {
        String permission = Manifest.permission.ACCESS_FINE_LOCATION;

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            fetchCustomerLocation();
        } else {
            locationPermissionLauncher.launch(permission);
        }
    }

    private void fetchCustomerLocation() {
        try {
            LocationServices.getFusedLocationProviderClient(this)
                    .getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            customerLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            updateCustomerMarker(customerLatLng);
                            updateCameraForAllPoints();

                            binding.etPickupLat.setText(String.valueOf(location.getLatitude()));
                            binding.etPickupLng.setText(String.valueOf(location.getLongitude()));

                            appendLog("Müşteri konumu alındı: " +
                                    location.getLatitude() + ", " + location.getLongitude());
                        } else {
                            appendLog("Müşteri son konumu alınamadı");
                        }
                    })
                    .addOnFailureListener(e ->
                            appendLog("Müşteri konumu alınırken hata oluştu: " + e.getMessage()));
        } catch (SecurityException e) {
            appendLog("Konum izni yok: " + e.getMessage());
        }
    }

    @Override
    public void onConnected() {
        runOnUiThread(() -> {
            binding.tvSocketState.setText("Socket Durumu: Bağlı");
            appendLog("Socket bağlandı");
        });
    }

    @Override
    public void onDisconnected() {
        runOnUiThread(() -> {
            binding.tvSocketState.setText("Socket Durumu: Bağlı değil");
            appendLog("Socket bağlantısı kapandı");
        });
    }

    @Override
    public void onMessage(String message) {
        runOnUiThread(() -> {
            binding.tvLastSocketEvent.setText("Son Event: " + message);
            appendLog("Mesaj: " + message);

            String eventName = SocketEventParser.getEventName(message);

            if ("AUTH_SUCCESS".equals(eventName)) {
                appendLog("Kimlik doğrulama başarılı");
            } else if ("RIDE_ACCEPTED".equals(eventName)
                    || "RIDE_STATUS_CHANGED".equals(eventName)
                    || "RIDE_CANCELLED".equals(eventName)) {
                String status = SocketEventParser.parseRideStatus(message);
                if (status != null) {
                    binding.tvRideStatus.setText("Ride Durumu: " + status);
                }
            } else if ("DRIVER_LOCATION".equals(eventName)) {
                DriverLocationEvent event = SocketEventParser.parseDriverLocation(message);
                if (event != null) {
                    activeRideId = event.getRideId();

                    binding.tvDriverLat.setText("Taksi Enlem: " + event.getLat());
                    binding.tvDriverLng.setText("Taksi Boylam: " + event.getLng());
                    binding.tvLastLocationUpdate.setText("Son Konum Güncelleme: " + getCurrentTimeText());

                    updateDriverMarker(event.getLat(), event.getLng());
                }
            } else if ("PONG".equals(eventName)) {
                appendLog("Sunucudan pong alındı");
            }
        });
    }

    @Override
    public void onError(String errorMessage) {
        runOnUiThread(() -> {
            appendLog("Socket hata: " + errorMessage);
            binding.tvLastSocketEvent.setText("Son Event: Socket hata: " + errorMessage);
        });
    }

    private void updateCustomerMarker(LatLng latLng) {
        if (googleMap == null) return;

        if (customerMarker == null) {
            customerMarker = googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Benim Konumum")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        } else {
            customerMarker.setPosition(latLng);
        }
    }

    private void updatePickupMarker(LatLng latLng) {
        if (googleMap == null) return;

        if (pickupMarker == null) {
            pickupMarker = googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Alınış Noktası")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        } else {
            pickupMarker.setPosition(latLng);
        }
    }

    private void updateDropoffMarker(LatLng latLng) {
        if (googleMap == null) return;

        if (dropoffMarker == null) {
            dropoffMarker = googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Varış Noktası")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        } else {
            dropoffMarker.setPosition(latLng);
        }
    }

    private void updateDriverMarker(double lat, double lng) {
        if (googleMap == null) return;

        driverLatLng = new LatLng(lat, lng);

        if (driverMarker == null) {
            driverMarker = googleMap.addMarker(new MarkerOptions()
                    .position(driverLatLng)
                    .title("Taksiniz")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
        } else {
            driverMarker.setPosition(driverLatLng);
        }

        updateCameraForAllPoints();
    }

    private void updateCameraForAllPoints() {
        if (googleMap == null) return;

        ArrayList<LatLng> points = new ArrayList<>();
        if (customerLatLng != null) points.add(customerLatLng);
        if (pickupLatLng != null) points.add(pickupLatLng);
        if (dropoffLatLng != null) points.add(dropoffLatLng);
        if (driverLatLng != null) points.add(driverLatLng);

        if (points.isEmpty()) return;

        if (points.size() == 1) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(points.get(0), 16f));
            return;
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : points) {
            builder.include(point);
        }

        googleMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(builder.build(), 160)
        );
    }

    private void appendLog(String text) {
        String current = binding.tvSocketLog.getText().toString();
        binding.tvSocketLog.setText(current + "\n" + text);
    }

    private String getCurrentTimeText() {
        return new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    private Double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return null;
        }
    }
}
