package com.example.mapwithmarker;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {
    private List<String> tags;
    private String specialTag;
    private CheckBox chFood;
    private CheckBox chSocial;
    private CheckBox chMarket;
    private CheckBox chScenic;
    private CheckBox chAthletic;
    private CheckBox chArt;
    private CheckBox chResource;
    private CheckBox chHousing;
    private CheckBox chParking;
    private EditText txSpecific;

    boolean mLocationPermissionGranted = false;
    private GoogleMap map;
    private GoogleApiClient mGoogleApiClient;
    private double latitude;
    private double longitude;
    private static final int REQUEST_LOCATION = 123;
    private Location mLastLocation = null;
    CameraPosition mCameraPosition = null;
    private static final float DEFAULT_ZOOM = 1;
    private LatLng mDefaultLocation = new LatLng(34.056484, -117.821605);
    private final int CPP_GREEN= Color.rgb(32, 74, 0);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        tags = new ArrayList<String>();
        specialTag = "";
        //capture layout interactive components
        chFood = (CheckBox) findViewById(R.id.chFood);
        chSocial = (CheckBox) findViewById(R.id.chSocial);
        chMarket = (CheckBox) findViewById(R.id.chMarket);
        chScenic = (CheckBox) findViewById(R.id.chScenic);
        chAthletic = (CheckBox) findViewById(R.id.chAthletic);
        chArt = (CheckBox) findViewById(R.id.chArt);
        chResource = (CheckBox) findViewById(R.id.chResource);
        chHousing = (CheckBox) findViewById(R.id.chHousing);
        chParking = (CheckBox) findViewById(R.id.chParking);
        txSpecific = (EditText) findViewById(R.id.txSpecific);
        Button btnSearch = (Button) findViewById(R.id.btnSearch);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //assign listeners
        btnSearch.setOnClickListener(btnListener);
        txSpecific.setOnClickListener(txListener);
        txSpecific.setImeOptions(EditorInfo.IME_ACTION_DONE);
        txSpecific.setSingleLine(true);
    }
    public void onMapReady(GoogleMap thisMap) {
        map = thisMap;
        Log.d("order: ", "onMapReady");
        //USE MARKER VISIBILITY
        //EX) CLICK SHOW ALL FOOD AND ONLY FOOD MARKERS WILL APPEAR
        boolean filtering = false;

        // Set a preference for minimum and maximum zoom.
        map.setMinZoomPreference(15.5f);
        map.setMaxZoomPreference(20.0f);

        //ADDED LOCK ROTATION
        map.getUiSettings().setRotateGesturesEnabled(false);

        //REMOVE MAP TOOLBAR
        map.getUiSettings().setMapToolbarEnabled(false);

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();


        // CREATES A BOUNDARY AROUND CPP
        // Create a LatLngBounds that includes the campus of Cal Poly Pomona
        LatLngBounds CalPolyPomona = new LatLngBounds(
                new LatLng(34.048039, -117.827632), new LatLng(34.063650, -117.810913));
        // Constrain the camera target to the CalPolyPomona bounds.
        map.setLatLngBoundsForCameraTarget(CalPolyPomona);


        map.moveCamera(CameraUpdateFactory.newLatLngZoom(CalPolyPomona.getCenter(), 1));
        //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLastLocationCoord, 1));


        // Add a marker in CPP, USA,
        // and move the map's camera to the same location.
        LatLng CPP = new LatLng(34.056484, -117.821605);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(CPP, 15));


        map.setInfoWindowAdapter(new SearchActivity.MarkerWindow());
        if (map != null) {
            getDeviceLocation();

        }
    }
    private void updateLocationUI() {
    /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        }
        if (mLocationPermissionGranted) {
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            map.setMyLocationEnabled(false);
            map.getUiSettings().setMyLocationButtonEnabled(false);
            mLastLocation = null;
        }
    }
    private class MarkerWindow implements GoogleMap.InfoWindowAdapter {
        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            Context context = getApplicationContext();

            LinearLayout info = new LinearLayout(context);
            info.setOrientation(LinearLayout.VERTICAL);
            info.setBackgroundColor(CPP_GREEN);
            info.setPadding(0,0,0,0);
            //info.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            TextView title = new TextView(context);
            title.setTextColor(Color.WHITE);
            title.setGravity(Gravity.CENTER);
            title.setTypeface(null, Typeface.BOLD_ITALIC);
            title.setText(marker.getTitle());

            TextView snippet = new TextView(context);
            snippet.setTextColor(Color.WHITE);
            //snippet.setTypeface(null, Typeface.BOLD);
            snippet.setText(marker.getSnippet());
            snippet.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            info.addView(title);
            info.addView(snippet);
            //ImageView image= new ImageView(context);
            // image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable));
            return info;
        }
    }
    public void getDeviceLocation() {
    /*
     * Before getting the device location, you must check location
     * permission, as described earlier in the tutorial. Then:
     * Get the best and most recent location of the device, which may be
     * null in rare cases when a location is not available.
     */
        Log.d("deviceLocation", "CALLED");
        if (!mLocationPermissionGranted) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION);
                Log.d("permissionDenied", "HATRED");
                return;
            }
            Log.d("permissionGranted", "THANK YOU");
