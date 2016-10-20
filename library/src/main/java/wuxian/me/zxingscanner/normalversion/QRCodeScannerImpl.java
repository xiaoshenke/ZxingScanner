package wuxian.me.zxingscanner.normalversion;

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

import wuxian.me.zxingscanner.R;
import wuxian.me.zxingscanner.normalversion.camera.CameraManager;
import wuxian.me.zxingscanner.normalversion.decoding.DecodeThread;
import wuxian.me.zxingscanner.share.view.ViewfinderResultPointCallback;
import wuxian.me.zxingscanner.share.view.IScanView;

import static android.content.Context.AUDIO_SERVICE;
import static android.content.Context.VIBRATOR_SERVICE;

/**
 * Created by wuxian on 25/8/2016.
 * <p>
 * QRCode Scanner Implementation class.
 * <p>
 * It implements these functions
 * 1 scanner begin to scan
 * 2 scanner stop scan
 * 3 decoding thread decode in background and notify QRCode scanner after producing result
 */

public final class QRCodeScannerImpl implements IQRCodeScaner, IActivityLifecycle {
    public enum State {
        PREVIEW, SUCCESS, DONE
    }

    private ScanProcessManager mManager;
    private Context mContext;

    private Vector<BarcodeFormat> mDecodeFormats;

    private IDecodeResultHandler mResultHandler;
    private SurfaceView mSurfaceView;
    private IScanView mScanView;

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

    public QRCodeScannerImpl(SurfaceView surfaceView, IScanView scanView, final IDecodeResultHandler handler) {
        mSurfaceView = surfaceView;
        mScanView = scanView;
        mContext = mSurfaceView.getContext();

        mResultHandler = new IDecodeResultHandler() {
            @Override
            public void handleDecodeSuccess(Result result, Bitmap bitmap) {
                playBeep();
                playVibrate();

                if (handler != null) {
                    handler.handleDecodeSuccess(result, bitmap);
                }
            }
        };
        mManager = new ScanProcessManager();
        initSurfaceView();
    }

    /**
     * activity OnResume的时候应该开启摄像机,解析线程
     */
    public void onActivityResume() {
        mManager.setState(State.SUCCESS);

        initMediaPlayer();
        initVibrator();

        startDecodeThread();
        if (mHasSurface) {
            initCamera();
            startScan();
        }

        mSurfaceView.requestFocus();
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

    private void scanInternal() {
        if (mManager.getCurrentState() == State.SUCCESS) {
            mManager.setState(State.PREVIEW);

            CameraManager.get().requestPreviewFrame(mDecodeThread.getHandler(), R.id.decode);
            CameraManager.get().requestAutoFocus(mManager, R.id.auto_focus);  //截图每帧数据并发送给解析线程

            if (mScanView != null) {
                mScanView.drawScanFrame();                             //绘制扫描框
            }
        }
    }

    private void stopScanInternal() {
        mManager.removeMessages(R.id.auto_focus);
        mDecodeThread.getHandler().removeMessages(R.id.decode);

        if (mScanView != null) {
            mScanView.stopDrawScanFrame();
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
                            mScanView
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
        CameraManager.init(mContext.getApplicationContext());
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
        if (CameraManager.get() != null) {
            CameraManager.get().stopPreview();
            CameraManager.get().destory();
        }
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

            mMediaPlayer = new MediaPlayer();
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

    /**
     * decode result handler
     */
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
            if (message.what == R.id.auto_focus) {
                if (mCurrentState == QRCodeScannerImpl.State.PREVIEW) {
                    CameraManager.get().requestAutoFocus(this, R.id.auto_focus);
                }
            } else if (message.what == R.id.restart_preview) {
                restartScan();
            } else if (message.what == R.id.decode_succeeded) {
                mCurrentState = State.SUCCESS;
                stopScan();

                Bundle bundle = message.getData();
                Bitmap barcode = bundle == null ? null : (Bitmap) bundle
                        .getParcelable(DecodeThread.BARCODE_BITMAP);
                if (mResultHandler != null) {
                    mResultHandler.handleDecodeSuccess((Result) message.obj, barcode);
                }
            } else if (message.what == R.id.decode_failed) {
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
