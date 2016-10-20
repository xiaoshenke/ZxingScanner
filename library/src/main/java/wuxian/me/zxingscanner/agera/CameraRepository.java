package wuxian.me.zxingscanner.agera;

import android.support.annotation.NonNull;

import com.google.android.agera.BaseObservable;
import com.google.android.agera.Supplier;

/**
 * Created by wuxian on 20/10/2016.
 * 循环@previewRepository
 * <p>
 * 现在的处理方法是,产出一个preview--decode流程的时候,可能是无效的(图片不是一张二维码图片)。
 * 因此需要失败-->重试-->失败-->重试的循环过程。
 */

public class CameraRepository extends BaseObservable implements Supplier<String> {

    private String qrcode = "";

    @Override
    protected void observableActivated() {
        runCameraLoop();
    }

    /**
     * 打开摄像头 此时android内置的camera会定时发送"照片截图" --> decode线程解析
     */
    private void runCameraLoop() {

    }

    @NonNull
    @Override
    public String get() {
        return qrcode;
    }
}
