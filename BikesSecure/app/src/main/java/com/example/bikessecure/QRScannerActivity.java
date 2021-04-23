package com.example.bikessecure;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.amplifyframework.api.rest.RestOptions;
import com.amplifyframework.core.Amplify;
import com.example.bikessecure.qrscanner.BarcodeScannerProcessor;
import com.example.bikessecure.qrscanner.CameraXViewModel;
import com.example.bikessecure.qrscanner.ExchangeScannedData;
import com.example.bikessecure.qrscanner.VisionImageProcessor;
import com.example.bikessecure.databinding.ActivityQRScannerBinding;
import com.google.mlkit.common.MlKitException;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;

/**
 * QR scanner code adapted from:
 *     https://github.com/zeeshan-elahi/BarcodeScannerAndCameraXDemo
 */
public class QRScannerActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback, ExchangeScannedData {

    private static final String TAG = QRScannerActivity.class.getSimpleName();

    private ActivityQRScannerBinding binding;

    private static final int PERMISSIONS_REQUEST_CAMERA = 1;
    private boolean cameraPermissionGranted;

    @Nullable
    private ProcessCameraProvider cameraProvider;
    @Nullable
    private Preview previewUseCase;
    @Nullable
    private ImageAnalysis analysisUseCase;
    @Nullable
    private VisionImageProcessor imageProcessor;
    private boolean needUpdateGraphicOverlayImageSourceInfo;

    private String request;
    private boolean receivedQRcodeData = false;
    public static final String RESPONSE_MESSAGE = "com.example.bikessecure.RESPONSE_MESSAGE";

    private CameraSelector cameraSelector;
    private int lensFacing = CameraSelector.LENS_FACING_BACK;
    private static final String STATE_LENS_FACING = "lens_facing";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* get request passed by DashboardActivity */
        request = getIntent().getStringExtra(DashboardActivity.REQUEST);
        Log.i(TAG, "received request: " + request);

        /* checks for camera permission, and request if necessary */
        getCameraPermission();

        /* camera selector, only thing I understand is choosing the back cam instead of face cam */
        if (savedInstanceState != null) {
            lensFacing = savedInstanceState.getInt(STATE_LENS_FACING, CameraSelector.LENS_FACING_BACK);
        }
        cameraSelector = new CameraSelector.Builder().requireLensFacing(lensFacing).build();

        /* this controls the view (UI); see https://developer.android.com/topic/libraries/view-binding */
        binding = ActivityQRScannerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /* this helps to handle cases where we rotate the phone and stuff */
        new ViewModelProvider(
                this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                .get(CameraXViewModel.class)
                .getProcessCameraProvider()
                .observe(
                        this,
                        provider -> {
                            cameraProvider = provider;
                            if (cameraPermissionGranted) {
                                // binds Preview and ImageAnalysis to view,
                                //  and run the processing (within the use cases code)
                                bindAllCameraUseCases();
                            }
                        });

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putInt(STATE_LENS_FACING, lensFacing);
    }

    @Override
    public void onResume() {
        super.onResume();
        bindAllCameraUseCases();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (imageProcessor != null) {
            imageProcessor.stop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (imageProcessor != null) {
            imageProcessor.stop();
        }
    }

    /**
     * bind Preview and ImageAnalysis use cases
     *  -> some processing (eg check null), bind to view bind and then lastly bind to lifecycle
     */
    private void bindAllCameraUseCases() {
        if (!receivedQRcodeData) {
            bindPreviewUseCase();
            bindAnalysisUseCase();
        }
    }

    /**
     * to see the camera stream
     * (preview use case is something related to the camera stream)
     * */
    private void bindPreviewUseCase() {

        if (cameraProvider == null) {
            return;
        }
        if (previewUseCase != null) {
            cameraProvider.unbind(previewUseCase);
        }

        previewUseCase = new Preview.Builder().build();
        previewUseCase.setSurfaceProvider(binding.previewView.createSurfaceProvider());
        cameraProvider.bindToLifecycle(/* lifecycleOwner= */ com.example.bikessecure.QRScannerActivity.this, cameraSelector, previewUseCase);
    }

    /**
     *  get the image, run analysis (detector runs concurrently in BarcodeScannerProcessor),
     *  bind to view and lifecycle
     */
    private void bindAnalysisUseCase() {
        if (cameraProvider == null) {
            return;
        }
        if (analysisUseCase != null) {
            cameraProvider.unbind(analysisUseCase);
        }
        if (imageProcessor != null) {
            imageProcessor.stop();
        }

        try {
            Log.i(TAG, "Using Barcode Detector Processor");
            imageProcessor = new BarcodeScannerProcessor(this, this);
        } catch (Exception e) {
            Log.e(TAG, "Can not create image processor.", e);
            Toast.makeText(
                    getApplicationContext(),
                    "Can not create image processor: " + e.getLocalizedMessage(),
                    Toast.LENGTH_LONG)
                    .show();
            return;
        }

        ImageAnalysis.Builder builder = new ImageAnalysis.Builder();
        analysisUseCase = builder.build();

        needUpdateGraphicOverlayImageSourceInfo = true;
        analysisUseCase.setAnalyzer(
                // imageProcessor.processImageProxy will use another thread to run the detection underneath,
                // thus we can just runs the analyzer itself on main thread.
                ContextCompat.getMainExecutor(this),
                imageProxy -> {
                    if (needUpdateGraphicOverlayImageSourceInfo) {
                        boolean isImageFlipped = lensFacing == CameraSelector.LENS_FACING_FRONT;
                        int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
                        if (rotationDegrees == 0 || rotationDegrees == 180) {
                            binding.graphicOverlay.setImageSourceInfo(
                                    imageProxy.getWidth(), imageProxy.getHeight(), isImageFlipped);
                        } else {
                            binding.graphicOverlay.setImageSourceInfo(
                                    imageProxy.getHeight(), imageProxy.getWidth(), isImageFlipped);
                        }
                        needUpdateGraphicOverlayImageSourceInfo = false;
                    }
                    try {
                        // requires minSdkVersion 21
                        imageProcessor.processImageProxy(imageProxy, binding.graphicOverlay);
                    } catch (   MlKitException e) {
                        Log.e(TAG, "Failed to process image. Error: " + e.getLocalizedMessage());
                        Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT)
                                .show();
                    }
                });

        cameraProvider.bindToLifecycle(/* lifecycleOwner= */ this, cameraSelector, analysisUseCase);
    }

