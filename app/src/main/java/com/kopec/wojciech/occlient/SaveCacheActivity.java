package com.kopec.wojciech.occlient;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
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

//        Spinner spinnerGrade = (Spinner)findViewById(R.id.spinner_grade);
//        spinnerGrade.setOnItemSelectedListener(this);

        EditText cords = (EditText)findViewById(R.id.map_center_edit);
        cords.setHint(R.string.set_localization);
        cords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard(SaveCacheActivity.this);
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
        String item = parent.getItemAtPosition(position).toString();
        Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
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

    boolean[] selectedFilters;

    public void showFilterDialog(){
        selectedFilters = new boolean[6];
        selectedFilters[0] = sharedPreferences.getBoolean("notFound", true);
        selectedFilters[1] = sharedPreferences.getBoolean("found", false);
        selectedFilters[2] = sharedPreferences.getBoolean("ignored", true);
        selectedFilters[3] = sharedPreferences.getBoolean("own", true);
        selectedFilters[4] = sharedPreferences.getBoolean("temporarilyUnavailable", true);
        selectedFilters[5] = sharedPreferences.getBoolean("archived", true);

        AlertDialog.Builder builder = new AlertDialog.Builder(SaveCacheActivity.this);
        builder.setTitle("Pomiń skrzynki")
                .setMultiChoiceItems(R.array.filters, null,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                if (isChecked) {
                                    selectedFilters[which] = true;
                                } else if (selectedFilters[which]) {
                                    selectedFilters[which] = false;
                                }
                                Log.d("Tablica", String.valueOf(selectedFilters[which]));
                            }
                        })

                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences.Editor mEditor = sharedPreferences.edit();
                        mEditor.putBoolean("notFound", selectedFilters[0]);
                        mEditor.putBoolean("found", selectedFilters[1]);
                        mEditor.putBoolean("ignored", selectedFilters[2]);
                        mEditor.putBoolean("own", selectedFilters[3]);
                        mEditor.putBoolean("temporarilyUnavailable", selectedFilters[4]);
                        mEditor.putBoolean("archived", selectedFilters[5]);
                        mEditor.apply();
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

                for (int i = 0; i<selectedFilters.length; i++) {
                    list.setItemChecked(i, selectedFilters[i]);
                }
            }
        });
        multichoiceDialog.show();
    }


    public void onChangeFiltersClick(View view) {
        if(sharedPreferences.getString("user_uuid", "").equals("")){
            Toast.makeText(SaveCacheActivity.this, "Wprowadź użytkownika w ustawieniach", Toast.LENGTH_LONG).show();
        }
        else{
            showFilterDialog();
        }
    }
}
