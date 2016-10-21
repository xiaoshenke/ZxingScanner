package wuxian.me.zxingscanner.ageraversion;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.android.agera.BaseObservable;
import com.google.android.agera.Function;
import com.google.android.agera.Repositories;
import com.google.android.agera.Repository;
import com.google.android.agera.Result;
import com.google.android.agera.Supplier;
import com.google.android.agera.Updatable;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.common.HybridBinarizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import wuxian.me.zxingscanner.ageraversion.camera.AgeraCamera;
import wuxian.me.zxingscanner.ageraversion.camera.AgeraPreviewCallback;
import wuxian.me.zxingscanner.ageraversion.camera.ICamera;
import wuxian.me.zxingscanner.ageraversion.camera.OnNewpreview;
import wuxian.me.zxingscanner.ageraversion.camera.PreviewData;
import wuxian.me.zxingscanner.share.CameraConfigurationManager;
import wuxian.me.zxingscanner.share.PlanarYUVLuminanceSource;
import wuxian.me.zxingscanner.share.DecodeFormatManager;
import wuxian.me.zxingscanner.share.view.ViewfinderResultPointCallback;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

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
 * 
 */

public class QRCodeCameraRepository extends BaseObservable implements Supplier<String>, Updatable, OnNewpreview {

    private static final String TAG = "repository";

    private ICamera camera;
    private String qrcode = "";
    private boolean inLoop = false;
    private Context context;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private boolean hasSurface = false;
    private boolean activated = false;

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

