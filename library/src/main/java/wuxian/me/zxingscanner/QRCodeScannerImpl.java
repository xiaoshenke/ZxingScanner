package wuxian.me.zxingscanner;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.SurfaceView;

import com.google.zxing.Result;

import java.io.IOException;

import wuxian.me.zxingscanner.camera.Camera;
import wuxian.me.zxingscanner.decode.DecodeConstants;
import wuxian.me.zxingscanner.decode.DecodeThread;
import wuxian.me.zxingscanner.decode.PreviewCallback;
import wuxian.me.zxingscanner.scanview.RedPointCallback;
import wuxian.me.zxingscanner.scanview.IScanView;

import static android.content.Context.AUDIO_SERVICE;
import static android.content.Context.VIBRATOR_SERVICE;

/**
 * Created by wuxian on 25/8/2016.
 */

public final class QRCodeScannerImpl implements IQRCodeScaner {

    private DecodeStateHandler mDecodeStateHandler;
    private Context mContext;
    private IScanResult mResultHandler;
    private SurfaceView mSurfaceView;
    private Camera mCamera;
    private boolean mCameraReady = false;
    private boolean mDecodeThreadStart = false;

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
                             @NonNull final IScanResult handler) throws IOException {
        mSurfaceView = surfaceView;
        mScanView = scanView;
        mContext = mSurfaceView.getContext();
        mCamera = Camera.getInstance(mContext.getApplicationContext());
        mCamera.openAndDisplay(surfaceView, mCameraListener);

        mDecodeStateHandler = new DecodeStateHandler();
        mDecodeStateHandler.setState(DecodeStateHandler.DONE);

        mResultHandler = new IScanResult() {
            @Override
            public void onScanResult(Result result, Bitmap bitmap) {
                playBeep();
                playVibrate();
                if (handler != null) {
                    handler.onScanResult(result, bitmap);
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
        mStartScanCalled = true;
        //it should be in STATE.DONE
        if (mDecodeStateHandler.getCurrentState() != DecodeStateHandler.DONE) {
            return;
        }
        startDecodeThread(); //不管有没有摄像机 开启解析线程
        maybeStartScan();

        if (mScanView != null) {
            mScanView.drawScanFrame(); //绘制扫描框
        }
    }

    private synchronized void maybeStartScan() {
        if (mCameraReady && mStartScanCalled && mDecodeThreadStart) {
            scanInternal();
        }
    }

    private PreviewCallback mPreviewCallback;

    private PreviewCallback getPreviewCallback() {
        if (mPreviewCallback == null) {
            mPreviewCallback = new PreviewCallback(
                    mCamera.getConfig(),
                    mDecodeThread.decodeHandler(),
                    Integer.parseInt(Build.VERSION.SDK) > 3);
        }
        mPreviewCallback.setRequestAnother(true);
        return mPreviewCallback;
    }

    private void scanInternal() {
        if (mDecodeStateHandler.getCurrentState() == DecodeStateHandler.SUCCESS || mDecodeStateHandler.getCurrentState() == DecodeStateHandler.DONE) {
            mDecodeStateHandler.setState(DecodeStateHandler.PREVIEW);

            startAutoFocus();
            startRequestPreview();
        }
    }

    private void startAutoFocus() {
        if (mCamera != null) {
            mCamera.startAutoFocus();
        }
    }

    private void startRequestPreview() {
        if (mCamera != null) {
            mCamera.requestPreview(getPreviewCallback());
        }
    }

    public void quit() {
        stopScan();
        quitDecodeThread();
        destroyCamera();
        mDecodeStateHandler.setState(DecodeStateHandler.DONE);

    }

    private void stopScanInternal() {
        mCamera.stopAutoFocus();
        if (mDecodeThread != null) {
            mDecodeThread.stopDecode();
        }
        mDecodeStateHandler.setState(DecodeStateHandler.SUCCESS);
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
            mDecodeThread = new DecodeThread(mCamera, mDecodeStateHandler, null,
                    new RedPointCallback(mScanView));
            mDecodeThread.start();
            mDecodeThreadStart = true;

            maybeStartScan();
        }
    }

    private void quitDecodeThread() {
        if (mDecodeThread == null) {
            return;
        }
        mDecodeThread.stopThreadSafely();
        try {
            mDecodeThread.join();
        } catch (InterruptedException e) {
        }
        mDecodeThread = null;
        mDecodeThreadStart = false;
    }

    private void destroyCamera() {
        if (mCamera != null) {
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

    public class DecodeStateHandler extends Handler {
        public static final int PREVIEW = 0;
        public static final int SUCCESS = 1;
        public static final int DONE = 2;

        private int mCurrentState = DONE;

        public int getCurrentState() {
            return mCurrentState;
        }

        public void setState(int state) {
            mCurrentState = state;

            if (state == DONE) {
                removeMessages(DecodeConstants.Action.ACTION_DECODE_SUCCESS);
                removeMessages(DecodeConstants.Action.ACTION_DECODE_FAIL);
            }
        }

        @Override
        public void handleMessage(Message message) {
            if (message.what == DecodeConstants.Action.ACTION_DECODE_SUCCESS) {
                mCurrentState = SUCCESS;
                stopScan();

                Bundle bundle = message.getData();
                Bitmap barcode = bundle == null ? null : (Bitmap) bundle
                        .getParcelable(DecodeThread.BARCODE_BITMAP);
                if (mResultHandler != null) {
                    mResultHandler.onScanResult((Result) message.obj, barcode);
                }
            } else if (message.what == DecodeConstants.Action.ACTION_DECODE_FAIL) {
                mCurrentState = PREVIEW;
                startRequestPreview();  //request another preview data
            }
        }

    }
}
