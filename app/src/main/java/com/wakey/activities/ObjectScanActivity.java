package com.wakey.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;

import com.wakey.R;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class ObjectScanActivity extends AppCompatActivity {

    private PreviewView previewView;
    private TextView detectedText;

    private ObjectDetector objectDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_scan);

        previewView = findViewById(R.id.previewView);
        detectedText = findViewById(R.id.detectedObjectText);

        // ML Kit Options
        ObjectDetectorOptions options =
                new ObjectDetectorOptions.Builder()
                        .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
                        .enableClassification()
                        .build();

        objectDetector = ObjectDetection.getClient(options);

        checkCameraPermission();
    }

    private void startCamera() {

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {

            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis analysis =
                        new ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build();

                // RULEAZĂ ANALYZE PE MAIN THREAD
                analysis.setAnalyzer(
                        ContextCompat.getMainExecutor(this),
                        this::analyzeImage
                );

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, analysis
                );

            } catch (Exception e) {
                e.printStackTrace();
            }

        }, ContextCompat.getMainExecutor(this));  // ATENȚIE ȘI AICI
    }


    private void analyzeImage(@NonNull ImageProxy imageProxy) {
        if (imageProxy.getImage() == null) {
            imageProxy.close();
            return;
        }

        InputImage image = InputImage.fromMediaImage(
                imageProxy.getImage(),
                imageProxy.getImageInfo().getRotationDegrees()
        );

        objectDetector.process(image)
                .addOnSuccessListener(objects -> handleDetectedObjects(objects))
                .addOnFailureListener(Throwable::printStackTrace)
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private void handleDetectedObjects(List<DetectedObject> objects) {
        if (objects.isEmpty()) {
            detectedText.setText("No object detected...");
            return;
        }

        DetectedObject obj = objects.get(0);

        if (!obj.getLabels().isEmpty()) {
            String label = obj.getLabels().get(0).getText();
            detectedText.setText("Detected: " + label);
        }
    }

    private static final int CAMERA_PERMISSION_REQUEST = 1001;

    private void checkCameraPermission() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{android.Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST);

        } else {
            startCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                startCamera();

            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
                finish(); // închide activitatea dacă nu acceptă
            }
        }
    }


}
