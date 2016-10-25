package wuxian.me.zxingscanner.rxversion.version2;

import android.content.Context;
import android.text.TextUtils;

import rx.Observable;
import rx.Subscriber;
import wuxian.me.zxingscanner.share.camera.QRCodeCamera;

/**
 * Created by wuxian on 25/10/2016.
 * <p>
 * QRResult of a Camera capture a new Preview --> decode preview data flow.
 * If the string in subscribe.onNext is null,it means the decode flow fails,we need other flow.
 * Other wise we succeed in Capture a QRCode.
 * <p>
 * TODO: to be finished.
 */

public class OperatorQRResult implements Observable.Operator<String, String> {
    private Context context;
    private Observable observable;

    public OperatorQRResult(Context context) {
        this.context = context;
        //this.observable = observable;
    }

    @Override
    public Subscriber<? super String> call(Subscriber<? super String> subscriber) {

        return new QRResultSubscriber(context, subscriber);
    }

    private class QRResultSubscriber extends Subscriber<String> {
        Subscriber child;
        Context context;

        QRResultSubscriber(Context context, Subscriber child) {
            this.context = context;
            this.child = child;
        }

        @Override
        public void onCompleted() {
            child.onCompleted();
        }

        @Override
        public void onError(Throwable e) {
            child.onError(e);
        }

        @Override
        public void onNext(String s) {
            if (TextUtils.isEmpty(s)) {
                QRCodeCamera.getInstance(context).requestPreview();

                //observable.unsafeSubscribe(child);  //FIXME: 停止当前的subscriber 并重新发射一个subscriber
            } else {
                child.onNext(s);
                QRCodeCamera.getInstance(context).stopPreview();
            }

        }
    }
}
