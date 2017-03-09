package wuxian.me.zxingscanner;

/**
 * Created by wuxian on 25/8/2016.
 * 在这里scan和decode是同步的。
 * 就是说我只要看到扫描框的线在扫描,它一定在解析
 */

public interface IQRCodeScaner {

    void startScan();

    void restartScan();

    void stopScan();

    void quit();
}
