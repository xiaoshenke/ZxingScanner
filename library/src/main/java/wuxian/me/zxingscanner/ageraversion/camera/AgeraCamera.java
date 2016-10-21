package wuxian.me.zxingscanner.ageraversion.camera;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;

import java.io.IOException;

import wuxian.me.zxingscanner.share.CameraConfigurationManager;
import wuxian.me.zxingscanner.share.PlanarYUVLuminanceSource;

/**
 * Created by wuxian on 20/10/2016.
 *
 * Todo: the camera should keep focusing... impl?
 */

public class AgeraCamera implements ICamera {

    private static AgeraCamera ageraCamera;
    private Camera camera;

    private boolean isPreviewing = false;
    private Camera.PreviewCallback mPreviewCallback;

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
        mPreviewCallback = callback;
        if(camera != null){
            camera.setPreviewCallback(callback);
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
        }

    }

    @Override
    public void stopPreview() {
        isPreviewing = false;
        if(camera != null){
            camera.stopPreview();
        }

    }

    /**
     * 调用这个函数能够取到一张preview
     */
    @Override
    public void requestPreview() {
        if(camera != null && isPreviewing){
            camera.setPreviewCallback(mPreviewCallback);
        }
    }

    /**
     * todo: add autofocus callback
     */
    @Override
    public void requestAutoFocus() {
        if(camera != null && isPreviewing){
            camera.autoFocus(null);
        }
    }

    /**
     * Todo
     */
    public static PlanarYUVLuminanceSource buildLuminanceSource(byte[] data,
                                                                int width, int height){
        return null;
    }
}
