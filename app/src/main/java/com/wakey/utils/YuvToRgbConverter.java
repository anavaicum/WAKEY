package com.wakey.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.media.Image;
import androidx.camera.core.ImageProxy;
import java.nio.ByteBuffer;

public class YuvToRgbConverter {

    private final int[] rgbBuffer;

    public YuvToRgbConverter(Context context) {
        rgbBuffer = new int[1];
    }

    public void yuvToRgb(ImageProxy image, Bitmap output) {
        ImageProxy.PlaneProxy[] planes = image.getPlanes();

        ByteBuffer yBuffer = planes[0].getBuffer(); // Y
        ByteBuffer uBuffer = planes[1].getBuffer(); // U
        ByteBuffer vBuffer = planes[2].getBuffer(); // V

        int width = image.getWidth();
        int height = image.getHeight();

        int pixelStride = planes[2].getPixelStride();
        int rowStride = planes[2].getRowStride();

        byte[] yuvBytes = new byte[yBuffer.capacity() + uBuffer.capacity() + vBuffer.capacity()];

        yBuffer.get(yuvBytes, 0, yBuffer.capacity());
        uBuffer.get(yuvBytes, yBuffer.capacity(), uBuffer.capacity());
        vBuffer.get(yuvBytes, yBuffer.capacity() + uBuffer.capacity(), vBuffer.capacity());

        int[] out = new int[width * height];
        decodeYUV420(out, yuvBytes, width, height);

        output.setPixels(out, 0, width, 0, 0, width, height);
    }

    private void decodeYUV420(int[] rgb, byte[] yuv420, int width, int height) {
        final int frameSize = width * height;

        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width;
            int u = 0, v = 0;

            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420[yp])) - 16;
                if (y < 0) y = 0;

                if ((i & 1) == 0) {
                    v = (0xff & yuv420[uvp++]) - 128;
                    u = (0xff & yuv420[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = y1192 + 1634 * v;
                int g = y1192 - 833 * v - 400 * u;
                int b = y1192 + 2066 * u;

                r = r < 0 ? 0 : Math.min(r, 262143);
                g = g < 0 ? 0 : Math.min(g, 262143);
                b = b < 0 ? 0 : Math.min(b, 262143);

                rgb[yp] = 0xff000000 |
                        ((r << 6) & 0xff0000) |
                        ((g >> 2) & 0xff00) |
                        ((b >> 10) & 0xff);
            }
        }
    }
}