    /**
     * Prompts the user for permission to use the device camera
     *  (adapted from getLocationPermission)
     */
    private void getCameraPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            cameraPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSIONS_REQUEST_CAMERA);
        }
    }

    /**
     * handles permission request results called by getCameraPermission()
     *  binds all camera use cases after each permission is granted
     *  needs Activity to implement OnRequestPermissionsResultCallback (?)
     */
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        Log.i(TAG, "Permission granted!");
        if (cameraPermissionGranted) {
            bindAllCameraUseCases();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * from interface ExchangeScannedData
     *  receives QR code data, kills all use cases (preview & analysis),
     *  sends POST request with the QR code data
     */
    @Override
    public void sendScannedCode(String code) {
        if (!receivedQRcodeData) {
            receivedQRcodeData = true;  // stops bindAllUseCase()
            Toast.makeText(getApplicationContext(),"Sending scanned data...",
                    Toast.LENGTH_SHORT).show();
            // kill preview (freezes image)
            if (cameraProvider != null) {
                cameraProvider.unbind(previewUseCase);
            }
            if (previewUseCase != null) {
                previewUseCase.setSurfaceProvider(null);
            }
            // kill analysis
            if (imageProcessor != null) {
                imageProcessor.stop();
            }
            // make POST request
            postRequest(/*standID: */code.split(",")[0],
                        /*rackID: */code.split(",")[1],
                        request);
        }
    }

    /**
     * make a POST request using API attached to Amplify
     */
    private void postRequest(String standID, String rackID, String request) {

        String jsonFormat = String.format("{\"Stand ID\":\"%s\",\"Rack ID\":\"%s\",\"Request\":\"%s\",\"Password\":\"%s\"}",
                standID, rackID, request, LoginActivity.getUserSub());

        RestOptions options = RestOptions.builder()
                .addPath(getString(R.string.api_stage_name))
                .addBody(jsonFormat.getBytes())
                .build();

        Amplify.API.post(options,
                restResponse -> {
                    Log.i(TAG+"/POST", "POST succeeded: " + restResponse.getData().asString());
                    try {
                        JSONObject responseJSON = restResponse.getData().asJSONObject();

                        /* edit preferences if user managed to lock/unlock (check update & device) */
                        if (responseJSON.getString("update").equals("true")) {
                            Log.i(TAG+"/POST", "change in user state, updating preferences.");

                            // string set to put into preferences
                            HashSet<String> user_state = new HashSet<>();
                            switch (request) {
                                case "unlock":
                                    user_state.add("unlock");
                                    break;
                                case "lock":
                                    user_state.add("lock");
                                    user_state.add(rackID);
                                    user_state.add(standID);
                                    break;
                            }

                            SharedPreferences sharedPref = getSharedPreferences(
                                    getString(R.string.sharedpref_user_state), Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putStringSet(LoginActivity.getUserSub(),user_state);
                            editor.apply();
                            Log.i(TAG+"/POST", "state of " + LoginActivity.getUserSub()
                                    + ": " + user_state.toString());
                        }

                        /* get response message and pass it to the dashboard to display as a dialog */
                        Intent intent = new Intent(this, DashboardActivity.class);
                        intent.putExtra(RESPONSE_MESSAGE, responseJSON.getString("app"));
                        startActivity(intent);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG+"/POST","JSONException", e);
                    }
                },
                apiFailure -> Log.e(TAG+"/POST", "POST failed.", apiFailure)
        );
    }

    /*
    * changelog of original code:
    *  only enabled QR
    *  remove the scan results portion of the UI
    *  only allow the scanning of 1 QR code each time the activity is called (see sendScannedCode())
    *  sendScannedCode() creates the POST request to API Gateway
    *  simpler permissions checking
    * */

}