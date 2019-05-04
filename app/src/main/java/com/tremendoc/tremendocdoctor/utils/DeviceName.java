package com.tremendoc.tremendocdoctor.utils;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

public class DeviceName {
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        return  "";
    }

    public static String getUUID(Context context) {
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String uuid = "";
        if (Build.VERSION.SDK_INT >= 26) {
            uuid = manager.getImei();
        } else {
            uuid = manager.getDeviceId();
        }
        return uuid;
    }
}
