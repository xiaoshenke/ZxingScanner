package wuxian.me.zxingscanner.rxversion.version1;

import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

import rx.Observable;
import rx.Producer;
import rx.Subscriber;
import wuxian.me.zxingscanner.share.camera.QRCodeCamera;
import wuxian.me.zxingscanner.share.preview.RXPreviewCallback;
import wuxian.me.zxingscanner.share.camera.ICamera;
import wuxian.me.zxingscanner.share.preview.OnNewpreview;
import wuxian.me.zxingscanner.share.preview.PreviewData;

/**
 * Created by wuxian on 23/10/2016.
 * Origin data from camera' preview callback
 */

public class OnSubscribeFromCamera implements Observable.OnSubscribe<PreviewData>, OnNewpreview {
    private SurfaceHolder surfaceHolder;
    private SurfaceView surfaceView;
    private Subscriber subscriber;

    boolean hasSurface = false;
    boolean activated = false;
    private ICamera camera = null;

    private SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            surfaceHolder = holder;
            hasSurface = true;
            if (hasSurface && activated) {
                runCameraLoop();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            surfaceHolder = null;
            hasSurface = false;
        }
    };

    public OnSubscribeFromCamera(SurfaceView surfaceView) {
        if (surfaceView == null) {
            throw new IllegalArgumentException("surfaceView is null");
        }

        this.surfaceView = surfaceView;
        surfaceView.getHolder().addCallback(surfaceCallback);
    }

    private void runCameraLoop() {
        camera = QRCodeCamera.getInstance(surfaceView.getContext());

        if (!camera.isInpreview()) {
            try {
                camera.openCamera(surfaceHolder);
            } catch (IOException e) {
                return;
            }
            camera.setPreviewCallback(new RXPreviewCallback(camera.getConfigManager()), this);
            camera.startPreview();
        }
    }

    @Override
    public void onNewPreview(PreviewData data) {
        subscriber.setProducer(new NewPreviewProducer(subscriber, data));
        //or just
        // subscribe.call(data);
        // subscribe.onComplete();
    }

    @Override
    public void call(Subscriber<? super PreviewData> subscriber) {
        activated = true;
        this.subscriber = subscriber;

        if (activated && hasSurface) {
            runCameraLoop();
        }
    }

    private class NewPreviewProducer implements Producer {
        private static final String TAG = "Producer";
        private Subscriber child;
        private PreviewData data;

        public NewPreviewProducer(Subscriber<?> subscriber, PreviewData data) {
            this.child = subscriber;
            this.data = data;
        }

        /**
         * @param n n is not used
         */
        @Override
        public void request(long n) {

            Log.e(TAG, "request");
            if (child.isUnsubscribed()) {
                return;
            }
            child.onNext(data);

            if (child.isUnsubscribed()) {
                return;
            }
            child.onCompleted();
        }
    }
}
