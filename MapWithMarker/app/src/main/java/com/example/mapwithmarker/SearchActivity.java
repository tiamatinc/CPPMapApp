package com.example.mapwithmarker;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
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

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
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

    private GoogleMap map;
    private GoogleApiClient mGoogleApiClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        tags = new ArrayList<String>();
        specialTag = "";
        //capture layout interactive components
        chFood = (CheckBox)findViewById(R.id.chFood);
        chSocial = (CheckBox)findViewById(R.id.chSocial);
        chMarket = (CheckBox)findViewById(R.id.chMarket);
        chScenic = (CheckBox)findViewById(R.id.chScenic);
        chAthletic = (CheckBox)findViewById(R.id.chAthletic);
        chArt = (CheckBox)findViewById(R.id.chArt);
        chResource = (CheckBox)findViewById(R.id.chResource);
        chHousing = (CheckBox)findViewById(R.id.chHousing);
        chParking = (CheckBox)findViewById(R.id.chParking);
        txSpecific = (EditText)findViewById(R.id.txSpecific);
        Button btnSearch = (Button)findViewById(R.id.btnSearch);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        //assign listeners
        btnSearch.setOnClickListener(btnListener);

    }
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

                intent.putExtra(TAG_LIST, (ArrayList<String>)tags);
            //intent.putExtra(EXTRA_MESSAGE, message);
            startActivity(intent);

        }
    };
    private void addTags()
    {
        if(chFood.isChecked())
        {
            tags.add("Food");
        }
        if(chArt.isChecked())
        {
            tags.add("Art");
        }
        if(chAthletic.isChecked())
        {
            tags.add("Athletic");
        }
        if(chHousing.isChecked())
        {
            tags.add("Housing");
        }
        if(chMarket.isChecked())
        {
            tags.add("Market");
        }
        if(chParking.isChecked())
        {
            tags.add("Parking");
        }
        if(chResource.isChecked())
        {
            tags.add("Resource");
        }
        if(chScenic.isChecked())
        {
            tags.add("Scenic");
        }
        if(chSocial.isChecked())
        {
            tags.add("Social");
        }
        if(!txSpecific.getText().toString().equals("Building name, number, etc."))
        {
            specialTag = txSpecific.getText().toString();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
