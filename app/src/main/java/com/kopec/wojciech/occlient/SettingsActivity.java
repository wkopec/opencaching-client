package com.kopec.wojciech.occlient;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

public class SettingsActivity extends AppCompatPreferenceActivity{

    SharedPreferences sharedPreferences;
    Preference loggedAsPreference;
    Preference usernamePreferences;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        setupActionBar();
        FragmentManager mFragmentManager = getFragmentManager();
        FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
        PrefsFragment mPrefsFragment = new PrefsFragment();
        mFragmentTransaction.replace(android.R.id.content, mPrefsFragment);
        mFragmentTransaction.commit();
    }

    private Preference.OnPreferenceChangeListener onPreferenceChange = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {

            if(preference.getKey().equals("prefUsername")){
                usernamePreferences = preference;
                preference.setSummary(sharedPreferences.getString("view_map_as_username", ""));
                String response = "";
                try {
                    if(!value.toString().equals("")){
                        response = new MyClass(value.toString()).execute().get();
                    }
                    if(response.equals("") && !sharedPreferences.getString("username", "").equals("")){
                        response = new MyClass(sharedPreferences.getString("username", "")).execute().get();
                    }
                    else if(response.equals("")){
                        SharedPreferences.Editor mEditor = sharedPreferences.edit();
                        mEditor.putString("view_map_as_username", "");
                        mEditor.putString("user_uuid", "").apply();
                        preference.setSummary("");
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                if(response!=null && !response.equals("")) {
                    preference.setSummary(response);
                }
            }
            if(preference.getKey().equals("prefLoggedAs")){
                if(!sharedPreferences.getString("username", "").equals("")) {
                    preference.setSummary(sharedPreferences.getString("username", getString(R.string.logged_as_summary)));
                }
                else preference.setSummary(getString(R.string.logged_as_summary));

            }
            return true;
        }
    };
    private Preference.OnPreferenceClickListener onPreferenceClick = new Preference.OnPreferenceClickListener(){
        @Override
        public boolean onPreferenceClick(Preference preference) {
            if(preference.getKey().equals("prefLoggedAs")){
                loggedAsPreference = preference;
                startAuthorizationIntent();
            }
            return false;
        }
    };
    private void startAuthorizationIntent(){
        Intent authorizationIntent = new Intent(this, AuthorizationActivity.class);
        startActivityForResult(authorizationIntent, 2);
    }

    private void bindPreferenceListeners(Preference preference) {
        preference.setOnPreferenceChangeListener(onPreferenceChange);
        onPreferenceChange.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));

        preference.setOnPreferenceClickListener(onPreferenceClick);
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:

                super. onBackPressed();
                return true;
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    public class PrefsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
            bindPreferenceListeners(findPreference("prefUsername"));
            bindPreferenceListeners(findPreference("prefLoggedAs"));
        }
    }

    public class MyClass extends AsyncTask<Void, Void, String> {

        String stringValue;
        MyClass(String stringValue){
            this.stringValue = stringValue;
        }

        String user_uuid;
        @Override
        protected String doInBackground(Void... arg0) {
            try {
                String urlString = "http://opencaching.pl/okapi/services/users/by_username?consumer_key=" + getString(R.string.OKAPIConsumerKey) + "&fields=uuid" + "&username=" + stringValue;
                JSONObject jsonObj = jsonObjectRequest(new URL(urlString));
                user_uuid = jsonObj.getString("uuid");
                SharedPreferences.Editor mEditor = sharedPreferences.edit();
                mEditor.putString("view_map_as_username", stringValue);
                mEditor.putString("user_uuid", user_uuid).apply();

            } catch (UnknownHostException uhe) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SettingsActivity.this, getString(R.string.internet_connection_error), Toast.LENGTH_LONG).show();
                    }
                });
            }
            catch (FileNotFoundException fnfe){
                stringValue = "";
                SharedPreferences.Editor mEditor = sharedPreferences.edit();
                mEditor.putString("user_uuid", "").apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SettingsActivity.this, getString(R.string.couldnt_find_username), Toast.LENGTH_LONG).show();
                    }
                });
            }
            catch (Exception e) {
                Log.d("ERROR", e.toString());
            }
            return stringValue;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 2) {
            if(resultCode == Activity.RESULT_OK){
                loggedAsPreference.setSummary(data.getStringExtra("username"));
                if(usernamePreferences.getSummary().equals("") || usernamePreferences.getSummary().equals(data.getStringExtra("username"))){
                    SharedPreferences.Editor Editor = sharedPreferences.edit();
                    Editor.putString("view_map_as_username", data.getStringExtra("username")).apply();
                    usernamePreferences.setSummary(data.getStringExtra("username"));
                }
                else usernamePreferences.setSummary(sharedPreferences.getString("view_map_as_username", ""));
                Toast.makeText(SettingsActivity.this, getString(R.string.logged_in_successfully), Toast.LENGTH_LONG).show();
            }
            if(resultCode == Activity.RESULT_CANCELED){
                Toast.makeText(SettingsActivity.this, getString(R.string.internet_connection_error), Toast.LENGTH_LONG).show();
            }
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

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}