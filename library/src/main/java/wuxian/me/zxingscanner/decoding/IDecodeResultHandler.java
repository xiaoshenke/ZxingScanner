package wuxian.me.zxingscanner.decoding;

import android.graphics.Bitmap;

import com.google.zxing.Result;

/**
 * 处理decode结果
 * <p>
 * Created by wuxian on 25/8/2016.
 */

public interface IDecodeResultHandler {

    void handleDecodeSuccess(Result result, Bitmap bitmap);

    void handleDecodeFail();
}
