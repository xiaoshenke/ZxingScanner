package wuxian.me.zxingscannerdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.widget.Toast;

import rx.Subscriber;
import rx.functions.Action1;
import wuxian.me.zxingscanner.normalversion.decoding.InactivityTimer;
import wuxian.me.zxingscanner.demo.R;
import wuxian.me.zxingscanner.rxversion.QRCodeObservable;
import wuxian.me.zxingscanner.rxversion.old.RxQRCodeScanner;
import wuxian.me.zxingscanner.share.view.ScanView;


public class RxMainActivity extends AppCompatActivity {

    private ScanView mScanView;
    private SurfaceView mSurfaceView;
    private InactivityTimer mTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

    }

    private void initView() {
        mSurfaceView = (SurfaceView) findViewById(R.id.surface);
        mScanView = (ScanView) findViewById(R.id.viewfinder);
        mTimer = new InactivityTimer(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        new QRCodeObservable(mSurfaceView).subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {
                Toast.makeText(RxMainActivity.this, "qrcode is " + s, Toast.LENGTH_LONG).show();
            }
        });

        /*
        RxQRCodeScanner.getInstance().surfaceView(mSurfaceView).scanView(mScanView).scan().subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                Toast.makeText(RxMainActivity.this, "qrcode is " + s, Toast.LENGTH_LONG).show();
            }
        });
        */
    }

    @Override
    protected void onPause() {
        super.onPause();

        //RxQRCodeScanner.getInstance().stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTimer != null) {
            mTimer.shutdown();
        }
    }

}
