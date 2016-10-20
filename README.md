# ZxingScanner
A QRCode scanner library base on google [zxing](https://github.com/zxing/zxing).                       
QRCode scan function is so common in an Android Application development,but zxing demo is difficult to use,and you have to modify many code.That is why I create this library.                     
ZXingScanner is base on zxing,but much easier to use.You can interate QRCode scann funtion in just a few lines.                        

##  Base version
Step 1 add camera permission.In your AndroidManifest.xml          

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

    <wuxian.me.zxingscanner.base.view.ViewfinderView
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

##  RxJava support
you still have to do step1 and step2.
step3, In your onResume method,add these code                   

````
RxQRCodeScanner.getInstance().surfaceView(mSurfaceView).scanView(mScanView).scan().subscribe(new Action1<String>() {
            @Override
            public void call(String s) {  //replace your code here
                Toast.makeText(RxMainActivity.this, "qrcode is " + s, Toast.LENGTH_LONG).show();
            }
        });
````
##  Agera support
you still have to do step1 and step2.                        

step3, init a QRCodeScannerRepository,and in resume method call addUpdatable method.                   

````
mRepository = new QRCodeScannerRepository().surfaceView(mSurfaceView).scanView(mScanView);
mRepository.addUpdatable(this);      
````
step4, don't forget let your activity/fragment/view implement Updatable interface,because that is the qrcode scan result callback.           

````
class AgeraMainActivity extends AppCompatActivity implements Updatable

@Override
    public void update() { //replace your code here
        Toast.makeText(this,"qrcode is "+mRepository.get(),Toast.LENGTH_LONG).show();
    }
      
````

##Todo             
* ~~support [RxJava](https://github.com/ReactiveX/RxJava)~~
* ~~support [agera](https://github.com/google/agera)~~
* enable Activity lifecyle control   
 
##  Other
        
* If you aren't satisfied with the sanner ui,you can implement IViewfinder to custom your own ui. 
* In agera branch,the whole project is currently almost rewrited in agera style.

Check the code to know more details !


