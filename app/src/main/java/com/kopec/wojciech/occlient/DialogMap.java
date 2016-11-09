package com.kopec.wojciech.occlient;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Wojtek on 2016-10-30.
 */

public class DialogMap extends DialogFragment implements OnMapReadyCallback {

    GoogleMap mMap;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_set_map_center, null);

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
                                //((ViewGroup)view.getParent()).removeView(view);
                                dialog.dismiss();
                            }
                        }
                )
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //((ViewGroup)view.getParent()).removeView(view);
                                dialog.dismiss();
                            }
                        }
                )
                .create();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        LatLng faisUJ = new LatLng(50.029591, 19.905875);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(faisUJ, 14));
    }
}
