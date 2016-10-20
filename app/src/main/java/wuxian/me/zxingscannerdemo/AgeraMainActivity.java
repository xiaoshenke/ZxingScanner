package wuxian.me.zxingscannerdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.widget.Toast;
import com.google.android.agera.Updatable;

import wuxian.me.zxingscanner.agera.QRCodeScannerRepository;
import wuxian.me.zxingscanner.demo.R;
import wuxian.me.zxingscanner.view.ScanView;

/**
 * Created by wuxian on 18/10/2016.
 */
public class AgeraMainActivity extends AppCompatActivity implements Updatable {

    private ScanView mScanView;
    private SurfaceView mSurfaceView;

    private QRCodeScannerRepository mRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        mRepository = new QRCodeScannerRepository().surfaceView(mSurfaceView).scanView(mScanView);
    }

    private void initView() {
        mSurfaceView = (SurfaceView) findViewById(R.id.surface);
        mScanView = (ScanView) findViewById(R.id.viewfinder);
    }

    @Override
    public void onResume() {
        super.onResume();
        //mRepository.addUpdatable(this);

    }

    @Override
    protected void onPause() {
        super.onPause();

        //mRepository.removeUpdatable(this);
    }

    @Override
    public void update() {
        Toast.makeText(this, "qrcode is " + mRepository.get(), Toast.LENGTH_LONG).show();
    }
}
