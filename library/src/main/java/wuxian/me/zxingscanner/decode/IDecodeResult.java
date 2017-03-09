package wuxian.me.zxingscanner.decode;

import android.graphics.Bitmap;

import com.google.zxing.Result;

/**
 * Created by wuxian on 25/8/2016.
 */

public interface IDecodeResult {
    void handleDecode(Result result, Bitmap bitmap);
}
