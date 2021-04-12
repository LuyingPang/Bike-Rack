package com.example.bikessecure;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;


public class DashboardActivity extends AppCompatActivity {
    private static final String TAG = "DashboardActivity";
    public static final String REQUEST = "com.example.bikessecure.REQUEST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Authentication.signIn("username", "Password123");
//        Authentication.signOut();

    }

    /* attached to onClick to switch activities */
    public void toMapsActivity(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    public void toQRScannerActivity(View view) {
        Intent intent = new Intent(this, QRScannerActivity.class);
        intent.putExtra(REQUEST, (String) view.getTag());
        startActivity(intent);
    }

}