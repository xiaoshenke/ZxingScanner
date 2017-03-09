/*
 * Copyright (C) 2008 ZXing authors
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

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.ResultPointCallback;
import com.google.zxing.common.HybridBinarizer;

import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

import wuxian.me.zxingscanner.camera.Camera;

/**
 * @author dswitkin@google.com (Daniel Switkin)
 * 解析的结果通过@capturehandler告诉ui线程
 */
public class DecodeThread extends Thread {

    public static final String BARCODE_BITMAP = "barcode_bitmap";
    private Handler mDecodeStateHandler;
    private final Hashtable<DecodeHintType, Object> hints;
    private DecodeHandler mDecodeHandler;
    private final CountDownLatch handlerInitLatch;

    private Camera camera;

    public DecodeThread(Camera camera, Handler decodeStateHandler,
                        String characterSet,
                        ResultPointCallback resultPointCallback) {

        this.camera = camera;
        this.mDecodeStateHandler = decodeStateHandler;
        handlerInitLatch = new CountDownLatch(1);

        hints = new Hashtable<DecodeHintType, Object>(3);
        Vector<BarcodeFormat> decodeFormats = new Vector<BarcodeFormat>();
        decodeFormats.addAll(DecodeFormatMgr.ONE_D_FORMATS);
        decodeFormats.addAll(DecodeFormatMgr.QR_CODE_FORMATS);
        decodeFormats.addAll(DecodeFormatMgr.DATA_MATRIX_FORMATS);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);

        if (characterSet != null) {
            hints.put(DecodeHintType.CHARACTER_SET, characterSet);
        }
        hints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK,
                resultPointCallback);
    }

    public void stopThreadSafely() {
        Message quit = Message.obtain(decodeHandler(), DecodeConstants.Action.ACTION_QUIT_DECODE);
        quit.sendToTarget();
    }

    public void stopDecode() {
        decodeHandler().removeMessages(DecodeConstants.Action.ACTION_DO_DECODE);
    }

    public Handler decodeHandler() {
        try {
            handlerInitLatch.await();
        } catch (InterruptedException ie) {
        }
        return mDecodeHandler;
    }

    @Override
    public void run() {
        Looper.prepare();
        mDecodeHandler = new DecodeHandler();
        handlerInitLatch.countDown();
        Looper.loop();
    }

    public final class DecodeHandler extends Handler {
        private final MultiFormatReader multiFormatReader;

        public DecodeHandler() {
            multiFormatReader = new MultiFormatReader();
            multiFormatReader.setHints(hints);
        }

        @Override
        public void handleMessage(Message message) {
            if (message.what == DecodeConstants.Action.ACTION_DO_DECODE) {
                decode((byte[]) message.obj, message.arg1, message.arg2);

            } else if (message.what == DecodeConstants.Action.ACTION_QUIT_DECODE) {
                Looper.myLooper().quit();
            }
        }

        private void decode(byte[] data, int width, int height) {
            byte[] rotatedData = new byte[data.length];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++)
                    rotatedData[x * height + height - y - 1] = data[x + y * width];
            }
            int tmp = width;
            width = height;
            height = tmp;
            data = rotatedData;
            Result rawResult = null;
            PlanarYUVSource source = DecodeUtil
                    .buildLuminanceSource(camera, data, width, height);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                rawResult = multiFormatReader.decodeWithState(bitmap);
            } catch (ReaderException re) {

            } finally {
                multiFormatReader.reset();
            }

            if (rawResult != null) {
                Message message = Message.obtain(
                        mDecodeStateHandler,
                        DecodeConstants.Action.ACTION_DECODE_SUCCESS, rawResult);

                Bundle bundle = new Bundle();
                message.setData(bundle);
                message.sendToTarget();
            } else {
                Message message = Message.obtain(
                        mDecodeStateHandler,
                        DecodeConstants.Action.ACTION_DECODE_FAIL);
                message.sendToTarget();
            }
        }
    }
}
