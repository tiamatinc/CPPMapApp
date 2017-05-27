package com.example.mapwithmarker;

import android.content.pm.PackageManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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


/**
 * An activity that displays a Google map with a marker (pin) to indicate a particular location.
 */
public class MapsMarkerActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    //ARRAYLIST TO STORE THE POINTS OF INTEREST FOR EACH TAG
    ArrayList<Marker> FoodTag = new ArrayList<>();
    ArrayList<Marker> AthleticTag = new ArrayList<>();
    ArrayList<Marker> ScenicTag = new ArrayList<>();
    ArrayList<Marker> MarketTag = new ArrayList<>();
    ArrayList<Marker> BuildingTag = new ArrayList<>();
    ArrayList<Marker> ParkingTag = new ArrayList<>();
    ArrayList<Marker> ResourcesTag = new ArrayList<>();
    ArrayList<Marker> SocialTag = new ArrayList<>();
    ArrayList<Marker> HousingTag = new ArrayList<>();
    ArrayList<Marker> ArtTag = new ArrayList<>();

    //CREATE A GOOGLE MAP
    GoogleMap googleMap = null;

    // Create an instance of GoogleAPIClient.
    GoogleApiClient mGoogleApiClient = null;

    //CURRENT LOCATION
    private Location mLastLocation = null;
    //DESTINATION LOCATION
    private Marker dest = null;
    //DEFAULT LOCATION
    private LatLng mDefaultLocation = new LatLng(34.056484, -117.821605);

    //DEFAULT ZOOM
    private static final float DEFAULT_ZOOM = 1;

    //PERMISSIONS
    boolean mLocationPermissionGranted = false;

    //CAMERA POSITION
    CameraPosition mCameraPosition = null;

    //REQUEST
    private static final int REQUEST_LOCATION = 123;

    //CREATE A HASH MAP WITH THE TAGS AS THE KEYS
    //AND THE ARRAY LIST OF POINTS OF INTEREST AS THE VALUE
    HashMap<String, ArrayList<Marker>> PointsOfInterest = new HashMap<String, ArrayList<Marker>>();

    //CHECK IF DIRECTIONS ACTIVE
    private boolean directionsActive = false;

    //COLOR FOR INFO WINDOW
    private final int CPP_GREEN= Color.rgb(32, 74, 0);

    public void updateTags() {
        PointsOfInterest.put("FOOD", FoodTag);
        PointsOfInterest.put("ATHLETIC", AthleticTag);
        PointsOfInterest.put("SCENIC", ScenicTag);
        PointsOfInterest.put("SOCIAL", SocialTag);
        PointsOfInterest.put("MARKET", MarketTag);
        PointsOfInterest.put("BUILDING", BuildingTag);
        PointsOfInterest.put("PARKING", ParkingTag);
        PointsOfInterest.put("RESOURCES", ResourcesTag);
        PointsOfInterest.put("SOCIAL", SocialTag);
        PointsOfInterest.put("HOUSING", HousingTag);
        PointsOfInterest.put("ART", ArtTag);

        printHashMap(PointsOfInterest);
    }


    public void printHashMap(HashMap<String, ArrayList<Marker>> printThis) {
        //FOR EACH KEY/ TAG IN THE POINTSOFINTEREST HASHMAP
        for (String key : PointsOfInterest.keySet()) {
            for (Marker POI : PointsOfInterest.get(key)) {
                Log.d("printingkeys", "KEY: " + key + " Value: " + POI.getTitle());
            }
        }
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    //PERMISSIONS
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    //GET CURRENT LOCATION
    //CAN EXTRACT LONGITUDE AND LATITUDE
    private void getDeviceLocation() {
    /*
     * Before getting the device location, you must check location
     * permission, as described earlier in the tutorial. Then:
     * Get the best and most recent location of the device, which may be
     * null in rare cases when a location is not available.
     */
        Log.d("deviceLocation", "CALLED");
        if (mLocationPermissionGranted) {
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
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        } else if (mLastLocation != null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mLastLocation.getLatitude(),
                            mLastLocation.getLongitude()), DEFAULT_ZOOM));
        } else {
            Log.d("CURRENT LOCATION", "Current location is null. Using defaults.");
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    /**
     * Builds the map when the Google Play services client is successfully connected.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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
            return;
        }

        if (mGoogleApiClient.isConnected()) {
            Log.d("clientConnected", "YES");
            }
            Log.d("clientConnected", "NO");

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    //UPDATE CURRENT LOCATION
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
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            googleMap.setMyLocationEnabled(false);
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            mLastLocation = null;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }


        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);
        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map when it's available.
     * The API invokes this callback when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user receives a prompt to install
     * Play services inside the SupportMapFragment. The API invokes this method after the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap thisMap) {
        googleMap = thisMap;
        Log.d("order: ", "onMapReady");
        //USE MARKER VISIBILITY
        //EX) CLICK SHOW ALL FOOD AND ONLY FOOD MARKERS WILL APPEAR

        // Set a preference for minimum and maximum zoom.
        googleMap.setMinZoomPreference(15.5f);
        googleMap.setMaxZoomPreference(20.0f);

        //ADDED LOCK ROTATION
        googleMap.getUiSettings().setRotateGesturesEnabled(false);

        //REMOVE MAP TOOLBAR
        googleMap.getUiSettings().setMapToolbarEnabled(false);

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();


        // CREATES A BOUNDARY AROUND CPP
        // Create a LatLngBounds that includes the campus of Cal Poly Pomona
        LatLngBounds CalPolyPomona = new LatLngBounds(
                new LatLng(34.048039, -117.827632), new LatLng(34.063650, -117.810913));
        // Constrain the camera target to the CalPolyPomona bounds.
        googleMap.setLatLngBoundsForCameraTarget(CalPolyPomona);


        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CalPolyPomona.getCenter(), 1));
        //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLastLocationCoord, 1));


        // Add a marker in CPP, USA,
        // and move the map's camera to the same location.
        LatLng CPP = new LatLng(34.056484, -117.821605);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CPP, 15));

        if(!directionsActive) addMarkers();

        googleMap.setOnInfoWindowClickListener(new ClickForDirectionsListener());
        googleMap.setInfoWindowAdapter(new MarkerWindow());


        //ADD ALL TAG ARRAYLIST INTO THE HASHMAP WITH THEIR RESPECTIVE KEYS
        updateTags();
    }

    private void addMarkers() {
        //// Food Areas ////////////////////////////////////////////////////////////////

        LatLng LosOlivos = new LatLng(34.062360, -117.821519);
        Marker LosOlivosInfo = googleMap.addMarker(new MarkerOptions()
                .position(LosOlivos)
                .title("Los Olivos")
                .snippet("Features “all-you-care-to-eat” dining with many different dietary accommodations. Come in for a quick bite, or stay for a full meal! \n" +
                        "Hours:\n" +
                        "Monday -- Thursday\n" +
                        "7:00 am - 8:00 pm\n" +
                        "Friday\n" +
                        "7:00 am - 7:30 pm\t\n" +
                        "Saturday\n" +
                        "1:30 - 7:30 pm\t\n" +
                        "Sunday\n" +
                        "1:30 - 7:30 pm\n" +
                        "Late night options available Mon- Wed, Sun: 9 pm - Midnight;\n"));
        LosOlivosInfo.setTag("FOOD");
        FoodTag.add(LosOlivosInfo);

        LatLng TheDen = new LatLng(34.053992, -117.818052);
        Marker TheDenInfo = googleMap.addMarker(new MarkerOptions()
                .position(TheDen)
                .title("The Den")
                .snippet("Come in and you'll see that is not your dad's diner. This is a place with a vibe. For breakfast. At midnight. Whenever. Whyever. The look is lounge. The feel is easy-chair communal comfort zone. The menu is breakfast all-day, plus sandwiches, burritos, salads—all made the way we eat today. This is \"The Den.\" The next milestone in American dining from the people who invented American dining.\n" +
                        "Hours: \n" +
                        "Monday -- Thursday\n" +
                        "9:00 am - 12:00 am\t\n" +
                        "Friday\n" +
                        "9:00 am - 10:00 pm\t\n" +
                        "Saturday\n" +
                        "10:00 am - 10:00 pm\t\n" +
                        "Sunday\n" +
                        "10:00 am - 12:00 am\n"));
        TheDenInfo.setTag("FOOD SOCIAL");
        FoodTag.add(TheDenInfo);


        LatLng VistaMarket = new LatLng(34.053899, -117.817946);
        Marker VistaMarketInfo = googleMap.addMarker(new MarkerOptions()
                .position(VistaMarket)
                .title("Vista Market")
                .snippet("We are a fully stocked, one-stop-shop neighborhood store with a fun community vibe that offers personal items and the staples needed to prepare home-cooked meals. Every day, every palate, every need we've got you covered.\n" +
                        "Hours: \n" +
                        "Monday -- Thursday\n" +
                        "7:00 am - 1:00 am\t\n" +
                        "Friday\n" +
                        "7:00 am - 10:00 pm\t\n" +
                        "Saturday\n" +
                        "10:00 am - 10:00 pm\t\n" +
                        "Sunday\n" +
                        "10:00 am - 1:00 am\n"));
        VistaMarketInfo.setTag("FOOD MARKET");
        FoodTag.add(VistaMarketInfo);
        MarketTag.add(VistaMarketInfo);

        Marker MarketplaceInfo = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.057755, -117.823262))
                .title("Bronco Marketplace")
                .snippet("Campus Center Marketplace, located at Building 97, is in the heart of the Cal Poly Pomona campus, southeast of the College of Letters, Arts, and Social Sciences (Bldg. 5) and southwest of the College of Education and Integrative Studies (Bldg. 6). Campus Center Marketplace offers a wide variety of dining venues and has indoor and outdoor seating areas." +
                        "\n Hours may vary"));
        MarketplaceInfo.setTag("FOOD MARKET");
        FoodTag.add(MarketplaceInfo);
        MarketTag.add(MarketplaceInfo);

        Marker StarbucksLibInfo = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.057578, -117.821447))
                .title("Starbucks [Library]")
                .snippet("Drop in and enjoy a cup of Starbucks before class, or stay a while and peruse the campus library. No matter what your needs, this fully-functioning Starbucks is here to serve you daily!\n" +
                        "Hours\n" +
                        "Monday -- Thursday\n" +
                        "7:00 am - 10:00 pm\n" +
                        "Friday\n" +
                        "7:00 am - 5:00 pm\n" +
                        "Saturday\n" +
                        "10:00 am - 6:00 pm\n" +
                        "Sunday\n" +
                        "12:00 pm - 9:00 pm\n"));
        StarbucksLibInfo.setTag("FOOD SOCIAL");
        FoodTag.add(StarbucksLibInfo);
        SocialTag.add(StarbucksLibInfo);

        Marker BrewWorksInfo = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.049896, -117.814977))
                .title("Innovation Brew Works")
                .snippet("Innovation Brew Works is a café and brewery located on the Cal Poly Pomona campus at the Center for Training, Technology, and Incubation (CTTI) in Innovation Village. We are open to the public."));
        BrewWorksInfo.setTag("FOOD");
        FoodTag.add(BrewWorksInfo);

        Marker FarmStoreInfo = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.048408, -117.819184))
                .title("CPP Farm Store")
                .snippet("Local and organic produce. Craft sodas, plants, wine, and gifts. Features a line of products from the campus farm, orchards, and nursery. \n" +
                        "Open every day: 10:00 AM - 6:00PM\n"));
        FarmStoreInfo.setTag("FOOD");
        FoodTag.add(FarmStoreInfo);

        //// Visual POIs //////////////////////////////////////////////////////

        LatLng JapaneseGarden = new LatLng(34.05992, -117.820431);
        Marker JapaneseGardenInfo = googleMap.addMarker(new MarkerOptions()
                .position(JapaneseGarden)
                .title("Japanese Garden")
                .snippet("Japanese garden with large koi pond and seating. Scenic spot to sit and enjoy."));
        JapaneseGardenInfo.setTag("SCENIC");
        ScenicTag.add(JapaneseGardenInfo);

        LatLng RoseGarden = new LatLng(34.060829, -117.82034);
        Marker RoseGardenInfo = googleMap.addMarker(new MarkerOptions()
                .position(RoseGarden)
                .title("Rose Garden")
                .snippet("Famous Rose Garden that can be viewed from the Kellogg Mansion or walked through at any hour. Scenic gazebo resting place at the center. Note: Plucking the roses will get you fined."));
        RoseGardenInfo.setTag("SCENIC");
        ScenicTag.add(RoseGardenInfo);

        LatLng DuckPond = new LatLng(34.061024, -117.821583);
        Marker DuckPondInfo = googleMap.addMarker(new MarkerOptions()
                .position(DuckPond)
                .title("Duck Pond")
                .snippet("The duck pond is home to turtles, koi, carp, and of course ducks. Enjoy the water feature at the center of the pond while turtles sunbathe next to you. Open to visitors at all hours."));
        DuckPondInfo.setTag("SCENIC");
        ScenicTag.add(DuckPondInfo);

        LatLng EngineeringMeadow = new LatLng(34.059103, -117.821392);
        Marker EngineeringMeadowInfo = googleMap.addMarker(new MarkerOptions()
                .position(EngineeringMeadow)
                .title("Engineering Meadow")
                .snippet("Come relax at the Engineering Meadow between classes."));
        EngineeringMeadowInfo.setTag("SCENIC");
        ScenicTag.add(EngineeringMeadowInfo);

        LatLng HorseshoeHill = new LatLng(34.059103, -117.821392);
        Marker HorseshoeHillInfo = googleMap.addMarker(new MarkerOptions()
                .position(HorseshoeHill)
                .title("Horseshoe Hill")
                .snippet("Horseshoe Hill overlooks the CLA Building and some of the pastureland the university's Arabian horses call home. Horseshoe Hill is also one of the main locations for major concerts put on by ASI's Bronco Events and Activities Team."));
        HorseshoeHillInfo.setTag("SCENIC");
        ScenicTag.add(HorseshoeHillInfo);

        LatLng VoorhisPark = new LatLng(34.059963, -117.81876);
        Marker VoorhisParkInfo = googleMap.addMarker(new MarkerOptions()
                .position(VoorhisPark)
                .title("Voorhis Park")
                .snippet("Avaibility may vary due to ongoing construction."));
        VoorhisParkInfo.setTag("SCENIC");
        ScenicTag.add(VoorhisParkInfo);


        LatLng BroncoCommons = new LatLng(34.05514, -117.820247);
        Marker BroncoCommonsInfo = googleMap.addMarker(new MarkerOptions()
                .position(BroncoCommons)
                .title("Bronco Commons")
                .snippet("Grass field located in front of the BRIC with a cement stage used to hold many of the university’s events, such as BroncoFusion"));
        BroncoCommonsInfo.setTag("SCENIC SOCIAL");
        ScenicTag.add(BroncoCommonsInfo);
        SocialTag.add(BroncoCommonsInfo);

        LatLng ScolinosField = new LatLng(34.053873, -117.815944);
        Marker ScolinosFieldInfo = googleMap.addMarker(new MarkerOptions()
                .position(ScolinosField)
                .title("Scolinos Field")
                .snippet("University’s baseball field, located near the residential suites. Games for the university team are held here occasionally--to follow the season schedule, check http://broncoathletics.com/schedule.aspx?path=baseball."));
        ScolinosFieldInfo.setTag("SCENIC");
        ScenicTag.add(ScolinosFieldInfo);

        LatLng KelloggTrackStadium = new LatLng(34.052271, -117.816857);
        Marker KelloggTrackStadiumInfo = googleMap.addMarker(new MarkerOptions()
                .position(KelloggTrackStadium)
                .title("Kellogg Track & Soccer Stadium")
                .snippet("University’s soccer field with a running track surrounding it. A small section in this area also contains a practice field for pole vaulting."));
        KelloggTrackStadiumInfo.setTag("ATHLETIC");
        AthleticTag.add(KelloggTrackStadiumInfo);

        LatLng AuxAthleticsField = new LatLng(34.052794, -117.818419);
        Marker AuxAthleticsFieldInfo = googleMap.addMarker(new MarkerOptions()
                .position(AuxAthleticsField)
                .title("Auxiliary Athletics Field")
                .snippet("Home to the Cal Poly Pomona men's and women's soccer teams, as well as the men's and women's track and field squads."));
        AuxAthleticsFieldInfo.setTag("ATHLETIC");
        AthleticTag.add(AuxAthleticsFieldInfo);

        LatLng UniQuad = new LatLng(34.058537, -117.82364);
        Marker UniQuadInfo = googleMap.addMarker(new MarkerOptions()
                .position(UniQuad)
                .title("University Quad")
                .snippet("Grassy area- feel free to relax during your breaks and enjoy the sights and sounds of campus life."));
        UniQuadInfo.setTag("SCENIC SOCIAL");
        ScenicTag.add(UniQuadInfo);
        SocialTag.add(UniQuadInfo);


        LatLng UniPark = new LatLng(34.056744, -117.820972);
        Marker UniParkInfo = googleMap.addMarker(new MarkerOptions()
                .position(UniPark)
                .title("University Park")
                .snippet("University Park is host to many student activities including the Hot Dog Caper, noontime concerts, movie night and more. The Bronco Student Center is a central gathering place for the campus community and is home to the student government offices, Center Court food court, the Wellness Center, Visitor Center and Bronco Fitness Center."));
        UniParkInfo.setTag("SCENIC SOCIAL");
        ScenicTag.add(UniParkInfo);
        SocialTag.add(UniParkInfo);

        LatLng BioTrekGarden = new LatLng(34.057201, -117.826465);
        Marker BioTrekGardenInfo = googleMap.addMarker(new MarkerOptions()
                .position(BioTrekGarden)
                .title("BioTrek Ethnobotany Garden")
                .snippet("Developed out of concern for today's threat to plant and animal species and the viability of the biosphere, BioTrek is an education-based project that serves college students and reaches out to community members. The 107,000-square foot complex has an aquatic learning center, rainforest and labs for biotechnology research, education and enterprise."));
        BioTrekGardenInfo.setTag("SCENIC");
        ScenicTag.add(BioTrekGardenInfo);

        LatLng BioTrekCenter = new LatLng(34.058232, -117.826072);
        Marker BioTrekCenterInfo = googleMap.addMarker(new MarkerOptions()
                .position(BioTrekCenter)
                .title("BioTrek Learning Center")
                .snippet("BioTrek is an educational enterprise of the Biological Sciences Department of California State Polytechnic University, Pomona. In its greenhouse, garden, and labs, it brings to students and the public both hands-on and electronic educational experiences of the tropical rainforest and California indigenous plants and people. "));
        BioTrekGardenInfo.setTag("SCENIC");
        ScenicTag.add(BioTrekCenterInfo);

        Marker TennisCourtsInfo = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.052905, -117.820365))
                .title("Campus Tennis Courts")
                .snippet("Stop by our tennis courts and see what the CPP Tennis Club is planning! Hours and meeting times may vary"));
        TennisCourtsInfo.setTag("ATHLETIC SOCIAL");
        AthleticTag.add(TennisCourtsInfo);
        SocialTag.add(TennisCourtsInfo);

        //// Numbered Bldgs /////////////////////////////////////////////////////////

        LatLng Bldg1 = new LatLng(34.059561, -117.82434);
        Marker Bldg1Info = googleMap.addMarker(new MarkerOptions()
                .position(Bldg1)
                .title("Bldg 1")
                .snippet("Administration Building. Also home to economics."));
        Bldg1Info.setTag("BUILDING 1");
        BuildingTag.add(Bldg1Info);

        LatLng Bldg2 = new LatLng(34.057701, -117.826681);
        Marker Bldg2Info = googleMap.addMarker(new MarkerOptions()
                .position(Bldg2)
                .title("Bldg 2: College of Agriculture")
                .snippet("Agriculture and Veterinary Services"));
        Bldg2Info.setTag("BUILDING 2");
        BuildingTag.add(Bldg2Info);

        LatLng Bldg3 = new LatLng(34.058079, -117.825733);
        Marker Bldg3Info = googleMap.addMarker(new MarkerOptions()
                .position(Bldg3)
                .title("Bldg 3: Science Laboratory")
                .snippet("College of Science Advising. Various science laboratories."));
        Bldg3Info.setTag("BUILDING 3");
        BuildingTag.add(Bldg3Info);

        LatLng Bldg4 = new LatLng(34.057483, -117.825513);
        Marker Bldg4Info = googleMap.addMarker(new MarkerOptions()
                .position(Bldg4)
                .title("Bldg 4: Biotechnology Building")
                .snippet("Center for Excellence in Mathematics and Science Teaching. Institute for Cellular and Molecular Biology."));
        Bldg4Info.setTag("BUILDING 4");
        BuildingTag.add(Bldg4Info);

        LatLng Bldg5 = new LatLng(34.057803, -117.824435);
        Marker Bldg5Info = googleMap.addMarker(new MarkerOptions()
                .position(Bldg5)
                .title("Bldg 5: College of Letters, Arts, and Social Sciences")
                .snippet("The College of Letters, Arts, and Social Sciences advances knowledge and learning in established academic disciplines in the humanities, social sciences, and performing arts. It provides introductory and advanced course work in more than 20 degree and certificate programs."));
        Bldg5Info.setTag("BUILDING 5");
        BuildingTag.add(Bldg5Info);

        LatLng Bldg6 = new LatLng(34.058616, -117.822804);
        Marker Bldg6Info = googleMap.addMarker(new MarkerOptions()
                .position(Bldg6)
                .title("Bldg 6: College of Education and Integrated Studies")
                .snippet("The College of Education and Integrative Studies is a learning community focused on meeting the present and future needs of students in our communities. We educate students to become highly qualified and significant leaders in our society. We are committed to the principles of diversity, ethics and social justice, and life-long learning. Central to our mission are innovative and integrative thinking, reflective practice, collaborative action, and learning by doing."));
        Bldg6Info.setTag("BUILDING 6");
        BuildingTag.add(Bldg6Info);

        LatLng Bldg7 = new LatLng(34.057087, -117.827385);
        Marker Bldg7Info = googleMap.addMarker(new MarkerOptions()
                .position(Bldg7)
                .title("Bldg 7: College of Environmental Design")
                .snippet("The College of Environmental Design aims to educate the next generation of architects, graphic designers, landscape architects, and urban planners – people who will strive to change the world, or at least their part of the world – using the “learn by doing” philosophy for which Cal Poly Pomona is well-known."));
        Bldg7Info.setTag("BUILDING 7");
        BuildingTag.add(Bldg7Info);

        LatLng Bldg8 = new LatLng(34.058554, -117.824719);
        Marker Bldg8Info = googleMap.addMarker(new MarkerOptions()
                .snippet("As one of only eight polytechnic universities in the US, the College of Science (COS) at Cal Poly Pomona is committed to a top-notch science education through discovery and innovation. With the university’s ‘learn-by-doing philosophy,’ our programs advance science and foster real-world education. Our professionally active faculty members prepare students to become qualified science professionals.")
                .position(Bldg8)
                .title("Bldg 8: College of Science"));
        Bldg8Info.setTag("BUILDING 8");
        BuildingTag.add(Bldg8Info);

        LatLng Bldg9 = new LatLng(34.05947, -117.822552);
        Marker Bldg9Info = googleMap.addMarker(new MarkerOptions()
                .position(Bldg9)
                .title("Bldg 9: College of Engineering")
                .snippet("Students have opportunities to apply their knowledge to hands-on projects, collaborate with faculty members on research for a team-based learning experience, and participate in valuable internships and service-learning programs"));
        Bldg8Info.setTag("BUILDING 9");
        BuildingTag.add(Bldg9Info);

        LatLng Bldg13 = new LatLng(34.05883, -117.820771);
        Marker Bldg13Info = googleMap.addMarker(new MarkerOptions()
                .position(Bldg13)
                .title("Bldg 13: Art Department and Engineering Annex")
                .snippet("No further info available"));
        Bldg13Info.setTag("BUILDING 13");
        BuildingTag.add(Bldg13Info);

        Marker Bldg17 = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.060036, -117.821363))
                .title("Bldg 17: Engineering Laboratories")
                .snippet("No further info available"));
        Bldg17.setTag("BUILDING 17");
        BuildingTag.add(Bldg17);

        Marker Bldg24 = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.056836, -117.822951))
                .title("Bldg 24: Music Building")
                .snippet("In the Cal Poly Pomona tradition of \"learn by doing,\" music majors are given a variety of hands-on experiences through internships in the music industry, field experiences in teaching, and master classes with renowned artists. Our general education classes, such as Introduction to Music, Music Appreciation and World Music, as well as our ensembles are open to all university students, regardless of major."));
        Bldg24.setTag("BUILDING 24");
        BuildingTag.add(Bldg24);

        Marker Bldg26 = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.056957, -117.820378))
                .title("Bldg 26: University Plaza")
                .snippet("University Union Plaza was the original site of the Kellogg Arabian horse stables until the W.K. Kellogg Arabian Horse Center was built in 1974. The building was remodeled and became an extension of the University Union and a center for student organizations in 1981."));
        Bldg26.setTag("BUILDING 26");
        BuildingTag.add(Bldg26);

        Marker Bldg26A = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.056717, -117.820551))
                .title("Bldg 26A: Student Orientation Center")
                .snippet("No further info available"));
        Bldg26A.setTag("BUILDING 26A");
        BuildingTag.add(Bldg26A);

        //// Helpful Facilities //////////////////////////////////////////////////////

        Marker BRICInfo = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.054657, -117.820761))
                .title("The BRIC")
                .snippet("University gym open to all students of the university. The facilities include various exercise machines, basketball courts, racketball courts, studio rooms, a rock-climbing wall, and a pool. Various events and classes are held here, from movie showings in the pool to instructional exercise classes in the studios.\n" +
                        "Hours: \n" +
                        "Monday -- Thursday\n" +
                        "6:00 am - 12:00 am\t\n" +
                        "Friday\n" +
                        "6:00 am - 11:00 pm\t\n" +
                        "Saturday -- Sunday\n" +
                        "9:00 am - 11:00 pm\t"));
        BRICInfo.setTag("ATHLETIC SOCIAL");
        AthleticTag.add(BRICInfo);
        SocialTag.add(BRICInfo);

        Marker GymnasiumInfo = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.054195, -117.819188))
                .title("CPP Kellogg Gymnasium")
                .snippet("Opened in 1967, the Kellogg Gymnasium is the primary home of the powerful Bronco (NCAA Division II) Basketball team, and seats 4,765."));
        GymnasiumInfo.setTag("ATHLETIC SOCIAL");
        AthleticTag.add(GymnasiumInfo);
        SocialTag.add(GymnasiumInfo);

        Marker IPolyInfo = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.051013, -117.819499))
                .title("iPoly High School Campus")
                .snippet("nternational Polytechnic High School (iPoly) is a specialized project-based school. It provides students with a comprehensive high school (9-12) curriculum, as well as with a technological, cultural, and global foundation. As citizens of the world, graduates of iPoly are prepared to apply their global knowledge and experience toward assuming leadership roles in the community, nation, and world"));
        IPolyInfo.setTag("RESOURCES");
        ResourcesTag.add(IPolyInfo);

        Marker BookstoreInfo = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.055987, -117.820445))
                .title("Bronco Bookstore")
                .snippet("On campus retailer that offers textbooks, school supplies, convenience items, clothing, and even technology. Hours vary throughout the year."));
        BookstoreInfo.setTag("RESOURCES MARKET");
        ResourcesTag.add(BookstoreInfo);
        MarketTag.add(BookstoreInfo);

        Marker BSCInfo = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.056549, -117.82146))
                .title("Bronco Student Center [BSC]")
                .snippet("Bronco Student Center. Home to on campus post office, dining, arcade, ATMs, and events. \n" +
                        "Hours:\n" +
                        "Monday - Friday 7:00 AM - 10:00 PM \n" +
                        "Saturday 8:00 AM - 4:00 PM\n" +
                        "Sunday: Closed\n"));
        BSCInfo.setTag("RESOURCES");
        ResourcesTag.add(BSCInfo);

        Marker TheatreInfo = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.056696, -117.822208))
                .title("CPP Theatre")
                .snippet("lays and musicals put on by students throughout the year! Tickets are usually $10 with a student ID and $15 for general admission. Check in with the theatre website to see what is currently playing and available showtimes."));
        TheatreInfo.setTag("ART SCENIC");
        ArtTag.add(TheatreInfo);
        ScenicTag.add(TheatreInfo);

        Marker LibraryInfo = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.057891, -117.82133))
                .title("CPP University Library")
                .snippet("The University Library, located in Building 15, is not only a place to type research papers, check out books, and study, but a place where you can grab a pick-me-up beverage or snack. Starbucks is located on the first floor of the University Library and can be accessed from inside or outside the building. Indoor and outdoor seating is available."));
        LibraryInfo.setTag("RESOURCES");
        ResourcesTag.add(LibraryInfo);

        Marker CLAInfo = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.059601, -117.820071))
                .title("CLA Building")
                .snippet("Clearly visible from the nearby freeways, the distinctly shaped triangular building is the most recognizable structure on the Cal Poly Pomona campus and widely considered a university icon. Antoine Predock, an Albuquerque-based architect, won an international competition to design the building, and completed its construction in 1992."));
        CLAInfo.setTag("RESOURCES");
        ResourcesTag.add(CLAInfo);

        Marker AlamitosInfo = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.062217, -117.818069))
                .title("Alamitos Hall")
                .snippet("The four red brick halls (Alamitos, Aliso, Encinitas and Montecito) have a similar design and layout. Each hall accommodates approximately 212 residents and staffed by five student Resident Advisors (RA) and a professional live-in Hall Coordinator. The Phase I halls feature a small community atmosphere, spacious lounges on the first two floors, and a common garden patio. Each floor is divided into two wings. The majority of floors are coed by alternate male and female wings. Accommodations are available in Encinitas Hall for students who have a mobility restriction. All halls have conveniently located laundry facilities. Each room measures approximately 11' x 15'."));
        AlamitosInfo.setTag("HOUSING");
        HousingTag.add(AlamitosInfo);

        Marker MontecitoInfo = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.062177, -117.81933))
                .title("Montecito Hall")
                .snippet("The four red brick halls (Alamitos, Aliso, Encinitas and Montecito) have a similar design and layout. Each hall accommodates approximately 212 residents and staffed by five student Resident Advisors (RA) and a professional live-in Hall Coordinator. The Phase I halls feature a small community atmosphere, spacious lounges on the first two floors, and a common garden patio. Each floor is divided into two wings. The majority of floors are coed by alternate male and female wings. Accommodations are available in Encinitas Hall for students who have a mobility restriction. All halls have conveniently located laundry facilities. Each room measures approximately 11' x 15'."));
        MontecitoInfo.setTag("HOUSING");
        HousingTag.add(MontecitoInfo);

        Marker AlisoInfo = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.0629, -117.817805))
                .title("Aliso Hall")
                .snippet("The four red brick halls (Alamitos, Aliso, Encinitas and Montecito) have a similar design and layout. Each hall accommodates approximately 212 residents and staffed by five student Resident Advisors (RA) and a professional live-in Hall Coordinator. The Phase I halls feature a small community atmosphere, spacious lounges on the first two floors, and a common garden patio. Each floor is divided into two wings. The majority of floors are coed by alternate male and female wings. Accommodations are available in Encinitas Hall for students who have a mobility restriction. All halls have conveniently located laundry facilities. Each room measures approximately 11' x 15'."));
        AlisoInfo.setTag("HOUSING");
        HousingTag.add(AlisoInfo);

        Marker EncinitasInfo = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.062315, -117.820574))
                .title("Encinitas Hall")
                .snippet("The four red brick halls (Alamitos, Aliso, Encinitas and Montecito) have a similar design and layout. Each hall accommodates approximately 212 residents and staffed by five student Resident Advisors (RA) and a professional live-in Hall Coordinator. The Phase I halls feature a small community atmosphere, spacious lounges on the first two floors, and a common garden patio. Each floor is divided into two wings. The majority of floors are coed by alternate male and female wings. Accommodations are available in Encinitas Hall for students who have a mobility restriction. All halls have conveniently located laundry facilities. Each room measures approximately 11' x 15'."));
        EncinitasInfo.setTag("HOUSING");
        HousingTag.add(EncinitasInfo);

        Marker CedritosInfo = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.061527, -117.821217))
                .title("Cedritos Hall")
                .snippet("Both Cedritos and Palmitas Halls have a similar design and layout. Each hall accommodates approximately 185 students and staffed by five student Resident Advisors (RA) and a professional live-in Hall Coordinator. These halls offer the same small community atmosphere and amenities as the Phase One buildings. The only differences are the room shapes and the slightly smaller common area. All halls have conveniently located laundry facilities. Each room measures approximately 12' x 14'."));
        CedritosInfo.setTag("HOUSING");
        HousingTag.add(CedritosInfo);

        Marker PalmitasInfo = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.060547, -117.82233))
                .title("Palmitas Hall")
                .snippet("Both Cedritos and Palmitas Halls have a similar design and layout. Each hall accommodates approximately 185 students and staffed by five student Resident Advisors (RA) and a professional live-in Hall Coordinator. These halls offer the same small community atmosphere and amenities as the Phase One buildings. The only differences are the room shapes and the slightly smaller common area. All halls have conveniently located laundry facilities. Each room measures approximately 12' x 14'."));
        PalmitasInfo.setTag("HOUSING");
        HousingTag.add(PalmitasInfo);

        Marker LaCienegaInfo = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.060956, -117.821894))
                .title("La Cienega Center")
                .snippet("University Housing Services"));
        LaCienegaInfo.setTag("RESOUCES");
        ResourcesTag.add(LaCienegaInfo);

        Marker EstrellasInfo = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.055368, -117.818887))
                .title("Vista de las Estrellas")
                .snippet("One of the five student residential suites. Estrellas, for the most part, is home to freshman students only. Furthermore, residents of the suite can retrieve mailed packages at the front desk in Estrellas. All suite residents have access to this building via key card. Contains a study hall, balcony overlook, and public laundry room.\n" +
                        "Hours:\n" +
                        "Front Desk: 8:00 AM - 7:00 PM\n"));
        EstrellasInfo.setTag("HOUSING");
        HousingTag.add(EstrellasInfo);

        Marker BonitaInfo = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.054808, -117.818608))
                .title("Vista de las Bonita")
                .snippet("One of the five student residential suites. Only residents of this suite have access to this building via key card. Contains a study hall, balcony overlook, and public laundry room."));
        BonitaInfo.setTag("HOUSING");
        HousingTag.add(BonitaInfo);

        Marker SolInfo = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.054819, -117.818088))
                .title("Vista del Sol")
                .snippet("One of the five student residential suites. Only residents of this suite have access to this building via key card. Contains a study hall, balcony overlook, and public laundry room."));
        SolInfo.setTag("HOUSING");
        HousingTag.add(SolInfo);

        Marker MontanasInfo = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.054275, -117.817637))
                .title("Vista del Montanas")
                .snippet("One of the five student residential suites. Only residents of this suite have access to this building via key card. Contains a study hall, balcony overlook, and public laundry room."));
        MontanasInfo.setTag("HOUSING");
        HousingTag.add(MontanasInfo);

        Marker LunaInfo = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.053659, -117.817095))
                .title("Vista de las Luna")
                .snippet("One of the five student residential suites. Only residents of this suite have access to this building via key card. Contains a study hall, balcony overlook, and public laundry room."));
        LunaInfo.setTag("HOUSING");
        HousingTag.add(LunaInfo);

        Marker RoseFloatInfo = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.060167, -117.808230))
                .title("Rose Float Laboratory")
                .snippet("Come visit the Rose Float Lab and see what students are up to!"));
        RoseFloatInfo.setTag("SCENIC");
        ScenicTag.add(RoseFloatInfo);

        Marker PumpkinsInfo = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.049552, -117.818519))
                .title("Agriscapes Pumpkin Field")
                .snippet("Home to many local tourist attractions, such as the CPP Egg Hunt and the Halloween Pumpkin Patch"));
        PumpkinsInfo.setTag("SCENIC SOCIAL");
        ScenicTag.add(PumpkinsInfo);

        //// Parking Lots ////////////////////////////////////////////

        Marker LotA = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.060614, -117.824645))
                .title("Parking Lot A")
                .snippet(""));
        LotA.setTag("PARKING");
        ParkingTag.add(LotA);

        Marker LotB = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.052753, -117.815283))
                .title("Parking Lot B")
                .snippet(""));
        LotB.setTag("PARKING");
        ParkingTag.add(LotB);

        Marker LotE1 = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.061486, -117.811566))
                .title("Parking Lot E1")
                .snippet(""));
        LotE1.setTag("PARKING");
        ParkingTag.add(LotE1);

        Marker LotE2 = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.060748, -117.812650))
                .title("Parking Lot E2")
                .snippet(""));
        LotE2.setTag("PARKING");
        ParkingTag.add(LotE2);

        Marker LotF1 = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.062263, -117.816958))
                .title("Parking Lot F1")
                .snippet(""));
        LotF1.setTag("PARKING");
        ParkingTag.add(LotF1);

        Marker LotF2 = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.061494, -117.817693))
                .title("Parking Lot F2")
                .snippet(""));
        LotF2.setTag("PARKING");
        ParkingTag.add(LotF2);

        Marker LotF3 = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.062045, -117.816325))
                .title("Parking Lot F3")
                .snippet(""));
        LotF3.setTag("PARKING");
        ParkingTag.add(LotF3);

        Marker LotF4 = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.061050, -117.817350))
                .title("Parking Lot F4")
                .snippet(""));
        LotF4.setTag("PARKING");
        ParkingTag.add(LotF4);

        Marker LotF5 = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.061579, -117.815456))
                .title("Parking Lot F5")
                .snippet(""));
        LotF5.setTag("PARKING");
        ParkingTag.add(LotF5);

        Marker LotF8 = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.059135, -117.817087))
                .title("Parking Lot F8")
                .snippet(""));
        LotF8.setTag("PARKING");
        ParkingTag.add(LotF8);

        Marker LotF9 = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.060099, -117.815472))
                .title("Parking Lot F9")
                .snippet(""));
        LotF9.setTag("PARKING");
        ParkingTag.add(LotF9);

        Marker LotF10 = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.061059, -117.814522))
                .title("Parking Lot F10")
                .snippet(""));
        LotF10.setTag("PARKING");
        ParkingTag.add(LotF10);

        Marker LotG = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.055620, -117.819694))
                .title("Parking Lot G")
                .snippet(""));
        LotG.setTag("PARKING");
        ParkingTag.add(LotG);

        Marker LotH = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.060928, -117.818784))
                .title("Parking Lot H")
                .snippet(""));
        LotH.setTag("PARKING");
        ParkingTag.add(LotH);

        Marker LotI = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.048855, -117.819519))
                .title("Parking Lot I")
                .snippet(""));
        LotI.setTag("PARKING");
        ParkingTag.add(LotI);

        Marker LotJ = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.057808, -117.828936))
                .title("Parking Lot J")
                .snippet(""));
        LotJ.setTag("PARKING");
        ParkingTag.add(LotJ);

        Marker LotL = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.055510, -117.825479))
                .title("Parking Lot L")
                .snippet(""));
        LotL.setTag("PARKING");
        ParkingTag.add(LotL);

        Marker LotM = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.055452, -117.829687))
                .title("Parking Lot M")
                .snippet(""));
        LotM.setTag("PARKING");
        ParkingTag.add(LotM);

        Marker LotN = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.056200, -117.818885))
                .title("Parking Lot N")
                .snippet(""));
        LotN.setTag("PARKING");
        ParkingTag.add(LotN);

        Marker LotO = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.050022, -117.814439))
                .title("Parking Lot O")
                .snippet(""));
        LotO.setTag("PARKING");
        ParkingTag.add(LotO);

        Marker LotP = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.054486, -117.815519))
                .title("Parking Lot P")
                .snippet(""));
        LotP.setTag("PARKING");
        ParkingTag.add(LotP);

        Marker LotQ = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.054939, -117.817))
                .title("Parking Lot Q")
                .snippet(""));
        LotQ.setTag("PARKING");
        ParkingTag.add(LotQ);

        Marker LotR = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.063572, -117.825608))
                .title("Parking Lot R")
                .snippet(""));
        LotR.setTag("PARKING");
        ParkingTag.add(LotR);

        Marker LotU = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.048754, -117.817544))
                .title("Parking Lot U")
                .snippet(""));
        LotU.setTag("PARKING");
        ParkingTag.add(LotU);

        Marker LotPatient = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.057453, -117.828142))
                .title("Health Center Patient Parking")
                .snippet(""));
        LotPatient.setTag("PARKING");
        ParkingTag.add(LotPatient);

        Marker LotOverflow = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.053262, -117.809458))
                .title("Overflow Parking Lot")
                .snippet(""));
        LotOverflow.setTag("PARKING");
        ParkingTag.add(LotOverflow);

        Marker LotStructure1 = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.060265, -117.816936))
                .title("Parking Structure 1")
                .snippet(""));
        LotStructure1.setTag("PARKING");
        ParkingTag.add(LotStructure1);

        Marker LotStructure2 = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(34.051848, -117.819712))
                .title("Parking Structure 2")
                .snippet(""));
        LotStructure2.setTag("PARKING");
        ParkingTag.add(LotStructure2);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("CONNECTION FAILED", "you suck");
    }

    private class ClickForDirectionsListener implements GoogleMap.OnInfoWindowClickListener {
        @Override
        public void onInfoWindowClick(Marker marker) {
            if(!directionsActive) {
                // Set Destination
                dest = marker;
                final String ssid = marker.getTitle();

                Toast.makeText(getBaseContext(), "Destination set to " + ssid, Toast.LENGTH_SHORT).show();

                // Clear Map
                googleMap.clear();

                // Set Marker
                LatLng endPoint = dest.getPosition();
                Marker destination = googleMap.addMarker(new MarkerOptions()
                        .position(endPoint)
                        .title(ssid)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                // Calculate Route
                LatLng user = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                String url = getUrl(user, endPoint);
                Log.d("onMapClick", url.toString());
                FetchUrl fetchUrl = new FetchUrl();

                fetchUrl.execute(url);
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(user));
                directionsActive = true;
            }
            else {
                googleMap.clear();
                addMarkers();
                directionsActive = false;
            }
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

    private String getUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Mode
        String mode = "mode=walking";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask",jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask","Executing routes");
                Log.d("ParserTask",routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask",e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.RED);

                Log.d("onPostExecute","onPostExecute lineoptions decoded");

            }

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                googleMap.addPolyline(lineOptions);
            }
            else {
                Log.d("onPostExecute","without Polylines drawn");
            }
        }
    }
}