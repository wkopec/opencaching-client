package com.kopec.wojciech.occlient;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Wojtek on 2016-11-11.
 */

public class FragmentSaveCaches extends Fragment {

    public static FragmentSaveCaches newInstance(Bundle bundle) {
        Bundle args = new Bundle();
        FragmentSaveCaches fragment = new FragmentSaveCaches();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle bundle) {

        View myInflatedView = inflater.inflate(R.layout.fragment_save_caches, container, false);
        myInflatedView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                SaveCacheActivity activity = (SaveCacheActivity) getActivity();
                activity.makeRequest();
            }
        });

        return myInflatedView;
    }

    public void changeEstimated(String size)
    {
        TextView estimated = (TextView) getView().findViewById(R.id.sizeOfDownloadingCaches);
        estimated.setText(size);
    }

}
