package com.kopec.wojciech.occlient;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;

public class FragmentCacheLogs extends android.support.v4.app.Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static String waypoint;
    SharedPreferences jsonPreferences;

    public FragmentCacheLogs() {
    }

    public static FragmentCacheLogs newInstance(int sectionNumber, Bundle bundle) {
        waypoint = bundle.getString("waypoint");
        FragmentCacheLogs fragment = new FragmentCacheLogs();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_cache_logs, container, false);
        final CacheLog.List logList = new CacheLog.List();
        final int numberOfLogs = 100;

        new AsyncTask<Void, Void, Void>() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    JSONObject jsonObject;
                    JSONArray list;
                    jsonPreferences = getActivity().getSharedPreferences("jsonCacheObjects", Context.MODE_PRIVATE);
                    if(jsonPreferences.getString(waypoint, null) != null){
                        jsonObject = new JSONObject(jsonPreferences.getString(waypoint, null));
                        list = jsonObject.getJSONArray("latest_logs");
                    }
                    else{
                        String urlString = "http://opencaching.pl/okapi/services/caches/geocache?consumer_key=" + getString(R.string.OKAPIConsumerKey) + "&cache_code=" + waypoint + "&fields=latest_logs&lpc=" + numberOfLogs;
                        JSONObject response = jsonObjectRequest(new URL(urlString));
                        list = response.getJSONArray("latest_logs");
                    }
                    Log.d("LIST", list.toString());
                    for(int i=0; i<list.length(); i++){
                        JSONObject obj = list.getJSONObject(i);
                        JSONObject loginObj = obj.getJSONObject("user");
                        String date = obj.getString("date");
                        String[] parts = date.split("T");
                        String day = parts[0];

                        logList.add(new CacheLog(day, obj.getString("type"), obj.getString("comment"), loginObj.getString("username")));
                    }
                } catch (UnknownHostException uhe) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), getString(R.string.internet_connection_error), Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    Log.d("ERROR", e.toString());
                }

                return null;
            }
             @Override
             protected void onPostExecute(Void result) {
                 RecyclerView mRecyclerView = (RecyclerView) rootView.findViewById(R.id.logListView);
                 RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
                 mLayoutManager.setAutoMeasureEnabled(false);
                 mRecyclerView.setLayoutManager(mLayoutManager);
                 LogAdapter mAdapter = new LogAdapter();
                 mAdapter.addLogs(logList);
                 mRecyclerView.setAdapter(mAdapter);
              }
        }.execute();

        return rootView;
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

}

