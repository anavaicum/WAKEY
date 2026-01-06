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

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import com.wakey.R;
import com.wakey.alarm.AlarmRingHolder;
import com.wakey.database.WakeHistoryEntity;
import com.wakey.database.WakeyDatabase;
import com.wakey.utils.YuvToRgbConverter;
import com.wakey.utils.YoloV8Detector;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ObjectScanActivity extends AppCompatActivity {

    private static final String TAG = "ObjectScanActivity";
    private static final int CAMERA_PERMISSION_REQUEST = 1001;

    private PreviewView previewView;
    private TextView detectedText;

    private ExecutorService cameraExecutor;
    private boolean processingFrame = false;

    private String targetObject = "";

    private YuvToRgbConverter yuvToRgbConverter;
    private YoloV8Detector detector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_scan);

        previewView = findViewById(R.id.previewView);
        detectedText = findViewById(R.id.detectedObjectText);

        yuvToRgbConverter = new YuvToRgbConverter(this);
        cameraExecutor = Executors.newSingleThreadExecutor();

        targetObject = getIntent().getStringExtra("target_object");
        if (targetObject == null) targetObject = "";

        initTFLite();
        checkCameraPermission();
    }

    private void initTFLite() {
        try {
            // dacă ai redenumit fișierul, schimbă numele aici
            detector = new YoloV8Detector(
                    this,
                    "best_float16.tflite",   // numele modelului
                    "labels.txt"             // fișierul cu clasele
            );
        } catch (Exception e) {
            Log.e(TAG, "Error loading YOLO model", e);
            Toast.makeText(this, "Eroare la încărcarea modelului YOLO", Toast.LENGTH_LONG).show();
        }
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

                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                analysis.setAnalyzer(cameraExecutor, this::analyzeImage);

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, analysis
                );

            } catch (Exception e) {
                Log.e(TAG, "Error starting camera", e);
            }

        }, ContextCompat.getMainExecutor(this));
    }

    private void showDetectionDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(this);

        builder.setTitle("Obiect detectat!");
        builder.setMessage("Ai găsit cu succes obiectul: " + targetObject);

        builder.setPositiveButton("OK", (dialog, which) -> {
            dialog.dismiss();
            finish(); // închide activitatea după OK
        });

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }


    private int consecutiveMatches = 0;
    @SuppressLint("SetTextI18n")
    private void analyzeImage(@NonNull ImageProxy image) {
        // Log.d("YOLO_DEBUG", "Frame received");
        if (processingFrame || detector == null) {
            image.close();
            return;
        }
        processingFrame = true;

        try {
            //Log.d("YOLO_DEBUG", "Running detection...");

            // Pasul 1. Convertim YUV → Bitmap
            Bitmap bitmap = Bitmap.createBitmap(
                    image.getWidth(),
                    image.getHeight(),
                    Bitmap.Config.ARGB_8888
            );

            // EXTREM DE IMPORTANT
            yuvToRgbConverter.yuvToRgb(image, bitmap);

            // Pasul 2. Rotire imagine
            int rotationDegrees = image.getImageInfo().getRotationDegrees();
            if (rotationDegrees != 0) {
                Matrix m = new Matrix();
                m.postRotate(rotationDegrees);
                bitmap = Bitmap.createBitmap(
                        bitmap,
                        0, 0,
                        bitmap.getWidth(),
                        bitmap.getHeight(),
                        m,
                        true
                );
            }

            // 3. Rulăm YOLO pe bitmap
            List<YoloV8Detector.Recognition> results = detector.detect(bitmap);
            //Log.d("YOLO_DEBUG", "Număr detecții: " + results.size());

            for (YoloV8Detector.Recognition r : results) {
                Log.d("YOLO_DEBUG", "-> " + r.label + " conf=" + r.confidence);
            }

            // Confidența minimă acceptată (poți crește după reantrenare)
            float CONF_THRESHOLD = 0.55f;

            YoloV8Detector.Recognition best = null;

            // Filtrează doar clasa target (cea pe care trebuie s-o găsească)
            for (YoloV8Detector.Recognition r : results) {
                //  Ignorăm orice altă clasă în afară de target
                if (!r.label.equalsIgnoreCase(targetObject))
                    continue;

                //  Ignorăm detecțiile slabe
                if (r.confidence < CONF_THRESHOLD)
                    continue;

                if (best == null || r.confidence > best.confidence) {
                    best = r;
                }
            }

            if (best == null) {
                // Nimic valid detectat
                runOnUiThread(() ->
                        detectedText.setText("Searching for: " + targetObject)
                );
                consecutiveMatches = 0; // resetăm
            }
            else {
                float confPercent = best.confidence * 100f;

                runOnUiThread(() ->
                        detectedText.setText("Detected " + targetObject + " (" +
                                String.format("%.1f", confPercent) + "%)")
                );

                // Dacă detectăm același obiect pe mai multe frame-uri
                consecutiveMatches++;

                if (consecutiveMatches >= 3) {

                    Log.d("YOLO_ALARM", "OBIECTUL A FOST DETECTAT: " + targetObject + " — oprire alarmă!");

                    // Oprire alarmă dacă încă sună
                    if (AlarmRingHolder.currentRingtone != null) {
                        AlarmRingHolder.currentRingtone.stop();
                        AlarmRingHolder.currentRingtone = null;
                        Log.d("YOLO_ALARM", "Alarma oprită cu succes.");
                    }

                    saveSuccessfulWake();

                    runOnUiThread(() -> showDetectionDialog());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error analyzing image", e);
        } finally {
            image.close();
            processingFrame = false;
        }
    }


    private void checkCameraPermission() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{android.Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST
            );

        } else {
            startCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            startCamera();
        } else {
            Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void saveSuccessfulWake() {
        new Thread(() -> {
            WakeyDatabase db = WakeyDatabase.getInstance(this);

            long now = System.currentTimeMillis();

            WakeHistoryEntity e = new WakeHistoryEntity();
            e.date = now;
            e.wakeTime = now;
            e.success = true;
            e.emergencyUsed = false;

            db.wakeHistoryDao().insert(e);

            // streak crește doar la succes
            db.lifeDao().incrementStreak();

            Log.d("WAKE_HISTORY", "Successful wake saved");
        }).start();
    }


}
