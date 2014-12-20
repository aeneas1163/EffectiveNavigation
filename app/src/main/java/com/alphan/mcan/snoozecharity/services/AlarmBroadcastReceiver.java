package com.alphan.mcan.snoozecharity.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.alphan.mcan.snoozecharity.data.model.AlarmDataModel;


// class that handles call backs from androids alarm manager api
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

    private void handleAlarmIntent(final Context context, final Intent intent) {
        long alarmID = intent.getLongExtra(AlarmManagerHelper.ID, -1);
        if ( !isValidAlarm(context, alarmID))
            return;

        //TODO: move rescheduling of the next alarm here so we do not have to do it on the receiver
        DialogServiceDEPRECATED.startActionRingAlarm(context, alarmID);
    }

    private boolean isValidAlarm(final Context context, final long alarmID) {
        AlarmDataModel receivedAlarm = AlarmManagerHelper.getAlarm(context, alarmID);
        if (receivedAlarm == null)
            return false;
        //TODO: check time of the received alarm versus current time, handle if it is too different.
        if (receivedAlarm.isEnabled())
            return true;
        return false;
    }

    private void handleDonationCheckIntent(Context context, Intent intent) {
        //TODO:
        //get the largest pending donation from db
        //check if it larger than warning threshold
        //if it is show notification to the user
        //profit.
    }

}
