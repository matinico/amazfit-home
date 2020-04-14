package com.matias.android.musicsocial.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;

import androidx.core.content.res.ResourcesCompat;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.HashMap;
import java.util.Map;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

public class QRUtils {
    public static Bitmap generateQRCode(Context context, String data) throws WriterException {
        Map<EncodeHintType, ErrorCorrectionLevel> hintMap = new HashMap<EncodeHintType, ErrorCorrectionLevel>();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

        //generating qr code in bitmatrix type
        BitMatrix matrix = new MultiFormatWriter().encode(data,
                BarcodeFormat.QR_CODE, 500, 500, hintMap);
        //converting bitmatrix to bitmap

        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int[] pixels = new int[width * height];
        // All are 0, or black, by default
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                //pixels[offset + x] = matrix.get(x, y) ? BLACK : WHITE;
                pixels[offset + x] = matrix.get(x, y) ? WHITE : BLACK;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        //setting bitmap to image view
        return bitmap;
        //Bitmap overlay = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_foreground);
        /*Drawable drawable = DrawableHelper
                .withContext(context)
                .withColor(R.color.colorPrimary)
                .withDrawable(R.mipmap.ic_launcher_round)
                .tint()
                .get();*/
        /*Bitmap bitmap1 = changeBitmapColor(overlay, Color.BLACK);
        if(overlay != null) return mergeBitmaps(bitmap1, bitmap);
        else return bitmap;*/
    }

    private static Bitmap mergeBitmaps(Bitmap overlay, Bitmap bitmap) {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();

        Bitmap combined = Bitmap.createBitmap(width, height, bitmap.getConfig());
        Canvas canvas = new Canvas(combined);
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();

        canvas.drawBitmap(bitmap, new Matrix(), null);

        int centreX = (canvasWidth  - overlay.getWidth()) /2;
        int centreY = (canvasHeight - overlay.getHeight()) /2 ;
        canvas.drawBitmap(overlay, centreX, centreY, null);

        return combined;
    }

    private static Bitmap changeBitmapColor(Bitmap sourceBitmap, int color) {
        Bitmap resultBitmap = Bitmap.createBitmap(sourceBitmap, 0, 0,
                sourceBitmap.getWidth() - 1, sourceBitmap.getHeight() - 1);
        Paint p = new Paint();
        ColorFilter filter = new LightingColorFilter(color, 1);
        p.setColorFilter(filter);
        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(resultBitmap, 0, 0, p);
        return resultBitmap;
    }
}