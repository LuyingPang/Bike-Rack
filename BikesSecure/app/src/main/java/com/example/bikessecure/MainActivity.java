package com.example.bikessecure;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.amplifyframework.api.rest.RestOptions;
import com.amplifyframework.auth.AuthUserAttributeKey;
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession;
import com.amplifyframework.auth.options.AuthSignUpOptions;
import com.amplifyframework.core.Amplify;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Register
//        AuthSignUpOptions options = AuthSignUpOptions.builder()
//                .userAttribute(AuthUserAttributeKey.email(), "1002820@mymail.sutd.edu.sg")
//                .build();
//        Amplify.Auth.signUp("gabriel", "Password123", options,
//                result -> Log.i("AuthQuickstart", "Result: " + result.toString()),
//                error -> Log.e("AuthQuickstart", "Sign up failed", error)
//        );

        // confirmation code
//        Amplify.Auth.confirmSignUp(
//                "gabriel",
//                "668952",
//                result -> Log.i("AuthQuickstart", result.isSignUpComplete() ? "Confirm signUp succeeded" : "Confirm sign up not complete"),
//                error -> Log.e("AuthQuickstart", error.toString())
//        );

        // sign in
        Amplify.Auth.signIn(
                "username",
                "Password123",
                result -> Log.i("AuthQuickstart", result.isSignInComplete() ? "Sign in succeeded" : "Sign in not complete"),
                error -> Log.e("AuthQuickstart", error.toString())
        );

        // identityId
        Amplify.Auth.fetchAuthSession(
                result -> {
                    AWSCognitoAuthSession cognitoAuthSession = (AWSCognitoAuthSession) result;
                    switch(cognitoAuthSession.getUserPoolTokens().getType()) {
                        case SUCCESS:
                            Log.i("AuthQuickStart", "ID Token: " + cognitoAuthSession.getUserPoolTokens().getValue().getIdToken());
                            String idToken = cognitoAuthSession.getUserPoolTokens().getValue().getIdToken();
                            RestOptions options = RestOptions.builder()
                                    .addHeader("Authorization", idToken)
                                    .addBody("{\"Stand ID\":\"stand 1\",\"Rack ID\":\"rack 1\",\"request\":\"lock\"}".getBytes())
                                    .build();

                            Amplify.API.post(options,
                                    restResponse -> Log.i("AuthRestAPI", "POST succeeded: " + restResponse.getData().asString()),
                                    apiFailure -> Log.e("AuthRestAPI", "POST failed.", apiFailure)
                            );
                            break;
                        case FAILURE:
                            Log.i("AuthQuickStart", "UserPoolTokens not present because: " + cognitoAuthSession.getUserPoolTokens().getError().toString());
                    }
                },
                error -> Log.e("AuthQuickStart", error.toString())
        );
    }

    public void mapButton(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }
}