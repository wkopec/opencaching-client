package com.kopec.wojciech.occlient;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
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
import java.util.Iterator;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static GoogleMap mMap;
    private ArrayList<String> globalWaypointList = new ArrayList<>();
    private FragmentMapCacheInfo globalFragmentMapCacheInfo = new FragmentMapCacheInfo();
    private JSONObject globalJsonObject = new JSONObject();
    private Marker lastSelectedMarker;
    private String lastSelectedMarkerType;

    SharedPreferences sharedPreferences;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        LatLng faisUJ = new LatLng(50.029591, 19.905875);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(faisUJ, 14));
        //mMap.getUiSettings().setZoomControlsEnabled(true);
        //mMap.setMapType(googleMap.MAP_TYPE_SATELLITE);

        JSONObject loadedJson;
        try {
            if(sharedPreferences.getString("jsonCaches", null) != null){
                loadedJson = new JSONObject(sharedPreferences.getString("jsonCaches", null));
                Iterator<String> iter = loadedJson.keys();
                while (iter.hasNext()) {
                    String key = iter.next();
                    if(!globalWaypointList.contains(key)){
                        globalWaypointList.add(key);
                        globalJsonObject.put(key, loadedJson.get(key));}
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        showWaypoints(globalWaypointList);

        Log.d("JSON GLOBAL", globalJsonObject.toString());


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
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
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

    boolean[] selectedFilters;
    private MenuItem menuItem;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        menuItem = item;
        switch (item.getItemId()) {
            case R.id.action_download_caches:
                LatLng mapCenterLatLng = mMap.getCameraPosition().target;
                String mapCenterString = String.valueOf(mapCenterLatLng.latitude) + "|" + String.valueOf(mapCenterLatLng.longitude);
                String limit = "&limit=100";
                waypointsRequest(mapCenterString, limit);
                return true;

            case R.id.action_filter_caches:
                selectedFilters = new boolean[6];
                selectedFilters[0] = sharedPreferences.getBoolean("notFound", true);
                selectedFilters[1] = sharedPreferences.getBoolean("found", false);
                selectedFilters[2] = sharedPreferences.getBoolean("ignored", true);
                selectedFilters[3] = sharedPreferences.getBoolean("own", true);
                selectedFilters[4] = sharedPreferences.getBoolean("temporarilyUnavailable", true);
                selectedFilters[5] = sharedPreferences.getBoolean("archived", true);

                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                builder.setTitle("Ukryj skrzynki")
                        .setMultiChoiceItems(R.array.filters, null,
                                new DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                        if (isChecked) {
                                            // If the user checked the item, add it to the selected items
                                            selectedFilters[which] = true;
                                        } else if (selectedFilters[which]) {
                                            // Else, if the item is already in the array, remove it
                                            selectedFilters[which] = false;
                                        }
                                        Log.d("Tablica", String.valueOf(selectedFilters[which]));
                                    }
                                })

                        // Set the action buttons
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                                mMap.clear();
                                globalWaypointList.clear();
                                lastSelectedMarker = null;
                                lastSelectedMarkerType = null;
                                FragmentManager fragmentManager = getSupportFragmentManager();
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                fragmentTransaction.remove(globalFragmentMapCacheInfo).commit();

                                LatLng mapCenterLatLng = mMap.getCameraPosition().target;
                                String mapCenterString = String.valueOf(mapCenterLatLng.latitude) + "|" + String.valueOf(mapCenterLatLng.longitude);
                                String limit = "&limit=100";

                                for (int i = 0; i < selectedFilters.length; i++) {
                                    SharedPreferences.Editor mEditor = sharedPreferences.edit();
                                    switch (i) {
                                        case 0:
                                            if (selectedFilters[i])
                                                mEditor.putBoolean("notFound", true).apply();
                                            else mEditor.putBoolean("notFound", false).apply();
                                            break;

                                        case 1:
                                            if (selectedFilters[i])
                                                mEditor.putBoolean("found", true).apply();
                                            else mEditor.putBoolean("found", false).apply();
                                            break;

                                        case 2:
                                            if (selectedFilters[i])
                                                mEditor.putBoolean("ignored", true).apply();
                                            else mEditor.putBoolean("ignored", false).apply();
                                            break;

                                        case 3:
                                            if (selectedFilters[i])
                                                mEditor.putBoolean("own", true).apply();
                                            else mEditor.putBoolean("own", false).apply();
                                            break;

                                        case 4:
                                            if (selectedFilters[i])
                                                mEditor.putBoolean("temporarilyUnavailable", true).apply();
                                            else
                                                mEditor.putBoolean("temporarilyUnavailable", false).apply();
                                            break;

                                        case 5:
                                            if (selectedFilters[i])
                                                mEditor.putBoolean("archived", true).apply();
                                            else mEditor.putBoolean("archived", false).apply();
                                            break;
                                    }
                                }
                                waypointsRequest(mapCenterString, limit);
                            }
                        })
                        .setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                //...
                            }
                        });

                AlertDialog multichoiceDialog = builder.create();
                multichoiceDialog.setOnShowListener(new DialogInterface.OnShowListener() {

                    @Override
                    public void onShow(DialogInterface dialog) {
                        ListView list = ((AlertDialog) dialog).getListView();
                        list.setItemChecked(0, sharedPreferences.getBoolean("notFound", true));
                        list.setItemChecked(1, sharedPreferences.getBoolean("found", false));
                        list.setItemChecked(2, sharedPreferences.getBoolean("ignored", true));
                        list.setItemChecked(3, sharedPreferences.getBoolean("own", true));
                        list.setItemChecked(4, sharedPreferences.getBoolean("temporarilyUnavailable", true));
                        list.setItemChecked(5, sharedPreferences.getBoolean("archived", true));
                    }
                });
                multichoiceDialog.show();
                return true;

            case R.id.action_save_caches:

                Intent intent = new Intent(this, SaveCacheActivity.class);
                startActivity(intent);

