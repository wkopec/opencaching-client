package com.kopec.wojciech.occlient;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.viewpagerindicator.IconPagerAdapter;

import java.util.ArrayList;


/**
 * Created by Wojtek on 2016-08-07.
 */
public class PlaceSlidesFragmentAdapter extends FragmentPagerAdapter implements
        IconPagerAdapter {

    protected ArrayList<Bitmap> imgDraws;
    private int mCount;
    public PlaceSlidesFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    public PlaceSlidesFragmentAdapter(FragmentManager fm, ArrayList<Bitmap> bmp) {
        super(fm);
        Log.d("W KLASIE !!!", bmp.toString());
        imgDraws = bmp;
        mCount = imgDraws.size();
    }

//    private int[] Images = new int[] { R.drawable.cache_quiz, R.drawable.cache_quiz,
//            R.drawable.cache_quiz, R.drawable.cache_quiz
//
//    };

    protected static final int[] ICONS = new int[] { R.drawable.cache_own,
            R.drawable.cache_own, R.drawable.cache_own, R.drawable.cache_own };

    //private int mCount = Images.length;
    //private int mCount = imgDraws.size();



    @Override
    public Fragment getItem(int position) {
        return new PlaceSlideFragment(imgDraws.get(position));
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
