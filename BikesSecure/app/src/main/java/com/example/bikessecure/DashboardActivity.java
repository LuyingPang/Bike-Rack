package com.example.bikessecure;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.bikessecure.databinding.ActivityDashboardBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public class DashboardActivity extends AppCompatActivity {
    private static final String TAG = DashboardActivity.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST = 1;
    public static final String REQUEST = "com.example.bikessecure.REQUEST";

    private ActivityDashboardBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // inflate first so I can change it
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());

        /* check preferences and switch before showing dashboard */
        SharedPreferences sharedPref = getSharedPreferences(
                getString(R.string.sharedpref_user_state), Context.MODE_PRIVATE);
        Log.i(TAG, sharedPref.getAll().toString());
        HashSet<String> user_state = (HashSet<String>) sharedPref.getStringSet(
                Authentication.getUserSub(), null);

        if (user_state != null && user_state.contains("lock")) {
            Log.i(TAG, "state of " + Authentication.getUserSub()
                    + ": " + user_state.toString());
            binding.cardLock.setVisibility(View.INVISIBLE);
            HashSet<String> user_state_copy = new HashSet<>(user_state);
            user_state_copy.remove("lock");
            String card_text_unlock = getString(R.string.card_text_unlock);
            binding.textUnlock.setText(card_text_unlock.replace("[here]", user_state_copy.toString()));
            binding.cardUnlock.setVisibility(View.VISIBLE);
        }
        else {
            if (user_state != null)
                Log.i(TAG, "state of " + Authentication.getUserSub()
                        + ": " + user_state.toString());
            binding.cardUnlock.setVisibility(View.INVISIBLE);
            binding.cardLock.setVisibility(View.VISIBLE);
        }

        setContentView(binding.getRoot());


        /* get extras from intent */
        Bundle extras = getIntent().getExtras();
        /* alert dialog */
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // get methods return null if object is not present
        //  -> useful to check who sent the intent
        if (extras != null) {
            String response_message = extras.getCharSequence(QRScannerActivity.RESPONSE_MESSAGE).toString();
            if (!response_message.equals("")) { // check for null response
                alertDialogBuilder.setMessage(response_message).setCancelable(true)
                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        }

        Authentication.signIn("username", "Password123");
//        Authentication.signOut();

        // TODO: only run this when sign in happens
        /* check that all permissions required are given */
//        if (!allPermissionsGranted()) {
//            getRuntimePermissions();
//        }

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

    /* permissions checker code */
    /**
     * gets required permissions (getRequiredPermissions) and checks
     *  if each permission is granted (isPermissionGranted)
     */
    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     *  gets required permissions (getRequiredPermissions) and requests them
     */
    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), PERMISSIONS_REQUEST);
        }
    }

    private String[] getRequiredPermissions() {
        try {
            PackageInfo info =
                    this.getPackageManager()
                            .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted: " + permission);
            return true;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return false;
    }

    /**
     * handles permission request results called by getRuntimePermissions()
     *  needs Activity to implement OnRequestPermissionsResultCallback (?)
     */
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        if (allPermissionsGranted()) {
            Log.i(TAG, "Permissions all granted!");
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}