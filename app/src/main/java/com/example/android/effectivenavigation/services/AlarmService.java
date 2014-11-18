package com.example.android.effectivenavigation.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class AlarmService extends IntentService {

    public AlarmService() {
        super("AlarmService");
    }

    private static final String ACTION_SET_ALARM = "SET ALARM";
    private static final String ACTION_CANCEL_ALARM = "CANCEL ALARM";

    private static final String EXTRA_ALARM_ID = "ALARM ID";
    private static final String EXTRA_ALARM_TIME = "ALARM TIME";

    /**
     * Starts this service to perform action setAlarm with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void setAlarm(Context context, String alarmID, String alarmTime) {
        Intent intent = new Intent(context, AlarmService.class);
        intent.setAction(ACTION_SET_ALARM);
        intent.putExtra(EXTRA_ALARM_ID, alarmID);
        intent.putExtra(EXTRA_ALARM_TIME, alarmTime);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action cancelAlarm with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void cancelAlarm(Context context, String alarmID) {
        Intent intent = new Intent(context, AlarmService.class);
        intent.setAction(ACTION_CANCEL_ALARM);
        intent.putExtra(EXTRA_ALARM_ID, alarmID);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SET_ALARM.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_ALARM_ID);
                final String param2 = intent.getStringExtra(EXTRA_ALARM_TIME);
                handleSetAlarm(param1, param2);
            } else if (ACTION_CANCEL_ALARM.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_ALARM_ID);
                handleCancelAlarm(param1);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleSetAlarm(String param1, String param2) {
        // TODO: Handle action, need db access
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleCancelAlarm(String param1) {
        // TODO: Handle action
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
