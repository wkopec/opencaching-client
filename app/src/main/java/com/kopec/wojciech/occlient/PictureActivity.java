package com.kopec.wojciech.occlient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by Wojtek on 2016-11-13.
 */

public class PictureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);

        getSupportActionBar().hide();

        Bundle bundle = getIntent().getExtras();
        byte[] byteArray = bundle.getByteArray("image");
        Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        ImageView imageView = (ImageView) findViewById(R.id.picture);
        imageView.setImageBitmap(bmp);

        PhotoViewAttacher mAttacher = new PhotoViewAttacher(imageView);
        mAttacher.update();
    }
}
