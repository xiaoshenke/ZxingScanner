package wuxian.me.zxingscanner.rxversion;

import android.view.SurfaceView;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

/**
 * Created by wuxian on 24/10/2016.
 * <p>
 * Todo: to be finished. new version of @QRCodeObservable
 */

public class RxQRCodeScanner {
    private RxQRCodeScanner() {
        throw new AssertionError("no instances");
    }

    public static Observable<String> sufaceView(SurfaceView surfaceView) {

        if (surfaceView == null) {
            throw new AssertionError("surfaceview is null");
        }

        Observable<String> ret = Observable.create(new OnSubscribeFromCamera(surfaceView))
                .observeOn(Schedulers.newThread())
                .map(new NewpreviewFunction(surfaceView.getContext()))
                .observeOn(AndroidSchedulers.mainThread());
        return ret.lift(new OperatorQRResult(surfaceView.getContext(), ret));
    }
}
