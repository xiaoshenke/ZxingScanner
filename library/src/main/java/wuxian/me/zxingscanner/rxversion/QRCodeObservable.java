package wuxian.me.zxingscanner.rxversion;

import android.view.SurfaceView;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import wuxian.me.zxingscanner.share.camera.QRCodeCamera;

/**
 * Created by wuxian on 23/10/2016.
 *
 * Todo add ScanView
 */

public class QRCodeObservable {
    private Observable observable;
    private SurfaceView surfaceView;

    private Subscriber<String> subscriber;

    public QRCodeObservable(SurfaceView surfaceView) {
        this.surfaceView = surfaceView;
    }

    /**
     * refactor using @OperatorQRResult
     *
     * @return
     */
    private Subscriber<String> getQrSubscriber() {
        if (subscriber == null) {
            throw new IllegalStateException("subscriber is null!");
        }

        Subscriber<String> ret = new Subscriber<String>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(String s) {
                if (s == null) {
                    QRCodeCamera.getInstance(surfaceView.getContext()).requestPreview();
                    observable.subscribe(getQrSubscriber());
                } else {
                    subscriber.onNext(s);
                    QRCodeCamera.getInstance(surfaceView.getContext()).stopPreview();
                }
            }
        };

        return ret;
    }

    public void subscribe(final Subscriber<String> subscriber) {

        observable = Observable.create(new OnSubscribeFromCamera(surfaceView))
                .observeOn(Schedulers.newThread())
                .map(new NewpreviewFunction(surfaceView.getContext()))
                .observeOn(AndroidSchedulers.mainThread());
        //.lift(new OperatorQRResult(surfaceView.getContext()));

        this.subscriber = subscriber;
        observable.subscribe(getQrSubscriber());
    }

}
