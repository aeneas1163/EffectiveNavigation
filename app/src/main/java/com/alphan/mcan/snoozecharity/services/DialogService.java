package com.alphan.mcan.snoozecharity.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.alphan.mcan.snoozecharity.viewModels.AlarmScreen;

public class DialogService extends IntentService {

    private static final String ACTION_RING_ALARM = "com.alphan.mcan.snoozecharity.services.action.RING.ALARM";

    public DialogService() {
        super("DialogService");
    }

    /**
     * Starts this service to perform action ACTION_RING_ALARM with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionRingAlarm(Context context, long alarmID) {
        Intent intent = new Intent(context, DialogService.class);
        intent.setAction(ACTION_RING_ALARM);
        intent.putExtra(AlarmManagerHelper.ID, alarmID);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_RING_ALARM.equals(action)) {
                final long alarmID = intent.getLongExtra(AlarmManagerHelper.ID, -1);
                handleActionRingAlarm(alarmID);
            }
            //TODO: more actions later?
        }
    }

    /**
     * Handle action RingAlarm in the provided background thread with the provided
     * parameters.
     */
    private void handleActionRingAlarm(final long alarmID) {
        // start the Alarm Screen activity, the activity will deceide
        Intent alarmIntent = new Intent(this, AlarmScreen.class);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        alarmIntent.putExtra(AlarmManagerHelper.ID, alarmID);
        startActivity(alarmIntent);

        //TODO: exchange this with Dialog instead
    }
}
