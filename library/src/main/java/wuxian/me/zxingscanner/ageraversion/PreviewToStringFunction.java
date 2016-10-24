package wuxian.me.zxingscanner.ageraversion;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.agera.Function;
import com.google.android.agera.Result;

import wuxian.me.zxingscanner.share.decode.DecodeException;
import wuxian.me.zxingscanner.share.decode.DecodeManager;
import wuxian.me.zxingscanner.share.preview.PreviewData;

/**
 * Created by wuxian on 24/10/2016.
 */

public class PreviewToStringFunction implements Function<PreviewData, Result<String>> {

    private Context context;

    public PreviewToStringFunction(Context context) {
        this.context = context;
    }


    @NonNull
    @Override
    public Result<String> apply(@NonNull PreviewData input) {

        try {
            String code = DecodeManager.getQrcodeFromPreviewData(context, input);
            return Result.success(code);

        } catch (DecodeException e) {
            return Result.failure();
        }
    }
}
