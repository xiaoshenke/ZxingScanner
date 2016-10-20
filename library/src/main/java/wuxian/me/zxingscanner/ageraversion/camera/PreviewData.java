package wuxian.me.zxingscanner.ageraversion.camera;

import android.graphics.Point;

/**
 * Created by wuxian on 20/10/2016.
 */

public class PreviewData {
    public Point resolution;
    public byte[] data;

    public PreviewData(Point resolution, byte[] data) {
        this.resolution = resolution;
        this.data = data;
    }
}
