package com.kopec.wojciech.occlient;

import android.annotation.TargetApi;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

public class FragmentCacheLogs extends android.support.v4.app.Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static String waypoint;

    private RecyclerView mRecyclerView;
    private LogAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public FragmentCacheLogs() {
    }

    public static FragmentCacheLogs newInstance(int sectionNumber, Bundle bundle) {
        waypoint = bundle.getString("waypoint");
        Log.d("Waypoint from LOGS", waypoint);
        FragmentCacheLogs fragment = new FragmentCacheLogs();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    //OnCreate
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_cache_logs, container, false);

        //Request
        final int numberOfLogs = 100;
        final String tag_json_obj = "json_obj_req";
        String url = "http://opencaching.pl/okapi/services/caches/geocache?consumer_key=mcuwKK4dZSphKHzD5K4C&cache_code=" + waypoint + "&fields=latest_logs&lpc=" + numberOfLogs ;
        final String TAG = MapsActivity.class.getSimpleName();

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());

                try {
                    CacheLog.List logList = new CacheLog.List();
                    JSONArray list = response.getJSONArray("latest_logs");

                    for(int i=0; i<list.length(); i++){
                        JSONObject obj = list.getJSONObject(i);
                        JSONObject loginObj = obj.getJSONObject("user");

                        String date = obj.getString("date");
                        String[] parts = date.split("T");
                        String day = parts[0];
                        //String time = parts[1];

                        logList.add(new CacheLog(day, obj.getString("type"), obj.getString("comment"), loginObj.getString("username")));
                    }

                    mRecyclerView = (RecyclerView) rootView.findViewById(R.id.logListView);
                    mLayoutManager = new LinearLayoutManager(getActivity());
                    mLayoutManager.setAutoMeasureEnabled(false);
                    mRecyclerView.setLayoutManager(mLayoutManager);
                    mAdapter = new LogAdapter();
                    mAdapter.addLogs(logList);
                    mRecyclerView.setAdapter(mAdapter);

                    //mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

//                    logAdapter = new LogAdapter();
//                    recyclerView.setAdapter(logAdapter);


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

        return rootView;
    }

}

