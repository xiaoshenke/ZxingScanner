package wuxian.me.zxingscannerdemo;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.SurfaceView;
import android.widget.Toast;

import com.google.zxing.Result;

import java.io.IOException;

import wuxian.me.zxingscanner.QRCodeScannerImpl;
import wuxian.me.zxingscanner.decode.IDecodeResult;
import wuxian.me.zxingscanner.demo.R;
import wuxian.me.zxingscanner.scanview.ScanView;


public class MainActivity extends AppCompatActivity implements IDecodeResult {
    private ScanView mScanView;
    private SurfaceView mSurfaceView;
    private QRCodeScannerImpl mQRCodeScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        try {
            mQRCodeScanner = new QRCodeScannerImpl(mSurfaceView, mScanView, this);
        } catch (IOException e) {
            finish();
        }

    }

    private void initView() {
        mSurfaceView = (SurfaceView) findViewById(R.id.surface);
        mScanView = (ScanView) findViewById(R.id.viewfinder);
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
    }

    @Override
    public void handleDecode(Result result, Bitmap bitmap) {
        final String code = result.getText().trim();
        if (!TextUtils.isEmpty(code)) {
            Toast.makeText(this, "QRCode is " + code, Toast.LENGTH_LONG).show();
        }

    }

}
