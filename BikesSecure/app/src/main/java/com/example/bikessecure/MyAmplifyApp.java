package com.example.bikessecure;

import android.util.Log;

import androidx.multidex.MultiDexApplication;

import com.amplifyframework.AmplifyException;
import com.amplifyframework.core.Amplify;

public class MyAmplifyApp extends MultiDexApplication {
    public void onCreate() {
        super.onCreate();

        try {
            Amplify.configure(getApplicationContext());
            Log.i("MyAmplifyApp", "Initialized Amplify");
        } catch (AmplifyException error) {
            Log.e("MyAmplifyApp", "Could not initialize Amplify", error);
        }
    }
}
