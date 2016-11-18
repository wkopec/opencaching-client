package com.kopec.wojciech.occlient;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.PageIndicator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class FragmentCacheInfo extends android.support.v4.app.Fragment {

    SharedPreferences jsonPreferences;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {

        final View rootView = inflater.inflate(R.layout.fragment_cache_info, container, false);
        jsonPreferences = this.getActivity().getSharedPreferences("jsonCacheObjects", Context.MODE_PRIVATE);
        final CacheObject cacheObject = new CacheObject();
        new AsyncTask<Void, Void, Void>() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    String urlString;
                    JSONObject response;
                    jsonPreferences = getActivity().getSharedPreferences("jsonCacheObjects", Context.MODE_PRIVATE);
                    if(jsonPreferences.getString(globalBundle.getString("waypoint"), null) != null){
                        response = new JSONObject(jsonPreferences.getString(globalBundle.getString("waypoint"), null));
                    }
                    else{
                        urlString = "http://opencaching.pl/okapi/services/caches/geocache?consumer_key=mcuwKK4dZSphKHzD5K4C&cache_code=" +  globalBundle.getString("waypoint") + "&fields=description|hint2|images";
                        response = jsonObjectRequest(new URL(urlString));
                    }

                    cacheObject.description = response.getString("description").replaceAll("<img", "<img style=\"max-width:100%; height: auto; width: auto;\"");
                    cacheObject.hint = response.getString("hint2");

                    final JSONArray images = response.getJSONArray("images");
                    for(int i=0; i<images.length(); i++){
                        JSONObject img = images.getJSONObject(i);
                        String smallImg = img.getString("thumb_url");
                        String bigImg = img.getString("url");
                        String imgDescription = img.getString("caption");
                        boolean is_spoiler = img.getBoolean("is_spoiler");
                        cacheObject.bigImgList.add(bigImg);
                    }

                } catch (UnknownHostException uhe){
                    getActivity().runOnUiThread(new Runnable(){
                        @Override
                        public void run(){Toast.makeText(getActivity(), "Brak dostępu do Internetu", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    Log.d("ERROR", e.toString());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result){
                WebView webView = (WebView) rootView.findViewById(R.id.webView);
                webView.getSettings().setJavaScriptEnabled(true);
                webView.loadDataWithBaseURL("", cacheObject.description, "text/html", "UTF-8", "");

                if(!cacheObject.hint.equals("")){
                    Button button = (Button) rootView.findViewById(R.id.show_hint);
                    button.setEnabled(true);
                    TextView nameView = (TextView) rootView.findViewById(R.id.hint);
                    nameView.setText(cacheObject.hint);
                }
            }
        }.execute();

        final ArrayList<Bitmap> imgDraws = new ArrayList<>();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    if(cacheObject.bigImgList != null){
                        for(int i=0; i<cacheObject.bigImgList.size(); i++) {


                            //File fotoDirectory = new File(Environment.getExternalStorageDirectory() + File.separator + "Opencaching Map" + File.separator + globalBundle.getString("waypoint") + File.separator + images.getJSONObject(i).getString("unique_caption") + ".jpg");
                            File fotoDirectory = new File(Environment.getExternalStorageDirectory() + File.separator + "Opencaching Map" + File.separator + globalBundle.getString("waypoint") + File.separator + (i+1) + ".jpg");
                            Log.d("Adres", fotoDirectory.toString());
                            if(fotoDirectory.exists()){
                                Log.d("TESTER", "OFFLINE");
                                Bitmap myBitmap = BitmapFactory.decodeFile(fotoDirectory.getAbsolutePath());
                                imgDraws.add(myBitmap);
                            }
                            else{
                                Log.d("TESTER", "ONLINE");
                                InputStream in = new URL(cacheObject.bigImgList.get(i)).openStream();
                                Bitmap bmp = BitmapFactory.decodeStream(in);
                                imgDraws.add(bmp);
                            }




//                            InputStream in = new URL(cacheObject.bigImgList.get(i)).openStream();
//                            Bitmap bmp = BitmapFactory.decodeStream(in);
//                            imgDraws.add(bmp);
                        }
                    }

                } catch (Exception e) {
                    Log.d("ERROR", e.toString());
                }
                return null;
            }

            @TargetApi(Build.VERSION_CODES.M)
            @Override
            protected void onPostExecute(Void result) {

                if(!imgDraws.isEmpty()){
                    mAdapter = new PlaceSlidesFragmentAdapter(getActivity().getSupportFragmentManager(), imgDraws);
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
                            //Toast.makeText(FragmentCacheInfo.this.getActivity(), "Changed to page " + position, Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                        }
                        @Override
                        public void onPageScrollStateChanged(int state) {

                        }
                    });

                    Button button = (Button) rootView.findViewById(R.id.show_gallery);
                    button.setEnabled(true);

                }
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

    class CacheObject{
        String description = "";
        String hint = "";
        ArrayList<String> bigImgList = new ArrayList<>();
    }
}







