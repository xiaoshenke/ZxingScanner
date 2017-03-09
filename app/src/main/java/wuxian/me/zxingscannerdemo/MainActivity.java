package wuxian.me.zxingscannerdemo;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.SurfaceView;
import android.widget.Toast;

import com.google.zxing.Result;

import wuxian.me.zxingscanner.QRCodeScannerImpl;
import wuxian.me.zxingscanner.decode.IDecodeResultHandler;
import wuxian.me.zxingscanner.decode.InactivityTimer;
import wuxian.me.zxingscanner.demo.R;
import wuxian.me.zxingscanner.scanview.ScanView;


public class MainActivity extends AppCompatActivity implements IDecodeResultHandler {
    private ScanView mScanView;
    private SurfaceView mSurfaceView;
    private InactivityTimer mTimer;
    private QRCodeScannerImpl mQRCodeScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        mQRCodeScanner = new QRCodeScannerImpl(mSurfaceView, mScanView, this);
    }

    private void initView() {
        mSurfaceView = (SurfaceView) findViewById(R.id.surface);
        mScanView = (ScanView) findViewById(R.id.viewfinder);
        mTimer = new InactivityTimer(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mQRCodeScanner.startScan();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mQRCodeScanner.quit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTimer != null) {
            mTimer.shutdown();
        }
    }

    @Override
    public void handleDecodeSuccess(Result result, Bitmap bitmap) {
        if (mTimer != null) {
            mTimer.onActivity();
        }

        final String code = result.getText().trim();

        if (!TextUtils.isEmpty(code)) {
            if (mTimer != null) {
                mTimer.shutdown();
                mTimer = null;
            }
            Toast.makeText(this, "QRCode is " + code, Toast.LENGTH_LONG).show();
        }

    }

}
