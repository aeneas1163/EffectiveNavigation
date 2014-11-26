package com.alphan.mcan.snoozecharity.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.alphan.mcan.snoozecharity.viewModels.AlarmScreen;

public class AlarmBroadcastReceiver extends BroadcastReceiver {

    public final String TAG = this.getClass().getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        int intentType = intent.getIntExtra(AlarmManagerHelper.INTENT_TYPE, -1);
        switch (intentType) {
            case AlarmManagerHelper.ALARM_INTENT : handleAlarmIntent(context, intent);
                break;
            case AlarmManagerHelper.DONATION_INTENT : handleDonationCheckIntent(context, intent);
                break;
            default:
                break;
        }
    }

    private void handleAlarmIntent(final Context context, Intent intent) {
        // start the Alarm Screen activity, the activity will deceide
        Intent alarmIntent = new Intent(context, AlarmScreen.class);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        alarmIntent.putExtras(intent);
        context.startActivity(alarmIntent);

        //TODO: start a servicec or dialog from here
    }

    private void handleDonationCheckIntent(Context context, Intent intent) {
        //TODO:
        //get the largest pending donation from db
        //check if it larger than warning threshold
        //if it is show notification to the user
        //profit.
    }

}
