package wuxian.me.zxingscanner;

import android.graphics.Bitmap;

import com.google.zxing.Result;

/**
 * Created by wuxian on 25/8/2016.
 */

public interface IScanResult {
    void onScanResult(Result result, Bitmap bitmap);
}
