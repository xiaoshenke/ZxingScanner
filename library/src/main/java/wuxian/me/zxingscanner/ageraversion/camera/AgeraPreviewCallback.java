package wuxian.me.zxingscanner.ageraversion.camera;

import android.graphics.Point;
import android.hardware.Camera;
import android.util.Log;

import wuxian.me.zxingscanner.share.CameraConfigurationManager;

/**
 * Created by wuxian on 20/10/2016.
 */

public class AgeraPreviewCallback implements Camera.PreviewCallback {
    private OnNewpreview onNewpreview;
    private CameraConfigurationManager manager;

    public AgeraPreviewCallback(CameraConfigurationManager manager, OnNewpreview onNewpreview) {
        this.onNewpreview = onNewpreview;
        this.manager = manager;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Log.e("previewCallback", "receive data");
        if (manager == null) {
            return;
        }
        Point point = manager.getCameraResolution();

        if (onNewpreview == null) {
            return;
        }

        onNewpreview.onNewPreview(new PreviewData(point, data));
    }

}