//                String jsonObj = globalJsonObject.toString();
//                SharedPreferences.Editor mEditor = sharedPreferences.edit();
//                mEditor.putString("jsonCaches", jsonObj);
//                mEditor.apply();

                return true;

            case R.id.action_load_caches:

                JSONObject loadedJson = null;
                try {
                    if(sharedPreferences.getString("jsonCaches", null) != null){
                        loadedJson = new JSONObject(sharedPreferences.getString("jsonCaches", null));
                        Iterator<String> iter = loadedJson.keys();
                        while (iter.hasNext()) {
                            String key = iter.next();
                            if(!globalWaypointList.contains(key)){
                                globalWaypointList.add(key);
                                    globalJsonObject.put(key, loadedJson.get(key));}
                            }
                        }
                    } catch (JSONException e) {
                    e.printStackTrace();
                }


                Log.d("LOADED", globalWaypointList.toString());
                showWaypoints(globalWaypointList);

                return true;
//            case R.id.action_check_updates:
//                // check for updates action
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Requests
    public void waypointsRequest(final String center, final String limit) {

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
                try {
                    String urlString = "http://opencaching.pl/okapi/services/caches/search/nearest?consumer_key=mcuwKK4dZSphKHzD5K4C&center=" + center + limit;

                    if (sharedPreferences.getBoolean("notFound", true))
                        urlString += "&not_found_by=03767B69-4960-065E-0A2A-984EDE6BBC83";
                    if (sharedPreferences.getBoolean("found", false))
                        urlString += "&found_by=03767B69-4960-065E-0A2A-984EDE6BBC83";

                    JSONObject jsonObj = jsonObjectRequest(new URL(urlString));

                    JSONArray list = jsonObj.getJSONArray("results");

                    for (int i = 0; i < list.length(); i++) {
                        if (!globalWaypointList.contains(list.getString(i))) {
                            newWaypoints.add(list.getString(i));
                        }
                    }
                    globalWaypointList.addAll(newWaypoints);

                } catch (UnknownHostException uhe) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
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

                    String urlString = "http://opencaching.pl/okapi/services/caches/geocaches?consumer_key=mcuwKK4dZSphKHzD5K4C&fields=name|type|size2|rating|owner|recommendations|location|status|code&cache_codes=" + codes;
                    JSONObject response = jsonObjectRequest(new URL(urlString));

                    for (int i = 0; i < waypointList.size(); i++) {
                        globalJsonObject.put(waypointList.get(i), response.getJSONObject(waypointList.get(i)));
                    }

                } catch (UnknownHostException uhe) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
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
                showWaypoints(waypointList);
            }
        }.execute();
    }

    public void showWaypoints(ArrayList<String> waypointList) {

        for (int i = 0; i < waypointList.size(); i++) {
            try {
                JSONObject tempjson = globalJsonObject.getJSONObject(waypointList.get(i));
                String[] parts;
                parts = tempjson.getString("location").split("\\|");
                Double latitude = Double.parseDouble(parts[0]);
                Double longitude = Double.parseDouble(parts[1]);

                switch (tempjson.getString("type")) {
                    case "Traditional":
                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(latitude, longitude))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.cache_traditional))
                                .snippet(tempjson.getString("code"))
                        );
                        break;
                    case "Other":
                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(latitude, longitude))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.cache_unknown))
                                .snippet(tempjson.getString("code"))
                        );
                        break;
                    case "Quiz":
                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(latitude, longitude))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.cache_quiz))
                                .snippet(tempjson.getString("code"))
                        );
                        break;
                    case "Multi":
                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(latitude, longitude))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.cache_multi))
                                .snippet(tempjson.getString("code"))
                        );
                        break;
                    case "Virtual":
                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(latitude, longitude))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.cache_virtual))
                                .snippet(tempjson.getString("code"))
                        );
                        break;
                    case "Own":
                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(latitude, longitude))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.cache_own))
                                .snippet(tempjson.getString("code"))
                        );
                        break;
                    case "Moving":
                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(latitude, longitude))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.cache_moving))
                                .snippet(tempjson.getString("code"))
                        );
                        break;
                    case "Event":
                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(latitude, longitude))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.cache_event))
                                .snippet(tempjson.getString("code"))
                        );
                        break;
                    case "Webcam":
                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(latitude, longitude))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.cache_webcam))
                                .snippet(tempjson.getString("code"))
                        );
                        break;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(menuItem != null){
            menuItem.collapseActionView();
            menuItem.setActionView(null);
        }
    }

    public void cacheRequest(final String code, final Marker marker) {


        Bundle bundle = new Bundle();
        try {
            bundle.putString("waypoint", code);
            bundle.putString("name", globalJsonObject.getJSONObject(code).getString("name"));
            bundle.putString("type", globalJsonObject.getJSONObject(code).getString("type"));
            bundle.putString("size", globalJsonObject.getJSONObject(code).getString("size2"));
            bundle.putString("rating", globalJsonObject.getJSONObject(code).getString("rating"));
            bundle.putString("owner", globalJsonObject.getJSONObject(code).getJSONObject("owner").getString("username"));
            bundle.putString("recommendations", globalJsonObject.getJSONObject(code).getString("recommendations"));
            bundle.putString("location", globalJsonObject.getJSONObject(code).getString("location"));

            setMarkerSelected(marker, globalJsonObject.getJSONObject(code).getString("type"));
            if (lastSelectedMarker != null) setPreviousMarkerDisable();
            lastSelectedMarker = marker;
            lastSelectedMarkerType = globalJsonObject.getJSONObject(code).getString("type");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        globalFragmentMapCacheInfo = FragmentMapCacheInfo.newInstance(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.mapa, globalFragmentMapCacheInfo).commit();

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), mMap.getCameraPosition().zoom));

    }

    public void setMarkerSelected(Marker marker, String type) {
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
        return new JSONObject(String.valueOf(buffer));
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Maps Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}


