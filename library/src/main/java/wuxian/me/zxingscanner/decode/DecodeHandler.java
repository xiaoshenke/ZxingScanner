/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wuxian.me.zxingscanner.decode;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.util.Hashtable;

import wuxian.me.zxingscanner.R;
import wuxian.me.zxingscanner.camera.Camera;

public final class DecodeHandler extends Handler {

    private static final String TAG = DecodeHandler.class.getSimpleName();

    private Handler handler;
    private final MultiFormatReader multiFormatReader;

    private Camera camera;

    public DecodeHandler(Camera camera, Handler handler,
                         Hashtable<DecodeHintType, Object> hints) {
        this.camera = camera;
        multiFormatReader = new MultiFormatReader();
        multiFormatReader.setHints(hints);
        this.handler = handler;
    }

    /**
     * Method of stopping the decoding thread is send message R.id.quit message to your handler
     * @param message
     */
    @Override
    public void handleMessage(Message message) {
        if(message.what == R.id.decode){
            decode((byte[]) message.obj, message.arg1, message.arg2);
        } else if(message.what == R.id.quit){
            Looper.myLooper().quit();
        }
    }

    /**
     * Decode the data within the viewfinder rectangle, and time how long it
     * took. For efficiency, reuse the same reader objects from one decode to
     * the next.
     *
     * @param data   The YUV preview frame.
     * @param width  The width of the preview frame.
     * @param height The height of the preview frame.
     */
    private void decode(byte[] data, int width, int height) {
        byte[] rotatedData = new byte[data.length];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++)
                rotatedData[x * height + height - y - 1] = data[x + y * width];
        }
        int tmp = width; // Here we are swapping, that's the difference to #11
        width = height;
        height = tmp;
        data = rotatedData;
        long start = System.currentTimeMillis();
        Result rawResult = null;
        PlanarYUVSource source = DecodeUtil
                .buildLuminanceSource(camera, data, width, height);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        try {
            rawResult = multiFormatReader.decodeWithState(bitmap);
        } catch (ReaderException re) {
            // continue
        } finally {
            multiFormatReader.reset();
        }

        if (rawResult != null) {
            long end = System.currentTimeMillis();
            Log.d(TAG, "Found barcode (" + (end - start) + " ms):\n"
                    + rawResult.toString());
            Message message = Message.obtain(
                    handler,
                    R.id.decode_succeeded, rawResult);
            Bundle bundle = new Bundle();
            message.setData(bundle);
            message.sendToTarget();
        } else {
            Message message = Message.obtain(
                    handler,
                    R.id.decode_failed);
            message.sendToTarget();
        }
    }

}
