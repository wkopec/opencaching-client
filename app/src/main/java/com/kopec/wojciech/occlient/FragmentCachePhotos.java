package com.kopec.wojciech.occlient;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Wojtek on 2016-06-12.
 */
public class FragmentCachePhotos extends android.support.v4.app.Fragment{
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    public FragmentCachePhotos() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static FragmentCachePhotos newInstance(int sectionNumber) {
        FragmentCachePhotos fragment = new FragmentCachePhotos();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    //OnCreate
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_cache_photos, container, false);
        return rootView;
    }
}
