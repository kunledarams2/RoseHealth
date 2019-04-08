package com.digicomme.tremendocdoctor.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;


import com.digicomme.tremendocdoctor.api.API;
import com.digicomme.tremendocdoctor.service.CallService;
import com.digicomme.tremendocdoctor.service.ChatService;
import com.digicomme.tremendocdoctor.utils.ToastUtil;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class BaseActivity extends AppCompatActivity implements ServiceConnection {

    private CallService.CallServiceInterface mSinchServiceInterface;
    private ChatService.WebSocketInterface mWebSocketInterface;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindService();
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        /*getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);*/
        log("onCreate()");
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        log("onPostCreate()");
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        log("onServiceConnected()");

        if (CallService.class.getName().equals(componentName.getClassName())) {
            mSinchServiceInterface = (CallService.CallServiceInterface) iBinder;
            log(" CONNECTING CALL SERVICE");
            onServiceConnected();
        } else {
            log("FALSE");
        }
        if (ChatService.class.getName().equals(componentName.getClassName())) {
            log(" CONNECTING CHAT SERVICE");
            mWebSocketInterface = (ChatService.WebSocketInterface) iBinder;
            //mWebSocketInterface.setOnline();
            onServiceConnected();

        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        log("onServiceDisconnected()");
        if (CallService.class.getName().equals(componentName.getClassName())) {
            log(" TRUE");
            mSinchServiceInterface = null;
            onServiceDisconnected();
        } else {
            log("FALSE");
        }
    }

    protected void onServiceConnected() {
        //for subclasses
    }

    protected void onServiceDisconnected() {
        //for subclasses
    }

    protected CallService.CallServiceInterface getSinchServiceInterface() {
        return mSinchServiceInterface;
    }


    protected ChatService.WebSocketInterface getWebSocketInterface() {
        return mWebSocketInterface;
    }

    private Messenger messenger = new Messenger(new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CallService.MESSAGE_PERMISSIONS_NEEDED:
                    Bundle bundle = msg.getData();
                    String requiredPermission = bundle.getString(CallService.REQUIRED_PERMISSION);
                    ActivityCompat.requestPermissions(BaseActivity.this, new String[]{requiredPermission}, 0);
                    break;
            }
        }
    });

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        boolean granted = grantResults.length > 0;
        for (int grantResult: grantResults) {
            granted &= grantResult == PackageManager.PERMISSION_GRANTED;
        }
        if (granted) {
            ToastUtil.showLong(this, "You may now place a call");
        } else {
            ToastUtil.showLong(this, "This application needs permission to use your microphone and camera to function properly.");
        }
        if (API.isLoggedIn(this))
            mSinchServiceInterface.retryStartAfterPermissionGranted();
    }

    private void bindService() {
        Intent intent = new Intent(this, CallService.class);
        intent.putExtra(CallService.MESSENGER, messenger);
        getApplicationContext().bindService(intent, this, BIND_AUTO_CREATE);

        Intent intent2 = new Intent(this, ChatService.class);
        //intent.putExtra(CallService.MESSENGER, messenger);
        getApplicationContext().bindService(intent2, this, BIND_AUTO_CREATE);
        log("bindService()");
    }

    private void log(String string) {
        Log.d("BaseActivity", "--_--_------------___-__--__--_--__-_ " + string);
    }
}
