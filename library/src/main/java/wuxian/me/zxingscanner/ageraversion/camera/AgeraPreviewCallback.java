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
    private boolean once = false;  //for test

    public AgeraPreviewCallback(CameraConfigurationManager manager, OnNewpreview onNewpreview) {
        this.onNewpreview = onNewpreview;
        this.manager = manager;
    }

    public void setOnNewpreview(OnNewpreview onNewpreview) {
        this.onNewpreview = onNewpreview;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Log.e("previewCallback", "receive data in thread: " + Thread.currentThread());
        if (onNewpreview != null && true) {
            once = true;
            if (manager == null) {
                return;
            }
            Point point = manager.getCameraResolution();
            onNewpreview.onNewPreview(new PreviewData(point, data));
            onNewpreview = null; //防止发送preview太过频繁 --> fixme???
        }
    }

}
