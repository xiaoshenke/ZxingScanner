package wuxian.me.zxingscanner.share.camera;

import android.hardware.Camera;
import android.view.SurfaceHolder;

import java.io.IOException;

import wuxian.me.zxingscanner.share.preview.OnNewpreview;

/**
 * Created by wuxian on 20/10/2016.
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
