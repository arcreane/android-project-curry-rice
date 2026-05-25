package com.example.recyclescan3;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.recyclescan3.ml.TFLiteClassifier;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;



public class ScannerActivity extends AppCompatActivity {

    private static final String TAG = "ScannerActivity";
    private static final int PERMISSION_REQUEST_CODE = 100;

    private PreviewView previewView;
    private Button captureButton;
    private TextView statusText;

    private ImageCapture imageCapture;
    private ProcessCameraProvider cameraProvider;
    private TFLiteClassifier classifier;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        previewView = findViewById(R.id.preview_view); // inflates layout + binds views
        captureButton = findViewById(R.id.btn_capture);
        statusText = findViewById(R.id.status_text);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) // checks camera permission
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CODE);
        } else {
            startCamera();
        }

        captureButton.setOnClickListener(v -> captureImage());

        try {
            classifier = new TFLiteClassifier(this);
        } catch (IOException e) {
            Log.e(TAG, "Failed to load TFLite model", e);
            statusText.setText("Model load failed");
        }
    }


    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(this);
        // initializes CameraX: creates Preview and ImageCapture
        future.addListener(() -> {
            try {
                cameraProvider = future.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .build();

                cameraProvider.bindToLifecycle(
                        this,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageCapture);

                statusText.setText("Ready to scan");

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Camera init failed", e);
                statusText.setText("Camera error");
            }
        }, ContextCompat.getMainExecutor(this));
    }


    private void captureImage() {
        if (imageCapture == null) {
            Toast.makeText(this, "Camera not ready", Toast.LENGTH_SHORT).show();
            return;
        }

        File outputDir = new File(getCacheDir(), "scans");
        if (!outputDir.exists()) outputDir.mkdirs();

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File outputFile = new File(outputDir, "scan_" + timestamp + ".jpg"); // takes a photo and saves it to cache/scans/scan_TIMESTAMP.jpg

        imageCapture.takePicture(
                new ImageCapture.OutputFileOptions.Builder(outputFile).build(),
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults results) {
                        Log.d(TAG, "Saved: " + outputFile.getAbsolutePath());
                        statusText.setText("Classifying...");

                        Bitmap raw = BitmapFactory.decodeFile(outputFile.getAbsolutePath());
                        if (raw == null || classifier == null) {
                            statusText.setText("Classification failed");
                            return;
                        }

                        TFLiteClassifier.Result result = classifier.classify(raw);
                        String name         = labelToDisplayName(result.label);
                        String instructions = String.format(Locale.US, "Confidence: %.0f%%", result.confidence * 100);

                        Product product = new Product(name, null, result.category, instructions);
                        Intent intent = new Intent(ScannerActivity.this, ResultActivity.class);
                        intent.putExtra(ResultActivity.EXTRA_PRODUCT, product);
                        startActivity(intent);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException e) {
                        Log.e(TAG, "Capture failed", e);
                        statusText.setText("Capture failed");
                    }});
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // system callback after user responds to the camera permission
        if (requestCode == PERMISSION_REQUEST_CODE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            statusText.setText("Camera permission denied");
        }
    }


    private String labelToDisplayName(String label) {
        switch (label) {
            case "poubelle_jaune":               return "Yellow Bin Item";
            case "poubelle_verre":               return "Glass Item";
            case "ordures_menageres":            return "Household Waste";
            case "decheterie_collecte_speciale": return "Special Collection Item";
            case "compost_biodechets":           return "Compostable Item";
            default:                             return "Unknown Item";
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraProvider != null) cameraProvider.unbindAll(); // releases the camera when activity is destroyed.
        if (classifier != null) classifier.close();
    }
}
