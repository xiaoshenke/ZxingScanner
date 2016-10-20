package wuxian.me.zxingscanner.agera;

import android.app.Activity;

import com.google.android.agera.BaseObservable;

/**
 * Created by wuxian on 20/10/2016.
 */

public abstract class BaseActivityObservable extends BaseObservable {
    protected Activity mActivity;

    public BaseActivityObservable(Activity activity){
        if(activity == null){
            throw new IllegalArgumentException("activity is null");
        }

        mActivity = activity;
    }

    protected abstract void onActivityEvent();
}
