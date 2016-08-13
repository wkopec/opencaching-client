package com.kopec.wojciech.occlient;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.PageIndicator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

public class FragmentCacheInfo extends android.support.v4.app.Fragment {

    PlaceSlidesFragmentAdapter mAdapter;
    ViewPager mPager;
    PageIndicator mIndicator;

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static Bundle globalBundle;
    public FragmentCacheInfo() {
    }

    public static FragmentCacheInfo newInstance(int sectionNumber, Bundle bundle) {
        globalBundle = bundle;
        FragmentCacheInfo fragment = new FragmentCacheInfo();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    //OnCreate
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        final View rootView = inflater.inflate(R.layout.fragment_cache_info, container, false);

        final String tag_json_obj = "json_obj_req";
        String url = "http://opencaching.pl/okapi/services/caches/geocache?consumer_key=mcuwKK4dZSphKHzD5K4C&cache_code=" +  globalBundle.getString("waypoint") + "&fields=description|hint2|images";
        final String TAG = MapsActivity.class.getSimpleName();

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());

                try {
                    String description = response.getString("description");
                    Log.d("Description", description);
                    String htmlText = description.replaceAll("<img", "<img style=\"max-width:100%; height: auto; width: auto;\"");
                    WebView webView = (WebView) getView().findViewById(R.id.webView);
                    webView.getSettings().setJavaScriptEnabled(true);
                    webView.loadDataWithBaseURL("", htmlText, "text/html", "UTF-8", "");

                    if(!response.getString("hint2").equals("")){
                        Button button = (Button) getView().findViewById(R.id.show_hint);
                        button.setEnabled(true);
                        TextView nameView = (TextView) getView().findViewById(R.id.hint);
                        nameView.setText(response.getString("hint2"));
                    }


                    final ArrayList<String> bigImgList = new ArrayList<>();
                    final JSONArray images = response.getJSONArray("images");
                    for(int i=0; i<images.length(); i++){
                        JSONObject img = images.getJSONObject(i);
                        String smallImg = img.getString("thumb_url");
                        String bigImg = img.getString("url");
                        String imgDescription = img.getString("caption");
                        boolean is_spoiler = img.getBoolean("is_spoiler");
                        Log.d("Strona", smallImg);
                        bigImgList.add(bigImg);
                    }

                    final ArrayList<Bitmap> imgDraws = new ArrayList<>();
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            try {
                                for(int i=0; i<bigImgList.size(); i++) {
                                    InputStream in = new URL(bigImgList.get(i)).openStream();
                                    Bitmap bmp = BitmapFactory.decodeStream(in);;
                                    imgDraws.add(bmp);
                                }
                            } catch (Exception e) {
                                Log.d("ERROR", e.toString());
                            }
                            return null;
                        }

                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        protected void onPostExecute(Void result) {

                            if(imgDraws.size() != 0){
                                Button button = (Button) getView().findViewById(R.id.show_gallery);
                                button.setEnabled(true);
                            }

                            mAdapter = new PlaceSlidesFragmentAdapter(getActivity()
                                    .getSupportFragmentManager(), imgDraws);

                            mPager = (ViewPager) rootView.findViewById(R.id.pager);
                            mPager.setAdapter(mAdapter);

                            mIndicator = (CirclePageIndicator) rootView.findViewById(R.id.indicator);
                            mIndicator.setViewPager(mPager);
                            ((CirclePageIndicator) mIndicator).setSnap(true);
                            ((CirclePageIndicator) mIndicator).setRadius(16);
                            ((CirclePageIndicator) mIndicator).setFillColor(R.color.colorPrimaryDark);
                            mIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                                @Override
                                public void onPageSelected(int position) {
//                                    Toast.makeText(FragmentCacheInfo.this.getActivity(),
//                                            "Changed to page " + position,
//                                            Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                                }
                                    @Override
                                    public void onPageScrollStateChanged(int state) {

                                    }
                            });
                        }
                    }.execute();

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
