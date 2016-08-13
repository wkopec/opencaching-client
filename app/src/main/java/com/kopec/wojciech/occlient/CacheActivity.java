package com.kopec.wojciech.occlient;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
//import com.google.android.gms.appindexing.Action;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.Locale;

public class CacheActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private Bundle bundle;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bundle = getIntent().getExtras();
        setTitle(bundle.getString("name"));

        setContentView(R.layout.activity_cache);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.title_section1));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.title_section2));
        //tabLayout.addTab(tabLayout.newTab().setText(R.string.title_section3));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cache, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.navigate_to_cache:
                String loc = bundle.getString("location");
                String[] parts = loc.split("\\|");
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q="+parts[0]+","+parts[1]));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                return true;
            case R.id.action_settings:

                return true;
//            case R.id.action_refresh:
//                // refresh
//                return true;
//            case R.id.action_help:
//                // help action
//                return true;
//            case R.id.action_check_updates:
//                // check for updates action
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

//        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
    }

    public void showHint(View view) {
        TextView nameView = (TextView) findViewById(R.id.hint);
        LinearLayout galleryView = (LinearLayout) findViewById(R.id.gallery);
        if(nameView.getVisibility()==View.GONE){
            if(galleryView.getVisibility()==View.VISIBLE){
                galleryView.setVisibility(View.GONE);
            }
            nameView.setVisibility(View.VISIBLE);
        }
        else{
            nameView.setVisibility(View.GONE);
        }
    }

    public void showGallery(View view) {
        TextView nameView = (TextView) findViewById(R.id.hint);
        LinearLayout galleryView = (LinearLayout) findViewById(R.id.gallery);
        if(galleryView.getVisibility()==View.GONE){
            if(nameView.getVisibility()==View.VISIBLE){
                nameView.setVisibility(View.GONE);
            }
            galleryView.setVisibility(View.VISIBLE);
        }
        else{
            galleryView.setVisibility(View.GONE);
        }

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return FragmentCacheInfo.newInstance(position, bundle);
                case 1:
                    return FragmentCacheLogs.newInstance(position, bundle);
//                case 2:
//                    return FragmentCachePhotos.newInstance(position);
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
//                case 2:
//                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }
}
