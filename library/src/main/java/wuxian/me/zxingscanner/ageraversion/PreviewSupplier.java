package wuxian.me.zxingscanner.ageraversion;

import android.support.annotation.NonNull;

import com.google.android.agera.Supplier;

import wuxian.me.zxingscanner.share.preview.PreviewData;

/**
 * Created by wuxian on 24/10/2016.
 */

public class PreviewSupplier implements Supplier<PreviewData> {
    private PreviewData data;

    public PreviewSupplier(PreviewData data) {
        this.data = data;
    }

    @NonNull
    @Override
    public PreviewData get() {
        return data;
    }
}