        this.surfaceView = surfaceView;
        if (surfaceView == null) {
            throw new IllegalArgumentException("surfaceview is null");
        }
        surfaceView.getHolder().addCallback(surfaceCallback);
    }

    private List<Repository> mPreviewRepos = new ArrayList<>();

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
        camera = AgeraCamera.getInstance(context);
        try {
            camera.openCamera(surfaceHolder);
        } catch (IOException e) {
            return;
        }
        camera.setPreviewCallback(new AgeraPreviewCallback(camera.getConfigManager(), this));
        inLoop = true;
        camera.startPreview();
    }

    @NonNull
    @Override
    public String get() {
        return qrcode;
    }

    @Override
    public synchronized void onNewPreview(PreviewData data) {

        Repository<Result<String>> repository = Repositories.repositoryWithInitialValue(Result.<String>absent())
                .observe().onUpdatesPerLoop().goTo(newSingleThreadExecutor()).getFrom(new SimpleSuplier(data))
                .thenTransform(new PreviewToStringFunction()).compile();
        repository.addUpdatable(this);

        mPreviewRepos.add(repository);

    }

    /**
     * one of these previewRepo has returned qrcode string
     *
     * Todo: fix can't remove updatable??
     * Todo: after recognize qrcode, stop decoding?
     * Todo: out of memory,too much thread?
     */
    @Override
    public synchronized void update() {
        Log.e(TAG,"in repository.update");

        for (Repository rep : mPreviewRepos) {
            if (rep.get() == Result.<String>absent()) {
                Log.e(TAG,"absent");
                continue;
            }
            if (rep.get() == Result.failure()) {
                Log.e(TAG,"failure");
                //rep.removeUpdatable(this);
                continue;
            }

            qrcode = (String) (((Result) rep.get()).get());
            Log.e(TAG,"success? qrcode is "+qrcode);
            dispatchUpdate();
            break;
            //cleanUp();
        }

    }

    private void cleanUp() {
        for (Repository rep : mPreviewRepos) {
            rep.removeUpdatable(this);
        }
        mPreviewRepos.clear();

        //close camera???
    }

    private class SimpleSuplier implements Supplier<PreviewData> {
        private PreviewData data;

        public SimpleSuplier(PreviewData data) {
            this.data = data;
        }

        @NonNull
        @Override
        public PreviewData get() {
            Log.e(TAG,"simplesupplier.get is in thread: "+Thread.currentThread().getName());
            return data;
        }
    }

    private class PreviewToStringFunction implements Function<PreviewData, Result<String>> {

        MultiFormatReader reader;

        public PreviewToStringFunction() {
            init();
        }

        private void init() {
            reader = new MultiFormatReader();
            reader.setHints(getDefaultHints());
        }

        @NonNull
        @Override
        public Result<String> apply(@NonNull PreviewData input) {
            Log.e(TAG,"previewToStringFunction.apply is in thread: "+Thread.currentThread().getName());

            byte[] data = input.data;
            int height = input.resolution.y;
            int width = input.resolution.x;

            byte[] rotatedData = new byte[data.length];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++)
                    rotatedData[x * height + height - y - 1] = data[x + y * width];
            }
            int tmp = width; // Here we are swapping, that's the difference to #11
            width = height;
            height = tmp;
            data = rotatedData;

            com.google.zxing.Result rawResult = null;
            PlanarYUVLuminanceSource source = getSourceFromPreviewData(new PreviewData(new Point(width, height), data));

            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                rawResult = reader.decodeWithState(bitmap);
            } catch (ReaderException re) {
                return Result.failure();
            } finally {
                reader.reset();
            }

            if (rawResult != null) {
                final String code = rawResult.getText().trim();

                if (!TextUtils.isEmpty(code)) {
                    return Result.success(code);
                }
            }

            return Result.failure();
        }

        private Rect getFramingRect() {
            CameraConfigurationManager configManager = AgeraCamera.getInstance(context).getConfigManager();
            if (configManager == null) {
                return null;
            }

            Point screenResolution = configManager.getScreenResolution();
            Rect framingRect;
            int width = screenResolution.x;
            int height = screenResolution.y;

            int leftOffset = (screenResolution.x - width) / 2;
            int topOffset = (screenResolution.y - height) / 3;
            framingRect = new Rect(leftOffset, topOffset, leftOffset + width,
                    topOffset + height);
            return framingRect;
        }

        private Rect getFramingRectInPreview() {
            Rect frameRect = getFramingRect();
            if (frameRect == null) {
                return null;
            }

            CameraConfigurationManager configManager = AgeraCamera.getInstance(context).getConfigManager();

            Rect rect = new Rect(frameRect);
            Point cameraResolution = configManager.getCameraResolution();
            Point screenResolution = configManager.getScreenResolution();
            rect.left = rect.left * cameraResolution.y / screenResolution.x;
            rect.right = rect.right * cameraResolution.y / screenResolution.x;
            rect.top = rect.top * cameraResolution.x / screenResolution.y;
            rect.bottom = rect.bottom * cameraResolution.x / screenResolution.y;

            return rect;
        }

        private PlanarYUVLuminanceSource getSourceFromPreviewData(PreviewData previewData) {
            CameraConfigurationManager configManager = AgeraCamera.getInstance(context).getConfigManager();
            if (configManager == null) {
                return null;
            }

            byte[] data = previewData.data;
            int width = previewData.resolution.x;
            int height = previewData.resolution.y;

            Rect rect = getFramingRectInPreview();
            int previewFormat = configManager.getPreviewFormat();
            String previewFormatString = configManager.getPreviewFormatString();
            switch (previewFormat) {
                // This is the standard Android format which all devices are REQUIRED to
                // support.
                // In theory, it's the only one we should ever care about.
                case PixelFormat.YCbCr_420_SP:
                    // This format has never been seen in the wild, but is compatible as
                    // we only care
                    // about the Y channel, so allow it.
                case PixelFormat.YCbCr_422_SP:
                    return new PlanarYUVLuminanceSource(data, width, height, rect.left,
                            rect.top, rect.width(), rect.height());
                default:
                    // The Samsung Moment incorrectly uses this variant instead of the
                    // 'sp' version.
                    // Fortunately, it too has all the Y data up front, so we can read
                    // it.
                    if ("yuv420p".equals(previewFormatString)) {
                        return new PlanarYUVLuminanceSource(data, width, height,
                                rect.left, rect.top, rect.width(), rect.height());
                    }
            }
            throw new IllegalArgumentException("Unsupported picture format: "
                    + previewFormat + '/' + previewFormatString);

        }

        Hashtable<DecodeHintType, Object> getDefaultHints() {
            Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>();
            Vector<BarcodeFormat> decodeFormats = new Vector<BarcodeFormat>();
            decodeFormats.addAll(DecodeFormatManager.ONE_D_FORMATS);
            decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS);
            decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS);
            hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);

            String characterSet = null;
            if (characterSet != null) {
                hints.put(DecodeHintType.CHARACTER_SET, characterSet);
            }

            hints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK,
                    new ViewfinderResultPointCallback(null));  //Todo: replace last parameter

            return hints;
        }
    }
}
