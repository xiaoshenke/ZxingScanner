package wuxian.me.zxingscannerdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;
import wuxian.me.zxingscanner.share.camera.QRCodeCamera;
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


        /*
        create(this,new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION)).map(new Func1<Intent, String>() {
            @Override
            public String call(Intent intent) {
                return "current wifi is "+intent.getIntExtra(EXTRA_WIFI_STATE,-1);
            }
        }).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                Toast.makeText(RxMainActivity.this,s,Toast.LENGTH_LONG).show();
            }
        });
        */

    }

    @Override
    protected void onPause() {
        super.onPause();

        QRCodeCamera.getInstance(this).closeCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public static Observable<Intent> create(@NonNull final Context context,
                                            @NonNull final IntentFilter intentFilter) {
        //checkNotNull(context, "context == null");
        //checkNotNull(intentFilter, "intentFilter == null");
        return Observable.create(new Observable.OnSubscribe<Intent>() {
            @Override
            public void call(final Subscriber<? super Intent> subscriber) {
                final BroadcastReceiver receiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Log.e("OnReceive", "onReceive");
                        subscriber.onNext(intent);
                    }
                };

                context.registerReceiver(receiver, intentFilter);

                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        context.unregisterReceiver(receiver);
                    }
                }));
            }
        });
    }

}
