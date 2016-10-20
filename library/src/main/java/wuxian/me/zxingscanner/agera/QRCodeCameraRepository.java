package wuxian.me.zxingscanner.agera;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.agera.BaseObservable;
import com.google.android.agera.Function;
import com.google.android.agera.Repositories;
import com.google.android.agera.Repository;
import com.google.android.agera.Result;
import com.google.android.agera.Supplier;
import com.google.android.agera.Updatable;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.common.HybridBinarizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import wuxian.me.zxingscanner.agera.camera.AgeraCamera;
import wuxian.me.zxingscanner.agera.camera.AgeraPreviewCallback;
import wuxian.me.zxingscanner.agera.camera.ICamera;
import wuxian.me.zxingscanner.agera.camera.OnNewpreview;
import wuxian.me.zxingscanner.agera.camera.PreviewData;
import wuxian.me.zxingscanner.camera.PlanarYUVLuminanceSource;

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
 */

public class QRCodeCameraRepository extends BaseObservable implements Supplier<String>, Updatable, OnNewpreview {

    private ICamera camera;
    private String qrcode = "";
    private boolean inLoop = false;

    private List<Repository> mPreviewRepos = new ArrayList<>();

    @Override
    protected void observableActivated() {
        runCameraLoop();
    }

    /**
     * 打开摄像头 此时android内置的camera会定时发送"照片截图" --> decode线程解析
     */
    private void runCameraLoop() {
        camera = AgeraCamera.getInstance();
        try {
            camera.openCamera();
        } catch (IOException e) {
            return;
        }
        camera.setPreviewCallback(new AgeraPreviewCallback(this));
        inLoop = true;
        camera.startPreview();

    }

    @NonNull
    @Override
    public String get() {
        return qrcode;
    }

    @Override
    public void onNewPreview(PreviewData data) {
        Repository<Result<String>> repository = Repositories.repositoryWithInitialValue(Result.<String>absent())
                .observe().onUpdatesPerLoop().goTo(newSingleThreadExecutor()).getFrom(new SimpleSuplier(data))
                .thenTransform(new PreviewToStringFunction()).compile();
        repository.addUpdatable(this);

        mPreviewRepos.add(repository);
    }

    /**
     * one previewRepo has returned qrcode string
     * <p>
     * Todo: verify if is right
     */
    @Override
    public synchronized void update() {
        for (Repository rep : mPreviewRepos) {
            if (rep.get() == Result.<String>absent()) {
                continue;
            }

            if (rep.get() == Result.failure()) {
                rep.removeUpdatable(this);
                continue;
            }
            qrcode = (String) (((Result) rep.get()).get());

            dispatchUpdate();
            cleanUp();
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
            return data;
        }
    }

    private class PreviewToStringFunction implements Function<PreviewData, Result<String>> {
        //Todo init
        MultiFormatReader reader = new MultiFormatReader();

        //Todo
        PlanarYUVLuminanceSource getSourceFromPreviewData(PreviewData data) {
            return null;
        }

        @NonNull
        @Override
        public Result<String> apply(@NonNull PreviewData input) {

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
            long start = System.currentTimeMillis();
            com.google.zxing.Result rawResult = null;

            PlanarYUVLuminanceSource source = getSourceFromPreviewData(input);

            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                rawResult = reader.decodeWithState(bitmap);
            } catch (ReaderException re) {
                // continue

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
    }
}
