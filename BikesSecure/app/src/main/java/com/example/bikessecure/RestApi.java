package com.example.bikessecure;

import android.util.Log;

import com.amplifyframework.api.rest.RestOptions;
import com.amplifyframework.core.Amplify;

public class RestApi {

    private static final String TAG = "RestApi";

    public static void postRequest(String standID, String rackID, String request) {

        String jsonFormat = String.format("{\"Stand ID\":\"%s\",\"Rack ID\":\"%s\",\"Request\":\"%s\",\"Password\":\"\"}",
                                            standID, rackID, request);

        RestOptions options = RestOptions.builder()
                .addPath("/bikestage")
                .addBody(jsonFormat.getBytes())
                .build();

        Amplify.API.post(options,
                restResponse -> {
                    Log.i(TAG+"/postRequest", "POST succeeded: " + restResponse.getData().asString());
                },
                apiFailure -> Log.e(TAG+"/postRequest", "POST failed.", apiFailure)
        );
    }

    public static void postRequest(String standID, String rackID, String request, String password) {

        String jsonFormat = String.format("{\"Stand ID\":\"%s\",\"Rack ID\":\"%s\",\"Request\":\"%s\",\"Password\":\"%s\"}",
                standID, rackID, request, password);

        RestOptions options = RestOptions.builder()
                .addPath("/bikestage")
                .addBody(jsonFormat.getBytes())
                .build();

        Amplify.API.post(options,
                restResponse -> {
                    Log.i(TAG+"/postRequest", "POST succeeded: " + restResponse.getData().asString());
                },
                apiFailure -> Log.e(TAG+"/postRequest", "POST failed.", apiFailure)
        );
    }


}
