package wuxian.me.zxingscanner.normalversion;

/**
 * Created by wuxian on 26/8/2016.
 *
 * Interface monit Android Activity LifeCycle
 */

public interface IActivityLifecycle {
    /**
     * activity onResume的时候应该调用一下这个函数
     *
     * 在这个函数中应该做以下事情
     * 1 重新开启摄像头
     * 2 开启扫描线程 开启扫描框
     *
     */
    void onActivityResume();

    /**
     * activity onPause的时候应该调用一下这个函数
     *
     * 在这个函数中应该做以下事情
     * 1 关闭摄像头
     * 2 停止扫描线程 扫描框
     *
     */
    void onActivityPause();
}
