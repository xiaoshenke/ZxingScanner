package wuxian.me.zxingscanner.ageraversion.camera;

import android.hardware.Camera;
import android.view.SurfaceHolder;

import java.io.IOException;

import wuxian.me.zxingscanner.share.CameraConfigurationManager;

/**
 * Created by wuxian on 20/10/2016.
 */

public interface ICamera {
    void setPreviewCallback(Camera.PreviewCallback callback);

    void startPreview();

    void stopPreview();

    void requestPreview(OnNewpreview onNewpreview);

    void requestAutoFocus();

    void openCamera(SurfaceHolder holder) throws IOException;

    void closeCamera();

    CameraConfigurationManager getConfigManager();
}
