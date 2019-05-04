package com.tremendoc.tremendocdoctor.activity;

import android.app.Activity;
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
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.tremendoc.tremendocdoctor.api.API;
import com.tremendoc.tremendocdoctor.service.CallService;
import com.tremendoc.tremendocdoctor.service.ChatService;
import com.tremendoc.tremendocdoctor.utils.ToastUtil;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class BaseActivity extends AppCompatActivity implements ServiceConnection {

    private CallService.CallServiceInterface mSinchServiceInterface;
    private ChatService.ChatServiceInterface mChatServiceInterface;

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
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
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
            mSinchServiceInterface.startClient();
            log(" CONNECTING CALL SERVICE");
            onServiceConnected();
        } else {
            log("FALSE");
        }
        if (ChatService.class.getName().equals(componentName.getClassName())) {
            mChatServiceInterface = (ChatService.ChatServiceInterface) iBinder;

            onServiceConnected();
            log(" CONNECTING ChatService SERVICE");
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


    protected ChatService.ChatServiceInterface getChatServiceInterface() {
        return mChatServiceInterface;
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


    public static void hideKeyboard(Activity activity) {
        View view = activity.findViewById(android.R.id.content);
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
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
