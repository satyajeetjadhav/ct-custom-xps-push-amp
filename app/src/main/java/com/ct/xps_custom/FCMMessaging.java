package com.ct.xps_custom;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;

import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.pushnotification.NotificationInfo;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class FCMMessaging extends FirebaseMessagingService {

    Context context;
    CleverTapAPI cleverTapAPI;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        try {
            Log.i("Clevertap_Demo_App","Inside Push Templates");
            if (remoteMessage.getData().size() > 0) {
                final Bundle extras = new Bundle();
                for (Map.Entry<String, String> entry : remoteMessage.getData().entrySet()) {
                    extras.putString(entry.getKey(), entry.getValue());
                }
                final NotificationHelper notificationHelper = NotificationHelper.getInstance(context.getApplicationContext());
                notificationHelper.postAsyncSafely("Duplicate Check", new Runnable() {
                    @Override
                    public void run() {
                        if (!notificationHelper.isNotificationPresent(extras)) {
                            //TODO: ADD CUSTOM RENDERING LOGIC HERE
                            notificationHelper.saveNotification(extras);

                        }
                    }
                });
            }

        } catch (Throwable throwable) {
        }
    }
    @Override
    public void onNewToken(@NonNull final String s) {
        //no-op
    }
}
