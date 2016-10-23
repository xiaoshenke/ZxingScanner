package wuxian.me.zxingscanner.rxversion;

import android.view.SurfaceHolder;
import android.view.SurfaceView;

import rx.Observable;
import rx.Producer;
import rx.Subscriber;

/**
 * Created by wuxian on 23/10/2016.
 * <p>
 * Todo: to be finished
 * <p>
 * Origin data from camera' preview callback
 */

public class OnSubscribeFromCamera implements Observable.OnSubscribe<SurfaceView> {
    private SurfaceHolder surfaceHolder;

    public OnSubscribeFromCamera(SurfaceView surfaceView) {
        //Todo init surfaceHolder
    }

    @Override
    public void call(Subscriber<? super SurfaceView> subscriber) {

        subscriber.setProducer(new NewPreviewProducer(subscriber));
    }

    private class NewPreviewProducer implements Producer {

        public NewPreviewProducer(Subscriber<?> subscriber) {
            ;
        }

        /**
         * Todo: call subscriber.onNext onCompleted onError
         *
         * @param n
         */
        @Override
        public void request(long n) {

        }
    }
}
