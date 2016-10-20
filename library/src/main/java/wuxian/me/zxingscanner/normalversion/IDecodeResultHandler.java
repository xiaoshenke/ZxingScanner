package wuxian.me.zxingscanner.normalversion;

import android.graphics.Bitmap;

import com.google.zxing.Result;

/**
 * Created by wuxian on 25/8/2016.
 * <p>
 * Interface of a QRCode-Scanner Result callback
 */

public interface IDecodeResultHandler {
    void handleDecodeSuccess(Result result, Bitmap bitmap);
}
