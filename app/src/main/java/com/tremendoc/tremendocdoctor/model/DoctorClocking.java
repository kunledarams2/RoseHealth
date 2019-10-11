package com.tremendoc.tremendocdoctor.model;

import android.content.Context;
import android.os.Bundle;

public class DoctorClocking {

    public static final String  CLOCKING_TIME= "nextClockInTime";
    private String mClockInTime;

    private Bundle bundle;
    private Context context;

    public DoctorClocking(Context coext) {
        bundle= new Bundle();
        context = coext;
    }

    public void set(String key, String value) {
        bundle.putString(key, value);
    }

    public String get(String key) {
        return bundle.getString(key);
    }


    public String getmClockInTime() {
        return mClockInTime;
    }

    public void setmClockInTime(String mClockInTime) {
        this.mClockInTime = mClockInTime;
    }
}
