package wuxian.me.zxingscanner.camera;

import android.hardware.Camera;
import android.view.SurfaceHolder;

import java.io.IOException;

import wuxian.me.zxingscanner.preview.OnNewpreview;

/**
 * Created by wuxian on 20/10/2016.
 *
 * camera only used as part of a QRCode camera,only requires very few functions.
 */

public interface ICamera {

    void setPreviewCallback(Camera.PreviewCallback callback, OnNewpreview onNewpreview);

    void startPreview();

    void stopPreview();

    void requestPreview();

    void requestPreview(OnNewpreview onNewpreview);

    void requestAutoFocus();

    void openCamera(SurfaceHolder holder) throws IOException;

    void closeCamera();

    CameraConfigurationManager getConfigManager();

    boolean isInpreview();
}
