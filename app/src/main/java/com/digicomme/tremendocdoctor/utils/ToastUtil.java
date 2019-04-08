package com.digicomme.tremendocdoctor.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

public class ToastUtil {
    public static void showLong(Context context, String msg){
        //Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
    public static void showShort(Context context, String msg){
        //Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void showModal(Context ctx, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setMessage(msg);
        builder.setNegativeButton("CLOSE", (dialogInterface, i) -> dialogInterface.dismiss());
        builder.create().show();
    }

}
