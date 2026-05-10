package com.example.locationtracker;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class MapActivity extends AppCompatActivity {

    private MapView mapView;
    private RequestQueue requestQueue;
    private final String SERVER_URL = "http://192.168.8.102/tracker/get_locations.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

         Configuration.getInstance().load(getApplicationContext(),
                getSharedPreferences("osmdroid", MODE_PRIVATE));

         mapView = findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

         mapView.getController().setZoom(12.0);
        mapView.getController().setCenter(new GeoPoint(46.603354, 1.888334));

         requestQueue = Volley.newRequestQueue(this);

         loadLocations();
    }

    private void loadLocations() {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, SERVER_URL, null,
                response -> {
                    try {
                        JSONArray locations = response.getJSONArray("locations");

                        for (int i = 0; i < locations.length(); i++) {
                            JSONObject location = locations.getJSONObject(i);
                            double lat = location.getDouble("latitude");
                            double lng = location.getDouble("longitude");

                            // Ajout du marqueur
                            Marker marker = new Marker(mapView);
                            marker.setPosition(new GeoPoint(lat, lng));
                            marker.setTitle("Point " + (i + 1));
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

                            mapView.getOverlays().add(marker);
                        }

                        mapView.invalidate();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Erreur de chargement", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Erreur réseau", Toast.LENGTH_SHORT).show();
                });

        requestQueue.add(request);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
}