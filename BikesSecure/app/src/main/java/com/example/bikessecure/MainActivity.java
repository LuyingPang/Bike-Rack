package com.example.bikessecure;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.amplifyframework.api.rest.RestOptions;
import com.amplifyframework.core.Amplify;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Authentication.signIn("username", "Password123");

        Authentication.signOut();

    }

    /* simple button created to refresh using intents */
    public void mapButton(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    /* template POST request code, not sure where to put it yet */
    void postRequest() {
        // tested: needs to be signed in to use the rest API
        RestOptions options = RestOptions.builder()
                .addBody("{\"Stand ID\":\"stand 1\",\"Rack ID\":\"rack 1\",\"Request\":\"unlock\"}".getBytes())
                .build();

        Amplify.API.post(options,
                restResponse -> Log.i("AuthRestAPI", "POST succeeded: " + restResponse.getData().asString()),
                apiFailure -> Log.e("AuthRestAPI", "POST failed.", apiFailure)
        );
    }

}