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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.zxing.Result;

import java.io.IOException;

import wuxian.me.zxingscanner.camera.Camera;
import wuxian.me.zxingscanner.decode.IDecodeResult;
import wuxian.me.zxingscanner.decode.ViewfinderResultPointCallback;
import wuxian.me.zxingscanner.scanview.IScanView;

import static android.content.Context.AUDIO_SERVICE;
import static android.content.Context.VIBRATOR_SERVICE;

/**
 * Created by wuxian on 25/8/2016.
 */

public final class QRCodeScannerImpl implements IQRCodeScaner {

    private DecodeManager mDecodeManager;
    private Context mContext;
    private IDecodeResult mResultHandler;
    private SurfaceView mSurfaceView;
    private Camera mCamera;
    private boolean mCameraReady = false;

    @Nullable
    private IScanView mScanView;

    private Camera.ICameraListener mCameraListener = new Camera.ICameraListener() {
        @Override
        public void onCameraOpen() {
            mCameraReady = true;
            maybeStartScan();
        }

        @Override
        public void onOpenError() {
            //Todo:
        }
    };

    public QRCodeScannerImpl(@NonNull SurfaceView surfaceView,
                             @Nullable IScanView scanView,
                             @NonNull final IDecodeResult handler) throws IOException {
        mSurfaceView = surfaceView;
        mScanView = scanView;
        mContext = mSurfaceView.getContext();
        mCamera = Camera.getInstance(mContext.getApplicationContext());
        mCamera.openAndDisplay(surfaceView, mCameraListener);

        mDecodeManager = new DecodeManager();
        mDecodeManager.setState(DecodeManager.DONE);

        mResultHandler = new IDecodeResult() {
            @Override
            public void handleDecode(Result result, Bitmap bitmap) {
                playBeep();
                playVibrate();
                if (handler != null) {
                    handler.handleDecode(result, bitmap);
                }
            }
        };

        initMediaPlayer();
        initVibrator();
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


    private MediaPlayer mMediaPlayer;
    private boolean mPlayBeep;
    private final float BEEP_VOLUME = 0.10f;
    private Vibrator mVibrator;
    private final long VIBRATE_DURATION = 200L;

    private boolean mStartScanCalled = false;

    private DecodeThread mDecodeThread;


    @Override
    public void startScan() {
        //it should be in STATE.DONE
        if (mDecodeManager.getCurrentState() != DecodeManager.DONE) {
            return;
        }
        startDecodeThread(); //不管有没有摄像机 开启解析线程
        maybeStartScan();

        if (mScanView != null) {
            mScanView.drawScanFrame();                             //绘制扫描框
        }
    }

    private synchronized void maybeStartScan() {
        if (mCameraReady && mStartScanCalled) {
            scanInternal();
        }
    }

    private void scanInternal() {
        if (mDecodeManager.getCurrentState() == DecodeManager.SUCCESS) {
            mDecodeManager.setState(DecodeManager.PREVIEW);

            mCamera.requestPreviewFrame(mDecodeThread.getHandler(), R.id.decode);
            mCamera.requestAutoFocus(mDecodeManager, R.id.auto_focus);  //截图每帧数据并发送给解析线程
        }
    }

    public void quit() {
        mDecodeManager.setState(DecodeManager.DONE);
        mDecodeManager.unInit();
        stopScan();
        destroyCamera();
        quitDecodeThread();
    }

    private void stopScanInternal() {
        mDecodeManager.removeMessages(R.id.auto_focus);
        mDecodeThread.getHandler().removeMessages(R.id.decode);
        mDecodeManager.setState(DecodeManager.SUCCESS);
    }

    @Override
    public void restartScan() {
        stopScanInternal();
        scanInternal();
    }

    @Override
    public void stopScan() {
        stopScanInternal();
        if (mScanView != null) {
            mScanView.stopDrawScanFrame();
        }
    }

    private void startDecodeThread() {
        if (mDecodeThread == null) {
            mDecodeThread = new DecodeThread(mDecodeManager, null,
                    new ViewfinderResultPointCallback(mScanView));
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

    private void destroyCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.destory();
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

    public class DecodeManager extends Handler {
        public static final int PREVIEW = 0;
        public static final int SUCCESS = 1;
        public static final int DONE = 2;

        private int mCurrentState = DONE;

        public int getCurrentState() {
            return mCurrentState;
        }

        public void setState(int state) {
            mCurrentState = state;
        }

        @Override
        public void handleMessage(Message message) {
            if (message.what == R.id.auto_focus) {
                if (mCurrentState == PREVIEW) {
                    mCamera.requestAutoFocus(this, R.id.auto_focus);
                }
            } else if (message.what == R.id.restart_preview) {
                restartScan();
            } else if (message.what == R.id.decode_succeeded) {
                mCurrentState = SUCCESS;
                stopScan();

                Bundle bundle = message.getData();
                Bitmap barcode = bundle == null ? null : (Bitmap) bundle
                        .getParcelable(DecodeThread.BARCODE_BITMAP);
                if (mResultHandler != null) {
                    mResultHandler.handleDecode((Result) message.obj, barcode);
                }
            } else if (message.what == R.id.decode_failed) {
                mCurrentState = PREVIEW;
                if (mDecodeThread == null) {
                    return;
                }
                mCamera.requestPreviewFrame(mDecodeThread.getHandler(),
                        R.id.decode);
            }
        }

        public void unInit() {
            removeMessages(R.id.decode_succeeded);
            removeMessages(R.id.decode_failed);
        }
    }
}
