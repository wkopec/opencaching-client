package com.kopec.wojciech.occlient;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Wojtek on 2016-10-29.
 */

public class SaveCacheActivity extends AppCompatActivity implements OnMapReadyCallback, AdapterView.OnItemSelectedListener {

    private static GoogleMap mMap;

    private ArrayList<String> globalWaypointList = new ArrayList<>();
    SharedPreferences sharedPreferences;
    String mapCenterString;
    int limitSeekBar = 10;
    String limit = "&limit=10";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_cache);
        setTitle(R.string.save_caches);
        sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);

        EditText cords = (EditText)findViewById(R.id.map_center_edit);
        cords.setHint("Wybierz lokalizację");
        cords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                android.app.FragmentManager manager = getFragmentManager();
                DialogMap dialog = new DialogMap();
                dialog.show(manager, "mapDialog");

            }
        });



        SeekBar seekBar = (SeekBar)findViewById(R.id.limitSeekBar);
        seekBar.setProgress(10);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            TextView textView = (TextView)findViewById(R.id.limitTextView);
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                limitSeekBar = seekBar.getProgress();
                limit = "&limit=" + limitSeekBar;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub
                textView.setText("Ilośc skrzynek: " + String.valueOf(progress));
            }
        });


        Bundle bundle = new Bundle();
        FragmentSaveCaches fragmentSaveCaches = FragmentSaveCaches.newInstance(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.save_caches_fragment, fragmentSaveCaches).commit();


        Spinner spinnerGrade = (Spinner)findViewById(R.id.spinner_grade);
        spinnerGrade.setOnItemSelectedListener(this);

    }

    public void onReturnMapCenter(LatLng mapCenterLatLng) {
        mapCenterString = String.valueOf(mapCenterLatLng.latitude) + "|" + String.valueOf(mapCenterLatLng.longitude);
        EditText e = (EditText) findViewById(R.id.map_center_edit);
        e.setText(mapCenterLatLng.latitude + ", " + mapCenterLatLng.longitude);

    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        // On selecting a spinner item
        String item = parent.getItemAtPosition(position).toString();

        // Showing selected spinner item
        Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }



    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        LatLng faisUJ = new LatLng(50.029591, 19.905875);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(faisUJ, 14));

        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        } else {
            mMap.setMyLocationEnabled(true);
        }
    }



    public void waypointsRequest(final String center, final String limit) {
        final ArrayList<String> waypointList = new ArrayList<>();

        new AsyncTask<Void, Void, Void>() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
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

                    JSONArray jsonWaypointList = jsonObj.getJSONArray("results");

                    for (int i = 0; i < jsonWaypointList.length(); i++) {
                        waypointList.add(jsonWaypointList.getString(i));
                    }

                } catch (UnknownHostException uhe) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SaveCacheActivity.this, "Błąd połączenia z opencaching.pl", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    Log.d("ERROR", e.toString());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                cachesRequest(waypointList);
            }
        }.execute();
    }

    public void cachesRequest(final ArrayList<String> waypointList) {

        final JSONObject globalJsonObject = new JSONObject();
        new AsyncTask<Void, Void, Void>() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            protected Void doInBackground(Void... params) {
                try {

                    Log.d("TEST", waypointList.toString());
                    String codes = waypointList.get(0);
                    if (waypointList.size() > 1) {
                        for (int i = 1; i < waypointList.size(); i++) {
                            codes += "|" + waypointList.get(i);
                        }
                    }
                    String logsLimit = "999";
                    String urlString = "http://opencaching.pl/okapi/services/caches/geocaches?consumer_key=mcuwKK4dZSphKHzD5K4C&fields=name|type|size2|rating|owner|recommendations|location|status|code|description|hint2|images|latest_logs&cache_codes=" + codes + "&lpc=" + logsLimit;
                    JSONObject response = jsonObjectRequest(new URL(urlString));

                    for (int i = 0; i < waypointList.size(); i++) {
                        globalJsonObject.put(waypointList.get(i), response.getJSONObject(waypointList.get(i)));

                        final JSONArray images = response.getJSONObject(waypointList.get(i)).getJSONArray("images");

                        for(int j=0; j<images.length(); j++){
                            JSONObject img = images.getJSONObject(j);
                            //String bigImg = img.getString("url");
                            InputStream in = new URL(img.getString("url")).openStream();
                            Bitmap bmp = BitmapFactory.decodeStream(in);
                            savebitmap(bmp, img.getString("unique_caption"), waypointList.get(i));
                        }
                        Log.d("PROGRESS", String.valueOf(i) + " [" + waypointList.get(i) + "]");
                    }

                    //Saving images

                } catch (UnknownHostException uhe) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SaveCacheActivity.this, "Błąd połączenia z opencaching.pl", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    Log.d("ERROR", e.toString());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {

                JSONObject loadedJson = new JSONObject();
                try {
                    if(sharedPreferences.getString("jsonCaches", null) != null){
                        loadedJson = new JSONObject(sharedPreferences.getString("jsonCaches", null));
                    }
                        Iterator<String> iter = globalJsonObject.keys();
                        while (iter.hasNext()) {
                            String key = iter.next();
                                loadedJson.put(key, globalJsonObject.get(key));
                        }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                SharedPreferences.Editor mEditor = sharedPreferences.edit();
                mEditor.putString("jsonCaches", loadedJson.toString());
                mEditor.apply();

                Log.d("global JSON", globalJsonObject.toString());
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

    public void savebitmap(Bitmap bmp, String name, String waypoint){
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        File fotoDirectory = new File(Environment.getExternalStorageDirectory() + File.separator + "Opencaching Map" + File.separator + waypoint);

        fotoDirectory.mkdirs();
        File f = new File(fotoDirectory, name + ".jpg");

        try {
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //return f;
    }

    public void makeRequest() {
        waypointsRequest(mapCenterString, limit);
    }
}

class FragmentSaveCaches extends Fragment {

    public static FragmentSaveCaches newInstance(Bundle bundle) {
        Bundle args = new Bundle();
        FragmentSaveCaches fragment = new FragmentSaveCaches();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle bundle) {

        View myInflatedView = inflater.inflate(R.layout.fragment_save_caches, container, false);
        myInflatedView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                SaveCacheActivity activity = (SaveCacheActivity) getActivity();
                activity.makeRequest();
            }
        });

        return myInflatedView;
    }

}