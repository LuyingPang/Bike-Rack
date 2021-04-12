package com.example.bikessecure;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.amplifyframework.api.rest.RestOptions;
import com.amplifyframework.core.Amplify;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = MapsActivity.class.getSimpleName();
    private CameraPosition cameraPosition;
    private GoogleMap mMap;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient fusedLocationProviderClient;

    // A default location (Singapore) and default zoom to use when location permission is
    // not granted.
    private final LatLng defaultLocation = new LatLng(1.3521, 103.8198);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location lastKnownLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
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
        // add and move the map's camera to Singapore location.
        this.mMap = googleMap;

        // add locations of donation places
        RestOptions options = RestOptions.builder()
                .addPath("/getstage")
                .build();

        Amplify.API.get(options,
                restResponse -> {
                    final String rackinfo = restResponse.getData().asString();
//                    copy_rackinfo += rackinfo;
                    Log.i("MyAmplifyApp", "GET succeeded: " + rackinfo);
                    final ArrayList<String> name_location = new ArrayList<String>(); // Create an ArrayList object for location name
                    final ArrayList<String> avail_location = new ArrayList<String>(); // Create an ArrayList object for locations
                    try {
                        // get JSONObject from json string
                        JSONObject rackjson = new JSONObject(rackinfo);
                        System.out.println("rackjson: "+rackjson);
                        String racks = rackjson.getString("Items");
                        System.out.println(racks);
                        JSONArray rackitems = new JSONArray(racks);
                        for(int i=0; i < rackitems.length(); i++) {
                            JSONObject jsonobject = rackitems.getJSONObject(i);
                            int freestand = jsonobject.getInt("Free Stands");
                            String rackloc    = jsonobject.getString("Location");
                            String rackid = jsonobject.getString("Rack ID");
                            System.out.println(i+" Free stand: "+freestand+"\track location: "+rackloc+"rackid"+rackid);
                            if (freestand>0) {
                                name_location.add(rackid);
                                avail_location.add(rackloc);
                            }
                        }
                        for (int i=0;i<avail_location.size();i++){
                            String[] rloc = avail_location.get(i).split(",");
                            float lat = Float.parseFloat(rloc[0]);
                            float lng = Float.parseFloat(rloc[1]);
                            System.out.println(lat+lng);
                            LatLng rackpos = new LatLng(lat,lng);
                            System.out.println(rackpos+"--------");
                            String addressname = name_location.get(i);
                            System.out.println(addressname);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mMap.addMarker(new MarkerOptions().position(rackpos).title(addressname));
                                }
                            });

                        }

                    } catch (JSONException e){
                        e.printStackTrace();
                    }
                },
                apiFailure -> Log.e("MyAmplifyApp", "GET failed.", apiFailure)
        );

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        getDeviceLocation();

    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {// check gps on/off
            final LocationManager manager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE );

            if (locationPermissionGranted && manager.isProviderEnabled( LocationManager.GPS_PROVIDER )) {
                Toast.makeText(getApplicationContext(), "GPS is Enabled!", Toast.LENGTH_LONG).show();
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {

                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, 10));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
            else{
                if (locationPermissionGranted) {
                    gpsChecker(manager);
                }
                Log.d(TAG, "Using defaults cuz gps/location permission not allowed");
                mMap.moveCamera(CameraUpdateFactory
                        .newLatLngZoom(defaultLocation, 10));
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;

                    final LocationManager manager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE );
                    gpsChecker(manager);
                }
            }
        }
        updateLocationUI();
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
//                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void gpsChecker(LocationManager manager){

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            Toast.makeText(getApplicationContext(), "GPS is disable! Please enable it.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }
}

