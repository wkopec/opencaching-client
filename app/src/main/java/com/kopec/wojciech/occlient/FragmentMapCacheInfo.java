package com.kopec.wojciech.occlient;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Wojtek on 2016-07-04.
 */

public class FragmentMapCacheInfo extends android.support.v4.app.Fragment{
    private static Bundle globalBundle;

    public static FragmentMapCacheInfo newInstance(Bundle bundle) {
        globalBundle = bundle;
        Bundle args = new Bundle();
        FragmentMapCacheInfo fragment = new FragmentMapCacheInfo();
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentMapCacheInfo() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle bundle) {

        View myInflatedView = inflater.inflate(R.layout.fragment_map_cache_info, container,false);

        TextView nameView = (TextView) myInflatedView.findViewById(R.id.nameTextView);
        TextView typeView = (TextView) myInflatedView.findViewById(R.id.typeTextView);
        TextView sizeView = (TextView) myInflatedView.findViewById(R.id.sizeTextView);
        TextView ratingView = (TextView) myInflatedView.findViewById(R.id.ratingTextView);
        TextView ownerView = (TextView) myInflatedView.findViewById(R.id.ownerTextView);
        TextView recommendationsView = (TextView) myInflatedView.findViewById(R.id.recommendationsTextView);

        nameView.setText(globalBundle.getString("name"));
        typeView.setText("(" + checkType(globalBundle.getString("type")) + ")");
        sizeView.setText(checkSize(globalBundle.getString("size")));
        ratingView.setText(checkRating(globalBundle.getString("rating")));
        ownerView.setText(globalBundle.getString("owner"));
        recommendationsView.setText(globalBundle.getString("recommendations"));

        myInflatedView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_MOVE){
                    Intent intent = new Intent(getActivity(), CacheActivity.class);
                    intent.putExtras(globalBundle);
                    startActivity(intent);
                }
                return true;
            }
        });

        return myInflatedView;
    }

    private String checkType(String type){
        if(type.equals("Traditional")) return "Tradycyjna";
        if(type.equals("Other")) return "Nietypowa";
        if(type.equals("Quiz")) return "Quiz";
        if(type.equals("Multi")) return "Multicache";
        if(type.equals("Virtual")) return "Wirtualna";
        if(type.equals("Own")) return "Own cache";
        if(type.equals("Moving")) return "Mobilna";
        if(type.equals("Event")) return "Wydarzenie";
        if(type.equals("Webcam")) return "Webcam";
        return type;
    }

    private String checkSize(String size){
        if(size.equals("small")) return "mała";
        if(size.equals("micro")) return "mikro";
        if(size.equals("regular")) return "normalna";
        if(size.equals("large")) return "duża";
        if(size.equals("xlarge")) return "bardzo duża";
        if(size.equals("none")) return "bez pojemnika";
        if(size.equals("nano")) return "nano";
        if(size.equals("other")) return "inna";
        return size;
    }

    private String checkRating(String rating){
        if(rating.equals("null")) return "--/5";
        return rating + "/5";
    }
}
