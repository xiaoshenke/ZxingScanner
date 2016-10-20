package wuxian.me.zxingscanner.agera.camera;

import com.google.android.agera.BaseObservable;

/**
 * Created by wuxian on 20/10/2016.
 *
 * camera通过截图连续产出一系列图片 说明它是一个observable
 * 产出的图片交给decodethread处理
 */

public class CameraObservable extends BaseObservable {
    @Override
    protected void observableDeactivated() {
        super.observableDeactivated();
    }

    @Override
    protected void observableActivated() {
        super.observableActivated();
    }
}
