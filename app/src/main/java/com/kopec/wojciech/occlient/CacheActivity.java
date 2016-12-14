package com.kopec.wojciech.occlient;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Locale;

public class CacheActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private Bundle bundle;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bundle = getIntent().getExtras();
        String[] parts = bundle.getString("location").split("\\|");
        String location = Location.convert(Double.parseDouble(parts[0]), Location.FORMAT_MINUTES) + "' " + Location.convert(Double.parseDouble(parts[1]), Location.FORMAT_MINUTES) + "'";
        location = location.replaceAll(":", "\u00B0");
        location = location.replaceAll(",", ".");
        getSupportActionBar().setTitle(bundle.getString("waypoint"));
        getSupportActionBar().setSubtitle(location);

        //getSupportActionBar().setSubtitle(Html.fromHtml("<font style='font-size:20px' color='#ff0000'>" + location + "</font>"));

        setContentView(R.layout.activity_cache);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.title_section1));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.title_section2));
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

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        final FragmentNewLog fragmentNewLog = FragmentNewLog.newInstance(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.add_new_cache_fragment, fragmentNewLog).commit();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 3) {
            if(resultCode == Activity.RESULT_OK){
                Toast.makeText(CacheActivity.this, "Dodano nowy wpis", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cache, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.navigate_to_cache:
                String loc = bundle.getString("location");
                String[] parts = loc.split("\\|");
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + parts[0] + "," + parts[1]));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                return true;

            case R.id.action_compass:

                Intent compassIntent = new Intent(this, CompassActivity.class);
                compassIntent.putExtras(bundle);
                startActivity(compassIntent);

                return true;

            case R.id.action_settings:

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
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
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
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
            }
            return null;
        }
    }
}
