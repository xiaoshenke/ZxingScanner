package wuxian.me.zxingscanner.rx;

import rx.Single;

/**
 * Created by wuxian on 17/10/2016.
 */

public interface IRXScanner {
    Single<String> scan();
}
