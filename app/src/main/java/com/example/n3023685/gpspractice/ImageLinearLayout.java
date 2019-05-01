package com.example.n3023685.gpspractice;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;

import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * Created by n3023685 on 01/05/19.
 */

public class ImageLinearLayout extends LinearLayout {

    private ImageView mHorseImage;
    //private final RequestQueue queue;

    private Response.Listener<Bitmap> mBitmapResponseListener = new Response.Listener<Bitmap>() {

        @Override
        public void onResponse(final Bitmap bitmap) {
            //addHorseImage(bitmap);
        }
    };

    private Response.ErrorListener mBitmapErrorResponseListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, error.getMessage());
        }
    };

    public ImageLinearLayout(Context context) {
        super(context);
    }

    private void init() {
        setOrientation(LinearLayout.HORIZONTAL);

        // Progammatically add layout parameters
        //
        setLayoutParams(
                new LinearLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.WRAP_CONTENT));
    }


    private void addHorseImage(final Bitmap bitmap) {
        if (mHorseImage == null) {
            mHorseImage = new ImageView(getContext());

            addView(mHorseImage, new LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 0.60f));
        }

        mHorseImage.setImageBitmap(bitmap);

    }
}
