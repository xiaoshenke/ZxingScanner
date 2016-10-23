package wuxian.me.zxingscanner.ageraversion.camera;

import android.content.Context;
import android.hardware.Camera;
import android.os.Message;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import wuxian.me.zxingscanner.R;
import wuxian.me.zxingscanner.share.CameraConfigurationManager;
import wuxian.me.zxingscanner.share.PlanarYUVLuminanceSource;

/**
 * Created by wuxian on 20/10/2016.
 */

public class AgeraCamera implements ICamera {

    private static AgeraCamera ageraCamera;
    private Camera camera;

    private boolean isPreviewing = false;
    private AgeraPreviewCallback mPreviewCallback;

    private CameraConfigurationManager configManager;
    private Context context;
    private boolean initialized = false;

    private AgeraCamera(Context context) {
        this.context = context;
    }

    public static AgeraCamera getInstance(Context context) {
        if(ageraCamera == null){
            ageraCamera = new AgeraCamera(context);
        }

        return ageraCamera;
    }

    @Override
    public CameraConfigurationManager getConfigManager() {
        return configManager;
    }

    @Override
    public void openCamera(SurfaceHolder holder) throws IOException {
        if(camera == null){
            camera = Camera.open();

            if(camera == null){
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
    public void closeCamera(){
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    public void setPreviewCallback(Camera.PreviewCallback callback) {
        if(callback instanceof AgeraPreviewCallback){
            mPreviewCallback = (AgeraPreviewCallback) callback;

            if(camera != null){
                camera.setPreviewCallback(callback);
            }
        }
    }

    @Override
    public void startPreview() {
        if(camera != null && !isPreviewing){
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
    public void requestPreview(OnNewpreview newpreview) {
        if(camera != null && isPreviewing){
            mPreviewCallback.setOnNewpreview(newpreview);
            camera.setPreviewCallback(mPreviewCallback);
        }
    }

    private AgeraAutoFocusCallback autoFocusCallback;
    private AutofocusHandler autofocusHandler;

    @Override
    public void requestAutoFocus() {
        if (autoFocusCallback == null) {
            autoFocusCallback = new AgeraAutoFocusCallback();
        }

        if (autofocusHandler == null) {
            autofocusHandler = new AutofocusHandler();
        }

        if(camera != null && isPreviewing){
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
