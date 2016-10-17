package wuxian.me.zxingscannerdemo;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.SurfaceView;
import android.widget.Toast;

import com.google.zxing.Result;

import wuxian.me.zxingscanner.IDecodeResultHandler;
import wuxian.me.zxingscanner.QRCodeScannerImpl;
import wuxian.me.zxingscanner.decoding.InactivityTimer;
import wuxian.me.zxingscanner.demo.R;
import wuxian.me.zxingscanner.view.ScanView;


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

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTimer != null) {
            mTimer.shutdown();
        }
    }

}
