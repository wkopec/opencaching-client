package com.kopec.wojciech.occlient;

import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.viewpagerindicator.IconPagerAdapter;
import java.util.ArrayList;

/**
 * Created by Wojtek on 2016-08-07.
 */
public class PlaceSlidesFragmentAdapter extends FragmentPagerAdapter implements
        IconPagerAdapter {

    private ArrayList<Bitmap> imgDraws;
    private  boolean[] isSpoilerTab;
    private int mCount;

    public PlaceSlidesFragmentAdapter(FragmentManager fm, ArrayList<Bitmap> bmp, boolean[] isSpoilerTab) {
        super(fm);
        this.isSpoilerTab = isSpoilerTab;
        imgDraws = bmp;
        mCount = imgDraws.size();
    }

    private static final int[] ICONS = new int[] {};

    @Override
    public Fragment getItem(int position) {
        return new PlaceSlideFragment(imgDraws.get(position), isSpoilerTab[position]);
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public int getIconResId(int index) {
        return ICONS[index % ICONS.length];
    }

    public void setCount(int count) {
        if (count > 0 && count <= 10) {
            mCount = count;
            notifyDataSetChanged();
        }
    }
}
