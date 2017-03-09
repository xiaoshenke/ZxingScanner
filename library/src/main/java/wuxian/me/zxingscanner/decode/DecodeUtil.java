package wuxian.me.zxingscanner.decode;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.view.Display;
import android.view.WindowManager;

import wuxian.me.zxingscanner.camera.Camera;
import wuxian.me.zxingscanner.camera.CameraConfigMgr;

/**
 * Created by wuxian on 9/3/2017.
 */

public class DecodeUtil {
    private DecodeUtil() {
    }

    public static PlanarYUVSource buildLuminanceSource(
            @NonNull Camera camera, byte[] data,
            int width, int height) {
        CameraConfigMgr mConfigMgr = camera.getConfig();
        Rect rect = DecodeUtil.getFramingRectInPreview(mConfigMgr);
        int previewFormat = mConfigMgr.getPreviewFormat();
        String previewFormatString = mConfigMgr.getPreviewFormatString();

        switch (previewFormat) {
            case PixelFormat.YCbCr_420_SP:
            case PixelFormat.YCbCr_422_SP:
                return new PlanarYUVSource(data, width, height, rect.left,
                        rect.top, rect.width(), rect.height());
            default:
                if ("yuv420p".equals(previewFormatString)) {
                    return new PlanarYUVSource(data, width, height,
                            rect.left, rect.top, rect.width(), rect.height());
                }
        }
        throw new IllegalArgumentException("Unsupported picture format: "
                + previewFormat + '/' + previewFormatString);
    }

    public static Rect getFramingRectForDraw(@NonNull Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point screenResolution = new Point(display.getWidth(), display.getHeight());  //拿到屏幕

        int width = screenResolution.x * 2 / 3;
        int height = screenResolution.y * 2 / 3;
        if (width >= height) {
            width = height;
        } else {
            height = width;
        }

        int leftOffset = (screenResolution.x - width) / 2;
        int topOffset = (screenResolution.y - height) / 3;
        return new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
    }


    public static Rect getFramingRectInPreview(@NonNull CameraConfigMgr configMgr) {
        return getFramingRectInPreview(configMgr.getCameraResolution(),
                configMgr.getScreenResolution());
    }


    private static Rect getFramingRectInPreview(Point cameraResolution,
                                                Point screenResolution) {
        Rect rect = new Rect(getFramingRect(screenResolution));
        rect.left = rect.left * cameraResolution.y / screenResolution.x;
        rect.right = rect.right * cameraResolution.y / screenResolution.x;
        rect.top = rect.top * cameraResolution.x / screenResolution.y;
        rect.bottom = rect.bottom * cameraResolution.x / screenResolution.y;
        return rect;
    }

    private static Rect getFramingRect(Point screenResolution) {
        int width = screenResolution.x;
        int height = screenResolution.y;
        int leftOffset = (screenResolution.x - width) / 2;
        int topOffset = (screenResolution.y - height) / 3;
        return new Rect(leftOffset, topOffset, leftOffset + width,
                topOffset + height);
    }
}
