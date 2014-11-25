package com.alphan.mcan.snoozecharity.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.alphan.mcan.snoozecharity.viewModels.AlarmScreen;

public class AlarmBroadcastReceiver extends BroadcastReceiver {

    public static String TAG = AlarmBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        // start the Alarm Screen activity, the activity will deceide
        Intent alarmIntent = new Intent(context, AlarmScreen.class);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        alarmIntent.putExtras(intent);
        context.startActivity(alarmIntent);

    }

}
