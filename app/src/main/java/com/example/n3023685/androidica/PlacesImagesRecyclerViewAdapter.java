package com.example.n3023685.androidica;

import android.graphics.Bitmap;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by n3023685 on 01/05/19.
 */

public class PlacesImagesRecyclerViewAdapter extends RecyclerView.Adapter<PlacesImagesRecyclerViewAdapter.PlacesImagesViewHolder> {
    public static class PlacesImagesViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout mConstraintLayout;

        public PlacesImagesViewHolder(ConstraintLayout constraintLayout) {
            super(constraintLayout);
            mConstraintLayout = constraintLayout;
        }

        private void setBitmap(final Bitmap bm) {
            ImageView iv = mConstraintLayout.findViewById(R.id.placeImageView);
            iv.setImageBitmap(bm);
        }
    }

    private final PlaceModel[] mPlaceModels;

    public PlacesImagesRecyclerViewAdapter(final PlaceModel[] placeModels) {
        mPlaceModels = placeModels;
    }

    @Override
    public PlacesImagesRecyclerViewAdapter.PlacesImagesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final ConstraintLayout cl = (ConstraintLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.place_image_view_layout, parent, false);
        return new PlacesImagesViewHolder(cl);
    }

    @Override
    public void onBindViewHolder(PlacesImagesRecyclerViewAdapter.PlacesImagesViewHolder holder, int position) {
        holder.setBitmap(mPlaceModels[position].getBitmap());
    }

    @Override
    public int getItemCount() {
        return mPlaceModels.length;
    }
}

