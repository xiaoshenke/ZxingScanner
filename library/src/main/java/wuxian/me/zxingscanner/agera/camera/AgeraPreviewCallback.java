package wuxian.me.zxingscanner.agera.camera;

import android.graphics.Point;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;

import wuxian.me.zxingscanner.camera.CameraConfigurationManager;

/**
 * Created by wuxian on 20/10/2016.
 */

public class AgeraPreviewCallback implements Camera.PreviewCallback {
    private final CameraConfigurationManager configManager;
    private final boolean useOneShotPreviewCallback;
    private Handler previewHandler;
    private int previewMessage;

    AgeraPreviewCallback(){
        this(null,false);
    }

    AgeraPreviewCallback(CameraConfigurationManager configManager,
                    boolean useOneShotPreviewCallback) {
        this.configManager = configManager;
        this.useOneShotPreviewCallback = useOneShotPreviewCallback;
    }

    void setHandler(Handler previewHandler, int previewMessage) {
        this.previewHandler = previewHandler;
        this.previewMessage = previewMessage;
    }

    /**
     * 这里应该向cameraObservable输出截图
     *
     * @param data
     * @param camera
     */
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Point cameraResolution = configManager.getCameraResolution();
        if (!useOneShotPreviewCallback) {
            camera.setPreviewCallback(null);
        }
        if (previewHandler != null) {
            Message message = previewHandler.obtainMessage(previewMessage,
                    cameraResolution.x, cameraResolution.y, data);
            message.sendToTarget();
            previewHandler = null;
        } else {
            //Log.d(TAG, "Got preview callback, but no handler for it");
        }

        PreviewRepository.getInstance().setPreviewData(new PreviewRepository.PreviewData(cameraResolution, data));
        PreviewRepository.getInstance().addUpdatable(null);
    }



}
