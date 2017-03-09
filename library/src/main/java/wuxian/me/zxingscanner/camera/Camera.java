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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

import wuxian.me.zxingscanner.decode.AutoFocusCallback;
import wuxian.me.zxingscanner.decode.PreviewCallback;


public final class Camera {
    private static Camera sCamera;
    public static final int SDK_INT;

    static {
        int sdkInt;
        try {
            sdkInt = Integer.parseInt(Build.VERSION.SDK);
        } catch (NumberFormatException nfe) {
            sdkInt = 10000;
        }
        SDK_INT = sdkInt;
    }

    private CameraConfigMgr mConfigMgr;

    @NonNull
    public CameraConfigMgr getConfig() {
        return mConfigMgr;
    }

    private android.hardware.Camera camera = null;

    private boolean mInited = false;
    private boolean mInpreview = false;
    private boolean mOneShot = false;

    private final PreviewCallback previewCallback;
    private final AutoFocusCallback autoFocusCallback;

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

        previewCallback = new PreviewCallback(mConfigMgr, mOneShot);
        autoFocusCallback = new AutoFocusCallback();
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
            camera.stopPreview();
            previewCallback.setHandler(null, 0);
            autoFocusCallback.setHandler(null, 0);
            mInpreview = false;
        }
    }

    public void requestPreviewFrame(Handler handler, int message) {
        if (camera != null && mInpreview) {
            previewCallback.setHandler(handler, message);
            if (mOneShot) {
                camera.setOneShotPreviewCallback(previewCallback);
            } else {
                camera.setPreviewCallback(previewCallback);
            }
        }
    }

    public void requestAutoFocus(Handler handler, int message) {
        if (camera != null && mInpreview) {
            autoFocusCallback.setHandler(handler, message);
            camera.autoFocus(autoFocusCallback);
        }
    }

    public void destory() {
        stopPreview();
        closeCamera();
    }

    public interface ICameraListener {
        void onCameraOpen();

        void onOpenError();
    }
}
