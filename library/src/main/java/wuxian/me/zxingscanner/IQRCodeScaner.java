package wuxian.me.zxingscanner;

/**
 * Created by wuxian on 25/8/2016.
 *
 * Interface of a QRCode scanner
 *
 * A scanner should be able to
 * 1 startScan
 * 2 stopScan
 */

public interface IQRCodeScaner {
    void startScan();

    void restartScan();

    void stopScan();
}
