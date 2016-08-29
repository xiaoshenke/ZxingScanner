package wuxian.me.zxingscanner;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.io.IOException;
import java.util.Vector;

import wuxian.me.zxingscanner.camera.CameraManager;
import wuxian.me.zxingscanner.decoding.DecodeThread;
import wuxian.me.zxingscanner.decoding.IDecodeResultHandler;
import wuxian.me.zxingscanner.view.ViewfinderResultPointCallback;
import wuxian.me.zxingscanner.view.ViewfinderView;

import static android.content.Context.AUDIO_SERVICE;
import static android.content.Context.VIBRATOR_SERVICE;

/**
 * Created by wuxian on 25/8/2016.
 * <p>
 * scanner的实现类。activity只需传入context,surfaceview,viewerfinderview,以及结果回调即可
 * <p>
 * 应该具备的能力
 * 1 扫描框开始扫描(同时解析线程开始工作) --> 需要摄像头(surfaceview),需要扫描框(viewfinderview)
 * 2 扫描框停止扫描
 * 3 扫描框重新开始扫描
 * 4 摄像头应该是全屏的。而且只有屏幕退到后台或不可见的时候摄像头才需要关闭。其它情况不管是否正在进行扫描(解析),摄像头都应该是打开的
 * 5 在实现中。扫描框的扫描动作和实际解析线程的动作应该是同步的,扫描框停止扫描的时候,解析线程也应该停止或暂停。
 *
 * //Todo:紧急情况被系统干掉的实现
 *
 */

public class QRCodeScannerImpl implements IQRCodeScaner, IActivityLifeCycle {

    public enum State {
        PREVIEW, SUCCESS, DONE
    }

    private ScanProcessManager mManager;
    private Context mContext;
    
    private Vector<BarcodeFormat> mDecodeFormats;

    private IDecodeResultHandler mResultHandler;
    private SurfaceView mSurfaceView;
    private ViewfinderView mViewfinderView;

    private boolean mHasSurface;
    private SurfaceHolder mSurfaceHolder;

    private MediaPlayer mMediaPlayer;
    private boolean mPlayBeep;
    private final float BEEP_VOLUME = 0.10f;

    private Vibrator mVibrator;
    private final long VIBRATE_DURATION = 200L;

    private DecodeThread mDecodeThread;

