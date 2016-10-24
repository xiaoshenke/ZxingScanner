package wuxian.me.zxingscannerdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.widget.Toast;

import rx.Subscriber;
import wuxian.me.zxingscanner.share.camera.RxCamera;
import wuxian.me.zxingscanner.demo.R;
import wuxian.me.zxingscanner.rxversion.QRCodeObservable;
import wuxian.me.zxingscanner.share.view.ScanView;


public class RxMainActivity extends AppCompatActivity {
    private ScanView mScanView;
    private SurfaceView mSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

    }

    private void initView() {
        mSurfaceView = (SurfaceView) findViewById(R.id.surface);
        mScanView = (ScanView) findViewById(R.id.viewfinder);
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        RxCamera.getInstance(this).closeCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
