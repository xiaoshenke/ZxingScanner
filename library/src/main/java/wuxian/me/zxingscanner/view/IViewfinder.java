package wuxian.me.zxingscanner.view;

import com.google.zxing.ResultPoint;

/**
 * Created by wuxian on 14/10/2016.
 *
 * Interface of Viewfinder view.
 * a viewfinder should be able to
 * 1 drawViewfinder  --> a red or whatever color line scan from top down
 * 2 stopDrawViewFinder
 * 3 addPossibleResultPoint --> draw some small circle around all possible result point
 */

public interface IViewfinder {
    void drawViewfinder();

    void stopDrawViewfinder();

    void addPossibleResultPoint(ResultPoint resultPoint);
}
