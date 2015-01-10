package com.alphan.mcan.snoozecharity.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.alphan.mcan.snoozecharity.data.model.AlarmDataModel;


// class that handles call backs from androids alarm manager api
public class AlarmBroadcastReceiver extends BroadcastReceiver {


    // actions and params
    private static final String DELETE_ALARM = "snoozecharity.services.action.DELETE.ALARM";

    public final String TAG = this.getClass().getSimpleName();

    /**
     * Helper method to create a pending-intent for this service which to delete an alarm
     */
    public static PendingIntent getDeleteAlarmPendingIntent (Context context, long alarmID) {
        // generate pendingIntent
        Intent intent = getDeleteAlarmIntent(context, alarmID);
        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pIntent;
    }

    private static Intent getDeleteAlarmIntent(Context context, long alarmID) {
        Intent intent = new Intent(context, AlarmBroadcastReceiver.class);
        intent.setAction(DELETE_ALARM);
        intent.putExtra(AlarmManagerHelper.ID, alarmID);
        return intent;
    }


    @Override
    public void onReceive(Context context, Intent intent) {

        // intents from androids alarm manager:
        int intentType = intent.getIntExtra(AlarmManagerHelper.INTENT_TYPE, -1);
        switch (intentType) {
            case AlarmManagerHelper.ALARM_INTENT:
                handleAlarmIntent(context, intent);
                break;
            case AlarmManagerHelper.DONATION_INTENT:
                handleDonationCheckIntent(context, intent);
                break;
            default:
                break;
        }

        // internal intents used for alarm management
        final long alarmID = intent.getLongExtra(AlarmManagerHelper.ID, -1);
        final String action = intent.getAction();

        if (action != null) {
            if (action.equals(DELETE_ALARM))
                handleDeleteAlarmIntent(context, alarmID);
        }
    }

    // handler for alarm manager call back
    private void handleAlarmIntent(final Context context, final Intent intent) {
        long alarmID = intent.getLongExtra(AlarmManagerHelper.ID, -1);
        AlarmDataModel receivedAlarm = AlarmManagerHelper.getAlarm(context, alarmID);
        if ( !isValidAlarm(receivedAlarm))
            return;

        //reschedule next alarm:
        AlarmManagerHelper.resetAlarm(context, receivedAlarm);

        AlarmRingService.startRingAlarmIntent(context, alarmID);
    }

    private boolean isValidAlarm(AlarmDataModel receivedAlarm) {
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

    // internal handlers
    private void handleDeleteAlarmIntent(Context context, long alarmID) {
        Log.d(TAG, "Received Delete Alarm Intent...");

        //get and check alarm"
        AlarmDataModel alarmToDelete = AlarmManagerHelper.getAlarm(context, alarmID);
        if(alarmToDelete == null) {
            Log.d(TAG, "Delete Intent received for invalid alarm, ignoring!");
            return;
        }

        //try and check delete
        Boolean deleted = AlarmManagerHelper.deleteAlarm(context, alarmToDelete);
        if(!deleted) {
            Log.d(TAG, "Alarm deleted failed! (id:  " + alarmID + ")");
            return;
        }

        // remove alarm notification if there is one
        NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(AlarmRingService.getNotificationID(alarmToDelete));
        Log.d(TAG, "Alarm deleted: " + alarmID);
    }
}
