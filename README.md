# ZxingScanner
A QRCode scanner library base on google [zxing](https://github.com/zxing/zxing).                       
QRCode scan function is so common in an Android Application development,but zxing demo is difficult to use,and you have to modify many code.That is why I create this library.                     
ZXingScanner is base on zxing,but much easier to use.You can interate QRCode scann funtion in just a few lines.                        

Step
1 add camera permission.In your AndroidManifest.xml          

````
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.VIBRATE" />

````                                     
2 In your activity/fragment/view xml file,add SurfaceView and ViewfinderView node.                 

````
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/activity_main"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <SurfaceView
        android:id="@+id/surface"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_gravity="center" />

    <wuxian.me.zxingscanner.view.ViewfinderView
        android:id="@+id/viewfinder"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center" />
</FrameLayout>
````
3 create a ZxingScannerImpl variable in your activity.        
           
````
mQRCodeScanner = new QRCodeScannerImpl(mSurfaceView, mViewfinderView, mDecodeResultHandler);
````
the third paramer is the qrcode result callback.

4 call mQRCodeScanner.onActivityResume,onActivityPause in activity's lifecycle.                         

````
    @Override
    public void onResume() {
        super.onResume();
        mQRCodeScanner.onActivityResume();  //don't forget to call this!
    }

    @Override
    protected void onPause() {
        super.onPause();
        mQRCodeScanner.onActivityPause();   //don't forget to call this!
    }
````

wola,now you have successfully integrated QRCode function to your application!            

If you aren't satisfied with the sanner ui,you can implement IViewfinder to custom your own ui. 

##Todo             
* ~~support [RxJava](https://github.com/ReactiveX/RxJava)~~
* ~~support [agera](https://github.com/google/agera)~~
* add [RxLifecycle](https://github.com/trello/RxLifecycle)     
 

Check the code to know more details !


