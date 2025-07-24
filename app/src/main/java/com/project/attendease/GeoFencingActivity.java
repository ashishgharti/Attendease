package com.project.attendease;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import com.project.attendease.apiservice.ApiService;
import com.project.attendease.apiservice.RetrofitClient;
import com.project.attendease.request.CheckInRequest;
import com.project.attendease.request.CheckOutRequest;
import com.project.attendease.response.CheckInResponse;
import com.project.attendease.response.CheckOutResponse;
import com.project.attendease.response.GeofenceResponse;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GeoFencingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final double GEOFENCE_RADIUS = 100.0; // 100 meters
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private MapView mapView;
    private GoogleMap googleMap;
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    private TextView tvStatus, tvTime, tvDate, tvLocation;
    private LatLng currentLocation;
    private LatLng companyLocation;
    private ApiService apiService;
    private FusedLocationProviderClient fusedLocationClient;
    private boolean isCheckOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_fencing);

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        tvStatus = findViewById(R.id.tvStatus);
        tvTime = findViewById(R.id.time);
        tvDate = findViewById(R.id.date);
        tvLocation = findViewById(R.id.tvLocation);

        RetrofitClient.initialize(this);
        apiService = RetrofitClient.getApiService();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapView = findViewById(R.id.mapView);
        Bundle mapViewBundle = savedInstanceState != null ? savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY) : null;
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        isCheckOut = getIntent().getBooleanExtra("isCheckOut", false);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getDeviceLocation(); // Fetch dynamic device location
        }

        updateTimeAndDate();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            enableMyLocation();
        }
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (googleMap != null) {
                googleMap.setMyLocationEnabled(true);
            }
        } else {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
                getDeviceLocation();  // Proceed with fetching location if permission is granted
            } else {
                tvStatus.setText("Cannot check-in due to denied location access");
                tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            }
        }
    }

    private void getDeviceLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                Log.d("GeoFencingActivity", "Latitude: " + currentLocation.latitude);
                Log.d("GeoFencingActivity", "Longitude: " + currentLocation.longitude);

                updateLocationText(currentLocation);
                fetchCompanyLocationAndCompare(); // Fetch company location and then compare
            } else {
                requestNewLocationData();
            }
        });
    }


    private void requestNewLocationData() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setNumUpdates(1);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location location = locationResult.getLastLocation();
            if (location != null) {
                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                updateLocationText(currentLocation);
                fetchCompanyLocationAndCompare(); // Fetch company location and then compare
            }
        }
    };

    private void fetchCompanyLocationAndCompare() {
        // Fetch company location from the server or local storage
        apiService.getGeofence().enqueue(new Callback<List<GeofenceResponse>>() {
            @Override
            public void onResponse(Call<List<GeofenceResponse>> call, Response<List<GeofenceResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    GeofenceResponse geofenceResponse = response.body().get(0);
                    companyLocation = new LatLng(geofenceResponse.getLocation().getCoordinates().get(1), geofenceResponse.getLocation().getCoordinates().get(0));

                    // Draw geofence circle on the map
                    googleMap.addCircle(new CircleOptions()
                            .center(companyLocation)
                            .radius(GEOFENCE_RADIUS)
                            .strokeColor(ContextCompat.getColor(GeoFencingActivity.this, android.R.color.holo_green_light))
                            .fillColor(ColorUtils.setAlphaComponent(ContextCompat.getColor(GeoFencingActivity.this, android.R.color.holo_green_light), 50))
                            .strokeWidth(2));

                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(companyLocation, 15));

                    performActionBasedOnCompanyLocation();
                } else {
                    tvStatus.setText("Failed to fetch company location.");
                    tvStatus.setTextColor(ContextCompat.getColor(GeoFencingActivity.this, android.R.color.holo_red_dark));
                }
            }

            @Override
            public void onFailure(Call<List<GeofenceResponse>> call, Throwable t) {
                tvStatus.setText("Error fetching company location.");
                tvStatus.setTextColor(ContextCompat.getColor(GeoFencingActivity.this, android.R.color.holo_red_dark));
            }
        });
    }

    private void performActionBasedOnCompanyLocation() {
        if (currentLocation != null && companyLocation != null && isWithinGeofence(currentLocation, companyLocation, GEOFENCE_RADIUS)) {
            if (isCheckOut) {
                performCheckOut(currentLocation);
            } else {
                performCheckIn(currentLocation);
            }
        } else {
            tvStatus.setText("Not within company location range.");
            tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            Toast.makeText(this, "You are not within the geofence range for check-in or check-out", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTimeAndDate() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM dd", Locale.getDefault());

        String currentTime = timeFormat.format(new Date());
        String currentDate = dateFormat.format(new Date());

        tvTime.setText(currentTime);
        tvDate.setText(currentDate);
    }

    private void updateLocationText(LatLng currentLocation) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(currentLocation.latitude, currentLocation.longitude, 1);

            if (CollectionUtils.isNotEmpty(addresses)) {
                Address address = addresses.get(0);
                String addressLine = address.getAddressLine(0);
                tvLocation.setText(addressLine);
            } else {
                tvLocation.setText("Unknown Location");
            }
        } catch (IOException e) {
            e.printStackTrace();
            tvLocation.setText("Error getting location");
        }
    }

    private void performCheckIn(LatLng currentLocation) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        CheckInRequest checkInRequest = new CheckInRequest(currentLocation.latitude, currentLocation.longitude, token);

        apiService.checkIn("Bearer" + token, checkInRequest).enqueue(new Callback<CheckInResponse>() {
            @Override
            public void onResponse(Call<CheckInResponse> call, Response<CheckInResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleCheckInResponse(response.body());
                } else {
                    tvStatus.setText("Check-in failed. Please try again.");
                    tvStatus.setTextColor(ContextCompat.getColor(GeoFencingActivity.this, android.R.color.holo_red_dark));
                }
            }

            @Override
            public void onFailure(Call<CheckInResponse> call, Throwable t) {
                tvStatus.setText("Check-in failed");
                tvStatus.setTextColor(ContextCompat.getColor(GeoFencingActivity.this, android.R.color.holo_red_dark));
            }
        });
    }

    private void performCheckOut(LatLng currentLocation) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        CheckOutRequest checkOutRequest = new CheckOutRequest(currentLocation.latitude, currentLocation.longitude, token);

        apiService.checkOut("Bearer" + token, checkOutRequest).enqueue(new Callback<CheckOutResponse>() {
            @Override
            public void onResponse(Call<CheckOutResponse> call, Response<CheckOutResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleCheckOutResponse(response.body());
                } else {
                    tvStatus.setText("Check-out failed. Please try again.");
                    tvStatus.setTextColor(ContextCompat.getColor(GeoFencingActivity.this, android.R.color.holo_red_dark));
                }
            }

            @Override
            public void onFailure(Call<CheckOutResponse> call, Throwable t) {
                tvStatus.setText("Check-out failed");
                tvStatus.setTextColor(ContextCompat.getColor(GeoFencingActivity.this, android.R.color.holo_red_dark));
            }
        });
    }

    private boolean isWithinGeofence(LatLng currentLocation, LatLng companyLocation, double radius) {
        float[] distance = new float[2];
        android.location.Location.distanceBetween(currentLocation.latitude, currentLocation.longitude,
                companyLocation.latitude, companyLocation.longitude, distance);
        return distance[0] <= radius;
    }

    private void handleCheckInResponse(CheckInResponse checkInResponse) {
        String message = checkInResponse.getMessage();
        if (StringUtils.isNotBlank(message)) {
            if ("Checked in successfully".equals(message)) {
                tvStatus.setText("You are at office range");
                tvStatus.setTextColor(ContextCompat.getColor(GeoFencingActivity.this, android.R.color.black));

                String checkInTime = checkInResponse.getAttendance().getCheckInTime();
                String checkInDate = checkInResponse.getAttendance().getCheckInDate();

                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isCheckedIn", true);
                editor.putString("checkInTime", checkInTime);
                editor.putString("checkInDate", checkInDate);
                editor.apply();

                // Delay before finishing the activity to allow viewing the map
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    Toast.makeText(GeoFencingActivity.this, "Check-in successful!", Toast.LENGTH_LONG).show();
                    finish();
                }, 9000); // 9-second delay
            } else {
                tvStatus.setText("Operation failed");
                tvStatus.setTextColor(ContextCompat.getColor(GeoFencingActivity.this, android.R.color.holo_red_dark));
            }
        } else {
            tvStatus.setText("Check-in failed. No message received.");
            tvStatus.setTextColor(ContextCompat.getColor(GeoFencingActivity.this, android.R.color.holo_red_dark));
        }
    }
    private void handleCheckOutResponse(CheckOutResponse checkOutResponse) {
        String message = checkOutResponse.getMessage();
        if (message != null) {
            if (message.contains("Checked out successfully")) {
                tvStatus.setText("You have checked out");
                tvStatus.setTextColor(ContextCompat.getColor(GeoFencingActivity.this, android.R.color.black));

                String checkOutTime = checkOutResponse.getAttendance().getCheckOutTime();
                String checkOutDate = checkOutResponse.getAttendance().getCheckOutDate();

                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isCheckedIn", false);
                editor.putString("checkOutTime", checkOutTime);
                editor.putString("checkOutDate", checkOutDate);
                editor.apply();

                // Delay before finishing the activity to allow viewing the map
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    Toast.makeText(GeoFencingActivity.this, "Check-out successful!", Toast.LENGTH_LONG).show();
                    finish();
                }, 9000); // 9-second delay
            } else {
                tvStatus.setText("Operation failed");
                tvStatus.setTextColor(ContextCompat.getColor(GeoFencingActivity.this, android.R.color.holo_red_dark));
            }
        } else {
            tvStatus.setText("Check-out failed. No message received.");
            tvStatus.setTextColor(ContextCompat.getColor(GeoFencingActivity.this, android.R.color.holo_red_dark));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }
        mapView.onSaveInstanceState(mapViewBundle);
    }
}
