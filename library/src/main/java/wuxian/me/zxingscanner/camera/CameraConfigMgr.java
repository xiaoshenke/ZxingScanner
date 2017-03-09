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

package wuxian.me.zxingscanner.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

public final class CameraConfigMgr {

    private static final int TEN_DESIRED_ZOOM = 27;
    private static final int DESIRED_SHARPNESS = 30;

    private final Context context;
    private Point mScreenResolution;
    private Point mCameraResolution;
    private int mPreviewFormat;
    private String mPreviewFormatString;

    public Point getCameraResolution() {
        return mCameraResolution;
    }

    public Point getScreenResolution() {
        return mScreenResolution;
    }

    public int getPreviewFormat() {
        return mPreviewFormat;
    }

    public String getPreviewFormatString() {
        return mPreviewFormatString;
    }

    public CameraConfigMgr(Context context) {
        this.context = context;
    }

    public void init(android.hardware.Camera camera) {
        android.hardware.Camera.Parameters parameters = camera.getParameters();
        mPreviewFormat = parameters.getPreviewFormat();
        mPreviewFormatString = parameters.get("preview-format");

        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        mScreenResolution = new Point(display.getWidth(), display.getHeight());  //拿到屏幕

        Point screenRes = new Point();
        screenRes.x = this.mScreenResolution.x;
        screenRes.y = this.mScreenResolution.y;
        if (this.mScreenResolution.x < this.mScreenResolution.y) {
            screenRes.x = this.mScreenResolution.y;
            screenRes.y = this.mScreenResolution.x;
        }
        mCameraResolution = CameraUtil.getCameraResolution(parameters, screenRes);//拿到相机数据
    }

    @SuppressLint("NewApi")
    public void setDesiredParameters(android.hardware.Camera camera) {
        android.hardware.Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewSize(mCameraResolution.x, mCameraResolution.y);
        initFlash(parameters);
        initZoom(parameters);
        camera.setDisplayOrientation(90);
        camera.setParameters(parameters);
    }

    private void initFlash(android.hardware.Camera.Parameters parameters) {
        if (Build.MODEL.contains("Behold II") && Camera.SDK_INT == 3) { // 3
            parameters.set("flash-value", 1);
        } else {
            parameters.set("flash-value", 2);
        }
        parameters.set("flash-mode", "off");
    }

    private void initZoom(android.hardware.Camera.Parameters parameters) {

        String zoomSupportedString = parameters.get("zoom-supported");
        if (zoomSupportedString != null
                && !Boolean.parseBoolean(zoomSupportedString)) {
            return;
        }

        int tenDesiredZoom = TEN_DESIRED_ZOOM;

        String maxZoomString = parameters.get("max-zoom");
        if (maxZoomString != null) {
            try {
                int tenMaxZoom = (int) (10.0 * Double
                        .parseDouble(maxZoomString));
                if (tenDesiredZoom > tenMaxZoom) {
                    tenDesiredZoom = tenMaxZoom;
                }
            } catch (NumberFormatException nfe) {
            }
        }

        String takingPictureZoomMaxString = parameters
                .get("taking-picture-zoom-max");
        if (takingPictureZoomMaxString != null) {
            try {
                int tenMaxZoom = Integer.parseInt(takingPictureZoomMaxString);
                if (tenDesiredZoom > tenMaxZoom) {
                    tenDesiredZoom = tenMaxZoom;
                }
            } catch (NumberFormatException nfe) {
            }
        }

        String motZoomValuesString = parameters.get("mot-zoom-values");
        if (motZoomValuesString != null) {
            tenDesiredZoom = CameraUtil.findBestMotZoomValue(motZoomValuesString,
                    tenDesiredZoom);
        }

        String motZoomStepString = parameters.get("mot-zoom-step");
        if (motZoomStepString != null) {
            try {
                double motZoomStep = Double.parseDouble(motZoomStepString
                        .trim());
                int tenZoomStep = (int) (10.0 * motZoomStep);
                if (tenZoomStep > 1) {
                    tenDesiredZoom -= tenDesiredZoom % tenZoomStep;
                }
            } catch (NumberFormatException nfe) {
                // continue
            }
        }
        if (maxZoomString != null || motZoomValuesString != null) {
            parameters.set("zoom", String.valueOf(tenDesiredZoom / 10.0));
        }

        if (takingPictureZoomMaxString != null) {
            parameters.set("taking-picture-zoom", tenDesiredZoom);
        }
    }
}
