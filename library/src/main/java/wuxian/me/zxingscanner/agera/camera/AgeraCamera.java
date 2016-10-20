package wuxian.me.zxingscanner.agera.camera;

import android.hardware.Camera;

import java.io.IOException;

import wuxian.me.zxingscanner.camera.PlanarYUVLuminanceSource;

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

    private AgeraCamera(){
        init();
    }

    private void init() {
        ;//Todo init camera with default camera parameter
    }

    public static AgeraCamera getInstance(){
        if(ageraCamera == null){
            ageraCamera = new AgeraCamera();
        }

        return ageraCamera;
    }

    @Override
    public void openCamera() throws IOException{
        if(camera == null){
            camera = Camera.open();

            if(camera == null){
                throw new IOException();
            }
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