//            if (mGoogleApiClient.isConnected()) {
//                Log.d("clientConnected", "YES");
//            }
//            Log.d("clientConnected", "NO");

            mLastLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                Log.d("currentLocation", "FOUND");
                Log.d("nav", mLastLocation.getLatitude() + ", " + mLastLocation.getLongitude());
            }
        }

        // Set the map's camera position to the current location of the device.
        if (mCameraPosition != null) {
            map.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        } else if (mLastLocation != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mLastLocation.getLatitude(),
                            mLastLocation.getLongitude()), DEFAULT_ZOOM));
        } else {
            Log.d("CURRENT LOCATION", "Current location is null. Using defaults.");
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
            map.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }
    private View.OnClickListener txListener = new View.OnClickListener() {
        public void onClick(View v) {
            // do something when the button is clicked
            txSpecific.setText("");
        }
    };
    //extra keys
    public static final String SPECIFIC_SEARCH = "com.example.MapWithMarker.SPECIFIC_SEARCH";
    public static final String TAG_LIST = "com.example.MapWithMarker.TAGS";
    private View.OnClickListener btnListener = new View.OnClickListener() {
        public void onClick(View v) {
            // do something when the button is clicked
            //add tags to tag list based on what is checked.
            addTags();
            //create new intent
            Intent intent = new Intent(SearchActivity.this, MapsMarkerActivity.class);
            //add extras to intent for filtering

            intent.putExtra(SPECIFIC_SEARCH, specialTag);

            intent.putExtra(TAG_LIST, (ArrayList<String>) tags);
            //intent.putExtra(EXTRA_MESSAGE, message);
            startActivity(intent);

        }
    };

    private void addTags() {
        if (chFood.isChecked()) {
            tags.add("Food");
        }
        if (chArt.isChecked()) {
            tags.add("Art");
        }
        if (chAthletic.isChecked()) {
            tags.add("Athletic");
        }
        if (chHousing.isChecked()) {
            tags.add("Housing");
        }
        if (chMarket.isChecked()) {
            tags.add("Market");
        }
        if (chParking.isChecked()) {
            tags.add("Parking");
        }
        if (chResource.isChecked()) {
            tags.add("Resource");
        }
        if (chScenic.isChecked()) {
            tags.add("Scenic");
        }
        if (chSocial.isChecked()) {
            tags.add("Social");
        }
        if (!txSpecific.getText().toString().equals("Building name, number, etc.")) {
            specialTag = txSpecific.getText().toString();
        }
    }



    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

}
