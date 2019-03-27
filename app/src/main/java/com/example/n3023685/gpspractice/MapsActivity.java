package com.example.n3023685.gpspractice;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
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
            System.out.println("your lattitude is" + myLat);
            System.out.println("your long is" + myLong);
            lattitude = Float.parseFloat(myLat);
            longitude = Float.parseFloat(myLong);

        }

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        LatLng newMarker = new LatLng(lattitude, longitude);
        mMap.addMarker(new MarkerOptions().position(newMarker).title("Here is your marker"));
        float zoomLevel = 15.0f; //This goes up to 21
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newMarker, zoomLevel));
        mMap.setOnPoiClickListener(this);

    }

    @Override
    public void onPoiClick(PointOfInterest poi) {
        final PlacesClient placesClient = Places.createClient(this);
        LatLng newMarker = new LatLng(poi.latLng.latitude, poi.latLng.longitude);
        mMap.addMarker(new MarkerOptions().position(newMarker).title("Here is your marker"));
        final ImageView imageView = findViewById(R.id.photoView);
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
            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    int statusCode = apiException.getStatusCode();
                    // Handle error with given status code.
                    Log.e(TAG, "Place not found: " + exception.getMessage());
                }
            });
        });
    }

}
