package com.alphan.mcan.snoozecharity.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.alphan.mcan.snoozecharity.data.model.AlarmDataModel;
import com.alphan.mcan.snoozecharity.data.persistence.AlarmDBAssistant;
import com.alphan.mcan.snoozecharity.viewModels.AlarmScreen;

public class AlarmReceiverService extends BroadcastReceiver {

    public static String TAG = AlarmReceiverService.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        // get received alarm from DB
        long receivedAlarmID = intent.getLongExtra(AlarmManagerHelper.ID, -1);
        AlarmDBAssistant dbHelper = new AlarmDBAssistant(context);
        AlarmDataModel receivedAlarm = dbHelper.getAlarm(receivedAlarmID);

        // if it is found in DB, disable it and update db, or snooze or what ever
        if (receivedAlarm != null) {
            receivedAlarm.setEnabled(false);
            AlarmManagerHelper.triggerAlarmModelUpdate(context, receivedAlarm);
        }

        Intent alarmIntent = new Intent(context, AlarmScreen.class);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        alarmIntent.putExtras(intent);
        context.startActivity(alarmIntent);

    }

}
