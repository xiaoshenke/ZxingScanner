package wuxian.me.zxingscanner.agera.camera;

import android.graphics.Point;
import android.hardware.Camera;

/**
 * Created by wuxian on 20/10/2016.
 */

public class AgeraPreviewCallback implements Camera.PreviewCallback {
    private OnNewpreview onNewpreview;

    public AgeraPreviewCallback(OnNewpreview onNewpreview) {
        this.onNewpreview = onNewpreview;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (onNewpreview == null) {
            return;
        }
        onNewpreview.onNewPreview(new PreviewData(new Point(0, 0), data));
    }

}
