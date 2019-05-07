package com.example.n3023685.androidica;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPhotoResponse;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnPoiClickListener {

    private GoogleMap mMap;
    float lattitude;
    float longitude;
    SupportMapFragment mapFragment;
    LatLng newMarker;
    Marker poiMarker;
    public final String TAG = MapsActivity.class.getSimpleName();

    //private PlacesClient mPlacesClient;
    private Place mPlace;
    private PlacesClient placesClient;

    private PlaceModel[] mPlaceModels;
    String myPlaceId = "ChIJ71jChRHtfkgRPwv2TqaUCnA";



    private final OnFailureListener mFailureListener = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
        }
    };

    private class PhotoSuccessListener implements OnSuccessListener<FetchPhotoResponse> {

        private final int mIndex;

        public PhotoSuccessListener(int index) {
            mIndex = index;
        }

        @Override
        public void onSuccess(FetchPhotoResponse fetchPhotoResponse) {
            mPlaceModels[mIndex].setBitmap(fetchPhotoResponse.getBitmap());
            loadedImageCount++;
        }
    }

    private static int CHECK_ALL_IMAGES_LOADED = 1;

    private int loadedImageCount = 0;

    private static class CheckAllImagesLoadedHandler extends Handler {
        //this method checks if all the available images have been downloaded
        private final WeakReference<MapsActivity> mActivity;

        public CheckAllImagesLoadedHandler(MapsActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(final Message message) {
            if (message.what == CHECK_ALL_IMAGES_LOADED) {

                Log.i(mActivity.get().TAG, "Checking if all the images have fully loaded");

                if (mActivity.get().loadedImageCount == mActivity.get().mPlaceModels.length) {
                    mActivity.get().initRecyclerView();
                } else {
                    mActivity.get().instigateAllImageLoadedCheck();
                }
            }
        }
    }

    private final CheckAllImagesLoadedHandler mCheckAllImagesLoadedHandler = new CheckAllImagesLoadedHandler(this);

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
        initPlacesClient();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //this method loads the map and sets the first marker
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        newMarker = new LatLng(lattitude, longitude);
        showLocation();
        mMap.setOnPoiClickListener(this);
    }

    @Override
    public void onPoiClick(PointOfInterest poi) {
        //this method displays a marker when a point of interest is clicked and starts downloading the images
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE); // the results will be higher than using the activity context object or the getWindowManager() shortcut
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;
        RelativeLayout mapLayout = findViewById(R.id.mapFrame);
        RelativeLayout photoLayout = findViewById(R.id.photoLayout);
        mapLayout.getLayoutParams().height = (int) Math.round(screenHeight * 0.68);
        photoLayout.getLayoutParams().height = (int) Math.round(screenHeight * 0.3);
        newMarker = new LatLng(poi.latLng.latitude, poi.latLng.longitude);
        poiMarker = mMap.addMarker(new MarkerOptions().position(newMarker).title("Here is your marker").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        // Define a Place ID.
        myPlaceId = poi.placeId;
        getPlaceInformation();
    }

    public void clearMarkers(View view) {
        clear();
    }

    public void clear(){
        //this method clears the markers and resizes the map
        mMap.clear();
        RelativeLayout mapLayout = findViewById(R.id.mapFrame);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE); // the results will be higher than using the activity context object or the getWindowManager() shortcut
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;
        int screenWidth = displayMetrics.widthPixels;
        ViewGroup.LayoutParams params = mapLayout.getLayoutParams();
        params.height = screenHeight;
        params.width = screenWidth;
        mapLayout.setLayoutParams(params);
    }

    public void showLocation() {
        // this method moves the camera and marks the users chosen location
        newMarker = new LatLng(lattitude, longitude);
        mMap.addMarker(new MarkerOptions().position(newMarker).title("You are here"));
        float zoomLevel = 15.0f; //This goes up to 21
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newMarker, zoomLevel));
    }


    public void currLocation() {
        // this method moves the camera and marks the users current location
        Intent intent = getIntent();
        if (intent.getStringExtra(MainActivity.Current_Latitude) != null) {
            String currLat = intent.getStringExtra(MainActivity.Current_Latitude);
            String currLong = intent.getStringExtra(MainActivity.Current_Longitude);
            newMarker = new LatLng(Double.parseDouble(currLat), Double.parseDouble(currLong));
            mMap.addMarker(new MarkerOptions().position(newMarker).title("You are here"));
            float zoomLevel = 15.0f; //This goes up to 21
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newMarker, zoomLevel));
        }
        else {
            Toast.makeText(MapsActivity.this, "Error fetching current location",
                    Toast.LENGTH_LONG).show();
        }
    }


    public void showLocationBtn(View view) {
        currLocation();
    }

    private void initPlacesClient() {
        //this method initializes the Google Places client
        final String apiKey = "AIzaSyDFFv6OVh2f3f4u2KUnaIGheJObLhlHkVQ";
        // Initialize Places.
        Places.initialize(getApplicationContext(), apiKey);
    }

    private void getPlaceInformation() {
        //this method attempts to find the information associated with the point of interest that has been clicked
        placesClient = Places.createClient(this);

        final List<Place.Field> placeFields = Arrays.asList(Place.Field.ADDRESS, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.PHOTO_METADATAS);

        final FetchPlaceRequest request = FetchPlaceRequest
                .builder(myPlaceId, placeFields)
                .build();

        placesClient.fetchPlace(request)
                .addOnSuccessListener(
                        new OnSuccessListener<FetchPlaceResponse>() {
                            @Override
                            public void onSuccess(final FetchPlaceResponse fetchPlaceResponse) {

                                mPlace = fetchPlaceResponse.getPlace();
                                fetchPlacePhotos(placesClient);
                            }
                        }
                )
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                if (e instanceof ApiException) {
                                    Log.e(TAG, e.getMessage());
                                    System.out.println("an error occurred");
                                }
                            }
                        }
                );
    }

    private void fetchPlacePhotos(final PlacesClient placesClient) {
        //this method attempts to download hte images associated with a clicked point of interest

        // Get the photo metadata.
        if (mPlace.getPhotoMetadatas() != null) {
            mPlaceModels = new PlaceModel[mPlace.getPhotoMetadatas().size()];
            final int maxWidth = 300;
            final int maxHeight = 300;
            int placeImageCounter = 0;

            for (PhotoMetadata photoMetadata : mPlace.getPhotoMetadatas()) {

                mPlaceModels[placeImageCounter] = new PlaceModel(photoMetadata.getAttributions());

                final FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                        .setMaxWidth(maxWidth) // Optional.
                        .setMaxHeight(maxHeight) // Optional.
                        .build();

                placesClient
                        .fetchPhoto(photoRequest)
                        .addOnSuccessListener(new PhotoSuccessListener(placeImageCounter))
                        .addOnFailureListener(mFailureListener);

                placeImageCounter++;
            }

            instigateAllImageLoadedCheck();
        }

        else {
            Toast.makeText(MapsActivity.this, "No images available",
                    Toast.LENGTH_LONG).show();
            clear();
        }
    }

    private void instigateAllImageLoadedCheck() {

        final Message msg = mCheckAllImagesLoadedHandler.obtainMessage(CHECK_ALL_IMAGES_LOADED);
        mCheckAllImagesLoadedHandler.sendMessageDelayed(msg, 50);
    }

    private void initRecyclerView() {
        //this method initialises the recycler view to display the downloaded images
        RecyclerView mRecyclerView = findViewById(R.id.placeImagesRecyclerView);
        RecyclerView.LayoutManager mRecyclerViewLayoutManager = new LinearLayoutManager(this);
        ((LinearLayoutManager) mRecyclerViewLayoutManager).setOrientation(LinearLayout.HORIZONTAL);
        mRecyclerView.setLayoutManager(mRecyclerViewLayoutManager);
        RecyclerView.Adapter mRecyclerViewAdapter = new PlacesImagesRecyclerViewAdapter(mPlaceModels);
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
        mRecyclerView.setVisibility(View.VISIBLE);
    }
}
