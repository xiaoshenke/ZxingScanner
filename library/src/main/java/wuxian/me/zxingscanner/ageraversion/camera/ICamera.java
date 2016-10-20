package wuxian.me.zxingscanner.ageraversion.camera;

import android.hardware.Camera;

import java.io.IOException;

/**
 * Created by wuxian on 20/10/2016.
 * <p>
 * a camera should be able to
 * 1 setCallback
 * 2 startPreview
 * 3 stopPreview
 */

public interface ICamera {
    void setPreviewCallback(Camera.PreviewCallback callback);

    void startPreview();

    void stopPreview();

    void requestPreview();

    void requestAutoFocus();

    void openCamera() throws IOException;

    void closeCamera();
}
