package com.digicomme.tremendocdoctor.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Created by ikay on 9/17/2018.
 */

public class Permission {

    public static final int PERMISSION_REQUEST = 100;

    public static boolean permissionIsGranted(Context context, String permission) {
        int result = ContextCompat.checkSelfPermission(context, permission);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean permissionsAreGranted(Context context, String[] permissions) {
        for (String permission: permissions) {
            if (!permissionIsGranted(context, permission)){
                return false;
            }
        }

        return true;
    }

    public static void  requestPermissions(Activity activity, String[] perms) {
        ActivityCompat.requestPermissions(activity, perms,  101);
    }


    public static void showModal(Context context, String msg, DialogInterface.OnClickListener posClick) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(msg);
        builder.setPositiveButton("Yes, Continue", posClick);
        builder.setNegativeButton("Deny", (dialog, i) -> dialog.cancel());
        builder.create().show();
    }

}
