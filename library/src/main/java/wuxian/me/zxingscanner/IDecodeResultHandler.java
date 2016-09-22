package wuxian.me.zxingscanner;

import android.graphics.Bitmap;

import com.google.zxing.Result;

/**
 * Created by wuxian on 25/8/2016.
 *
 * Interface of a QRCode-Scanner Result callback
 */

public interface IDecodeResultHandler {

    void handleDecodeSuccess(Result result, Bitmap bitmap);

    void handleDecodeFail();
}
