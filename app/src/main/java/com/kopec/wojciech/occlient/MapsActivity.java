package com.kopec.wojciech.occlient;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
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
import java.util.ArrayList;
import java.util.HashMap;

//public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, DialogInterface.OnClickListener {
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static GoogleMap mMap;
    private ArrayList<String> globalWaypointList = new ArrayList<>();
    private HashMap<String, CacheInfo> globalCacheMap = new HashMap<>();
    private FragmentMapCacheInfo globalFragmentMapCacheInfo = new FragmentMapCacheInfo();
    private Marker lastSelectedMarker;
    private String lastSelectedMarkerType;
    private float zIndexOfMarker = 1;
    private LocationManager locationMangaer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationMangaer = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        LatLng faisUJ = new LatLng(50.029591, 19.905875);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(faisUJ, 14));
        mMap.getUiSettings().setMapToolbarEnabled(false);
        //mMap.getUiSettings().setZoomControlsEnabled(true);
        //mMap.setMapType(googleMap.MAP_TYPE_SATELLITE);

        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        } else {
            mMap.setMyLocationEnabled(true);
        }

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Bundle bundle = new Bundle();
                String[] parts = marker.getSnippet().split("\\|");
                bundle.putString("waypoint", parts[0]);
                bundle.putString("name", parts[1]);
                Intent i = new Intent(MapsActivity.this, CacheActivity.class);
                i.putExtras(bundle);
                startActivity(i);
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(lastSelectedMarker != null){
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
                        String[] parts = marker.getSnippet().split("\\|");
                        cacheRequest(parts[0], marker);
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
                menuItem.setActionView(R.layout.progressbar);
                menuItem.expandActionView();

                LatLng mapCenterLatLng = mMap.getCameraPosition().target;
                String mapCenterString = String.valueOf(mapCenterLatLng.latitude) + "|" + String.valueOf(mapCenterLatLng.longitude);
                //String user_uuid = "03767B69-4960-065E-0A2A-984EDE6BBC83";      //Volframs uuid
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
    public void waypointsRequest(String center, String limit, String notFoundBy) {
        final ArrayList<String> waypointList = new ArrayList<>();
        String tag_json_obj = "json_obj_req";
        String url = "http://opencaching.pl/okapi/services/caches/search/nearest?consumer_key=mcuwKK4dZSphKHzD5K4C&center=" + center + "&limit=" + limit + "&not_found_by=" + notFoundBy;
        final String TAG = MapsActivity.class.getSimpleName();

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray list = response.getJSONArray("results");
                    for (int i = 0; i < list.length(); i++) {
                        waypointList.add(list.getString(i));
                    }

                    ArrayList<String> newWaypoints = new ArrayList<>();
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

                    if (!newWaypoints.isEmpty()) {
                        cachesRequest(newWaypoints);
                    }
                    else{
                        menuItem.collapseActionView();
                        menuItem.setActionView(null);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                menuItem.collapseActionView();
                menuItem.setActionView(null);
                Toast.makeText(MapsActivity.this,
                        "Błąd połączenia z serwerem", Toast.LENGTH_LONG).show();
            }
        });

        AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
    }

    public void cachesRequest(final ArrayList<String> waypointList) {
        String codes = waypointList.get(0);
        if (waypointList.size() > 1) {
            for (int i = 1; i < waypointList.size(); i++) {
                codes += "|" + waypointList.get(i);
            }
        }

        String tag_json_obj = "json_obj_req";
        String url = "http://opencaching.pl/okapi/services/caches/geocaches?consumer_key=mcuwKK4dZSphKHzD5K4C&cache_codes=" + codes + "";
        final String TAG = MapsActivity.class.getSimpleName();

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
            HashMap<String, CacheInfo> cacheMap = new HashMap<>();

            @Override
            public void onResponse(JSONObject response) {
                //Log.d(TAG, response.toString());
                try {
                    for (int i = 0; i < waypointList.size(); i++) {
                        JSONObject obj = response.getJSONObject(waypointList.get(i));
                        cacheMap.put(waypointList.get(i), new CacheInfo(obj.getString("code"), obj.getString("name"), obj.getString("location"), obj.getString("type"), obj.getString("status")));
                    }
                    globalCacheMap.putAll(cacheMap);
                    showWaypoints(cacheMap, waypointList);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }
        });
        AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
    }

    public void showWaypoints(HashMap<String, CacheInfo> cacheMap, ArrayList<String> waypointList) {

        for (int i = 0; i < waypointList.size(); i++) {
            String[] parts = cacheMap.get(waypointList.get(i)).location.split("\\|");
            Double latitude = Double.parseDouble(parts[0]);
            Double longitude = Double.parseDouble(parts[1]);

            if (cacheMap.get(waypointList.get(i)).type.equals("Traditional")) {
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title(cacheMap.get(waypointList.get(i)).name)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.cache_traditional))
                        .snippet(cacheMap.get(waypointList.get(i)).code + "|" + cacheMap.get(waypointList.get(i)).name)
                );
            } else if (cacheMap.get(waypointList.get(i)).type.equals("Other")) {
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title(cacheMap.get(waypointList.get(i)).name)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.cache_unknown))
                        .snippet(cacheMap.get(waypointList.get(i)).code + "|" + cacheMap.get(waypointList.get(i)).name)
                );
            } else if (cacheMap.get(waypointList.get(i)).type.equals("Quiz")) {
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title(cacheMap.get(waypointList.get(i)).name)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.cache_quiz))
                        .snippet(cacheMap.get(waypointList.get(i)).code + "|" + cacheMap.get(waypointList.get(i)).name)
                );
            } else if (cacheMap.get(waypointList.get(i)).type.equals("Multi")) {
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title(cacheMap.get(waypointList.get(i)).name)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.cache_multi))
                        .snippet(cacheMap.get(waypointList.get(i)).code + "|" + cacheMap.get(waypointList.get(i)).name)
                );
            } else if (cacheMap.get(waypointList.get(i)).type.equals("Virtual")) {
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title(cacheMap.get(waypointList.get(i)).name)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.cache_virtual))
                        .snippet(cacheMap.get(waypointList.get(i)).code + "|" + cacheMap.get(waypointList.get(i)).name)
                );
            } else if (cacheMap.get(waypointList.get(i)).type.equals("Own")) {
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title(cacheMap.get(waypointList.get(i)).name)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.cache_own))
                        .snippet(cacheMap.get(waypointList.get(i)).code + "|" + cacheMap.get(waypointList.get(i)).name)
                );
            } else if (cacheMap.get(waypointList.get(i)).type.equals("Moving")) {
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title(cacheMap.get(waypointList.get(i)).name)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.cache_moving))
                        .snippet(cacheMap.get(waypointList.get(i)).code + "|" + cacheMap.get(waypointList.get(i)).name)
                );
            } else if (cacheMap.get(waypointList.get(i)).type.equals("Event")) {
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title(cacheMap.get(waypointList.get(i)).name)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.cache_event))
                        .snippet(cacheMap.get(waypointList.get(i)).code + "|" + cacheMap.get(waypointList.get(i)).name)
                );
            } else if (cacheMap.get(waypointList.get(i)).type.equals("Webcam")) {
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title(cacheMap.get(waypointList.get(i)).name)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.cache_webcam))
                        .snippet(cacheMap.get(waypointList.get(i)).code + "|" + cacheMap.get(waypointList.get(i)).name)
                );
            }
        }

        menuItem.collapseActionView();
        menuItem.setActionView(null);
    }

    public void cacheRequest(final String code, final Marker marker) {
        if(marker.equals(lastSelectedMarker)) return;
        String tag_json_obj = "json_obj_req";
        String url = "http://opencaching.pl/okapi/services/caches/geocache?consumer_key=mcuwKK4dZSphKHzD5K4C&fields=name|type|size2|rating|owner|recommendations&cache_code=" + code + "";
        final String TAG = MapsActivity.class.getSimpleName();

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());
                try {
                    JSONObject ownerObj = response.getJSONObject("owner");
                    globalCacheMap.get(code).rating = response.getString("rating");
                    globalCacheMap.get(code).size = response.getString("size2");
                    globalCacheMap.get(code).recommendations = response.getString("recommendations");
                    globalCacheMap.get(code).owner = ownerObj.getString("username");

                    Bundle bundle = new Bundle();
                    bundle.putString("waypoint", code);
                    bundle.putString("name", globalCacheMap.get(code).name);
                    bundle.putString("type", globalCacheMap.get(code).type);
                    bundle.putString("size", globalCacheMap.get(code).size);
                    bundle.putString("rating", globalCacheMap.get(code).rating);
                    bundle.putString("owner", globalCacheMap.get(code).owner);
                    bundle.putString("recommendations", globalCacheMap.get(code).recommendations);
                    bundle.putString("location", globalCacheMap.get(code).location);
                    bundle.putString("caller", "MapsActivity");

                    if (!globalFragmentMapCacheInfo.isAdded()) {

                        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        globalFragmentMapCacheInfo = FragmentMapCacheInfo.newInstance(bundle);
                        fragmentTransaction.replace(R.id.mapa, globalFragmentMapCacheInfo);
                        fragmentTransaction.commit();

                    } else {
                        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                        globalFragmentMapCacheInfo = FragmentMapCacheInfo.newInstance(bundle);
                        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.mapa, globalFragmentMapCacheInfo).commit();
                    }
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), mMap.getCameraPosition().zoom));

                    if(globalCacheMap.get(code).type.equals("Traditional")){
                        if(lastSelectedMarker != null){
                            setPreviousMarkerDisable();
                        }
                        lastSelectedMarker = marker;
                        lastSelectedMarkerType = globalCacheMap.get(code).type;
                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.cache_traditional_selected));
                    }
                    else if(globalCacheMap.get(code).type.equals("Other")){
                        if(lastSelectedMarker != null){
                            setPreviousMarkerDisable();
                        }
                        lastSelectedMarker = marker;
                        lastSelectedMarkerType = globalCacheMap.get(code).type;
                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.cache_unknown_selected));
                    }
                    else if(globalCacheMap.get(code).type.equals("Quiz")){
                        if(lastSelectedMarker != null){
                            setPreviousMarkerDisable();
                        }
                        lastSelectedMarker = marker;
                        lastSelectedMarkerType = globalCacheMap.get(code).type;
                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.cache_quiz_selected));
                    }
                    else if(globalCacheMap.get(code).type.equals("Multi")){
                        if(lastSelectedMarker != null){
                            setPreviousMarkerDisable();
                        }
                        lastSelectedMarker = marker;
                        lastSelectedMarkerType = globalCacheMap.get(code).type;
                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.cache_multi_selected));
                    }
                    else if(globalCacheMap.get(code).type.equals("Virtual")){
                        if(lastSelectedMarker != null){
                            setPreviousMarkerDisable();
                        }
                        lastSelectedMarker = marker;
                        lastSelectedMarkerType = globalCacheMap.get(code).type;
                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.cache_virtual_selected));
                    }
                    else if(globalCacheMap.get(code).type.equals("Own")){
                        if(lastSelectedMarker != null){
                            setPreviousMarkerDisable();
                        }
                        lastSelectedMarker = marker;
                        lastSelectedMarkerType = globalCacheMap.get(code).type;
                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.cache_own_selected));
                    }
                    else if(globalCacheMap.get(code).type.equals("Moving")){
                        if(lastSelectedMarker != null){
                            setPreviousMarkerDisable();
                        }
                        lastSelectedMarker = marker;
                        lastSelectedMarkerType = globalCacheMap.get(code).type;
                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.cache_moving_selected));
                    }
                    else if(globalCacheMap.get(code).type.equals("Event")){
                        if(lastSelectedMarker != null){
                            setPreviousMarkerDisable();
                        }
                        lastSelectedMarker = marker;
                        lastSelectedMarkerType = globalCacheMap.get(code).type;
                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.cache_event_selected));
                    }
                    else if(globalCacheMap.get(code).type.equals("Webcam")){
                        if(lastSelectedMarker != null){
                            setPreviousMarkerDisable();
                        }
                        lastSelectedMarker = marker;
                        lastSelectedMarkerType = globalCacheMap.get(code).type;
                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.cache_webcam_selected));
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(MapsActivity.this,
                        "Błąd połączenia z serwerem", Toast.LENGTH_LONG).show();
            }
        });
        AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
    }

    public void setPreviousMarkerDisable(){
        if(lastSelectedMarkerType.equals("Traditional"))
            lastSelectedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.cache_traditional));
        else if(lastSelectedMarkerType.equals("Other"))
            lastSelectedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.cache_unknown));
        else if(lastSelectedMarkerType.equals("Quiz"))
            lastSelectedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.cache_quiz));
        else if(lastSelectedMarkerType.equals("Multi"))
            lastSelectedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.cache_multi));
        else if(lastSelectedMarkerType.equals("Virtual"))
            lastSelectedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.cache_virtual));
        else if(lastSelectedMarkerType.equals("Own"))
            lastSelectedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.cache_own));
        else if(lastSelectedMarkerType.equals("Moving"))
            lastSelectedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.cache_moving));
        else if(lastSelectedMarkerType.equals("Event"))
            lastSelectedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.cache_event));
        else if(lastSelectedMarkerType.equals("Webcam"))
            lastSelectedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.cache_webcam));
    }

}

