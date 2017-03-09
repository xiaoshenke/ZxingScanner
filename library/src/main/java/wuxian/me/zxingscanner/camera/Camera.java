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

package wuxian.me.zxingscanner.camera;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;

import wuxian.me.zxingscanner.decode.DecodeConstants;
import wuxian.me.zxingscanner.decode.PreviewCallback;


public final class Camera {
    private static Camera sCamera;

    private CameraConfigMgr mConfigMgr;

    @NonNull
    public CameraConfigMgr getConfig() {
        return mConfigMgr;
    }

    private android.hardware.Camera camera = null;

    private boolean mInited = false;
    private boolean mInpreview = false;
    private boolean mOneShot = false;
    private ICameraListener mCameraListener;

    public static Camera getInstance(Context context) {
        if (sCamera == null) {
            sCamera = new Camera(context);
        }
        return sCamera;
    }

    private Camera(@NonNull Context context) {
        this.mConfigMgr = new CameraConfigMgr(context);
        mOneShot = Integer.parseInt(Build.VERSION.SDK) > 3;
    }

    private void openCameraAndSetPreviewInner(SurfaceHolder holder) {
        if (camera == null) {
            try {
                camera = android.hardware.Camera.open();
                if (camera == null) {
                    throw new IOException();
                }
                camera.setPreviewDisplay(holder);
            } catch (IOException e) {
                if (mCameraListener != null) {
                    mCameraListener.onOpenError();
                }
            }

            if (!mInited) {
                mInited = true;
                mConfigMgr.init(camera);
            }
            mConfigMgr.setDesiredParameters(camera);
        }

        startPreview();
    }

    private boolean mHasSurface;
    private SurfaceHolder.Callback mCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (!mHasSurface) {
                mHasSurface = true;
            }
            openCameraAndSetPreviewInner(holder);
            if (mCameraListener != null) {
                mCameraListener.onCameraOpen();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            mHasSurface = false;
        }
    };


    public void openAndDisplay(@NonNull SurfaceView surfaceView,
                               @Nullable ICameraListener listener) throws IOException {
        mCameraListener = listener;

        mHasSurface = false;
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(mCallback);
    }


    public void closeCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    public void startPreview() {
        if (camera != null && !mInpreview) {
            camera.startPreview();
            mInpreview = true;
        }
    }

    public void stopPreview() {
        if (camera != null && mInpreview) {
            if (!mOneShot) {
                camera.setPreviewCallback(null);
            }
            camera.setPreviewCallback(null);
            camera.stopPreview();
            stopAutoFocus();
            mInpreview = false;
        }
    }

    public void requestPreview(@NonNull PreviewCallback previewCallback) {
        if (camera != null && mInpreview) {
            if (mOneShot) {
                camera.setOneShotPreviewCallback(previewCallback);
            } else {
                camera.setPreviewCallback(previewCallback);
            }
        }
    }


    public void startAutoFocus() {
        handler.sendEmptyMessage(DecodeConstants.Action.ACTION_AUTOFOCUS);
    }

    public void stopAutoFocus() {
        handler.removeMessages(DecodeConstants.Action.ACTION_AUTOFOCUS);
        if (camera != null) {
            camera.autoFocus(null);
        }
    }

    private static final long AUTOFOCUS_INTERVAL_MS = 1500L;


    private android.hardware.Camera.AutoFocusCallback autoFocusCb = new android.hardware.Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, android.hardware.Camera camera) {
            if (camera != null && mInpreview) {
                camera.autoFocus(null);
            }
            Message msg = handler.obtainMessage(DecodeConstants.Action.ACTION_AUTOFOCUS);
            handler.sendMessageDelayed(msg, AUTOFOCUS_INTERVAL_MS);

        }
    };

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == DecodeConstants.Action.ACTION_AUTOFOCUS) {
                if (camera != null && mInpreview) {
                    camera.autoFocus(autoFocusCb);
                }
            }
        }
    };

    public void destory() {
        stopPreview();
        closeCamera();
    }

    public interface ICameraListener {
        void onCameraOpen();

        void onOpenError();
    }
}
