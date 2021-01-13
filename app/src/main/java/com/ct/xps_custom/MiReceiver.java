package com.ct.xps_custom;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.clevertap.android.sdk.CleverTapAPI;
import com.xiaomi.mipush.sdk.ErrorCode;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;

import org.json.JSONException;

import java.util.List;
import java.util.Map;

import static com.clevertap.android.sdk.Utils.stringToBundle;

public class MiReceiver extends PushMessageReceiver {
    public void onReceivePassThroughMessage(final Context context, MiPushMessage message) {
        Log.v("MiReceiver",
                "onReceivePassThroughMessage is called. " + message.toString());


        Log.e("MiReceiver","Content = "+message.getContent());

        try {
            String ctData = message.getContent();
            final Bundle extras = stringToBundle(ctData);


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

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCommandResult(Context context, MiPushCommandMessage message) {
        String command = message.getCommand();
        List<String> arguments = message.getCommandArguments();
        String mRegId = ((arguments != null && arguments.size() > 0) ? arguments.get(0) : null);

        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                Log.d("MiReceiver","Xiaomi token - " + mRegId);
                CleverTapAPI.getDefaultInstance(context).pushXiaomiRegistrationId(mRegId,true);
            }
        }
    }

    @Override
    public void onReceiveRegisterResult(Context context, MiPushCommandMessage message) {
        String command = message.getCommand();
        List<String> arguments = message.getCommandArguments();
        String mRegId = ((arguments != null && arguments.size() > 0) ? arguments.get(0) : null);
        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                Log.d("MiReceiver","Xiaomi token - " + mRegId);
                CleverTapAPI.getDefaultInstance(context).pushXiaomiRegistrationId(mRegId,true);
            }
        }
    }

}
