package com.kopec.wojciech.occlient;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Wojtek on 2016-10-30.
 */

public class DialogMap extends DialogFragment implements OnMapReadyCallback {

    private static View view;
    GoogleMap mMap;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if(view == null)
        view = getActivity().getLayoutInflater().inflate(R.layout.dialog_set_map_center, null);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);

        return new AlertDialog.Builder(getActivity())
                .setTitle("Wybierz lokalizację")
                .setView(view)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                LatLng mapCenterLatLng = mMap.getCameraPosition().target;
                                SaveCacheActivity activity = (SaveCacheActivity) getActivity();
                                activity.onReturnMapCenter(mapCenterLatLng);
                                ((ViewGroup)view.getParent()).removeView(view);
                                dialog.dismiss();
                            }
                        }
                )
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ((ViewGroup)view.getParent()).removeView(view);
                                dialog.dismiss();
                            }
                        }
                )
                .create();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(50.062271, 19.938301), 10));
    }
}
