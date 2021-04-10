package com.example.bikessecure;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.util.Log;

import com.amplifyframework.api.rest.RestOptions;
import com.amplifyframework.core.Amplify;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

//    public ArrayList<String> name_location = new ArrayList<String>(); // Create an ArrayList object for location name
//    public ArrayList<String> avail_location = new ArrayList<String>(); // Create an ArrayList object for locations
//    String rackinfo = "";
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
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
        LatLng singa = new LatLng(1.3521, 103.8198);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(singa, 10.0f));
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
        System.out.println("is thus last?");
    }
}

