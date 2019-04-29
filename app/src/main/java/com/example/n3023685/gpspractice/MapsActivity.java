package com.example.n3023685.gpspractice;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.Arrays;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnPoiClickListener {

    private GoogleMap mMap;
    float lattitude;
    float longitude;
    public final String TAG = MapsActivity.class.getSimpleName();
    SupportMapFragment mapFragment;
    LatLng newMarker;
    Marker poiMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Intent intent = getIntent();
        if (intent.getStringExtra(MainActivity.LatLong) != null) {
            String myLatLong = intent.getStringExtra(MainActivity.LatLong);
            myLatLong = myLatLong.substring(10, myLatLong.length() - 1);
            String[] splitter = myLatLong.split(",");
            lattitude = Float.parseFloat(splitter[0]);
            longitude = Float.parseFloat(splitter[1]);
        }
        if (intent.getStringExtra(MainActivity.Latitude) != null) {
            String myLat = intent.getStringExtra(MainActivity.Latitude);
            String myLong = intent.getStringExtra(MainActivity.Longitude);
            lattitude = Float.parseFloat(myLat);
            longitude = Float.parseFloat(myLong);
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        newMarker = new LatLng(lattitude, longitude);
        showLocation();
        mMap.setOnPoiClickListener(this);

    }

    @Override
    public void onPoiClick(PointOfInterest poi) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE); // the results will be higher than using the activity context object or the getWindowManager() shortcut
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;
        RelativeLayout mapLayout = findViewById(R.id.mapFrame);
        RelativeLayout photoLayout = findViewById(R.id.photoLayout);
        System.out.println(mapLayout.getLayoutParams().height);
        mapLayout.getLayoutParams().height = (int) Math.round(screenHeight * 0.68);
        System.out.println(mapLayout.getLayoutParams().height);
        final PlacesClient placesClient = Places.createClient(this);
        final ImageView imageView = findViewById(R.id.photoView);
        imageView.getLayoutParams().height = (int) Math.round(screenHeight * 0.3);
        newMarker = new LatLng(poi.latLng.latitude, poi.latLng.longitude);
        poiMarker = mMap.addMarker(new MarkerOptions().position(newMarker).title("Here is your marker").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        // Define a Place ID.
        String placeId = poi.placeId;

        // Specify fields. Requests for photos must always have the PHOTO_METADATAS field.
        List<Place.Field> fields = Arrays.asList(Place.Field.PHOTO_METADATAS);

        // Get a Place object (this example uses fetchPlace(), but you can also use findCurrentPlace())
        FetchPlaceRequest placeRequest = FetchPlaceRequest.builder(placeId, fields).build();

        placesClient.fetchPlace(placeRequest).addOnSuccessListener((response) -> {
            Place place = response.getPlace();

            // Get the photo metadata.
            PhotoMetadata photoMetadata = place.getPhotoMetadatas().get(0);

            // Get the attribution text.
            String attributions = photoMetadata.getAttributions();

            // Create a FetchPhotoRequest.
            FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                    .setMaxWidth(500) // Optional.
                    .setMaxHeight(300) // Optional.
                    .build();
            placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                Bitmap bitmap = fetchPhotoResponse.getBitmap();
                imageView.setImageBitmap(bitmap);
            });
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                int statusCode = apiException.getStatusCode();
                // Handle error with given status code.
                Log.e(TAG, "Place not found: " + exception.getMessage());
            }
        });
    }

    public void clearMarkers(View view) {
        mMap.clear();
        RelativeLayout mapLayout = findViewById(R.id.mapFrame);
        final ImageView imageView = findViewById(R.id.photoView);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE); // the results will be higher than using the activity context object or the getWindowManager() shortcut
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;
        int screenWidth = displayMetrics.widthPixels;
        ViewGroup.LayoutParams params = mapLayout.getLayoutParams();
        params.height = screenHeight;
        params.width = screenWidth;
        mapLayout.setLayoutParams(params);
        imageView.setTop(screenHeight);

    }

    public void showLocation() {
        newMarker = new LatLng(lattitude, longitude);
        mMap.addMarker(new MarkerOptions().position(newMarker).title("You are here"));
        float zoomLevel = 15.0f; //This goes up to 21
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newMarker, zoomLevel));
    }

    public void showLocationBtn(View view) {
        showLocation();
    }

    private void resizeFragment(Fragment f, int newWidth, int newHeight) {
        if (f != null) {
            View view = f.getView();
            RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(newWidth, newHeight);
            view.setLayoutParams(p);
            view.requestLayout();
        }
    }
}
