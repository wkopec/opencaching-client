package com.kopec.wojciech.occlient;

import android.app.Fragment;
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

public class FragmentMapCacheInfo extends Fragment{
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {

        View myInflatedView = inflater.inflate(R.layout.fragment_map_cache_info, container,false);

        TextView nameView = (TextView) myInflatedView.findViewById(R.id.nameTextView);
        TextView typeView = (TextView) myInflatedView.findViewById(R.id.typeTextView);
        TextView sizeView = (TextView) myInflatedView.findViewById(R.id.sizeTextView);
        TextView ratingView = (TextView) myInflatedView.findViewById(R.id.ratingTextView);
        TextView ownerView = (TextView) myInflatedView.findViewById(R.id.ownerTextView);
        TextView recommendationsView = (TextView) myInflatedView.findViewById(R.id.recommendationsTextView);


        nameView.setText(globalBundle.getString("name"));
        typeView.setText("(" + globalBundle.getString("type") + ")");
        sizeView.setText(globalBundle.getString("size"));
        ratingView.setText(globalBundle.getString("rating") + "/5");
        ownerView.setText(globalBundle.getString("owner"));
        recommendationsView.setText(globalBundle.getString("recommendations"));

        myInflatedView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                if(event.getAction() == MotionEvent.ACTION_MOVE){
                    Bundle bundle = new Bundle();
                    bundle.putString("waypoint", globalBundle.getString("waypoint"));

                    Intent intent = new Intent(getActivity(), CacheActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);

                }
                return true;
            }
        });

        return myInflatedView;
    }
}
