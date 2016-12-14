package com.kopec.wojciech.occlient;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.text.DecimalFormat;

/**
 * Created by Wojtek on 2016-12-13.
 */

public class CompassActivity extends AppCompatActivity implements SensorEventListener {

    private ImageView image;
    private float currentDegree = 0f;
    private SensorManager mSensorManager;
    TextView headingValue;
    TextView distanceValue;
    TextView accuracyValue;
    private Location actualLocation;
    String cacheLatitde;
    String cacheLongitude;
    ProgressBar pbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);

        if(!checkLocationPermission()){
            finish();
        }
        pbar = (ProgressBar)findViewById(R.id.compassProgressBar);
        image = (ImageView) findViewById(R.id.navigateArrow);
        headingValue = (TextView) findViewById(R.id.headingValue);
        distanceValue = (TextView) findViewById(R.id.distanceValue);
        accuracyValue = (TextView) findViewById(R.id.accuracyValue);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        String[] parts = getIntent().getExtras().getString("location").split("\\|");
        cacheLatitde = parts[0];
        cacheLongitude = parts[1];

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {

            public void onLocationChanged(Location location) {
                actualLocation = location;
                Location loc1 = new Location("");
                loc1.setLatitude(Double.parseDouble(cacheLatitde));
                loc1.setLongitude(Double.parseDouble(cacheLongitude));


                float distanceInMeters = location.distanceTo(loc1);
                DecimalFormat format = new DecimalFormat("0.#");
                distanceValue.setText(String.valueOf(format.format(distanceInMeters)) + " m");
                accuracyValue.setText(String.valueOf(actualLocation.getAccuracy()) + "m");


            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );



        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {

            AlertDialog.Builder builder = new AlertDialog.Builder(CompassActivity.this);
            builder.setTitle(getString(R.string.localization_disable))
                    .setMessage(getString(R.string.do_set_localization_enable))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    }).create().show();
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

    }

    @Override
    protected void onResume() {
        super.onResume();

        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(actualLocation == null){
            return;
        }

        pbar.setVisibility(View.GONE);
        image.setVisibility(View.VISIBLE);

        float degree = Math.round(event.values[0] + bearing());

        headingValue.setText(String.valueOf(normalizeDegree(degree)));

        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        // how long the animation will take place
        ra.setDuration(210);

        // set the animation after the end of the reservation status
        ra.setFillAfter(true);

        // Start the animation
        image.startAnimation(ra);
        currentDegree = -degree;
    }

    private float normalizeDegree(float value) {
        value = value % 360;
        if (value >= 0.0f && value <= 180.0f) {
            return -value;
        } else {
            return (360 - value);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }

    private double bearing() {

        double longDiff = Math.toRadians(Double.parseDouble(cacheLongitude) - actualLocation.getLongitude());

        double y = Math.sin(longDiff) * Math.cos(Double.parseDouble(cacheLatitde));
        double x = Math.cos(actualLocation.getLatitude()) * Math.sin(Double.parseDouble(cacheLatitde)) - Math.sin(actualLocation.getLatitude()) * Math.cos(Double.parseDouble(cacheLatitde)) * Math.cos(longDiff);

        return Math.toDegrees(Math.atan2(y, x));
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                //  TODO: Prompt with explanation!

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }
}
