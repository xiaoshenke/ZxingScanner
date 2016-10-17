package wuxian.me.zxingscanner.rx;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.SurfaceView;

import com.google.zxing.Result;
import rx.Single;
import rx.SingleSubscriber;
import wuxian.me.zxingscanner.IDecodeResultHandler;
import wuxian.me.zxingscanner.QRCodeScannerImpl;
import wuxian.me.zxingscanner.view.IScanView;

/**
 * Created by wuxian on 17/10/2016.
 */

public class RxQRCodeScanner implements IRXScanner {
    private static RxQRCodeScanner scanner;
    private SurfaceView mSurfaceView;
    private IScanView mScanView;
    private IDecodeResultHandler mHandler;

    private QRCodeScannerImpl impl;

    private RxQRCodeScanner() {
    }

    public static RxQRCodeScanner getInstance() {
        if (scanner == null) {
            scanner = new RxQRCodeScanner();
        }

        return scanner;
    }

    public RxQRCodeScanner surfaceView(SurfaceView surfaceView) {
        mSurfaceView = surfaceView;
        return this;
    }

    public RxQRCodeScanner scanView(IScanView scanView) {
        mScanView = scanView;
        return this;
    }

    private IDecodeResultHandler generateHandler(final SingleSubscriber<? super String> subscriber) {
        IDecodeResultHandler result = new IDecodeResultHandler() {
            @Override
            public void handleDecodeSuccess(Result result, Bitmap bitmap) {
                final String code = result.getText().trim();
                if (!TextUtils.isEmpty(code)) {
                    subscriber.onSuccess(code);
                } else {
                    //something bad happens --> you can call mQRCodeScanner.restartScan
                }
            }
        };

        return result;
    }

    @Override
    public void stop() {
        if (impl != null) {
            impl.onActivityPause();
        }
    }


    @Override
    public Single<String> scan() {
        if (mSurfaceView == null) {
            throw new IllegalArgumentException("surfaceview is null");
        }
        if (mScanView == null) {
            throw new IllegalArgumentException("scanview is null");
        }

        Single<String> single = Single.create(new Single.OnSubscribe<String>() {
            @Override
            public void call(SingleSubscriber<? super String> singleSubscriber) {
                if (impl == null) {
                    mHandler = generateHandler(singleSubscriber);
                    impl = new QRCodeScannerImpl(mSurfaceView, mScanView, mHandler);
                    impl.onActivityResume();
                }
            }
        });

        return single;
    }
}
