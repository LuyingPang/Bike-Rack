package com.example.bikessecure;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class DashboardActivity extends AppCompatActivity {
    private static final String TAG = "DashboardActivity";
    private static final int PERMISSIONS_REQUEST = 1;
    public static final String REQUEST = "com.example.bikessecure.REQUEST";

    private AlertDialog.Builder alertDialogBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        /* get extras from intent */
        Bundle extras = getIntent().getExtras();

        /* alert dialog */
        alertDialogBuilder = new AlertDialog.Builder(this);

        if (extras != null) {
            String response_message = extras.getCharSequence(QRScannerActivity.RESPONSE_MESSAGE).toString();
            alertDialogBuilder.setMessage(response_message).setCancelable(true)
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }

        Authentication.signIn("username", "Password123");
//        Authentication.signOut();

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