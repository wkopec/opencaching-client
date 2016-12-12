package com.kopec.wojciech.occlient;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.text.DecimalFormat;

/**
 * Created by Wojtek on 2016-10-30.
 */

public class DialogMap extends DialogFragment implements OnMapReadyCallback {

    SharedPreferences sharedPreferences;
    View view;
    GoogleMap mMap;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        sharedPreferences = getActivity().getSharedPreferences("preferences", Context.MODE_PRIVATE);

        view = getActivity().getLayoutInflater().inflate(R.layout.dialog_set_map_center, null);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);

        return new AlertDialog.Builder(getActivity())
                .setTitle("Wybierz lokalizację")
                .setView(view)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                LatLng mapCenterLatLng = mMap.getCameraPosition().target;
                                float mapZoom = mMap.getCameraPosition().zoom;
                                //SaveCacheActivity activity = (SaveCacheActivity) getActivity();
                                SettingsActivity activity = (SettingsActivity) getActivity();
                                try {
                                    activity.onReturnMapCenter(mapCenterLatLng, mapZoom);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                dialog.dismiss();
                            }
                        }
                )
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }
                )
                .create();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((ViewGroup)view.getParent()).removeView(view);
        Fragment fragment = (getFragmentManager().findFragmentById(R.id.mapView));
        FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
        ft.remove(fragment);
        ft.commit();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                DecimalFormat format = new DecimalFormat("0.##");
                TextView zoom = (TextView) view.findViewById(R.id.mapZoom);
                zoom.setText("Zoom: " + String.valueOf((format.format(mMap.getCameraPosition().zoom))));
            }
        });
        float mapLat = sharedPreferences.getFloat("startMapLat", (float)52.41177549551888);
        float mapLang = sharedPreferences.getFloat("startMapLang", (float)19.17423415929079);
        float mapZoom = sharedPreferences.getFloat("startMapZoom", (float)5.3173866);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mapLat, mapLang), mapZoom));
    }
}
