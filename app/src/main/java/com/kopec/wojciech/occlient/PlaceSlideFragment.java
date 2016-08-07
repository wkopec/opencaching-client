package com.kopec.wojciech.occlient;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by Wojtek on 2016-08-07.
 */
public final class PlaceSlideFragment extends Fragment {
    //int imageResourceId;
    Bitmap imageResourceId;

    public PlaceSlideFragment(Bitmap i) {
        imageResourceId = i;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ImageView image = new ImageView(getActivity());
        //image.setImageResource(imageResourceId);
        //image.setImageDrawable(imageResourceId);
        image.setImageBitmap(imageResourceId);

        LinearLayout layout = new LinearLayout(getActivity());
        //layout.setLayoutParams(new LayoutParams());

        layout.setGravity(Gravity.CENTER);
        layout.addView(image);

        return layout;
    }
}
