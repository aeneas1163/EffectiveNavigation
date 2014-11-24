package com.alphan.mcan.snoozecharity.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.alphan.mcan.snoozecharity.viewModels.AlarmScreen;

public class AlarmReceiverService extends BroadcastReceiver {

    public static String TAG = AlarmReceiverService.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent != null) {

            Intent alarmIntent = new Intent(context, AlarmScreen.class);
            alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            alarmIntent.putExtras(intent);
            context.startActivity(alarmIntent);

            AlarmManagerHelper.setAlarms(context);
        }
    }

}
