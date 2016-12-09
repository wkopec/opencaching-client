package com.kopec.wojciech.occlient;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by Wojtek on 2016-12-08.
 */

public class NewLogActivity extends AppCompatActivity implements  AdapterView.OnItemSelectedListener, RatingBar.OnRatingBarChangeListener, View.OnTouchListener {

    List<String> list = new ArrayList<>();
    SharedPreferences sharedPreferences;
    Bundle bundle;
    String stringLogDate;
    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_log);
        bundle = getIntent().getExtras();
        setTitle(getString(R.string.new_log) + " - " + bundle.getString("waypoint"));
        sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);

        TextView cacheName = (TextView) findViewById(R.id.cacheName);
        cacheName.setText(bundle.getString("name"));

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+1:00"));
        Date currentLocalTime = cal.getTime();
        DateFormat date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        date.setTimeZone(TimeZone.getTimeZone("GMT+1:00"));
        stringLogDate = date.format(currentLocalTime);

        TextView dateOfLog = (TextView) findViewById(R.id.dateOfLog);
        dateOfLog.setText(getString(R.string.date) + ": " + stringLogDate.replace("T", ", "));

        RatingBar ratingbar = (RatingBar) findViewById(R.id.gradeRatingBar);
        ratingbar.setOnRatingBarChangeListener(this);

        Spinner spinnerLogType = (Spinner) findViewById(R.id.spinner_log_type);
        spinnerLogType.setOnItemSelectedListener(this);

        list.add(getString(R.string.found_it));
        list.add(getString(R.string.didnt_found_it));
        list.add(getString(R.string.comment));
        list.add(getString(R.string.need_maintenance));
        list.add(getString(R.string.maintenance_performed));
        list.add(getString(R.string.will_attend));
        list.add(getString(R.string.attended));
        list.add(getString(R.string.choose_log_type));

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLogType.setAdapter(dataAdapter);
        spinnerLogType.setOnTouchListener(this);
        spinnerLogType.setSelection(dataAdapter.getPosition(getString(R.string.choose_log_type)));
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void onAddNewLogClick(View view) throws UnsupportedEncodingException {

        CheckBox recomendation = (CheckBox) findViewById(R.id.recomendationCheckBox);
        Spinner logType = (Spinner)findViewById(R.id.spinner_log_type);
        EditText logText = (EditText) findViewById(R.id.logText);
        RatingBar ratingbar = (RatingBar) findViewById(R.id.gradeRatingBar);

        url = "http://opencaching.pl/okapi/services/logs/submit?cache_code=" + bundle.getString("waypoint");

        if(logType.getItemAtPosition(logType.getSelectedItemPosition()).toString().equals(getString(R.string.choose_log_type))){
            Toast.makeText(NewLogActivity.this, "Wybierz typ wpisu", Toast.LENGTH_SHORT).show();
            return;
        }
        else if(logType.getItemAtPosition(logType.getSelectedItemPosition()).toString().equals(getString(R.string.found_it))){
            url += "&logtype=" + URLEncoder.encode("Found it", "UTF-8");
            url += "&rating=" + (int)ratingbar.getRating();
            if(recomendation.isChecked()) {
                url += "&recommend=true";
            }
        }
        else if(logType.getItemAtPosition(logType.getSelectedItemPosition()).toString().equals(getString(R.string.didnt_found_it))){
            url += "&logtype=" + URLEncoder.encode("Didn't find it", "UTF-8");
        }
        else if(logType.getItemAtPosition(logType.getSelectedItemPosition()).toString().equals(getString(R.string.comment))){
            url += "&logtype=Comment";
        }
        else if(logType.getItemAtPosition(logType.getSelectedItemPosition()).toString().equals(getString(R.string.need_maintenance))){
            url += "&logtype=Comment";
            url += "&needs_maintenance2=true";
        }
        else if(logType.getItemAtPosition(logType.getSelectedItemPosition()).toString().equals(getString(R.string.maintenance_performed))){
            url += "&logtype=Comment";
            url += "&needs_maintenance2=false";
        }

        url += "&when=" + stringLogDate;
        if(!logText.getText().toString().equals("")){
            url += "&comment_format=plaintext";
            url += "&comment=" + URLEncoder.encode(logText.getText().toString(), "UTF-8");
        }

        Log.d("URL", url);
        new AsyncTask<Void, Void, Void>() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            protected Void doInBackground(Void... params) {

                OAuth10aService mService = new ServiceBuilder()
                        .apiKey(getString(R.string.OKAPIConsumerKey))
                        .apiSecret(getString(R.string.OKAPIConsumerSecret))
                        .build(OpencachingApi.instance());

                final OAuth1AccessToken accessToken = new OAuth1AccessToken(sharedPreferences.getString("oauth_token", ""), sharedPreferences.getString("oauth_token_secret", ""));
                //final OAuthRequest request = new OAuthRequest(Verb.GET, "http://opencaching.pl/okapi/services/users/user?fields=username|uuid|profile_url", mService);
                final OAuthRequest request = new OAuthRequest(Verb.GET, url, mService);
                mService.signRequest(accessToken, request);
                final Response response = request.send();
                Log.d("URL", url);
                try {
                    Log.d("OAUTH TEST", response.getBody());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();

        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d("TESTER", String.valueOf(list.contains(getString(R.string.choose_log_type))));
        if(list.contains(getString(R.string.choose_log_type))){
            list.remove(list.indexOf(getString(R.string.choose_log_type)));
        }
        if(bundle.getString("type", "").equals("Event")){
            if(list.contains(getString(R.string.found_it)))
                list.remove(list.indexOf(getString(R.string.found_it)));
            if(list.contains(getString(R.string.didnt_found_it)))
                list.remove(list.indexOf(getString(R.string.didnt_found_it)));
            if(list.contains(getString(R.string.need_maintenance)))
                list.remove(list.indexOf(getString(R.string.need_maintenance)));
            if(list.contains(getString(R.string.maintenance_performed)))
                list.remove(list.indexOf(getString(R.string.maintenance_performed)));
        }
        else{
            if(sharedPreferences.getString("logged_user_uuid", "").equals(bundle.getString("owner_uuid"))){
                if(list.contains(getString(R.string.found_it)))
                    list.remove(list.indexOf(getString(R.string.found_it)));
                if(list.contains(getString(R.string.didnt_found_it)))
                    list.remove(list.indexOf(getString(R.string.didnt_found_it)));
            }
            if(list.contains(getString(R.string.will_attend)))
                list.remove(list.indexOf(getString(R.string.will_attend)));
            if(list.contains(getString(R.string.attended)))
                list.remove(list.indexOf(getString(R.string.attended)));
        }
        return false;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.d("SPINNER", String.valueOf(position));
        Log.d("SPINNER value", parent.getItemAtPosition(position).toString());
        if(parent.getItemAtPosition(position).toString().equals(getString(R.string.found_it))) showRates();
        else hideRates();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.d("SPINER TEST", "LALA");
    }

    @Override
    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
        switch ((int)rating){
            case 1: Toast.makeText(NewLogActivity.this, getString(R.string.poor), Toast.LENGTH_SHORT).show();
                break;
            case 2: Toast.makeText(NewLogActivity.this, getString(R.string.below_average), Toast.LENGTH_SHORT).show();
                break;
            case 3: Toast.makeText(NewLogActivity.this, getString(R.string.average), Toast.LENGTH_SHORT).show();
                break;
            case 4: Toast.makeText(NewLogActivity.this, getString(R.string.good), Toast.LENGTH_SHORT).show();
                break;
            case 5: Toast.makeText(NewLogActivity.this, getString(R.string.excellent), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void hideRates(){
        CheckBox recomendation = (CheckBox) findViewById(R.id.recomendationCheckBox);
        RatingBar ratingbar = (RatingBar) findViewById(R.id.gradeRatingBar);
        recomendation.setVisibility(View.GONE);
        ratingbar.setVisibility(View.GONE);
    }
    private void showRates(){
        CheckBox recomendation = (CheckBox) findViewById(R.id.recomendationCheckBox);
        RatingBar ratingbar = (RatingBar) findViewById(R.id.gradeRatingBar);
        recomendation.setVisibility(View.VISIBLE);
        ratingbar.setVisibility(View.VISIBLE);
    }

    Calendar mDateAndTime = Calendar.getInstance();
    public void OnChangeDateClick(View view) {

        DatePickerDialog.OnDateSetListener mDateListener = new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                mDateAndTime.set(Calendar.YEAR, year);
                mDateAndTime.set(Calendar.MONTH, monthOfYear);
                mDateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                TimePickerDialog.OnTimeSetListener mTimeListener = new TimePickerDialog.OnTimeSetListener() {
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        mDateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        mDateAndTime.set(Calendar.MINUTE, minute);
                        updateDateAndTimeDisplay();
                    }
                };
                new TimePickerDialog(NewLogActivity.this, mTimeListener,
                        mDateAndTime.get(Calendar.HOUR_OF_DAY),
                        mDateAndTime.get(Calendar.MINUTE), true).show();
            }
        };

        new DatePickerDialog(NewLogActivity.this, mDateListener,
                mDateAndTime.get(Calendar.YEAR),
                mDateAndTime.get(Calendar.MONTH),
                mDateAndTime.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateAndTimeDisplay() {

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+1:00"));
        Date currentLocalTime = cal.getTime();
        Date logDate = mDateAndTime.getTime();

        if(logDate.after(currentLocalTime)){
            Toast.makeText(NewLogActivity.this, "Nieprawidłowa data", Toast.LENGTH_SHORT).show();
        }
        else{
            DateFormat date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
            date.setTimeZone(TimeZone.getTimeZone("GMT+1:00"));
            stringLogDate = date.format(mDateAndTime.getTimeInMillis());
            TextView mDisplayDateTime = (TextView) findViewById(R.id.dateOfLog);
            mDisplayDateTime.setText(getString(R.string.date) + ": " + date.format(mDateAndTime.getTimeInMillis()).replace("T", ", "));
        }

    }
}
