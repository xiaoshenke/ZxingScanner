package wuxian.me.zxingscanner.rxversion;

import android.content.Context;
import android.util.Log;

import rx.functions.Func1;
import wuxian.me.zxingscanner.share.decode.DecodeException;
import wuxian.me.zxingscanner.share.decode.DecodeManager;
import wuxian.me.zxingscanner.share.preview.PreviewData;

/**
 * Created by wuxian on 23/10/2016.
 *
 * Todo: Rxjava error handling?
 */

public class NewpreviewFunction implements Func1<PreviewData, String> {
    private static final String TAG = "NewPreviewFunc";
    private Context context;

    public NewpreviewFunction(Context context) {
        this.context = context;
    }

    @Override
    public String call(PreviewData data) {
        Log.e(TAG, "in call data is " + data);
        try {
            return DecodeManager.getQrcodeFromPreviewData(context, data);
        } catch (DecodeException e) {
            return null;
        }
    }
}
