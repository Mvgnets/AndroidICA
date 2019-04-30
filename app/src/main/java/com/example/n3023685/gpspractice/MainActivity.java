package com.example.n3023685.gpspractice;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.BaseColumns;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
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

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
    TextView locationMain;
    TextView locationSub;
    public static final String BASE_URL = "api.openweathermap.org/data/2.5/weather?";
    public static final String NOTIFICATION_CHANNEL_ID = "Weather obtained";

    String myLat = "1";
    String myLong = "1";
    String address = "";

    DatabaseHelper myDB;
    ListView listView;

    String[] nameArray;
    String[] infoArray;

    String weather;
    String temperature;

    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

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
                            weather();
                            address = getAddressFromLocation(Double.parseDouble(myLat),Double.parseDouble(myLong));
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
                placeLocation(place);
                String[] splitter = myPlace.getLatLng().toString().split(",");
                myLat = splitter[0].substring(10);
                myLong = splitter[1].substring(0, 8);
                weather();
                address = getAddressFromLocation(Double.parseDouble(myLat),Double.parseDouble(myLong));
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
        intent = new Intent(this, MapsActivity.class);
        arrayBuilder();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                sqlLocation(position);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                delRow(position);
                return true;
            }
        });
    }

    public void sendMessage(View view) {
        String myLatLong;
        String myPlaceID;
        if (myPlace != null) {
            String[] splitter = myPlace.getLatLng().toString().split(",");
            myDB.insertData(myPlace.getName(), splitter[0], splitter[1]);
            myLatLong = myPlace.getLatLng().toString();
            myPlaceID = myPlace.getId().toString();
            intent.putExtra(LatLong, myLatLong);
            intent.putExtra(PlaceID, myPlaceID);
        } else {
            intent.putExtra(Latitude, myLat);
            intent.putExtra(Longitude, myLong);
        }
        startActivity(intent);
    }

    public void weather() {
        weatherBox = findViewById(R.id.weatherBox);
        Response.Listener<String> mResponseHandler = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                final String[] tempSplitter = response.split("temp");
                String temperature = tempSplitter[1].substring(2, 5);
                String weather = weatherDesc(response);
                if (!address.isEmpty()){
                    weatherBox.setText("The temperature in " + address + " is: " + (Math.round(Double.parseDouble(temperature) - 273.15)) + "\u00b0 C with " + weather);
                }
                else{
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
        int row = i + 1;
        Cursor res = myDB.getRow(row);
        StringBuffer buffer = new StringBuffer();
        while (res.moveToNext()) {
            buffer.append("Name : " + res.getString(1) + ",Latitude : " + res.getString(2).substring(10, 17) + " Longitude : " + res.getString(3).substring(0, 7));
        }
        return buffer;
    }

    public void placeLocation(Place place) {
        locationMain = findViewById(R.id.locationMain);
        locationSub = findViewById(R.id.locationSub);
        locationMain.setText(place.getName().toString());
        locationSub.setText(place.getLatLng().toString());
    }

    public void sqlLocation(int i) {
        locationMain = findViewById(R.id.locationMain);
        locationSub = findViewById(R.id.locationSub);
        String[] splitter = viewRow(i).toString().split(",");
        String sqlLat = splitter[1].substring(11, 18);
        String sqlLong = splitter[1].substring(31);
        locationMain.setText(splitter[0]);
        locationSub.setText(splitter[1]);
        myLat = sqlLat;
        myLong = sqlLong;
        address = getAddressFromLocation(Double.parseDouble(myLat),Double.parseDouble(myLong));
        weather();
    }

    public void delRow(int i) {
        myDB.delRow(i);

        List<String> nameList = new ArrayList<String>(Arrays.asList(nameArray));
        nameList.remove(i);
        nameArray = nameList.toArray(new String[0]);

        List<String> infoList = new ArrayList<String>(Arrays.asList(infoArray));
        infoList.remove(i);
        infoArray = infoList.toArray(new String[0]);
        CustomListAdapter myAdapter = new CustomListAdapter(this, nameArray, infoArray);
        listView = findViewById(R.id.placesListView);
        listView.setAdapter(myAdapter);
    }

    public void arrayBuilder() {
        myDB = new DatabaseHelper(this);
        nameArray = new String[myDB.getAllData().getCount()];
        infoArray = new String[myDB.getAllData().getCount()];
        for (int i = 0; i < myDB.getAllData().getCount(); i++) {
            String[] splitter = viewRow(i).toString().split(",");
            nameArray[i] = splitter[0];
            infoArray[i] = splitter[1];
        }
        CustomListAdapter myAdapter = new CustomListAdapter(this, nameArray, infoArray);
        listView = findViewById(R.id.placesListView);
        listView.setAdapter(myAdapter);
    }

    public String weatherDesc(String input) {
        final String[] weathSplitter = input.split("description");
        String weather = weathSplitter[1].substring(3);
        String[] secondSplit = weather.split(",");
        return secondSplit[0].substring(0, secondSplit[0].length() - 1);
    }

    private String getAddressFromLocation(double latitude, double longitude) {

        Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);
        String address = "";

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                Address fetchedAddress = addresses.get(0);
                address = fetchedAddress.getSubAdminArea();
            } else {
                System.out.println("Searching Current Address");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }


}

