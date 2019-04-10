package com.example.n3023685.gpspractice;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {
    public static final String LatLong = "com.example.n3023685.gpspractice.LatLong";
    public static final String PlaceID = "com.example.n3023685.gpspractice.PlaceID";
    public static final String Latitude = "com.example.n3023685.gpspractice.Latitude";
    public static final String Longitude = "com.example.n3023685.gpspractice.Longitude";
    private static final String TAG = "MainActivity";
    Place myPlace;
    private FusedLocationProviderClient fusedLocationClient;
    Intent intent;
    TextView weatherBox;
    public static final String BASE_URL = "api.openweathermap.org/data/2.5/weather?";
    public static final String NOTIFICATION_CHANNEL_ID = "Weather obtained";

    String myLat = "1";
    String myLong = "1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Places.initialize(getApplicationContext(), "AIzaSyDFFv6OVh2f3f4u2KUnaIGheJObLhlHkVQ");
        PlacesClient placesClient = Places.createClient(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.RECEIVE_SMS, Manifest.permission.CALL_PHONE}, 1);
            return; //ask for the permissions the app requires
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            myLat = Double.toString(location.getLatitude());
                            myLong = Double.toString(location.getLongitude());
                            weather(myLat, myLong);
                        }
                    }
                });
        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
                myPlace = place;
                String[] splitter = myPlace.getLatLng().toString().split(",");
                myLat = splitter[0].substring(10);
                System.out.println(myLat);
                myLong = splitter[1].substring(0, 8);
                System.out.println(myLong);
                weather(myLat, myLong);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
        intent = new Intent(this, MapsActivity.class);
    }

    public void currentLocation(View view) {
        intent.putExtra(Latitude, myLat);
        intent.putExtra(Longitude, myLong);
    }

    public void sendMessage(View view) {
        String myLatLong;
        String myPlaceID;
        if (myPlace != null) {
            myLatLong = myPlace.getLatLng().toString();
            myPlaceID = myPlace.getId().toString();
            intent.putExtra(LatLong, myLatLong);
            intent.putExtra(PlaceID, myPlaceID);
        }
        System.out.println(intent);
        startActivity(intent);
    }

    public void weather(String latitude, String longitude) {
        weatherBox = findViewById(R.id.weatherBox);

        Response.Listener<String> mResponseHandler = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                final String[] splitter = response.split(",");
                weatherBox.setText("The temperature at your chosen location is: " + (Math.round(Double.parseDouble(splitter[7].substring(15)) - 273.15)) + "\u00b0 C");
            }
        };
        Response.ErrorListener mErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getMessage());
            }
        };
        //Use the volley library to fetch a list of the horse images
        //
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest weatherListing = new StringRequest(
                Request.Method.GET,
                "http://api.openweathermap.org/data/2.5/weather?lat=" + myLat + "&lon=" + myLong + "&APPID=646c6ad8b825a8fe88fb21654deab612",
                mResponseHandler,
                mErrorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                final String authz = "646c6ad8b825a8fe88fb21654deab612";
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Basic " + authz);
                return headers;
            }
        };
        queue.add(weatherListing);
    }
}

