package com.tremendoc.tremendocdoctor.activity;

import android.Manifest;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.os.Bundle;

import com.tremendoc.tremendocdoctor.api.API;
import com.tremendoc.tremendocdoctor.utils.Permission;

public class SplashActivity extends AppCompatActivity {
    private boolean askedBefore = false;
    private static final int INTERNET_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                Permission.showModal(this,"Tremendoc needs to access phone state and internet to continue",
                        (dialog, i) -> {
                            askedBefore = false;
                            tryNext();
                            dialog.cancel();
                        });
            }
        } else {
            gotoNext();
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
}
