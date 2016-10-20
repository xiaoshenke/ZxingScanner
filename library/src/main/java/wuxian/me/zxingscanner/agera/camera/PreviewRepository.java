package wuxian.me.zxingscanner.agera.camera;

import android.graphics.Point;
import android.support.annotation.NonNull;

import com.google.android.agera.BaseObservable;
import com.google.android.agera.Supplier;

/**
 * Created by wuxian on 20/10/2016.
 * <p>
 * 单个camera.preview --> decode repository。
 */

public class PreviewRepository extends BaseObservable implements Supplier<String> {

    private static PreviewRepository cameraObservable;

    private PreviewData data;
    private String qrcode = "";

    private ICamera camera;


    public void setPreviewData(PreviewData data) {
        this.data = data;
    }

    public static PreviewRepository getInstance() {
        if (cameraObservable == null) {
            cameraObservable = new PreviewRepository();
        }

        return cameraObservable;
    }

    private PreviewRepository() {
    }

    @Override
    protected void observableDeactivated() {
        //
    }

    @Override
    protected void observableActivated() {

        //camera.setPreviewCallback();
        //camera.startPreview();
    }

    @NonNull
    @Override
    public String get() {
        return qrcode;
    }


    public static class PreviewData {
        public Point resolution;
        public byte[] data;

        public PreviewData(Point resolution, byte[] data) {
            this.resolution = resolution;
            this.data = data;
        }
    }

}
