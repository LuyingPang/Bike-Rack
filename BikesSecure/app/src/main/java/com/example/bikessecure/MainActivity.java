package com.example.bikessecure;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    public static final String REQUEST = "com.example.bikessecure.REQUEST";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Authentication.signIn("username", "Password123");
//        Authentication.signOut();

    }

    /* simple buttons to switch activities */
    public void mapButton(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    public void qrButton(View view) {
        Intent intent = new Intent(this, QRScannerActivity.class);
        intent.putExtra(REQUEST, "lock");
        startActivity(intent);
    }

}