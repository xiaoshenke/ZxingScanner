package wuxian.me.zxingscanner.rx;

/**
 * Created by wuxian on 17/10/2016.
 * Todo: to be finished
 */

public class RxQRCodeScanner {
    private static RxQRCodeScanner scanner;

    private RxQRCodeScanner(){
        ;
    }

    public static RxQRCodeScanner getInstance(){
        if(scanner == null){
            scanner = new RxQRCodeScanner();
        }

        return scanner;
    }
}
