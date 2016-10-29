package com.kopec.wojciech.occlient;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Created by Wojtek on 2016-10-29.
 */

public class SaveCacheActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static GoogleMap mMap;
    private JSONObject globalJsonObject = new JSONObject();
    private ArrayList<String> globalWaypointList = new ArrayList<>();
    SharedPreferences sharedPreferences;

    String mapCenterString;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_cache);
        sharedPreferences = getSharedPreferences("searchFilters", Context.MODE_PRIVATE);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        LatLng faisUJ = new LatLng(50.029591, 19.905875);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(faisUJ, 14));
        //mMap.getUiSettings().setZoomControlsEnabled(true);
        //mMap.setMapType(googleMap.MAP_TYPE_SATELLITE);

        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        } else {
            mMap.setMyLocationEnabled(true);
        }
    }

    public void onShowMapClick(View view) {
        View v = findViewById(R.id.set_map_center);
        Button b1 = (Button) findViewById(R.id.show_map_button);
        Button b2 = (Button) findViewById(R.id.ok_map_button);
        Button b3 = (Button) findViewById(R.id.cancel_map_button);
        b1.setVisibility(View.GONE);
        b2.setVisibility(View.VISIBLE);
        b3.setVisibility(View.VISIBLE);
        v.setVisibility(View.VISIBLE);

    }

    public void onOKMapClick(View view) {

        LatLng mapCenterLatLng = mMap.getCameraPosition().target;
        mapCenterString = String.valueOf(mapCenterLatLng.latitude) + "|" + String.valueOf(mapCenterLatLng.longitude);

        View v = findViewById(R.id.set_map_center);
        Button b1 = (Button) findViewById(R.id.show_map_button);
        Button b2 = (Button) findViewById(R.id.ok_map_button);
        Button b3 = (Button) findViewById(R.id.cancel_map_button);
        b1.setVisibility(View.VISIBLE);
        b2.setVisibility(View.GONE);
        b3.setVisibility(View.GONE);
        v.setVisibility(View.GONE);
    }

    public void onCancelMapClick(View view) {
        View v = findViewById(R.id.set_map_center);
        Button b1 = (Button) findViewById(R.id.show_map_button);
        Button b2 = (Button) findViewById(R.id.ok_map_button);
        Button b3 = (Button) findViewById(R.id.cancel_map_button);
        b1.setVisibility(View.VISIBLE);
        b2.setVisibility(View.GONE);
        b3.setVisibility(View.GONE);
        v.setVisibility(View.GONE);
    }



    public void waypointsRequest(final String center, final String limit) {

        final ArrayList<String> newWaypoints = new ArrayList<>();
        new AsyncTask<Void, Void, Void>() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... params) {
                ArrayList<String> waypointList = new ArrayList<>();
                Log.d("TEST", "1");
                try {
                    String urlString = "http://opencaching.pl/okapi/services/caches/search/nearest?consumer_key=mcuwKK4dZSphKHzD5K4C&center=" + center + limit;
                    Log.d("TEST", "2");
                    if (sharedPreferences.getBoolean("notFound", true))
                        urlString += "&not_found_by=03767B69-4960-065E-0A2A-984EDE6BBC83";
                    if (sharedPreferences.getBoolean("found", false))
                        urlString += "&found_by=03767B69-4960-065E-0A2A-984EDE6BBC83";
                    Log.d("TEST", "3");
                    JSONObject jsonObj = jsonObjectRequest(new URL(urlString));

                    JSONArray list = jsonObj.getJSONArray("results");
                    for (int i = 0; i < list.length(); i++) {
                        waypointList.add(list.getString(i));
                    }
                    if (!globalWaypointList.isEmpty()) {
                        for (int i = 0; i < waypointList.size(); i++) {
                            if (!globalWaypointList.contains(waypointList.get(i))) {
                                newWaypoints.add(waypointList.get(i));
                            }
                        }
                        globalWaypointList.addAll(newWaypoints);
                    } else {
                        newWaypoints.addAll(waypointList);
                        globalWaypointList.addAll(newWaypoints);
                    }
                } catch (UnknownHostException uhe) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SaveCacheActivity.this, "Błąd połączenia z opencaching.pl", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    Log.d("ERROR1", e.toString());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (!newWaypoints.isEmpty()) {
                    cachesRequest(newWaypoints);
                }
            }
        }.execute();
    }

    public void cachesRequest(final ArrayList<String> waypointList) {

        new AsyncTask<Void, Void, Void>() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    String codes = waypointList.get(0);
                    if (waypointList.size() > 1) {
                        for (int i = 1; i < waypointList.size(); i++) {
                            codes += "|" + waypointList.get(i);
                        }
                    }

                    String urlString = "http://opencaching.pl/okapi/services/caches/geocaches?consumer_key=mcuwKK4dZSphKHzD5K4C&fields=name|type|size2|rating|owner|recommendations|location|status|code&cache_codes=" + codes + "";
                    JSONObject response = jsonObjectRequest(new URL(urlString));

                    for (int i = 0; i < waypointList.size(); i++) {
                        globalJsonObject.put(waypointList.get(i), response.getJSONObject(waypointList.get(i)));
                    }

                } catch (UnknownHostException uhe) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SaveCacheActivity.this, "Błąd połączenia z opencaching.pl", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    Log.d("ERROR2", e.toString());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                //showWaypoints(waypointList);
                String jsonObj = globalJsonObject.toString();
                SharedPreferences.Editor mEditor = sharedPreferences.edit();
                mEditor.putString("jsonCaches", jsonObj);
                mEditor.apply();
                Log.d("GLOBAL JSON", globalJsonObject.toString());
            }
        }.execute();
    }

    private JSONObject jsonObjectRequest(URL url) throws JSONException, IOException {
        BufferedReader reader;
        reader = new BufferedReader(new InputStreamReader(url.openStream()));
        StringBuffer buffer = new StringBuffer();
        int read;
        char[] chars = new char[1024];
        while ((read = reader.read(chars)) != -1) {
            buffer.append(chars, 0, read);
        }
        return new JSONObject(String.valueOf(buffer));
    }

    public void onDownloadCachesClick(View view) {

        String limit = "&limit=100";
        if(mapCenterString != null){
            waypointsRequest(mapCenterString, limit);
        }

    }
}
