package com.tremendoc.tremendocdoctor;

import android.app.Activity;
import android.app.Application;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class TremendocApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initCalligraphy();

        //Logging
        //OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.WARN);

        /*OneSignal.startInit(this)
                .setNotificationReceivedHandler(new NotificationReceiver())
                .setNotificationOpenedHandler(new NotificationOpener())
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();*/
    }


    private void initCalligraphy() {
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                //.setDefaultFontPath("fonts/montserrat/Montserrat-Regular.otf")
                .setDefaultFontPath("fonts/montserrat/Montserrat-Regular.otf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }


    /*class NotificationReceiver implements OneSignal.NotificationReceivedHandler{
        @Override
        public void notificationReceived(OSNotification notification) {
            JSONObject data = notification.payload.additionalData;
            Log.d("OneSignalNotification", "Received " + data.toString());
        }
    }

    class NotificationOpener implements OneSignal.NotificationOpenedHandler {
        @Override
        public void notificationOpened(OSNotificationOpenResult result) {
            JSONObject data = result.notification.payload.additionalData;
            Log.d("OneSignalNotification", "Opened " + data.toString());
        }
    }*/


    public static TremendocApplication get(Activity activity) {
        return (TremendocApplication) activity.getApplication();
    }


}
