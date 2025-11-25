package com.wakey.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class YoloV8Detector {

    private static final String TAG = "YoloV8Detector";
    private final Interpreter interpreter;
    private final List<String> labels = new ArrayList<>();

    private int inputSize = 320; // îl detectăm automat dacă vrei

    public static class Recognition {
        public final String label;
        public final float confidence;
        public final RectF box;

        public Recognition(String label, float confidence, RectF box) {
            this.label = label;
            this.confidence = confidence;
            this.box = box;
        }
    }

    public YoloV8Detector(Context ctx, String modelName, String labelsName) throws IOException {

        Log.d("YOLO_DEBUG", "Loading model... " + modelName);

        interpreter = new Interpreter(loadModelFile(ctx, modelName));
        int outIndex = 0;
        int[] outShape = interpreter.getOutputTensor(outIndex).shape();
        String outInfo = "OUTPUT SHAPE = [" + outShape[0] + "," + outShape[1] + "," + outShape[2] + "]";
        Log.d("YOLO_DEBUG", outInfo);

        loadLabels(ctx, labelsName);

        Log.d("YOLO_DEBUG", "Model loaded SUCCESSFULLY");

        int[] inputShape = interpreter.getInputTensor(0).shape();
        inputSize = inputShape[1];

        Log.d("YOLO_DEBUG", "Input size = " + inputSize);
    }


    private MappedByteBuffer loadModelFile(Context ctx, String filename) throws IOException {
        AssetFileDescriptor fd = ctx.getAssets().openFd(filename);
        FileInputStream input = new FileInputStream(fd.getFileDescriptor());
        FileChannel channel = input.getChannel();

        long startOffset = fd.getStartOffset();
        long declaredLength = fd.getDeclaredLength();

        return channel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }


    private void loadLabels(Context ctx, String filename) throws IOException {
        BufferedReader br = new BufferedReader(
                new InputStreamReader(ctx.getAssets().open(filename))
        );
        String line;
        while ((line = br.readLine()) != null)
            labels.add(line.trim());
        br.close();
    }

    private ByteBuffer bitmapToBuffer(Bitmap bitmap) {
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true);

        ByteBuffer buffer = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4);
        buffer.order(ByteOrder.nativeOrder());
        buffer.rewind();

        int[] pixels = new int[inputSize * inputSize];
        resized.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize);

        for (int px : pixels) {
            buffer.putFloat(((px >> 16) & 0xFF) / 255f);
            buffer.putFloat(((px >> 8) & 0xFF) / 255f);
            buffer.putFloat((px & 0xFF) / 255f);
        }
        return buffer;
    }

    // IoU pentru NMS
    private float iou(RectF a, RectF b) {
        float left = Math.max(a.left, b.left);
        float top = Math.max(a.top, b.top);
        float right = Math.min(a.right, b.right);
        float bottom = Math.min(a.bottom, b.bottom);

        float inter = Math.max(right - left, 0) * Math.max(bottom - top, 0);

        float union = a.width() * a.height() + b.width() * b.height() - inter;

        return inter / union;
    }

    public List<Recognition> detect(Bitmap bitmap) {

        ByteBuffer input = bitmapToBuffer(bitmap);

        // MODEL OUTPUT = [1,300,6]
        float[][][] output = new float[1][300][6];

        Log.d("YOLO_DEBUG", "Running interpreter...");
        interpreter.run(input, output);

        Log.d("YOLO_DEBUG",
                "RAW0 = " +
                        output[0][0][0] + ", " +
                        output[0][0][1] + ", " +
                        output[0][0][2] + ", " +
                        output[0][0][3] + ", " +
                        output[0][0][4] + ", " +
                        output[0][0][5]
        );

        List<Recognition> results = new ArrayList<>();
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        for (int i = 0; i < 300; i++) {

            float cx = output[0][i][0];
            float cy = output[0][i][1];
            float width = output[0][i][2];
            float height = output[0][i][3];
            float conf = output[0][i][4];
            float cls = output[0][i][5];

            if (conf < 0.3f) continue;  // scădem threshold pt debugging

            int classId = (int) cls;
            if (classId < 0 || classId >= labels.size()) continue;

            // Convertire la coordonate bitmap
            float x1 = (cx - width / 2f) * w;
            float y1 = (cy - height / 2f) * h;
            float x2 = (cx + width / 2f) * w;
            float y2 = (cy + height / 2f) * h;

            results.add(new Recognition(
                    labels.get(classId),
                    conf,
                    new RectF(x1, y1, x2, y2)
            ));
        }

        return results;
    }


}
