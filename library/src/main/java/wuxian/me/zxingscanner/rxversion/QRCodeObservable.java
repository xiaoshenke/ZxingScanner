package wuxian.me.zxingscanner.rxversion;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by wuxian on 23/10/2016.
 * <p>
 * In this way QRCodeObservable is not a Observable,it only has a Observable interface....
 * Fixme: a better way ? 比如说init camera 在@OnSubscribeFromCamera 类中
 * <p>
 * Todo: to be finished
 */

public class QRCodeObservable {
    private Observable observable;

    public void subscribe(Subscriber<String> subscriber) {
        //Todo: init camera etc
        if (observable == null) {
            return;
        }
        observable.subscribe(subscriber);
    }


}
