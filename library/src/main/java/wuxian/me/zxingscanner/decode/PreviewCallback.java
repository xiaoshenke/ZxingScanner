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

import android.graphics.Point;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;

import wuxian.me.zxingscanner.camera.CameraConfigMgr;

public final class PreviewCallback implements Camera.PreviewCallback {

    private final CameraConfigMgr mConfigManager;
    private final boolean mOneShot;
    private Handler mDecodeHandler;

    public PreviewCallback(CameraConfigMgr configManager, Handler decodeHandler,
                           boolean useOneShot) {
        this.mDecodeHandler = decodeHandler;
        this.mConfigManager = configManager;
        this.mOneShot = useOneShot;
    }

    public void onPreviewFrame(byte[] data, android.hardware.Camera camera) {
        Point cameraResolution = mConfigManager.getCameraResolution();
        if (mOneShot) {
            camera.setOneShotPreviewCallback(null);
        } else {
            camera.setPreviewCallback(null);
        }
        if (mDecodeHandler != null) {
            Message message = mDecodeHandler.obtainMessage(DecodeConstants.Action.ACTION_DO_DECODE,
                    cameraResolution.x, cameraResolution.y, data);
            message.sendToTarget();
            mDecodeHandler = null;
        }
    }

}
