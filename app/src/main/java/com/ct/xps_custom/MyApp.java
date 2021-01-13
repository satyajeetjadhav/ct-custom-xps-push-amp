package com.ct.xps_custom;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Process;

import com.clevertap.android.sdk.ActivityLifecycleCallback;
import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.pushnotification.amp.CTPushAmpListener;
import com.xiaomi.mipush.sdk.MiPushClient;

import java.util.List;

public class MyApp extends Application implements CTPushAmpListener {

    public static final String APP_ID = "2882303761518346520";
    public static final String APP_KEY = "5191834634520";

    @Override
    public void onCreate() {
        CleverTapAPI.setDebugLevel(3);
        ActivityLifecycleCallback.register(this);
        CleverTapAPI cleverTapAPI = CleverTapAPI.getDefaultInstance(getApplicationContext());
        cleverTapAPI.setCTPushAmpListener(this); if(shouldInit()) {
            MiPushClient.registerPush(this, APP_ID, APP_KEY);
        }
        super.onCreate();

    }

    private boolean shouldInit() {
        ActivityManager am = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        String mainProcessName = getPackageName();
        int myPid = Process.myPid();
        for (ActivityManager.RunningAppProcessInfo info : processInfos) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }
    @Override
    public void onPushAmpPayloadReceived(final Bundle extras) {

        final NotificationHelper notificationHelper = NotificationHelper.getInstance(getApplicationContext());
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
}