    private SurfaceHolder.Callback mCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (!mHasSurface) {
                mHasSurface = true;
                initCamera();

                startScan();
            }
        }
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            mHasSurface = false;
        }
    };

    public QRCodeScannerImpl(Context context, SurfaceView surfaceView, ViewfinderView viewfinderView, final IDecodeResultHandler handler) {
        mContext = context;
        mSurfaceView = surfaceView;
        mViewfinderView = viewfinderView;

        mResultHandler = new IDecodeResultHandler() {
            @Override
            public void handleDecodeSuccess(Result result, Bitmap bitmap) {  //扫描结束时的"bee"声音应该在这里控制
                playBeep();
                playVibrate();

                if (handler != null) {
                    handler.handleDecodeSuccess(result, bitmap);
                }
            }

            @Override
            public void handleDecodeFail() {
            }
        };
        mManager = new ScanProcessManager();


        initSurfaceView();  //按理说应该放在onResume过程 但是surfaceview是一个异步过程 因此放在构造函数中
    }

    /**
     * activity OnResume的时候应该开启摄像机,解析线程
     */
    public void onActivityResume() {
        mManager.setState(State.SUCCESS);

        initMediaPlayer();
        initVibrator();

        startDecodeThread();
        if (mHasSurface) {             //从后台切入前台 mHasSurface为true,因此这里打开相机并且开始scan
            initCamera();
            startScan();
        }

        mSurfaceView.requestFocus(); //???
    }

    /**
     * activity onPause的时候应该关闭摄像机,解析线程和扫描框
     */
    public void onActivityPause() {
        mManager.setState(State.DONE);
        mManager.unInit();

        stopScan();
        destroyCamera();

        quitDecodeThread();
    }

    /**
     * scan的实现包括扫描框和扫描线程。
     * 因此调用这个函数之前应该确保
     * 1 摄像机已经打开并正常工作
     * 2 扫描线程已经打开并正常工作
     * <p>
     * 3 当前state为State.SUCCESS ????
     */
    private void scanInternal() {
        if (mManager.getCurrentState() == State.SUCCESS) {
            mManager.setState(State.PREVIEW);

            CameraManager.get().requestPreviewFrame(mDecodeThread.getHandler(), R.id.decode);
            CameraManager.get().requestAutoFocus(mManager, R.id.auto_focus);  //截图每帧数据并发送给解析线程

            if (mViewfinderView != null) {
                mViewfinderView.drawViewfinder();                             //绘制扫描框
            }
        }
    }

    private void stopScanInternal() {
        mManager.removeMessages(R.id.auto_focus);
        mDecodeThread.getHandler().removeMessages(R.id.decode);

        if (mViewfinderView != null) {
            mViewfinderView.stopDrawViewfinder();
        }

        mManager.setState(State.SUCCESS);
    }

    /**
     * 扫描框和解析线程开始工作 摄像机照常工作
     */
    @Override
    public void startScan() {
        scanInternal();
    }

    @Override
    public void restartScan() {
        stopScanInternal();
        scanInternal();
    }

    /**
     * 扫描框停止工作,解析线程停止"decode"工作(不退出线程) 摄像机照常工作
     */
    @Override
    public void stopScan() {
        stopScanInternal();
    }


    private void startDecodeThread() {
        if (mDecodeThread == null) {
            mDecodeThread = new DecodeThread(mManager, mDecodeFormats, null,
                    new ViewfinderResultPointCallback(
                            mViewfinderView
                    ));
            mDecodeThread.start();
        }
    }

    private void quitDecodeThread() {
        Message quit = Message.obtain(mDecodeThread.getHandler(), R.id.quit);
        quit.sendToTarget();
        try {
            mDecodeThread.join();
        } catch (InterruptedException e) {
            // continue
        }

        mDecodeThread = null;
    }

    private void initCamera() {
        CameraManager.init(mContext.getApplicationContext());  //单例初始化
        try {
            CameraManager.get().openDriver(mSurfaceHolder);

            CameraManager.get().startPreview();
        } catch (IOException e) {
            return;
        } catch (RuntimeException e) {
            return;
        }

    }

    private void destroyCamera() {
        CameraManager.get().stopPreview();  //摄像机抓取的图片不再绘制在surfaceview
        CameraManager.get().destory();
    }


    private void playBeep() {
        if (mPlayBeep && mMediaPlayer != null) {
            mMediaPlayer.start();
        }
    }

    private void playVibrate() {
        mVibrator.vibrate(VIBRATE_DURATION);
    }



    private void initSurfaceView() {
        mHasSurface = false;

        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(mCallback);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    private void initVibrator() {
        if (mVibrator == null) {
            mVibrator = (Vibrator) mContext.getSystemService(VIBRATOR_SERVICE);
        }
    }

    private void initMediaPlayer() {
        AudioManager audioManager = (AudioManager) mContext.getSystemService(AUDIO_SERVICE);
        if (audioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            mPlayBeep = false;
        } else {
            mPlayBeep = true;
        }

        if (!mPlayBeep) {
            return;
        }

        if (mMediaPlayer == null) {
            ((Activity) mContext).setVolumeControlStream(AudioManager.STREAM_MUSIC);

            mMediaPlayer = new MediaPlayer();  //mMediaPlayer.destroy()?
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mMediaPlayer.seekTo(0);
                }
            });

            AssetFileDescriptor file = mContext.getResources().openRawResourceFd(
                    R.raw.beep);

            try {
                mMediaPlayer.setDataSource(file.getFileDescriptor(),
                        file.getStartOffset(), file.getLength());
                file.close();
                mMediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mMediaPlayer.prepare();
            } catch (IOException e) {
                mMediaPlayer = null;
            }

        }
    }


    public class ScanProcessManager extends Handler {
        private State mCurrentState;

        public State getCurrentState() {
            return mCurrentState;
        }

        public void setState(State state) {
            mCurrentState = state;
        }

        @Override
        public void handleMessage(Message message) {
            if(message.what == R.id.auto_focus){
                if (mCurrentState == QRCodeScannerImpl.State.PREVIEW) {
                    CameraManager.get().requestAutoFocus(this, R.id.auto_focus);
                }
            }else if(message.what == R.id.restart_preview){
                restartScan();
            }else if(message.what == R.id.decode_succeeded){
                mCurrentState = State.SUCCESS;
                stopScan();   //停止扫描

                Bundle bundle = message.getData();
                Bitmap barcode = bundle == null ? null : (Bitmap) bundle
                        .getParcelable(DecodeThread.BARCODE_BITMAP);
                if (mResultHandler != null) {
                    mResultHandler.handleDecodeSuccess((Result) message.obj, barcode);
                }
            }else if(message.what == R.id.decode_failed){
                mCurrentState = State.PREVIEW;
                if (mDecodeThread == null) {
                    return;
                }
                CameraManager.get().requestPreviewFrame(mDecodeThread.getHandler(),
                        R.id.decode);
            }
        }

        public void unInit() {
            removeMessages(R.id.decode_succeeded);
            removeMessages(R.id.decode_failed);
        }
    }
}
