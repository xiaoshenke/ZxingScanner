package wuxian.me.zxingscanner.ageraversion;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.android.agera.BaseObservable;
import com.google.android.agera.Function;
import com.google.android.agera.Receiver;
import com.google.android.agera.Repositories;
import com.google.android.agera.Repository;
import com.google.android.agera.Result;
import com.google.android.agera.Supplier;
import com.google.android.agera.Updatable;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import wuxian.me.zxingscanner.share.decode.DecodeException;
import wuxian.me.zxingscanner.share.decode.DecodeManager;
import wuxian.me.zxingscanner.share.camera.RxCamera;
import wuxian.me.zxingscanner.share.preview.RXPreviewCallback;
import wuxian.me.zxingscanner.share.camera.ICamera;
import wuxian.me.zxingscanner.share.preview.OnNewpreview;
import wuxian.me.zxingscanner.share.preview.PreviewData;

/**
 * Created by wuxian on 20/10/2016.
 * 循环@previewRepository
 * <p>
 * 现在的处理方法是,产出一个preview--decode流程的时候,可能是无效的(图片不是一张二维码图片)。
 * 因此需要失败-->重试-->失败-->重试的循环过程。
 * <p>
 * 调用到CameraRepository.addUpdatable的时候开启 流程池。
 * <p>
 * 流程池每一个线程是由 camera的previewcallback发起的。
 * <p>
 * <p>
 * Todo: add ScanView
 */

public class QRCodeCameraRepository extends BaseObservable implements Supplier<String>, Updatable, OnNewpreview {
    private static final String TAG = "repository";

    private ICamera camera;
    private String qrcode = "";

    private Context context;
    private SurfaceHolder surfaceHolder;
    private boolean hasSurface = false;
    private boolean activated = false;

    private Repository repository = null;

    private SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            surfaceHolder = holder;
            hasSurface = true;
            if (hasSurface && activated) {
                runCameraLoop();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            surfaceHolder = null;
            hasSurface = false;
        }
    };

    public QRCodeCameraRepository(Context context, SurfaceView surfaceView) {
        this.context = context;

        if (surfaceView == null) {
            throw new IllegalArgumentException("surfaceview is null");
        }
        surfaceView.getHolder().addCallback(surfaceCallback);
    }

    @Override
    protected void observableDeactivated() {
        activated = false;
        if (camera != null) {
            camera.closeCamera();
        }
    }

    @Override
    protected void observableActivated() {
        activated = true;
        if (activated && hasSurface) {
            runCameraLoop();
        }
    }

    /**
     * 打开摄像头 此时android内置的camera会定时发送"照片截图"
     */
    private void runCameraLoop() {
        camera = RxCamera.getInstance(context);
        try {
            camera.openCamera(surfaceHolder);
        } catch (IOException e) {
            return;
        }
        camera.setPreviewCallback(new RXPreviewCallback(camera.getConfigManager(), this));
        camera.startPreview();
    }

    @NonNull
    @Override
    public String get() {
        return qrcode;
    }

    private Result<String> getInitialResultValue() {
        return Result.absent();
    }

    private Executor executor;

    private Executor getDefaultExecutor() {
        if (executor == null) {
            executor = Executors.newFixedThreadPool(1);
        }
        return executor;
    }

    /**
     * called by @RXPreviewCallback
     *
     * @param data
     */
    @Override
    public void onNewPreview(PreviewData data) {
        if (repository == null) {
            repository = Repositories.repositoryWithInitialValue(getInitialResultValue())
                    .observe()
                    .onUpdatesPerLoop()
                    .goTo(getDefaultExecutor())
                    .getFrom(new PreviewSupplier(data))
                    .thenTransform(new PreviewToStringFunction(context))
                    .compile();
        }

        repository.addUpdatable(this);
    }

    @Override
    public synchronized void update() {
        Log.e(TAG, "in repository.update");

        /*
        if(repository.get() == Result.failure()){
            camera.requestPreview(this);
        } else {
            qrcode = (String) (((Result) repository.get()).get());

            cleanUp();
            dispatchUpdate();
        }
        */

        repository.removeUpdatable(QRCodeCameraRepository.this);

        ((Result) (repository.get())).ifFailedSendTo(new Receiver<Throwable>() {
            @Override
            public void accept(@NonNull Throwable value) {
                camera.requestPreview(QRCodeCameraRepository.this);
            }
        }).ifSucceededSendTo(new Receiver() {
            @Override
            public void accept(@NonNull Object value) {
                qrcode = (String) (((Result) repository.get()).get());
                cleanUp();
                dispatchUpdate();
            }
        });
    }

    /**
     * we have get our qrcode,do some clean work.
     * 1 remove all updatables.
     * 2 stop decode thread
     * 3 stop autofocus
     */
    private void cleanUp() {
        if (camera != null) {
            camera.stopPreview();  //Fixme: 调用stopPreview时会surfaceview会静止 但正确的行为应该是继续显示图片
        }
    }

}
