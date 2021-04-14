package com.example.bikessecure;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.amplifyframework.core.Amplify;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final String signInSuccessfulToast = "Sign in succeeded";
    private static final String signInFailedToast = "Sign in not complete";
    private Context forToast;
    public static final String LOGIN = "com.example.bikessecure.LOGIN";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        forToast = this;
    }

    public void loginButton (View view) {

        // grab text
        EditText editTextUsername = (EditText) findViewById(R.id.editText_username);
        String username = editTextUsername.getText().toString();
        EditText editTextPassword = (EditText) findViewById(R.id.editText_password);
        String password = editTextPassword.getText().toString();

        // sign in
        Amplify.Auth.signIn( username, password,
                result -> {
                    if (result.isSignInComplete()) {
                        Log.i(TAG, signInSuccessfulToast);
                        runOnUiThread(() -> Toast.makeText(getApplicationContext(), signInSuccessfulToast, Toast.LENGTH_SHORT).show());
                        Intent intent = new Intent(this, DashboardActivity.class);
                        intent.putExtra(LOGIN, "login");
                        startActivity(intent);
                    }
                    else {
                        Log.i(TAG, signInFailedToast);
                        runOnUiThread(() -> Toast.makeText(getApplicationContext(), signInFailedToast, Toast.LENGTH_SHORT).show());
                    }
                },
                error -> {
                    Log.e(TAG, error.toString());
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), error.getLocalizedMessage(), Toast.LENGTH_SHORT).show());
                }
        );
    }

}