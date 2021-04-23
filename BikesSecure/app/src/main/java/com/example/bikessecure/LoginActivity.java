package com.example.bikessecure;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amplifyframework.auth.AuthUserAttributeKey;
import com.amplifyframework.auth.options.AuthSignUpOptions;
import com.amplifyframework.core.Amplify;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final String signInSuccessfulToast = "Sign in succeeded";
    private static final String signInFailedToast = "Sign in not complete";
    public static final String LOGIN = "com.example.bikessecure.LOGIN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }


    public void loginButton (View view) {
        // grab text
        EditText editTextUsername = (EditText) findViewById(R.id.editText_username);
        String username = editTextUsername.getText().toString();
        EditText editTextPassword = (EditText) findViewById(R.id.editText_password);
        String password = editTextPassword.getText().toString();

        // sign in
        // wrote a separate method because code also used for confirmcodeButton
        signIn(username, password);
    }

    private void signIn(String username, String password) {
        Amplify.Auth.signIn( username, password,
                result -> {
                    if (result.isSignInComplete()) {
                        Log.i(TAG+"/signIn", signInSuccessfulToast);
                        runOnUiThread(() -> Toast.makeText(getApplicationContext(), signInSuccessfulToast, Toast.LENGTH_SHORT).show());
                        Intent intent = new Intent(this, DashboardActivity.class);
                        intent.putExtra(LOGIN, "login");
                        startActivity(intent);
                    }
                    else {
                        Log.i(TAG+"/signIn", signInFailedToast);
                        runOnUiThread(() -> Toast.makeText(getApplicationContext(), signInFailedToast, Toast.LENGTH_SHORT).show());
                    }
                },
                error -> {
                    Log.e(TAG+"/signIn", error.toString());
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), error.getLocalizedMessage(), Toast.LENGTH_SHORT).show());
                }
        );
    }


    public void signupButton (View view) {
        // grab text
        EditText editTextUsername = (EditText) findViewById(R.id.editText_username);
        String username = editTextUsername.getText().toString();
        EditText editTextPassword = (EditText) findViewById(R.id.editText_password);
        String password = editTextPassword.getText().toString();
        EditText editTextEmail = (EditText) findViewById(R.id.editText_email);
        String email = editTextEmail.getText().toString();

        AuthSignUpOptions options = AuthSignUpOptions.builder()
                .userAttribute(AuthUserAttributeKey.email(), email)
                .build();
        Amplify.Auth.signUp(username, password, options,
                result -> Log.i(TAG+"/signUp", "Result: " + result.toString()),
                error -> Log.e(TAG+"/signUp", "Sign up failed", error)
        );
        // check email for the code

        // enable the confirmation code UI
        EditText editTextCode = (EditText) findViewById(R.id.editText_code);
        if (editTextCode != null)
            editTextCode.setVisibility(View.VISIBLE);
        Button buttonConfirmCode = (Button) findViewById(R.id.button_confirmcode);
        if (buttonConfirmCode != null)
            buttonConfirmCode.setVisibility(View.VISIBLE);
    }


    public void confirmcodeButton (View view) {
        // grab text
        EditText editTextUsername = (EditText) findViewById(R.id.editText_username);
        String username = editTextUsername.getText().toString();
        EditText editTextCode = (EditText) findViewById(R.id.editText_code);
        String code = editTextCode.getText().toString();

        Amplify.Auth.confirmSignUp( username, code,
                result -> {
                    if (result.isSignUpComplete()) {
                        Log.i(TAG+"/confirm",  "Confirm sign up succeeded");
                        EditText editTextPassword = (EditText) findViewById(R.id.editText_password);
                        String password = editTextPassword.getText().toString();
                        signIn(username, password);
                    }
                    else
                        Log.i(TAG+"/confirm", "Confirm sign up not complete");
                },
                error -> Log.e(TAG+"/confirm", error.toString())
        );

    }


    public static String getUserSub () {
//        Log.i(TAG+"/UserSub", Amplify.Auth.getCurrentUser().getUserId());
        return Amplify.Auth.getCurrentUser().getUserId();
    }


    /* Methods not in use, but keeping code in case we need it */

    /*public static void signOut () {
        Amplify.Auth.signOut(
                () -> Log.i(TAG+"/signOut", "Signed out successfully"),
                error -> Log.e(TAG+"/signOut", error.toString())
        );
    }*/


    /*private static void getTokens () {
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
    }*/

}