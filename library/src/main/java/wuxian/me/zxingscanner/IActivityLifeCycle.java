package wuxian.me.zxingscanner;

/**
 * Created by wuxian on 26/8/2016.
 */

public interface IActivityLifeCycle {
    void onActivityResume();    //activity resume的时候一般来说需要重新开启摄像头 扫描线程,扫描框等

    void onActivityPause();     //activity pause 关闭摄像头 停止扫描线程,扫描框
}
