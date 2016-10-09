package com.kopec.wojciech.occlient;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static GoogleMap mMap;
    private ArrayList<String> globalWaypointList = new ArrayList<>();
    private HashMap<String, CacheInfo> globalCacheMap = new HashMap<>();
    private FragmentMapCacheInfo globalFragmentMapCacheInfo = new FragmentMapCacheInfo();
    private Marker lastSelectedMarker;
    private String lastSelectedMarkerType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
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

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (lastSelectedMarker != null) {
                    setPreviousMarkerDisable();
                    lastSelectedMarker = null;
                }
                android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.remove(globalFragmentMapCacheInfo).commit();
            }
        });

        mMap.setOnMarkerClickListener(
                new GoogleMap.OnMarkerClickListener() {
                    boolean doNotMoveCameraToCenterMarker = true;
                    public boolean onMarkerClick(Marker marker) {
                        cacheRequest(marker.getSnippet(), marker);
                        return doNotMoveCameraToCenterMarker;
                    }
                });
    }

    //MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private MenuItem menuItem;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_download_caches:
                menuItem = item;

                LatLng mapCenterLatLng = mMap.getCameraPosition().target;
                String mapCenterString = String.valueOf(mapCenterLatLng.latitude) + "|" + String.valueOf(mapCenterLatLng.longitude);
                String user_uuid = "";
                String limit = "100";
                waypointsRequest(mapCenterString, limit, user_uuid);

                return true;
