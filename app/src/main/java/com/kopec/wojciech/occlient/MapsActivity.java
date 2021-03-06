package com.kopec.wojciech.occlient;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static GoogleMap mMap;
    private GoogleApiClient client;
    private ArrayList<String> globalWaypointList = new ArrayList<>();
    private FragmentMapCacheInfo globalFragmentMapCacheInfo = new FragmentMapCacheInfo();
    private JSONObject globalJsonObject = new JSONObject();
    private Marker lastSelectedMarker;
    private String lastSelectedMarkerType;

    SharedPreferences sharedPreferences;
    SharedPreferences jsonPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        jsonPreferences = getSharedPreferences("jsonCacheObjects", Context.MODE_PRIVATE);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        float mapLat = sharedPreferences.getFloat("startMapLat", (float)51.92);
        float mapLang = sharedPreferences.getFloat("startMapLang", (float)19.15);
        float mapZoom = sharedPreferences.getFloat("startMapZoom", (float)5.6);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mapLat, mapLang), mapZoom));

        Set<String> waypointsSet = sharedPreferences.getStringSet("savedWaypoints", new HashSet<String>());
        showWaypoints(new ArrayList<>(waypointsSet));

        SharedPreferences.Editor mEditor = sharedPreferences.edit();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //User has previously accepted this permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mEditor.putBoolean("isDeviceLocationEnabled", true).apply();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            //Not in api-23, no need to prompt
            mEditor.putBoolean("isDeviceLocationEnabled", true).apply();
            mMap.setMyLocationEnabled(true);
        }
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                //  TODO: Prompt with explanation!

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    Toast.makeText(this, "Opcje lokalizacji nie będą dostępne", Toast.LENGTH_LONG).show();
                }
            }

        }
    }

    MenuItem menuItem;
    MenuItem loginItem;
    MenuItem logoutItem;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        loginItem = menu.findItem(R.id.action_login);
        logoutItem = menu.findItem(R.id.action_logout);

        if(sharedPreferences.getString("username", "").equals("")){
            loginItem.setVisible(true);
            logoutItem.setVisible(false);
        }
        else{
            loginItem.setVisible(false);
            logoutItem.setVisible(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        menuItem = item;
        switch (item.getItemId()) {
            case R.id.action_download_caches:
                LatLng mapCenterLatLng = mMap.getCameraPosition().target;
                String mapCenterString = String.valueOf(mapCenterLatLng.latitude) + "|" + String.valueOf(mapCenterLatLng.longitude);
                String limit = "&limit=100";
                waypointsRequest(mapCenterString, limit, false);
                return true;

            case R.id.action_filter_caches:
                if(sharedPreferences.getString("user_uuid", "").equals("")){
                    Toast.makeText(MapsActivity.this, getString(R.string.set_username_in_settings), Toast.LENGTH_LONG).show();
                }
                else{
                    showFilterDialog();
                }

                return true;

            case R.id.action_save_caches:

                Intent intent = new Intent(this, SaveCacheActivity.class);
                startActivityForResult(intent, 1);

                return true;

            case R.id.action_delete_caches:

                deleteSavedCaches(false);

                return true;

            case R.id.action_settings:

                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivityForResult(settingsIntent, 3);

                return true;

            case R.id.action_logout:

                SharedPreferences.Editor Editor = sharedPreferences.edit();
                Editor.remove("username");
                Editor.remove("logged_user_uuid");
                Editor.remove("oauth_token");
                Editor.remove("oauth_token_secret");
                if(sharedPreferences.getString("view_map_as_username", "").equals(sharedPreferences.getString("username", ""))){
                    Editor.remove("user_uuid");
                    Editor.remove("startMapLat");
                    Editor.remove("startMapLang");
                    Editor.remove("startMapZoom");
                }
                Editor.apply();

                loginItem.setVisible(true);
                logoutItem.setVisible(false);

                return true;

            case R.id.action_login:

                Intent authorizationIntent = new Intent(this, AuthorizationActivity.class);
                startActivityForResult(authorizationIntent, 2);

                return true;

            case R.id.action_clear_map:
                clearMap();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void waypointsRequest(final String center, final String limit, final boolean isSavingOffline) {

        final ArrayList<String> newWaypoints = new ArrayList<>();
        new AsyncTask<Void, Void, Void>() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if(!isSavingOffline){
                    menuItem.setActionView(R.layout.progressbar);
                    menuItem.expandActionView();
                }
            }

            @Override
            protected Void doInBackground(Void... params) {

                if(isOnline() && !isConnectedToServer("http://opencaching.pl/", 10000)){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MapsActivity.this, getString(R.string.opencaching_serwer_connection_error), Toast.LENGTH_LONG).show();
                        }
                    });
                    cancel(false);
                }

                try {
                    String urlString = "http://opencaching.pl/okapi/services/caches/search/nearest?consumer_key=" + getString(R.string.OKAPIConsumerKey) + "&center=" + center + limit;
                    if(! sharedPreferences.getString("user_uuid", "").equals("")){
                        if (sharedPreferences.getBoolean("notFound", true))
                            urlString += "&not_found_by=" + sharedPreferences.getString("user_uuid", "");
                        if (sharedPreferences.getBoolean("found", false))
                            urlString += "&found_by=" + sharedPreferences.getString("user_uuid", "");
                    }

                    JSONObject jsonObj = jsonObjectRequest(new URL(urlString));
                    JSONArray list = jsonObj.getJSONArray("results");

                    if(isSavingOffline){
                        for (int i = 0; i < list.length(); i++){
                            newWaypoints.add(list.getString(i));
                        }
                    }
                    else{
                        for (int i = 0; i < list.length(); i++) {
                            if (!globalWaypointList.contains(list.getString(i))) {
                                newWaypoints.add(list.getString(i));
                            }
                        }
                    }
                    globalWaypointList.addAll(newWaypoints);

                } catch (UnknownHostException uhe) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MapsActivity.this, getString(R.string.internet_connection_error), Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    Log.d("ERROR", e.toString());
                }
                return null;
            }

            @Override
            protected void onCancelled() {
                menuItem.collapseActionView();
                menuItem.setActionView(null);
            }

            @Override
            protected void onPostExecute(Void result) {
                if (!newWaypoints.isEmpty()) {
                    if(isSavingOffline) cachesRequest(newWaypoints, true);
                    else cachesRequest(newWaypoints, false);
                }
                else{
                    menuItem.collapseActionView();
                    menuItem.setActionView(null);
                }
            }
        }.execute();
    }

    public void cachesRequest(final ArrayList<String> waypointList, final boolean isSavingOffline) {

        new AsyncTask<Void, Integer, Void>() {
            boolean isCanceled = false;
            final ProgressDialog mProgressDialog = new ProgressDialog(MapsActivity.this);
            @Override
            protected void onPreExecute()
            {
                if(isSavingOffline){
                    this.mProgressDialog.setTitle(getString(R.string.downloading));
                    this.mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    this.mProgressDialog.setMax(waypointList.size());
                    this.mProgressDialog.setProgress(0);
                    this.mProgressDialog.setCanceledOnTouchOutside(false);
                    this.mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            isCanceled = true;
                            cancel(false);
                        }
                    });
                    this.mProgressDialog.show();
                }
            }

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

                    String urlString = "http://opencaching.pl/okapi/services/caches/geocaches?consumer_key=" + getString(R.string.OKAPIConsumerKey);
                    if(isSavingOffline){
                        String fields = "name|type|size2|rating|owner|recommendations|location|status|req_passwd|code|description|hint2|images|latest_logs";
                        if(!sharedPreferences.getString("username", "").equals("")){
                            urlString += "&user_uuid=" + sharedPreferences.getString("logged_user_uuid", "");
                            fields += "|is_found";
                        }
                        String logsLimit = "999";
                        urlString += "&fields=" + fields + "&cache_codes=" + codes + "&lpc=" + logsLimit;
                    }
                    else{
                        String fields ="name|type|size2|rating|owner|recommendations|location|status|req_passwd|code";
                        if(!sharedPreferences.getString("username", "").equals("")){
                            urlString += "&user_uuid=" + sharedPreferences.getString("logged_user_uuid", "");
                            fields += "|is_found";
                        }
                        urlString += "&fields=" + fields + "&cache_codes=" + codes;
                    }
                    Log.d("URL", urlString);
                    JSONObject response = jsonObjectRequest(new URL(urlString));

                    if(isSavingOffline){
                        Set<String> waypointsSet = sharedPreferences.getStringSet("savedWaypoints", new HashSet<String>());
                        waypointsSet.addAll(new HashSet<>(waypointList));
                        SharedPreferences.Editor mEditor = sharedPreferences.edit();
                        mEditor.putStringSet("savedWaypoints", waypointsSet);
                        mEditor.apply();
                    }
                    SharedPreferences.Editor jsonEditor = jsonPreferences.edit();

                    if(isSavingOffline){
                        for (int i = 0; i < waypointList.size(); i++) {
                            if(isCanceled)
                                break;
                            jsonEditor.putString(waypointList.get(i), response.getJSONObject(waypointList.get(i)).toString());

                            final JSONArray images = response.getJSONObject(waypointList.get(i)).getJSONArray("images");
                            for(int j=0; j<images.length(); j++){
                                JSONObject img = images.getJSONObject(j);
                                InputStream in = new URL(img.getString("url")).openStream();
                                Bitmap bmp = BitmapFactory.decodeStream(in);
                                savebitmap(bmp, img.getString("unique_caption"), waypointList.get(i));
                            }
                            publishProgress(i);
                        }
                        jsonEditor.apply();
                    }
                    else{
                        for (int i = 0; i < waypointList.size(); i++) {
                            if(isCanceled)
                                break;
                            globalJsonObject.put(waypointList.get(i), response.getJSONObject(waypointList.get(i)));
                        }
                    }
                } catch (UnknownHostException uhe) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MapsActivity.this, getString(R.string.internet_connection_error), Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    Log.d("ERROR", e.toString());
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                mProgressDialog.setProgress(values[0] + 1);
            }

            @Override
            protected void onCancelled() {
                if (this.mProgressDialog != null) {
                    this.mProgressDialog.dismiss();
                }
                deleteSavedCaches(true);
            }

            @Override
            protected void onPostExecute(Void result) {
                if(isSavingOffline){
                    mProgressDialog.dismiss();
                    showWaypoints(waypointList);
                }
                else showWaypoints(waypointList);
            }
        }.execute();
    }

    public void showWaypoints(ArrayList<String> waypointList) {

        for (int i = 0; i < waypointList.size(); i++) {
            try {
                JSONObject tempjson;
                if(jsonPreferences.getString(waypointList.get(i), null) != null){
                    tempjson = new JSONObject(jsonPreferences.getString(waypointList.get(i), null));
                }
                else{
                    tempjson = globalJsonObject.getJSONObject(waypointList.get(i));
                }
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

                mMap.setOnMarkerClickListener(
                new GoogleMap.OnMarkerClickListener() {
                    boolean doNotMoveCameraToCenterMarker = true;
                    public boolean onMarkerClick(Marker marker) {
                        if(lastSelectedMarker != null && lastSelectedMarker.equals(marker)){
                            return doNotMoveCameraToCenterMarker;
                        }
                        else{
                            try {
                                showCacheInfo(marker.getSnippet(), marker);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        return doNotMoveCameraToCenterMarker;
                    }
                });

                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        if (lastSelectedMarker != null) {
                            setPreviousMarkerDisable();
                            lastSelectedMarker = null;
                        }
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                        fragmentTransaction.remove(globalFragmentMapCacheInfo).commit();
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(menuItem != null){
            menuItem.collapseActionView();
            menuItem.setActionView(null);
        }
    }

    public void showCacheInfo(final String code, final Marker marker) throws JSONException {

        JSONObject jsonObject;
        if(jsonPreferences.getString(code, null) != null){
            jsonObject = new JSONObject(jsonPreferences.getString(code, null));
        }
        else{
            jsonObject = globalJsonObject.getJSONObject(code);
        }

        Bundle bundle = new Bundle();
        try {
            bundle.putString("waypoint", code);
            bundle.putString("name", jsonObject.getString("name"));
            bundle.putString("type", jsonObject.getString("type"));
            bundle.putString("size", jsonObject.getString("size2"));
            bundle.putString("rating", jsonObject.getString("rating"));
            bundle.putString("owner", jsonObject.getJSONObject("owner").getString("username"));
            bundle.putString("owner_uuid", jsonObject.getJSONObject("owner").getString("uuid"));
            bundle.putString("recommendations", jsonObject.getString("recommendations"));
            bundle.putString("location", jsonObject.getString("location"));
            bundle.putBoolean("req_passwd", jsonObject.getBoolean("req_passwd"));

            if(!sharedPreferences.getString("username", "").equals("")){
                bundle.putBoolean("is_found", jsonObject.getBoolean("is_found"));
                bundle.putString("downloaded_by_username_uuid", sharedPreferences.getString("logged_user_uuid", ""));

            }

            setMarkerSelected(marker, jsonObject.getString("type"));

            if (lastSelectedMarker != null) setPreviousMarkerDisable();
            lastSelectedMarker = marker;
            lastSelectedMarkerType = jsonObject.getString("type");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        globalFragmentMapCacheInfo = FragmentMapCacheInfo.newInstance(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        fragmentTransaction.replace(R.id.map, globalFragmentMapCacheInfo).commit();
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

    public void savebitmap(Bitmap bmp, String name, String waypoint){
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        File fotoDirectory = new File(Environment.getExternalStorageDirectory() + File.separator + "Opencaching Map" + File.separator + waypoint);
        fotoDirectory.mkdirs();

        try {
            File f = new File(fotoDirectory, name + ".jpg");
            if(f.createNewFile()){
                FileOutputStream fo = new FileOutputStream(f);
                fo.write(bytes.toByteArray());
                fo.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("Saving bitmap Error", e.toString());
            savebitmap(bmp, name, waypoint);
        }
    }

    public void clearMap(){
        mMap.clear();
        globalWaypointList.clear();
        lastSelectedMarker = null;
        lastSelectedMarkerType = null;

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove(globalFragmentMapCacheInfo).commit();
    }

    boolean[] selectedFilters;
    public void showFilterDialog(){
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
                                    selectedFilters[which] = true;
                                } else if (selectedFilters[which]) {
                                    selectedFilters[which] = false;
                                }
                            }
                        })

                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        clearMap();
                        LatLng mapCenterLatLng = mMap.getCameraPosition().target;
                        String mapCenterString = String.valueOf(mapCenterLatLng.latitude) + "|" + String.valueOf(mapCenterLatLng.longitude);
                        String limit = "&limit=200";
                        SharedPreferences.Editor mEditor = sharedPreferences.edit();
                        mEditor.putBoolean("notFound", selectedFilters[0]);
                        mEditor.putBoolean("found", selectedFilters[1]);
                        mEditor.putBoolean("ignored", selectedFilters[2]);
                        mEditor.putBoolean("own", selectedFilters[3]);
                        mEditor.putBoolean("temporarilyUnavailable", selectedFilters[4]);
                        mEditor.putBoolean("archived", selectedFilters[5]);
                        mEditor.apply();
                        waypointsRequest(mapCenterString, limit, false);
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
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

                for (int i = 0; i<selectedFilters.length; i++) {
                    list.setItemChecked(i, selectedFilters[i]);
                }
            }
        });
        multichoiceDialog.show();
    }

    private void deleteSavedCaches(boolean isCanceled) {
        SharedPreferences.Editor mEditor = sharedPreferences.edit();
        mEditor.putStringSet("savedWaypoints", null);
        mEditor.apply();

        SharedPreferences.Editor jsonEditor = jsonPreferences.edit();
        jsonEditor.clear().apply();

        clearMap();

        File fotoDirectory = new File(Environment.getExternalStorageDirectory() + File.separator + "Opencaching Map");
        if(delete(fotoDirectory) && !isCanceled){
            Toast.makeText(MapsActivity.this, getString(R.string.deleted_successfully), Toast.LENGTH_LONG).show();
        }
        else if(isCanceled){
            Toast.makeText(MapsActivity.this, getString(R.string.canceled), Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(MapsActivity.this, getString(R.string.no_caches_to_delete), Toast.LENGTH_LONG).show();
        }

    }

    public static boolean delete(File path) {
        boolean result = true;
        if (path.exists()) {
            if (path.isDirectory()) {
                for (File child : path.listFiles()) {
                    result &= delete(child);
                }
                result &= path.delete();
            } else if (path.isFile()) {
                result = path.delete();
            }
            return result;
        } else {
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                String mapCenter = data.getStringExtra("mapCenter");
                String limit = data.getStringExtra("cacheLimit");
                String[] parts = mapCenter.split("\\|");
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(parts[0]), Double.parseDouble(parts[1])), 10));
                waypointsRequest(mapCenter, limit, true);
            }
        }
        else if(requestCode == 2){
            if(resultCode == Activity.RESULT_OK){
                if(sharedPreferences.getFloat("startMapLat", 0) != 0 && sharedPreferences.getFloat("startMapLang", 0) != 0){
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(sharedPreferences.getFloat("startMapLat", 0), sharedPreferences.getFloat("startMapLang", 0)), 10));
                }
                loginItem.setVisible(false);
                logoutItem.setVisible(true);
                Toast.makeText(MapsActivity.this, getString(R.string.logged_in_successfully), Toast.LENGTH_LONG).show();
            }
            if(resultCode == Activity.RESULT_CANCELED){
                Toast.makeText(this, getString(R.string.internet_connection_error), Toast.LENGTH_LONG).show();
            }
        }
        else if(requestCode == 3){
            Log.d("Settings result", String.valueOf(resultCode));
            if(resultCode == Activity.RESULT_OK){
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //User has previously accepted this permission
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        //sharedPreferences.getBoolean("isDeviceLocationEnabled", false);
                        mMap.setMyLocationEnabled(sharedPreferences.getBoolean("isDeviceLocationEnabled", false));
                    }
                } else {
                    mMap.setMyLocationEnabled(sharedPreferences.getBoolean("isDeviceLocationEnabled", false));
                }
            }
        }
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

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public boolean isConnectedToServer(String url, int timeout) {
        try{
            URL myUrl = new URL(url);
            URLConnection connection = myUrl.openConnection();
            connection.setConnectTimeout(timeout);
            connection.connect();
            return true;
        } catch (Exception e) {
            return false;
        }
    }


}


