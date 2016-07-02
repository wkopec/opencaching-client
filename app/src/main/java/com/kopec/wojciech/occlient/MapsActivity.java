package com.kopec.wojciech.occlient;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
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
import com.google.android.gms.location.LocationListener;
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

public class MapsActivity  extends AppCompatActivity implements OnMapReadyCallback, DialogInterface.OnClickListener, GoogleMap.OnMarkerClickListener {

    private static GoogleMap mMap;
    private  ArrayList<String> globalWaypointList = new ArrayList<>();
    private HashMap<String, CacheInfo> globalCacheMap = new HashMap<>();
    private LocationManager locationMangaer;
    private LocationListener locationListener;

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
        LatLng faisUj = new LatLng(50.029591, 19.905875);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(faisUj, 14));
        mMap.setOnMarkerClickListener(this);
        //mMap.setMapType(googleMap.MAP_TYPE_SATELLITE);
        if(Build.VERSION.SDK_INT >= 23){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        else {
            mMap.setMyLocationEnabled(true);
        }

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Bundle bundle = new Bundle();
                bundle.putString("waypoint", marker.getSnippet());
                Intent i = new Intent(MapsActivity.this,CacheActivity.class);
                i.putExtras(bundle);
                startActivity(i);

            }
        });
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        locationListener = new MyLocationListener();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationMangaer.requestLocationUpdates(LocationManager
                .GPS_PROVIDER, 5000, 10, (android.location.LocationListener) locationListener);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();

        return false;
    }


    //MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click
        switch (item.getItemId()) {
            case R.id.action_download_caches:

                LatLng mapCenterLatLng = mMap.getCameraPosition().target;
                String mapCenterString = String.valueOf(mapCenterLatLng.latitude)+"|"+String.valueOf(mapCenterLatLng.longitude);
                waypointsRequest(mapCenterString, "300", "03767B69-4960-065E-0A2A-984EDE6BBC83");

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
    public void waypointsRequest(String center, String limit, String notFoundBy){
        final ArrayList<String> waypointList = new ArrayList<>();
        String tag_json_obj = "json_obj_req";
        String url = "http://opencaching.pl/okapi/services/caches/search/nearest?consumer_key=mcuwKK4dZSphKHzD5K4C&center=" +  center + "&limit=" + limit + "&not_found_by=" + notFoundBy;
        final String TAG = MapsActivity.class.getSimpleName();

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //Log.d(TAG, response.toString());
                try {
                    JSONArray list = response.getJSONArray("results");
                    for(int i=0; i<list.length(); i++){
                        waypointList.add(list.getString(i));
                    }

                    ArrayList<String> newWaypoints = new ArrayList<>();
                    if(!globalWaypointList.isEmpty()){
                        for(int i=0; i<waypointList.size(); i++){
                            if(!globalWaypointList.contains(waypointList.get(i))){
                                newWaypoints.add(waypointList.get(i));
                            }
                        }
                        globalWaypointList.addAll(newWaypoints);
                    }
                    else{
                        newWaypoints.addAll(waypointList);
                        globalWaypointList.addAll(newWaypoints);
                    }

                    if(!newWaypoints.isEmpty()){
                        cachesRequest(newWaypoints);
                    }

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

    public void cachesRequest(final ArrayList<String> waypointList){
        String codes = waypointList.get(0);
        if(waypointList.size()>1){
            for (int i=1; i<waypointList.size(); i++){
                codes += "|" + waypointList.get(i);
            }
        }

        String tag_json_obj = "json_obj_req";
        String url = "http://opencaching.pl/okapi/services/caches/geocaches?consumer_key=mcuwKK4dZSphKHzD5K4C&cache_codes=" +  codes + "";
        final String TAG = MapsActivity.class.getSimpleName();

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
            HashMap<String, CacheInfo> cacheMap = new HashMap<>();
            @Override
            public void onResponse(JSONObject response) {
                //Log.d(TAG, response.toString());
                try {

                    for(int i=0; i<waypointList.size(); i++){
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

    public void showWaypoints(HashMap<String, CacheInfo> cacheMap, ArrayList<String> waypointList){

        for(int i=0; i<waypointList.size(); i++){
            String[] parts = cacheMap.get(waypointList.get(i)).location.split("\\|");
            Double latitude = Double.parseDouble(parts[0]);
            Double longitude = Double.parseDouble(parts[1]);

            if(cacheMap.get(waypointList.get(i)).type.equals("Traditional")){
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title(cacheMap.get(waypointList.get(i)).name)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.cache_traditional))
                        .snippet(cacheMap.get(waypointList.get(i)).code)
                );
            }
            else if(cacheMap.get(waypointList.get(i)).type.equals("Other")){
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title(cacheMap.get(waypointList.get(i)).name)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.cache_unknown))
                        .snippet(cacheMap.get(waypointList.get(i)).code)
                );
            }
            else if(cacheMap.get(waypointList.get(i)).type.equals("Quiz")){
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title(cacheMap.get(waypointList.get(i)).name)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.cache_quiz))
                        .snippet(cacheMap.get(waypointList.get(i)).code)
                );
            }
            else if(cacheMap.get(waypointList.get(i)).type.equals("Multi")){
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title(cacheMap.get(waypointList.get(i)).name)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.cache_multi))
                        .snippet(cacheMap.get(waypointList.get(i)).code)
                );
            }
            else if(cacheMap.get(waypointList.get(i)).type.equals("Virtual")){
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title(cacheMap.get(waypointList.get(i)).name)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.cache_virtual))
                        .snippet(cacheMap.get(waypointList.get(i)).code)
                );
            }
            else if(cacheMap.get(waypointList.get(i)).type.equals("Own")){
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title(cacheMap.get(waypointList.get(i)).name)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.cache_own))
                        .snippet(cacheMap.get(waypointList.get(i)).code)
                );
            }
            else if(cacheMap.get(waypointList.get(i)).type.equals("Moving")){
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title(cacheMap.get(waypointList.get(i)).name)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.cache_moving))
                        .snippet(cacheMap.get(waypointList.get(i)).code)
                );
            }
            else if(cacheMap.get(waypointList.get(i)).type.equals("Event")){
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title(cacheMap.get(waypointList.get(i)).name)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.cache_event))
                        .snippet(cacheMap.get(waypointList.get(i)).code)
                );
            }
            else if(cacheMap.get(waypointList.get(i)).type.equals("Webcam")){
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title(cacheMap.get(waypointList.get(i)).name)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.cache_webcam))
                        .snippet(cacheMap.get(waypointList.get(i)).code)
                );
            }
        }
    }

    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {
        }

//        @Override
//        public void onProviderDisabled(String provider) {
//            // TODO Auto-generated method stub
//        }
//
//        @Override
//        public void onProviderEnabled(String provider) {
//            // TODO Auto-generated method stub
//        }
//
//        @Override
//        public void onStatusChanged(String provider,
//                                    int status, Bundle extras) {
//            // TODO Auto-generated method stub
//        }
    }
}

