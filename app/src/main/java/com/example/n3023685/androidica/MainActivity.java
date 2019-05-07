package com.example.n3023685.androidica;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public static final String LatLong = "com.example.n3023685.gpspractice.LatLong";
    public static final String PlaceID = "com.example.n3023685.gpspractice.PlaceID";
    public static final String Latitude = "com.example.n3023685.gpspractice.Latitude";
    public static final String Longitude = "com.example.n3023685.gpspractice.Longitude";
    public static final String Current_Latitude = "com.example.n3023685.gpspractice.Current_Latitude";
    public static final String Current_Longitude = "com.example.n3023685.gpspractice.Current_Longitude";
    public static final String ERROR_MESSAGE = "com.example.n3023685.gpspractice.MESSAGE";
    private static final String TAG = "MainActivity";
    Place myPlace;
    Intent intent;
    Intent errorIntent;

    TextView weatherBox;
    String myLat = "1";
    String myLong = "1";
    String currLat = "1";
    String currLong = "1";
    String address = "";

    DatabaseHelper myDB;
    ListView listView;

    String[] nameArray;
    String[] infoArray;

    int rowNum;

    CustomListAdapter myAdapter;

    int FINE_LOCATION_PERMISSION_CODE = 1;
    int COURSE_LOCATION_PERMISSION_CODE = 2;

    String error = "Oops something went wrong, please restart the app and try again";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE); // the results will be higher than using the activity context object or the getWindowManager() shortcut
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;
        ListView listView = findViewById(R.id.placesListView);
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = Math.round(screenHeight / 3);
        listView.setLayoutParams(params);
        errorIntent = new Intent(this, ErrorActivity.class);
        requestLocationPermission();
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    public void sendMessage(View view) {
        //this button sends the data about the chosen location to the map activity
        String myLatLong;
        String myPlaceID;
        rowNum = myDB.getAllData().getCount() - 1;
        if (myPlace != null) {
            String[] splitter = myPlace.getLatLng().toString().split(",");
            myDB.insertData(rowNum + 1, myPlace.getName(), splitter[0], splitter[1]);
            myLatLong = myPlace.getLatLng().toString();
            myPlaceID = myPlace.getId();
            intent.putExtra(LatLong, myLatLong);
            intent.putExtra(PlaceID, myPlaceID);
        } else {
            intent.putExtra(Latitude, myLat);
            intent.putExtra(Longitude, myLong);
            intent.putExtra(Current_Latitude, currLat);
            intent.putExtra(Current_Longitude, currLong);
        }
        startActivity(intent);
    }

    public void weather() {
        //this method uses latitude and longitude to call the OpenWeatherMap API and get the weather forecast for that location
        weatherBox = findViewById(R.id.weatherBox);
        Response.Listener<String> mResponseHandler = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                final String[] tempSplitter = response.split("temp");
                String temperature = tempSplitter[1].substring(2, 5);
                String weather = weatherDesc(response);
                if (!address.isEmpty()) {
                    weatherBox.setText("The temperature in " + address + " is: " + (Math.round(Double.parseDouble(temperature) - 273.15)) + "\u00b0 C with " + weather);
                } else {
                    if (myPlace != null) {
                        weatherBox.setText("The temperature at your chosen location is: " + (Math.round(Double.parseDouble(temperature) - 273.15)) + "\u00b0 C with " + weather);
                    } else {
                        weatherBox.setText("The temperature at your current location is: " + (Math.round(Double.parseDouble(temperature) - 273.15)) + "\u00b0 C with " + weather);
                    }
                }
            }
        };
        Response.ErrorListener mErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG, "An error occurred: " + error);
                Toast.makeText(MainActivity.this, "Error fetching weather for your location",
                        Toast.LENGTH_LONG).show();
            }
        };

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

    public StringBuffer viewRow(int i) {
        //this method gets calls the selected row number from the SQL database
        int row = i + 1;
        Cursor res = myDB.getRow(row);
        StringBuffer buffer = new StringBuffer();
        while (res.moveToNext()) {
            buffer.append(res.getString(2) + ",Latitude : " + res.getString(3).substring(10, 17) + " Longitude : " + res.getString(4).substring(0, 7));
        }
        return buffer;
    }

    public void placeLocation(Place place) {
        //this method updates the fragments to display the chosen location info
        ReceiverFragment locationFrag = (ReceiverFragment) getFragmentManager().findFragmentById(R.id.locationFrag);
        ReceiverFragment infoFrag = (ReceiverFragment) getFragmentManager().findFragmentById(R.id.infoFrag);
        if (place != null) {
            locationFrag.updateText(place.getName());
            infoFrag.updateText(address);
        }
    }

    public void sqlLocation(int i) {
        //this method calls a row from the SQL table and splits it into strings to update the display fragments
        ReceiverFragment locationFrag = (ReceiverFragment) getFragmentManager().findFragmentById(R.id.locationFrag);
        ReceiverFragment infoFrag = (ReceiverFragment) getFragmentManager().findFragmentById(R.id.infoFrag);
        String[] splitter = viewRow(i).toString().split(",");
        String sqlLat = splitter[1].substring(11, 18);
        String sqlLong = splitter[1].substring(31);
        myLat = sqlLat;
        myLong = sqlLong;
        address = getAddressFromLocation(Double.parseDouble(myLat), Double.parseDouble(myLong));
        locationFrag.updateText(splitter[0]);
        infoFrag.updateText(address);
        weather();
    }

    public void arrayBuilder() {
        //this method uses the SQL database to build arrays for the list adapter
        myDB = new DatabaseHelper(this);
        nameArray = new String[myDB.getAllData().getCount()];
        infoArray = new String[myDB.getAllData().getCount()];
        int j = 0;
        while (j < myDB.getAllData().getCount()) {
            String[] splitter = viewRow(j).toString().split(",");
            nameArray[j] = splitter[0];
            String sqlLat = splitter[1].substring(11, 18);
            String sqlLong = splitter[1].substring(31);
            myLat = sqlLat;
            myLong = sqlLong;
            infoArray[j] = getAddressFromLocation(Double.parseDouble(myLat), Double.parseDouble(myLong));
            j++;
        }
        myAdapter = new CustomListAdapter(this, nameArray, infoArray);
        listView = findViewById(R.id.placesListView);
        listView.setAdapter(myAdapter);
    }

    public String weatherDesc(String input) {
        //this method splits the weather condition from the rest of the returned weather string
        final String[] weathSplitter = input.split("description");
        String weather = weathSplitter[1].substring(3);
        String[] secondSplit = weather.split(",");
        return secondSplit[0].substring(0, secondSplit[0].length() - 1);
    }

    private String getAddressFromLocation(double latitude, double longitude) {
        //this method uses a Geocoder to get a location name from the lat/long coordinates provided
        Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);
        String address = "";
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                Address fetchedAddress = addresses.get(0);
                address = fetchedAddress.getSubAdminArea();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }

    public void clearHist(View view) {
        myDB.onUpgrade(myDB.getWritableDatabase(), 1, 2);
        arrayBuilder();
    }

    public void loader() {
        //this method initialises the UI
        intent = new Intent(this, MapsActivity.class);
        Places.initialize(getApplicationContext(), "AIzaSyDFFv6OVh2f3f4u2KUnaIGheJObLhlHkVQ");
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            myLat = Double.toString(location.getLatitude());
                            myLong = Double.toString(location.getLongitude());
                            currLat = Double.toString(location.getLatitude());
                            currLong = Double.toString(location.getLongitude());
                            weather();
                            address = getAddressFromLocation(Double.parseDouble(myLat), Double.parseDouble(myLong));
                        } else {
                            error = "Cannot find current location, please check your location settings and restart the app";
                            errorIntent.putExtra(ERROR_MESSAGE, error);
                            startActivity(errorIntent);
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
                myLong = splitter[1].substring(0, 8);
                weather();
                address = getAddressFromLocation(Double.parseDouble(myLat), Double.parseDouble(myLong));
                placeLocation(place);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
                errorIntent.putExtra(ERROR_MESSAGE, "Oops! An error occurred");
                startActivity(errorIntent);
            }
        });

        arrayBuilder();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                sqlLocation(position);
            }
        });
    }

    private void requestLocationPermission() {
        //this method checks if location permissions have already been granted and requests them if not
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {

            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This app requires location permissions to function properly")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION_CODE);
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, COURSE_LOCATION_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            error = "Permissions are required for this app, please check permissions and try again";
                            errorIntent.putExtra(ERROR_MESSAGE, error);
                            startActivity(errorIntent);
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // This method prevents the app from loading before the user has made a decision
        if (requestCode == FINE_LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loader();
            } else {
                error = "Permissions are required for this app, please check permissions and try again";
                errorIntent.putExtra(ERROR_MESSAGE, error);
                startActivity(errorIntent);
            }
        }
    }
}

