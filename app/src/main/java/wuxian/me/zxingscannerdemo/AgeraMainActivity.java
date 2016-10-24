package wuxian.me.zxingscannerdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.widget.Toast;
import com.google.android.agera.Updatable;

import wuxian.me.zxingscanner.ageraversion.QRCodeCameraRepository;
import wuxian.me.zxingscanner.demo.R;
import wuxian.me.zxingscanner.share.view.ScanView;

/**
 * Created by wuxian on 18/10/2016.
 */
public class AgeraMainActivity extends AppCompatActivity implements Updatable {

    private ScanView mScanView;
    private SurfaceView mSurfaceView;

    private QRCodeCameraRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        repository = new QRCodeCameraRepository(this, mSurfaceView);
    }

    private void initView() {
        mSurfaceView = (SurfaceView) findViewById(R.id.surface);
        mScanView = (ScanView) findViewById(R.id.viewfinder);
    }

    @Override
    public void onResume() {
        super.onResume();

        repository.addUpdatable(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        repository.removeUpdatable(this);
    }

    @Override
    public void update() {
        Toast.makeText(this, "qrcode is " + repository.get(), Toast.LENGTH_LONG).show();

    }
}
