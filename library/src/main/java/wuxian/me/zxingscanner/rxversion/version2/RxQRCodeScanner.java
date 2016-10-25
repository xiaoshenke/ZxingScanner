package wuxian.me.zxingscanner.rxversion.version2;

import android.view.SurfaceView;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import wuxian.me.zxingscanner.rxversion.NewpreviewFunction;
import wuxian.me.zxingscanner.rxversion.OnSubscribeFromCamera;

/**
 * Created by wuxian on 24/10/2016.
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
        return ret.lift(new OperatorQRResult(surfaceView.getContext()));
    }
}
