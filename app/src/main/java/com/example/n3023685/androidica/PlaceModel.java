package com.example.n3023685.androidica;

import android.graphics.Bitmap;

public class PlaceModel {
    private Bitmap mBitmap;
    private final String mInfo;

    public PlaceModel(final String info) {
        mInfo = info;
    }

    public String getInfo() {
        return mInfo;
    }

    public void setBitmap(final Bitmap bitmap) {
        mBitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }
}
