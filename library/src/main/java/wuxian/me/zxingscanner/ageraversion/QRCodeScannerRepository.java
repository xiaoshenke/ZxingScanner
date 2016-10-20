package wuxian.me.zxingscanner.ageraversion;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.SurfaceView;

import com.google.android.agera.BaseObservable;
import com.google.android.agera.Supplier;
import com.google.zxing.Result;

import wuxian.me.zxingscanner.normalversion.IDecodeResultHandler;
import wuxian.me.zxingscanner.normalversion.QRCodeScannerImpl;
import wuxian.me.zxingscanner.share.view.IScanView;

/**
 * Created by wuxian on 18/10/2016.
 */

public class QRCodeScannerRepository extends BaseObservable implements Supplier<String> {

    private SurfaceView mSurfaceView;
    private IScanView mScanView;

    private IDecodeResultHandler mHandler;

    private String qrcode = "";

    private QRCodeScannerImpl impl;

    public QRCodeScannerRepository surfaceView(SurfaceView surfaceView) {
        mSurfaceView = surfaceView;
        return this;
    }

    public QRCodeScannerRepository scanView(IScanView scanView) {
        mScanView = scanView;
        return this;
    }

    private IDecodeResultHandler generateHandler(){
        IDecodeResultHandler handler = new IDecodeResultHandler() {
            @Override
            public void handleDecodeSuccess(Result result, Bitmap bitmap) {
                final String code = result.getText().trim();
                if (!TextUtils.isEmpty(code)) {
                    qrcode = code;

                    dispatchUpdate();  //never forget this!
                } else {
                    //something bad happens --> or you can call mQRCodeScanner.restartScan
                    impl.restartScan();
                }
            }
        };

        return handler;
    }

    @Override
    protected void observableActivated() {
        if (mSurfaceView == null) {
            throw new IllegalArgumentException("surfaceview is null");
        }
        if (mScanView == null) {
            throw new IllegalArgumentException("scanview is null");
        }

        if(impl == null){
            mHandler = generateHandler();
            impl = new QRCodeScannerImpl(mSurfaceView, mScanView, mHandler);
            impl.onActivityResume();
        }
    }

    @Override
    protected void observableDeactivated() {
        if(impl != null){
            impl.onActivityPause();
        }
    }

    @NonNull
    @Override
    public String get() {
        return qrcode;
    }
}
