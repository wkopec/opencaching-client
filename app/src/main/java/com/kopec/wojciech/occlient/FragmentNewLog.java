package com.kopec.wojciech.occlient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * Created by Wojtek on 2016-12-08.
 */

public class FragmentNewLog extends Fragment {

    static Bundle globalBundle;
    public static FragmentNewLog newInstance(Bundle bundle) {
        globalBundle = bundle;
        Bundle args = new Bundle();
        FragmentNewLog fragment = new FragmentNewLog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle bundle) {
        final SharedPreferences preferences = this.getActivity().getSharedPreferences("preferences", Context.MODE_PRIVATE);

        View myInflatedView = inflater.inflate(R.layout.fragment_new_log, container, false);
        myInflatedView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(preferences.getString("username", "").equals("")){
                    Toast.makeText(getActivity(), "Musisz się najpierw zalogować", Toast.LENGTH_LONG).show();
                }
                else{
                    Intent newLogIntent = new Intent(getActivity(), NewLogActivity.class);
                    newLogIntent.putExtras(globalBundle);
                    getActivity().startActivityForResult(newLogIntent, 3);
                }

            }
        });

        return myInflatedView;
    }
}