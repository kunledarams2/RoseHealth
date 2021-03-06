package com.tremendoc.tremendocdoctor.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;


import com.tremendoc.tremendocdoctor.R;
import com.tremendoc.tremendocdoctor.activity.AuthActivity;
import com.tremendoc.tremendocdoctor.activity.MainActivity;

import androidx.core.app.NotificationCompat;

public class UI {
    private static final String CHANNEL_ID = "com.tremendoc.tremendoc";

    public static void setMargins(Context con, ViewGroup.LayoutParams params,
                                  int left, int top , int right, int bottom, View view) {

        final float scale = con.getResources().getDisplayMetrics().density;
        // convert the DP into pixel
        int pixel_left = (int) (left * scale + 0.5f);
        int pixel_top = (int) (top * scale + 0.5f);
        int pixel_right = (int) (right * scale + 0.5f);
        int pixel_bottom = (int) (bottom * scale + 0.5f);

        ViewGroup.MarginLayoutParams s = (ViewGroup.MarginLayoutParams) params;
        s.setMargins(pixel_left, pixel_top, pixel_right, pixel_bottom);

        view.setLayoutParams(params);
    }

    private static void createNotificationChannel(Context context, int importance) {
        //Create the notification channel, but only on API 26+
        //because the NotificationChannel class id new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Tremendoc";
            String description = "Tremendoc Notifications.";
            //int importance = channelId.equals(MISSED_CALLED_CHANNEL_ID)
            //        ? NotificationManager.IMPORTANCE_LOW : NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            //Register the channel with the system; you can't change the importance
            //or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void notifyMissedCall(Context appContext, String userId) {
        createNotificationChannel(appContext, NotificationManager.IMPORTANCE_LOW);

        Intent intent = new Intent(appContext, MainActivity.class);
        intent.putExtra("fragment", MainActivity.CALL_LOGS);
        PendingIntent contentIntent = PendingIntent.getActivity(appContext, 0,
                intent, 0);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(appContext, CHANNEL_ID)
                        .setSmallIcon(R.mipmap.logo)
                        .setContentTitle("Missed call from:")
                        .setContentText(userId);
        builder.setContentIntent(contentIntent);
        builder.setAutoCancel(true);
        NotificationManager manager =
                (NotificationManager) appContext
                        .getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(2, builder.build());
    }

    public static void notifyOnline(Context appContext) {
        createNotificationChannel(appContext, NotificationManager.IMPORTANCE_HIGH);

        Intent intent = new Intent(appContext, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(appContext, 0,
                intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext, CHANNEL_ID)
                .setSmallIcon(R.mipmap.logo)
                .setContentTitle("Tremendoc Doctor")
                .setContentText("You are currently online and can receive calls.")
                .setOngoing(true)
                .setContentIntent(contentIntent)
                .setAutoCancel(true);
        NotificationManager manager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, builder.build());
    }

    public static void clearOnlineNotification(Context mContext) {
        NotificationManager notificationManager = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1);
    }

    private static void createNotification(Service service, String title, String content) {
        createNotificationChannel(service, NotificationManager.IMPORTANCE_LOW);

        PendingIntent contentIntent = PendingIntent.getActivity(service, 0,
                new Intent(service, AuthActivity.class), 0);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(service, CHANNEL_ID)
                        .setSmallIcon(R.mipmap.logo)
                        .setContentTitle(title)
                        .setContentText(content);
        builder.setContentIntent(contentIntent);
        builder.setAutoCancel(true);
        //NotificationManager manager =
        //       (NotificationManager) service
        //                .getSystemService(Context.NOTIFICATION_SERVICE);
        //manager.notify(1, builder.build());
        service.startForeground(1, builder.build());

    }


}
