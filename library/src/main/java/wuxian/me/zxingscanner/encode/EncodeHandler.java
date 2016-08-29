package wuxian.me.zxingscanner.encode;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.Hashtable;

public final class EncodeHandler {
    private static final int BLACK = 0xff000000;
    private static final int WHITE = 0xffffffff;

    public static Bitmap createQRCodeImage(Bitmap avatar, String str,
                                           int screen_width) throws WriterException {

        Bitmap bitmap = null;

        try {
            Hashtable<EncodeHintType, Object> qrParam = new Hashtable<EncodeHintType, Object>();
            qrParam.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            qrParam.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            BitMatrix matrix = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, screen_width, screen_width, qrParam);
            int width = matrix.getWidth();
            int height = matrix.getHeight();
            int halfW = width / 2;
            int halfH = height / 2;
            int[] pixels = new int[width * height];
            Matrix m = new Matrix();
            if (avatar != null) {
                float sx = (float) width / (8 * avatar.getWidth());
                float sy = (float) height / (8 * avatar.getHeight());
                m.setScale(sx, sy);
                Bitmap QRBitmaphead = Bitmap.createBitmap(avatar, 0, 0,
                        avatar.getWidth(), avatar.getHeight(), m, false);
                int IMAGE_HALFWIDTH = QRBitmaphead.getWidth() / 2;
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        if (x > halfW - IMAGE_HALFWIDTH
                                && x < halfW + IMAGE_HALFWIDTH
                                && y > halfH - IMAGE_HALFWIDTH
                                && y < halfH + IMAGE_HALFWIDTH) {
                            pixels[y * width + x] = QRBitmaphead.getPixel(x
                                    - halfW + IMAGE_HALFWIDTH, y - halfH
                                    + IMAGE_HALFWIDTH);
                        } else {
                            if (matrix.get(x, y)) {
                                pixels[y * width + x] = BLACK;
                            }
                        }
                    }
                }
            } else {
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        if (matrix.get(x, y)) {
                            pixels[y * width + x] = BLACK;
                        }
                    }
                }
            }
            bitmap = Bitmap.createBitmap(width, height,
                    Bitmap.Config.ARGB_8888);
            if (bitmap != null) {
                bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            }
        } catch (OutOfMemoryError e) {
        }

        return bitmap;
    }
}