package com.example.bikessecure;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
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

import com.example.bikessecure.qrscanner.BarcodeScannerProcessor;
import com.example.bikessecure.qrscanner.CameraXViewModel;
import com.example.bikessecure.qrscanner.ExchangeScannedData;
import com.example.bikessecure.qrscanner.VisionImageProcessor;
import com.example.bikessecure.databinding.ActivityQRScannerBinding;
import com.google.mlkit.common.MlKitException;

import java.util.ArrayList;
import java.util.List;

/**
 * QR scanner code adapted from:
 *     https://github.com/zeeshan-elahi/BarcodeScannerAndCameraXDemo
 */
public class QRScannerActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback, ExchangeScannedData {

    private static final String TAG = "QRScannerActivity";  // for Log
    private static final int PERMISSION_REQUESTS = 1;  // used as a request code to this function

    private ActivityQRScannerBinding binding;

    @Nullable
    private ProcessCameraProvider cameraProvider;
    @Nullable
    private Preview previewUseCase;
    @Nullable
    private ImageAnalysis analysisUseCase;
    @Nullable
    private VisionImageProcessor imageProcessor;
    private boolean needUpdateGraphicOverlayImageSourceInfo;
    private boolean receivedQRcodeData = false;
    private String request;

    private CameraSelector cameraSelector;
    private int lensFacing = CameraSelector.LENS_FACING_BACK;
    private static final String STATE_LENS_FACING = "lens_facing";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        // get the request from the dashboard button
        Intent intent = getIntent();
        request = intent.getStringExtra(MainActivity.REQUEST);


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
                            if (allPermissionsGranted()) {
                                // binds Preview and ImageAnalysis to view,
                                //  and run the processing (within the use cases code)
                                bindAllCameraUseCases();
                            }
                        });

        /* permissions, check and request if necessary */
        if (!allPermissionsGranted()) {
            getRuntimePermissions();
        }

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
                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
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
     *  binds all camera use cases after each permission is granted
     *  needs Activity to implement OnRequestPermissionsResultCallback
     */
    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        Log.i(TAG, "Permission granted!");
        if (allPermissionsGranted()) {
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
            // kill preview (freezes image)
            if (previewUseCase != null) {
                previewUseCase.setSurfaceProvider(null);
            }
            if (cameraProvider != null) {
                cameraProvider.unbind(previewUseCase);
            }
            // kill analysis
            if (imageProcessor != null) {
                imageProcessor.stop();
            }
            // make POST request
            RestApi.postRequest(/*standID: */code.split(",")[0],
                                /*rackID: */code.split(",")[1],
                                request);
        }
    }

    /*
    * changelog of original code:
    *  only enabled QR
    *  remove the scan results portion of the UI
    *  only allow the scanning of 1 QR code each time the activity is called (see sendScannedCode())
    *  sendScannedCode() creates the POST request to API Gateway
    * */

}