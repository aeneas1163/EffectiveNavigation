package com.example.android.effectivenavigation.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AfterBootAlarmSetter extends BroadcastReceiver {
    public AfterBootAlarmSetter() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent service = new Intent(context, AlarmService.class);
        //TODO access db here and loop over every alarm to set it:
        AlarmService.setAlarm(context, "", "");
        //

        context.startService(service);
    }
}
