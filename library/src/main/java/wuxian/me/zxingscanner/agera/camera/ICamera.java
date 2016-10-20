package wuxian.me.zxingscanner.agera.camera;

import android.hardware.Camera;

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
}
