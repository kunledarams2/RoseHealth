package com.tremendoc.tremendocdoctor.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;

import com.tremendoc.tremendocdoctor.EndPointAPI.DoctorSchedule;
import com.tremendoc.tremendocdoctor.R;
import com.tremendoc.tremendocdoctor.api.API;
import com.tremendoc.tremendocdoctor.utils.IO;
import com.tremendoc.tremendocdoctor.utils.Permission;

import java.util.UUID;

public class SplashActivity extends AppCompatActivity {
    private boolean askedBefore = false;
    private static final int INTERNET_PERMISSION = 100;
    DoctorSchedule doctorSchedule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen_e_health);
        try {
            Thread.sleep(1000);

            tryNext();

        } catch (InterruptedException e){

        }
    }

    private void tryNext() {
        if (!Permission.permissionsAreGranted(this, new String[] {
                Manifest.permission.INTERNET, Manifest.permission.READ_PHONE_STATE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO})) {
            if (!askedBefore) {
                ActivityCompat.requestPermissions(this, new String[] {
                        Manifest.permission.INTERNET, Manifest.permission.READ_PHONE_STATE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                        INTERNET_PERMISSION);
                askedBefore = true;
            } else {
                Permission.showModal(this,"RoseHealth needs to access phone state and internet to continue",
                        (dialog, i) -> {
                            askedBefore = false;
                            tryNext();
                            dialog.cancel();
                        });
            }
        } else {
            gotoNext();
            checkClockIn();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == INTERNET_PERMISSION) {
            tryNext();
        }
    }

    private void gotoNext() {

        Intent intent = new Intent(this, API.isLoggedIn(this) ? MainActivity.class : AuthActivity.class);
        startActivity(intent);
        finish();
    }

    private void checkClockIn(){
        doctorSchedule = new DoctorSchedule(this);
        doctorSchedule.checkClockIn();

    }

}
