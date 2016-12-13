package com.kopec.wojciech.occlient;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import java.io.ByteArrayOutputStream;

/**
 * Created by Wojtek on 2016-08-07.
 */
public final class PlaceSlideFragment extends Fragment {

    public PlaceSlideFragment(){
    }

    Bitmap imageResourceId;
    boolean isSpoiler;

    public PlaceSlideFragment(Bitmap i, boolean isSpoiler) {
        this.isSpoiler = isSpoiler;
        imageResourceId = i;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ImageView image = new ImageView(getActivity());
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                imageResourceId.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();;
                Bundle b = new Bundle();
                b.putByteArray("image", byteArray);

                Intent intent = new Intent(getActivity(), PictureActivity.class);
                intent.putExtras(b);
                startActivity(intent);
            }
        });

        if(isSpoiler){
            Bitmap spoilerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.spoiler);
            image.setImageBitmap(spoilerBitmap);
        }
        else{
            image.setImageBitmap(imageResourceId);
        }

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setGravity(Gravity.CENTER);
        layout.addView(image);

        return layout;
    }
}
