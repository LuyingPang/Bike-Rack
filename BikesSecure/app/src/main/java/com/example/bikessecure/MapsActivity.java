package com.example.bikessecure;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

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
        LatLng singa = new LatLng(1.3521, 103.8198);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(singa));
        // add locations of donation places
        LatLng PraiseHaven = new LatLng(1.362390, 103.768227);
        LatLng TanglinFamHub = new LatLng(1.294710, 103.815086);
        LatLng Hopectr = new LatLng(1.353750, 103.964729);
        LatLng SalvationArmyHQ = new LatLng(1.360970, 103.842537);
        LatLng TheHaven = new LatLng(1.290360, 103.775960);
        LatLng IMMopenCP = new LatLng(1.349014, 103.743146);
        LatLng MandaiStation = new LatLng(1.408700, 103.755997);
        LatLng GraceHaven = new LatLng(1.371720, 103.877190);
        // Add a marker in different places for donation with the salvation army
        googleMap.addMarker(new MarkerOptions().position(PraiseHaven).title("Donate here at Praisehaven"));
        googleMap.addMarker(new MarkerOptions().position(TanglinFamHub).title("Donate here at Tanglin Family Hub"));
        googleMap.addMarker(new MarkerOptions().position(Hopectr).title("Donate here at Hope Centre"));
        googleMap.addMarker(new MarkerOptions().position(SalvationArmyHQ).title("Donate here at The Salvation Army HQ"));
        googleMap.addMarker(new MarkerOptions().position(TheHaven).title("Donate here at The Haven"));
        googleMap.addMarker(new MarkerOptions().position(IMMopenCP).title("Donate here at IMM Open Carpark"));
        googleMap.addMarker(new MarkerOptions().position(MandaiStation).title("Donate here at Mandai Station"));
        googleMap.addMarker(new MarkerOptions().position(GraceHaven).title("Donate here at Gracehaven"));


    }
}