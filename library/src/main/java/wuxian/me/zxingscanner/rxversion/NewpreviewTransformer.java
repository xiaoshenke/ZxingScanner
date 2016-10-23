package wuxian.me.zxingscanner.rxversion;

import rx.Observable;
import wuxian.me.zxingscanner.ageraversion.camera.PreviewData;

/**
 * Created by wuxian on 23/10/2016.
 * <p>
 * Todo: to be finished
 * <p>
 * Todo: Rxjava error handling?
 */

public class NewpreviewTransformer implements Observable.Transformer<PreviewData, String> {
    @Override
    public Observable<String> call(Observable<PreviewData> previewDataObservable) {

        return null;
    }
}
