package com.example.locationtracker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvLatitude, tvLongitude;
    private Button btnSaveLocation, btnViewMap;
    private LocationManager locationManager;
    private double currentLatitude = 0;
    private double currentLongitude = 0;
    private static final int LOCATION_PERMISSION_CODE = 100;
    private final String SERVER_URL = "http://192.168.8.102/tracker/save_location.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvLatitude = findViewById(R.id.tvLatitude);
        tvLongitude = findViewById(R.id.tvLongitude);
        btnSaveLocation = findViewById(R.id.btnSaveLocation);
        btnViewMap = findViewById(R.id.btnViewMap);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        checkLocationPermission();

        btnSaveLocation.setOnClickListener(v -> saveLocationToServer());
        btnViewMap.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            startActivity(intent);
        });
    }

    private void saveLocationToServer() {
        if (currentLatitude == 0 && currentLongitude == 0) {
            Toast.makeText(this, "Position non disponible - Attendez le GPS", Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(this, "Envoi en cours...", Toast.LENGTH_SHORT).show();

        // Lancer la requête dans un thread séparé (obligatoire pour réseau)
        new Thread(() -> {
            try {
                URL url = new URL(SERVER_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                 String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRANCE).format(new Date());

                String params = "latitude=" + currentLatitude +
                        "&longitude=" + currentLongitude +
                        "&date=" + date +
                        "&imei=" + androidId;

                // Envoyer les données
                OutputStream os = conn.getOutputStream();
                os.write(params.getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();

                 runOnUiThread(() -> {
                    if (responseCode == 200) {
                        Toast.makeText(MainActivity.this, "✓ Position enregistrée!\nLat: " + currentLatitude + "\nLng: " + currentLongitude, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Erreur HTTP: " + responseCode, Toast.LENGTH_SHORT).show();
                    }
                });

                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_CODE);
        } else {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    5000,
                    10,
                    locationListener
            );
        }
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();
            tvLatitude.setText(String.format(Locale.FRANCE, "Latitude: %.6f", currentLatitude));
            tvLongitude.setText(String.format(Locale.FRANCE, "Longitude: %.6f", currentLongitude));
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
            Toast.makeText(MainActivity.this, "GPS désactivé - Activez le GPS", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
            Toast.makeText(MainActivity.this, "GPS activé", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            Toast.makeText(this, "Permission GPS nécessaire", Toast.LENGTH_LONG).show();
        }
    }
}