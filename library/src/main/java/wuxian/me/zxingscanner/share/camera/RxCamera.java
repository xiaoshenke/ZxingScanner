package wuxian.me.zxingscanner.share.camera;

import android.content.Context;
import android.hardware.Camera;
import android.os.Message;
import android.view.SurfaceHolder;

import java.io.IOException;

import wuxian.me.zxingscanner.R;
import wuxian.me.zxingscanner.share.preview.RXPreviewCallback;
import wuxian.me.zxingscanner.share.preview.OnNewpreview;

/**
 * Created by wuxian on 20/10/2016.
 */

public class RxCamera implements ICamera {

    private OnNewpreview newpreview;
    private RxAutoFocusCallback autoFocusCallback;
    private AutofocusHandler autofocusHandler;

    private static RxCamera rxCamera;
    private Camera camera;

    private boolean isPreviewing = false;
    private RXPreviewCallback mPreviewCallback;

    private CameraConfigurationManager configManager;
    private Context context;
    private boolean initialized = false;

    private RxCamera(Context context) {
        this.context = context;
    }

    public static RxCamera getInstance(Context context) {
        if (rxCamera == null) {
            rxCamera = new RxCamera(context);
        }

        return rxCamera;
    }

    @Override
    public CameraConfigurationManager getConfigManager() {
        return configManager;
    }

    @Override
    public boolean isInpreview() {
        return isPreviewing;
    }

    @Override
    public void openCamera(SurfaceHolder holder) throws IOException {
        if (camera == null) {
            camera = Camera.open();

            if (camera == null) {
                throw new IOException();
            }

            camera.setPreviewDisplay(holder);

            if (!initialized) {
                initialized = true;

                this.configManager = new CameraConfigurationManager(context);
                configManager.initFromCameraParameters(camera);
            }

            configManager.setDesiredCameraParameters(camera);
        }
    }

    @Override
    public void closeCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    public void setPreviewCallback(Camera.PreviewCallback callback) {
        if (callback instanceof RXPreviewCallback) {
            mPreviewCallback = (RXPreviewCallback) callback;

            if (camera != null) {
                camera.setPreviewCallback(callback);
            }
        }
    }

    @Override
    public void startPreview() {
        if (camera != null && !isPreviewing) {
            isPreviewing = true;

            if (mPreviewCallback != null) {
                camera.setPreviewCallback(mPreviewCallback);
            }

            camera.startPreview();
            requestAutoFocus();
        }
    }

    @Override
    public void stopPreview() {
        if (camera != null && isPreviewing) {
            isPreviewing = false;
            camera.setPreviewCallback(null);
            camera.autoFocus(null);

            camera.stopPreview();
        }
    }

    @Override
    public void requestPreview() {
        if(newpreview == null){
            throw new IllegalStateException("you havn't init preview callback yet! can't call this function");
        }

        requestPreview(newpreview);
    }

    @Override
    public void requestPreview(OnNewpreview newpreview) {

        if(newpreview == null){
            throw new IllegalArgumentException("newPreview is null");
        }

        this.newpreview = newpreview;

        if (camera != null && isPreviewing) {
            mPreviewCallback.setOnNewpreview(newpreview);
            camera.setPreviewCallback(mPreviewCallback);
        }
    }

    @Override
    public void requestAutoFocus() {
        if (autoFocusCallback == null) {
            autoFocusCallback = new RxAutoFocusCallback();
        }

        if (autofocusHandler == null) {
            autofocusHandler = new AutofocusHandler();
        }

        if (camera != null && isPreviewing) {
            autoFocusCallback.setHandler(autofocusHandler);
            camera.autoFocus(autoFocusCallback);
        }
    }

    /**
     * keep auto focusing..
     */
    private class AutofocusHandler extends android.os.Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == R.id.auto_focus) {
                requestAutoFocus();
            }
        }
    }


}
