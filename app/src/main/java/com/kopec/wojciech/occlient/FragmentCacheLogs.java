package com.kopec.wojciech.occlient;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
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

import java.util.ArrayList;

public class FragmentCacheLogs extends android.support.v4.app.Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static String waypoint;

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
        final int numberOfLogs = 3;
        final String tag_json_obj = "json_obj_req";
        String url = "http://opencaching.pl/okapi/services/caches/geocache?consumer_key=mcuwKK4dZSphKHzD5K4C&cache_code=" + waypoint + "&fields=latest_logs&lpc=" + numberOfLogs ;
        final String TAG = MapsActivity.class.getSimpleName();

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());

                try {
                    ArrayList<CacheLog> logList = new ArrayList<>();
                    JSONArray list = response.getJSONArray("latest_logs");

                    for(int i=0; i<list.length(); i++){
                        JSONObject obj = list.getJSONObject(i);
                        JSONObject loginObj = obj.getJSONObject("user");
                        logList.add(new CacheLog(obj.getString("date"), obj.getString("type"), obj.getString("comment"), loginObj.getString("username")));
                    }
                    Log.d("LOBIEKT#####", logList.get(0).username);



                    WebView webView = (WebView) rootView.findViewById(R.id.webView);
                    //webView.getSettings().setJavaScriptEnabled(true);

                    //webView.loadDataWithBaseURL("", description, "text/html", "UTF-8", "");

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