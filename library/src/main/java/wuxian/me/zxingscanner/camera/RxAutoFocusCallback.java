package wuxian.me.zxingscanner.camera;

import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;

import wuxian.me.zxingscanner.R;

/**
 * Created by wuxian on 20/10/2016.
 */


public class RxAutoFocusCallback implements Camera.AutoFocusCallback {
    private static final long AUTOFOCUS_INTERVAL_MS = 1500L;
    Handler handler;

    public RxAutoFocusCallback() {
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        if (handler != null) {
            Message msg = handler.obtainMessage(R.id.auto_focus, success);
            if (msg == null) {
                msg = new Message();
                msg.what = R.id.auto_focus;
                msg.obj = success;
            }
            handler.sendMessageDelayed(msg, AUTOFOCUS_INTERVAL_MS);

            handler = null;  //防止一直发送??
        }

    }
}
