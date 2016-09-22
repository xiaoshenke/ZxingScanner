package wuxian.me.zxingscannerdemo;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.SurfaceView;
import com.google.zxing.Result;
import wuxian.me.zxingscanner.QRCodeScannerImpl;
import wuxian.me.zxingscanner.IDecodeResultHandler;
import wuxian.me.zxingscanner.decoding.InactivityTimer;
import wuxian.me.zxingscanner.demo.R;
import wuxian.me.zxingscanner.view.ViewfinderView;


public class MainActivity extends AppCompatActivity implements IDecodeResultHandler {

    private ViewfinderView mViewfinderView;
    private SurfaceView mSurfaceView;
    private InactivityTimer mTimer;
    private QRCodeScannerImpl mQRCodeScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        mQRCodeScanner = new QRCodeScannerImpl(this, mSurfaceView, mViewfinderView, this);
    }

    private void initView() {
        mSurfaceView = (SurfaceView) findViewById(R.id.surface);
        mViewfinderView = (ViewfinderView) findViewById(R.id.viewfinder);
        mTimer = new InactivityTimer(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mQRCodeScanner.onActivityResume();  //don't forget to call this!
    }

    @Override
    protected void onPause() {
        super.onPause();
        mQRCodeScanner.onActivityPause();   //don't forget to call this!
    }

    @Override
    protected void onDestroy(){
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
            //Todo:add your code here
        }

    }

    @Override
    public void handleDecodeFail() {
    }
}
