# ZxingScanner
二维码是android开发中一个非常常用的模块。

本库在google zxing的官方demo上进行了改造。只需几行代码就可以轻松集成zxing的二维码扫描功能。

使用方法。

1 class Your-Activity extends AppCompatActivity implements IDecodeResultHandler, IQRCodeScaner

2 在activity的onCreate函数中初始化ZxingScannerImpl
  mQRCodeScanner = new QRCodeScannerImpl(mContext, mSurfaceView, mViewfinderView, this);
  最后一个参数是IDecodeResultHandler。扫描结果回调。
  
3 activity的onResume,onPause函数中记得调用一下mQRCodeScanner.onActivityResume,onActivityPause.在onActivityResume的时候正式进行扫描工作。

4 在IQRCodeScaner的三个接口startScan,stopScan,restartScan中分别调用mQRCodeScanner.startScan,stopScan,restartScan即可。

Try it out yourself!


