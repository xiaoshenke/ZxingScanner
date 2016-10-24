package wuxian.me.zxingscanner.rxversion;

import android.view.SurfaceView;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by wuxian on 23/10/2016.
 * Todo: to be finished
 */

public class QRCodeObservable {
    private Observable observable;
    private SurfaceView surfaceView;

    public QRCodeObservable(SurfaceView surfaceView) {
        this.surfaceView = surfaceView;

    }

    public void subscribe(final Subscriber<String> subscriber) {

        observable = Observable.create(new OnSubscribeFromCamera(surfaceView))
                .observeOn(Schedulers.newThread())
                .map(new NewpreviewFunction(surfaceView.getContext()));

        observable.subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {
                if (s == null) {
                    //Todo: do the circle again
                    //AgeraCamera.getInstance(surfaceView.getContext()).requestPreview(); //???
                } else {
                    subscriber.onNext(s);
                }
            }
        });
    }


}
