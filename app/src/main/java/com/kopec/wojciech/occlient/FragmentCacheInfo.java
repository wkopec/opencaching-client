package com.kopec.wojciech.occlient;

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
import org.json.JSONException;
import org.json.JSONObject;


public class FragmentCacheInfo extends android.support.v4.app.Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static String waypoint;
    public FragmentCacheInfo() {
    }

    public static FragmentCacheInfo newInstance(int sectionNumber, Bundle bundle) {
        waypoint = bundle.getString("waypoint");
        FragmentCacheInfo fragment = new FragmentCacheInfo();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    //OnCreate
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_cache_info, container, false);
        //String waypoint = "OP85LA";

        //Request
        final String tag_json_obj = "json_obj_req";
        String url = "http://opencaching.pl/okapi/services/caches/geocache?consumer_key=mcuwKK4dZSphKHzD5K4C&cache_code=" +  waypoint + "&fields=description";
        final String TAG = MapsActivity.class.getSimpleName();

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());

                try {
                    String description = response.getString("description");
                    Log.d("Description", description);

                    WebView webView = (WebView) rootView.findViewById(R.id.webView);
                    webView.getSettings().setJavaScriptEnabled(true);

                    webView.loadDataWithBaseURL("", description, "text/html", "UTF-8", "");

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
