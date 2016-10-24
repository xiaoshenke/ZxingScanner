package wuxian.me.zxingscanner.rxversion;

import android.view.SurfaceView;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import wuxian.me.zxingscanner.ageraversion.camera.AgeraCamera;

/**
 * Created by wuxian on 23/10/2016.
 */

public class QRCodeObservable {
    private Observable observable;
    private SurfaceView surfaceView;

    private Subscriber<String> qrSubscriber;

    public QRCodeObservable(SurfaceView surfaceView) {
        this.surfaceView = surfaceView;
    }

    public void subscribe(final Subscriber<String> subscriber) {

        observable = Observable.create(new OnSubscribeFromCamera(surfaceView))
                .observeOn(Schedulers.newThread())
                .map(new NewpreviewFunction(surfaceView.getContext()));

        if(qrSubscriber == null){
            qrSubscriber = new Subscriber<String>() {
                @Override
                public void onCompleted() {
                }

                @Override
                public void onError(Throwable e) {
                }

                @Override
                public void onNext(String s) {
                    if (s == null) {
                        AgeraCamera.getInstance(surfaceView.getContext()).requestPreview();
                        observable.subscribe(QRCodeObservable.this.qrSubscriber);
                    } else {
                        subscriber.onNext(s);
                        AgeraCamera.getInstance(surfaceView.getContext()).stopPreview();


                    }
                }
            };
        }

        observable.subscribe(qrSubscriber);
    }


}
