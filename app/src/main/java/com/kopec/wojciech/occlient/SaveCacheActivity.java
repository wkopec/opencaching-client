package com.kopec.wojciech.occlient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.model.LatLng;
import java.text.DecimalFormat;

/**
 * Created by Wojtek on 2016-10-29.
 */

public class SaveCacheActivity extends AppCompatActivity implements  AdapterView.OnItemSelectedListener {

    SharedPreferences sharedPreferences;
    String mapCenterString;
    int limitSeekBar = 100;
    String limit = "&limit=100";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_save_cache);
        setTitle(R.string.action_save_caches);
        sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);

        Bundle bundle = new Bundle();
        final FragmentSaveCaches fragmentSaveCaches = FragmentSaveCaches.newInstance(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.save_caches_fragment, fragmentSaveCaches).commit();

        Spinner spinnerGrade = (Spinner)findViewById(R.id.spinner_grade);
        spinnerGrade.setOnItemSelectedListener(this);

        EditText cords = (EditText)findViewById(R.id.map_center_edit);
        cords.setHint(R.string.set_localization);
        cords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                android.app.FragmentManager manager = getFragmentManager();
                DialogMap dialog = new DialogMap();
                dialog.show(manager, "mapDialog");
            }
        });

        SeekBar seekBar = (SeekBar)findViewById(R.id.limitSeekBar);
        seekBar.setProgress(100);


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            TextView textView = (TextView)findViewById(R.id.numberOfDownloadingCaches);
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                limitSeekBar = seekBar.getProgress();
                limit = "&limit=" + limitSeekBar;
                DecimalFormat format = new DecimalFormat("0.##");
                fragmentSaveCaches.changeEstimated(String.valueOf(format.format(limitSeekBar * 0.18)) + " MB");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub
                textView.setText(String.valueOf(progress));
            }
        });
    }

    public void onReturnMapCenter(LatLng mapCenterLatLng) {
        mapCenterString = String.valueOf(mapCenterLatLng.latitude) + "|" + String.valueOf(mapCenterLatLng.longitude);
        EditText e = (EditText) findViewById(R.id.map_center_edit);
        DecimalFormat format = new DecimalFormat("0.#######");
        e.setText(format.format(mapCenterLatLng.latitude) + ", " + format.format(mapCenterLatLng.longitude));

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

    public void makeRequest() {
        if(mapCenterString == null){
            Toast.makeText(SaveCacheActivity.this, "Wybierz lokalizację", Toast.LENGTH_LONG).show();
        }
        else{
            Intent returnIntent = new Intent();
            returnIntent.putExtra("mapCenter", mapCenterString);
            returnIntent.putExtra("cacheLimit", limit);
            setResult(Activity.RESULT_OK,returnIntent);
            finish();
        }
    }
}
