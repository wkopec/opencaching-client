package com.kopec.wojciech.occlient;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.EditTextPreference;
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

public class SettingsActivity extends AppCompatPreferenceActivity  {

    SharedPreferences sharedPreferences;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();


        // Display the fragment as the main content.
        FragmentManager mFragmentManager = getFragmentManager();
        FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
        PrefsFragment mPrefsFragment = new PrefsFragment();
        mFragmentTransaction.replace(android.R.id.content, mPrefsFragment);
        mFragmentTransaction.commit();

    }

    private Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            final String stringValue = value.toString();
            sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
            if (preference instanceof EditTextPreference) {
                String response = null;
                if(preference.getKey().equals("prefUsername")){
                    try {
                        response = new MyClass(stringValue).execute().get();

                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                preference.setSummary(response);

            }
            else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }


    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                // ProjectsActivity is my 'home' activity
                super. onBackPressed();
                return true;
        }
        return (super.onOptionsItemSelected(menuItem));
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

    public class PrefsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
            bindPreferenceSummaryToValue(findPreference("prefUsername"));
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
                String urlString = "http://opencaching.pl/okapi/services/users/by_username?consumer_key=mcuwKK4dZSphKHzD5K4C&fields=uuid" + "&username=" + stringValue;

                JSONObject jsonObj = jsonObjectRequest(new URL(urlString));
                user_uuid = jsonObj.getString("uuid");
                Log.d("USER UUID", user_uuid);
                SharedPreferences.Editor mEditor = sharedPreferences.edit();
                mEditor.putString("user_uuid", user_uuid).apply();

            } catch (UnknownHostException uhe) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SettingsActivity.this, "Brak dostępu do Internetu", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(SettingsActivity.this, "Nie znaleziono użytkownika", Toast.LENGTH_LONG).show();
                    }
                });
            }
            catch (Exception e) {
                Log.d("ERROR", e.toString());
            }
            return stringValue;
        }
    }
}