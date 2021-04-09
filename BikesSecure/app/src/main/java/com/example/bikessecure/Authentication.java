package com.example.bikessecure;

import android.util.Log;

import com.amplifyframework.auth.AuthUserAttributeKey;
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession;
import com.amplifyframework.auth.options.AuthSignUpOptions;
import com.amplifyframework.core.Amplify;

public class Authentication {
    private static final String testUsername = "username";
    private static final String testPassword = "Password123";

    public static void signIn (String username, String password) {
        Amplify.Auth.signIn( username, password,
                result -> {
                    Log.i("Authentication/signIn", result.isSignInComplete() ? "Sign in succeeded" : "Sign in not complete");
                },
                error -> Log.e("Authentication/signIn", error.toString())
        );
    }

    public static void signUp (String username, String password, String email) {
        AuthSignUpOptions options = AuthSignUpOptions.builder()
                .userAttribute(AuthUserAttributeKey.email(), email)
                .build();
        Amplify.Auth.signUp(username, password, options,
                result -> Log.i("Authentication/signUp", "Result: " + result.toString()),
                error -> Log.e("Authentication/signUp", "Sign up failed", error)
        );
    }

    public static void confirmSignUp (String username, String confirmationCode) {
        Amplify.Auth.confirmSignUp( username, confirmationCode,
                result -> Log.i("Authentication/confirm", result.isSignUpComplete() ? "Confirm sign up succeeded" : "Confirm sign up not complete"),
                error -> Log.e("Authentication/confirm", error.toString())
        );
    }

    public static void signOut () {
        Amplify.Auth.signOut(
                () -> Log.i("Authentication/signOut", "Signed out successfully"),
                error -> Log.e("Authentication/signOut", error.toString())
        );
    }

    /* Method not in use, but keeping code in case we need it */
    private static void getTokens () {
        Amplify.Auth.fetchAuthSession(
                result -> {
                    AWSCognitoAuthSession cognitoAuthSession = (AWSCognitoAuthSession) result;
                    switch(cognitoAuthSession.getUserPoolTokens().getType()) {
                        case SUCCESS:
                            Log.i("AuthQuickStart", "ID Token: " + cognitoAuthSession.getUserPoolTokens().getValue().getIdToken());
//                            String idToken = cognitoAuthSession.getUserPoolTokens().getValue().getIdToken();
                            break;
                        case FAILURE:
                            Log.i("AuthQuickStart", "UserPoolTokens not present because: " + cognitoAuthSession.getUserPoolTokens().getError());
                    }
                },
                error -> Log.e("AuthQuickStart", error.toString())
        );
    }

}
