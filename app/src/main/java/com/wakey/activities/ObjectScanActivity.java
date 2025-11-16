package com.wakey.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import com.wakey.R;
import com.wakey.database.WakeyDatabase;

import java.util.List;

public class ObjectScanActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 2001;
    private String expectedObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_scan);

        // Obiectul așteptat transmis de AlarmRingActivity
        expectedObject = getIntent().getStringExtra("expectedObject");

        openCamera();
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            runRecognition(bitmap);
        }
    }

    private void runRecognition(Bitmap bitmap) {

        InputImage image = InputImage.fromBitmap(bitmap, 0);

        ImageLabeler labeler = ImageLabeling.getClient(
                new ImageLabelerOptions.Builder().setConfidenceThreshold(0.65f).build()
        );

        labeler.process(image)
                .addOnSuccessListener(labels -> {

                    for (ImageLabel label : labels) {

                        String detected = label.getText();
                        float confidence = label.getConfidence();

                        Log.d("MLKIT", "Detected: " + detected + " (" + confidence + ")");

                        if (detected.equalsIgnoreCase(expectedObject)) {
                            Toast.makeText(this, "Correct object!", Toast.LENGTH_SHORT).show();

                            // Trimite OK înapoi la AlarmRingActivity
                            Intent result = new Intent();
                            result.putExtra("scanSuccess", true);
                            setResult(RESULT_OK, result);
                            finish();
                            return;
                        }
                    }

                    Toast.makeText(this, "Wrong object! Try again.", Toast.LENGTH_SHORT).show();
                    openCamera();

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Scan failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
