package com.digicomme.tremendocdoctor;

import android.app.Activity;
import android.app.Application;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class TremendocApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initCalligraphy();
    }


    private void initCalligraphy() {
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                //.setDefaultFontPath("fonts/montserrat/Montserrat-Regular.otf")
                .setDefaultFontPath("fonts/montserrat/Montserrat-Regular.otf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }



    public static TremendocApplication get(Activity activity) {
        return (TremendocApplication) activity.getApplication();
    }


}
