package com.example.onlinetaksijava.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Looper;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class DriverLocationTracker {

    public interface OnLocationChangedListener {
        void onLocationChanged(double lat, double lng);
    }

    private final com.google.android.gms.location.FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    public DriverLocationTracker(Context context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    @SuppressLint("MissingPermission")
    public void start(OnLocationChangedListener listener) {
        LocationRequest request = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                5000L
        ).setMinUpdateIntervalMillis(3000L).build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                if (result.getLastLocation() != null) {
                    listener.onLocationChanged(
                            result.getLastLocation().getLatitude(),
                            result.getLastLocation().getLongitude()
                    );
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
        );
    }

    public void stop() {
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            locationCallback = null;
        }
    }
}