//            case R.id.action_location_found:
//                // location found
//                LocationFound();
//                return true;
//            case R.id.action_refresh:
//                // refresh
//                return true;
//            case R.id.action_help:
//                // help action
//                return true;
//            case R.id.action_check_updates:
//                // check for updates action
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Requests

    public void waypointsRequest(final String center, final String limit, final String notFoundBy){

        final ArrayList<String> newWaypoints = new ArrayList<>();
        new AsyncTask<Void, Void, Void>() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                menuItem.setActionView(R.layout.progressbar);
                menuItem.expandActionView();
            }
            @Override
            protected Void doInBackground(Void... params) {
                ArrayList<String> waypointList = new ArrayList<>();
                try {
                    String urlString = "http://opencaching.pl/okapi/services/caches/search/nearest?consumer_key=mcuwKK4dZSphKHzD5K4C&center=" + center + "&limit=" + limit + "&not_found_by=" + notFoundBy;
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

                } catch (UnknownHostException uhe){
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run(){
                            Toast.makeText(MapsActivity.this, "Błąd połączenia z opencaching.pl", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    Log.d("ERROR", e.toString());
                }
                return null;
            }
            @Override
            protected void onPostExecute(Void result) {
                if (!newWaypoints.isEmpty()) {
                    cachesRequest(newWaypoints);
                } else {
                    menuItem.collapseActionView();
                    menuItem.setActionView(null);
                }
            }
        }.execute();
    }

    public void cachesRequest(final ArrayList<String> waypointList) {

        final HashMap<String, CacheInfo> cacheMap = new HashMap<>();
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
                    String urlString = "http://opencaching.pl/okapi/services/caches/geocaches?consumer_key=mcuwKK4dZSphKHzD5K4C&cache_codes=" + codes + "";
                    JSONObject response = jsonObjectRequest(new URL(urlString));

                    for (int i = 0; i < waypointList.size(); i++) {
                        JSONObject obj = response.getJSONObject(waypointList.get(i));
                        cacheMap.put(waypointList.get(i), new CacheInfo(obj.getString("code"), obj.getString("name"), obj.getString("location"), obj.getString("type"), obj.getString("status")));
                    }
                    globalCacheMap.putAll(cacheMap);

                } catch (UnknownHostException uhe){
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run(){
                            Toast.makeText(MapsActivity.this, "Błąd połączenia z opencaching.pl", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    Log.d("ERROR", e.toString());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                showWaypoints(cacheMap, waypointList);
            }
        }.execute();
    }

    public void showWaypoints(HashMap<String, CacheInfo> cacheMap, ArrayList<String> waypointList) {

        for (int i = 0; i < waypointList.size(); i++) {
            String[] parts = cacheMap.get(waypointList.get(i)).location.split("\\|");
            Double latitude = Double.parseDouble(parts[0]);
            Double longitude = Double.parseDouble(parts[1]);

            switch (cacheMap.get(waypointList.get(i)).type) {
                case "Traditional":
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(latitude, longitude))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.cache_traditional))
                            .snippet(cacheMap.get(waypointList.get(i)).code)
                    );
                    break;
                case "Other":
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(latitude, longitude))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.cache_unknown))
                            .snippet(cacheMap.get(waypointList.get(i)).code)
                    );
                    break;
                case "Quiz":
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(latitude, longitude))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.cache_quiz))
                            .snippet(cacheMap.get(waypointList.get(i)).code)
                    );
                    break;
                case "Multi":
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(latitude, longitude))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.cache_multi))
                            .snippet(cacheMap.get(waypointList.get(i)).code)
                    );
                    break;
                case "Virtual":
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(latitude, longitude))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.cache_virtual))
                            .snippet(cacheMap.get(waypointList.get(i)).code)
                    );
                    break;
                case "Own":
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(latitude, longitude))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.cache_own))
                            .snippet(cacheMap.get(waypointList.get(i)).code)
                    );
                    break;
                case "Moving":
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(latitude, longitude))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.cache_moving))
                            .snippet(cacheMap.get(waypointList.get(i)).code)
                    );
                    break;
                case "Event":
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(latitude, longitude))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.cache_event))
                            .snippet(cacheMap.get(waypointList.get(i)).code)
                    );
                    break;
                case "Webcam":
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(latitude, longitude))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.cache_webcam))
                            .snippet(cacheMap.get(waypointList.get(i)).code)
                    );
                    break;
            }
        }
        menuItem.collapseActionView();
        menuItem.setActionView(null);
    }

    public void cacheRequest(final String code, final Marker marker) {

        new AsyncTask<Void, Void, Void>() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    String urlString = "http://opencaching.pl/okapi/services/caches/geocache?consumer_key=mcuwKK4dZSphKHzD5K4C&fields=name|type|size2|rating|owner|recommendations&cache_code=" + code;
                    JSONObject response = jsonObjectRequest(new URL(urlString));

                    JSONObject ownerObj = response.getJSONObject("owner");
                    globalCacheMap.get(code).rating = response.getString("rating");
                    globalCacheMap.get(code).size = response.getString("size2");
                    globalCacheMap.get(code).recommendations = response.getString("recommendations");
                    globalCacheMap.get(code).owner = ownerObj.getString("username");


                } catch (UnknownHostException uhe){
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run(){
                            Toast.makeText(MapsActivity.this, "Błąd połączenia z opencaching.pl", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    Log.d("ERROR", e.toString());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {

                Bundle bundle = new Bundle();
                bundle.putString("waypoint", code);
                bundle.putString("name", globalCacheMap.get(code).name);
                bundle.putString("type", globalCacheMap.get(code).type);
                bundle.putString("size", globalCacheMap.get(code).size);
                bundle.putString("rating", globalCacheMap.get(code).rating);
                bundle.putString("owner", globalCacheMap.get(code).owner);
                bundle.putString("recommendations", globalCacheMap.get(code).recommendations);
                bundle.putString("location", globalCacheMap.get(code).location);

                globalFragmentMapCacheInfo = FragmentMapCacheInfo.newInstance(bundle);
                android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.mapa, globalFragmentMapCacheInfo).commit();

                setMarkerSelected(marker, globalCacheMap.get(code).type);

                if (lastSelectedMarker != null) setPreviousMarkerDisable();
                lastSelectedMarker = marker;
                lastSelectedMarkerType = globalCacheMap.get(code).type;

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), mMap.getCameraPosition().zoom));
            }
        }.execute();
    }

    public void setMarkerSelected(Marker marker, String type){
        switch (type) {
            case "Traditional":
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.cache_traditional_selected));
                break;
            case "Other":
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.cache_unknown_selected));
                break;
            case "Quiz":
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.cache_quiz_selected));
                break;
            case "Multi":
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.cache_multi_selected));
                break;
            case "Virtual":
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.cache_virtual_selected));
                break;
            case "Own":
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.cache_own_selected));
                break;
            case "Moving":
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.cache_moving_selected));
                break;
            case "Event":
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.cache_event_selected));
                break;
            case "Webcam":
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.cache_webcam_selected));
                break;
        }
    }

    public void setPreviousMarkerDisable() {
        switch (lastSelectedMarkerType) {
            case "Traditional":
                lastSelectedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.cache_traditional));
                break;
            case "Other":
                lastSelectedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.cache_unknown));
                break;
            case "Quiz":
                lastSelectedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.cache_quiz));
                break;
            case "Multi":
                lastSelectedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.cache_multi));
                break;
            case "Virtual":
                lastSelectedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.cache_virtual));
                break;
            case "Own":
                lastSelectedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.cache_own));
                break;
            case "Moving":
                lastSelectedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.cache_moving));
                break;
            case "Event":
                lastSelectedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.cache_event));
                break;
            case "Webcam":
                lastSelectedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.cache_webcam));
                break;
        }
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
        JSONObject jsonObj = new JSONObject(String.valueOf(buffer));
        return jsonObj;
    }
}




