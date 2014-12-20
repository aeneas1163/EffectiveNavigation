package com.alphan.mcan.snoozecharity.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;

import com.alphan.mcan.snoozecharity.data.model.AlarmDataModel;

public class AlarmRingService extends Service {

    // actions and params
    private static final String RING_ALARM = "snoozecharity.services.action.RING.ALARM";
    private static final String DISMISS_ALARM = "snoozecharity.services.action.DISMISS.ALARM";
    private static final String SNOOZE_ALARM = "snoozecharity.services.action.SNOOZE.ALARM";
    private static final String PARAM_SNOOZE_DURATION = "snoozecharity.services.param.SNOOZE.DURATION";
    private static final String PARAM_DISMISS_TIMEOUT = "snoozecharity.services.param.DISMISS.TIMEOUT";

    // alarm members:
    private AlarmDataModel ringingAlarm = null;
    private Vibrator alarmVibrator = null;
    private final long[] vibrationPattern = new long[]{0, 100, 1000};  //TODO: make vibration pattern setting based?
    // without a delay, Vibrate for 100 milliseconds, Sleep for 1000 milliseconds
    private MediaPlayer alarmMediaPlater = null;
    private PowerManager.WakeLock wakeLock = null;

    // for logging and stuff
    public final String TAG = this.getClass().getSimpleName();

    /**
     * Helper method to create an intent for this service which starts ringing for
     * the supplied alarm
     */
    public static void startRingAlarmIntent (Context context, long alarmID) {
        Intent intent = new Intent(context, AlarmRingService.class);
        intent.setAction(RING_ALARM);
        intent.putExtra(AlarmManagerHelper.ID, alarmID);
        context.startService(intent);
    }

    /**
     * Helper method to create an intent for this service which snoozes the currently ringing
     * alarm for the supplied duration (supplied ID and the ID of the ringing alarm muss match)
     */
    public static void startSnoozeAlarmIntent (Context context, long alarmID, int snoozeDurationInMins) {
        Intent intent = new Intent(context, AlarmRingService.class);
        intent.setAction(SNOOZE_ALARM);
        intent.putExtra(AlarmManagerHelper.ID, alarmID);
        intent.putExtra(PARAM_SNOOZE_DURATION ,snoozeDurationInMins);
        context.startService(intent);
    }

    /**
     * Helper method to create an intent for this service which cancels the currently
     * ringing alarm (supplied ID and the ID of the ringing alarm muss match)
     */
    public static void startDismissAlarmIntent (Context context, long alarmID) {
        startDismissAlarmIntent(context, alarmID, false);
    }
    private static void startDismissAlarmIntent (Context context, long alarmID, boolean dismissedDueTimeOut) {
        Intent intent = new Intent(context, AlarmRingService.class);
        intent.setAction(DISMISS_ALARM);
        intent.putExtra(AlarmManagerHelper.ID, alarmID);
        intent.putExtra(PARAM_DISMISS_TIMEOUT, dismissedDueTimeOut);
        context.startService(intent);
    }

    public AlarmRingService() {
    }

    @Override
    public void onCreate() {
        // prepare vibrator
        alarmVibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        if (!alarmVibrator.hasVibrator())
            alarmVibrator = null;

        // prepare alarm media player
        alarmMediaPlater = new MediaPlayer();
        alarmMediaPlater.setAudioStreamType(AudioManager.STREAM_ALARM);
        alarmMediaPlater.setLooping(true);

        // prepare wakelock
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(( PowerManager.FULL_WAKE_LOCK
                                  | PowerManager.SCREEN_BRIGHT_WAKE_LOCK //TODO: these are possibly not needed
                                  | PowerManager.ACQUIRE_CAUSES_WAKEUP), TAG);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) { //This should never happen here
            Log.d(TAG, "Received null intent!");
            return START_NOT_STICKY;
        }

        final String action = intent.getAction();
        final long alarmID = intent.getLongExtra(AlarmManagerHelper.ID, -1);
        final int snoozeDurationInMinutes = intent.getIntExtra(PARAM_SNOOZE_DURATION, -1);
        if (action.equals(RING_ALARM))
            handleActionRingAlarm(alarmID);
        else if (action.equals(SNOOZE_ALARM))
            handleActionSnoozeAlarm(alarmID, snoozeDurationInMinutes);
        else if (action.equals(DISMISS_ALARM))
            handleActionDismissAlarm(alarmID, false);
        else
            Log.d(TAG, "received unrecognized intent: " + action);

        return START_NOT_STICKY; // this means if we are terminated for whatever reason system should not restart this
    }

    private void handleActionRingAlarm(long alarmID) {
        throw new UnsupportedOperationException("To Be Implemented,again..");
    }

    private void handleActionSnoozeAlarm(long alarmID, int snoozeDurationInMinutes) {
        throw new UnsupportedOperationException("To Be Implemented,again..");
    }

    private void handleActionDismissAlarm(long alarmID, boolean dismissedDueTimeOut) {
        throw new UnsupportedOperationException("To Be Implemented,again..");
    }


    @Override
    public void onDestroy() {
        //TODO
        throw new UnsupportedOperationException("Not yet Implemented.!");
        //super.onDestroy();

        //TODO: Ringing stuff
        //alarmVibrator.vibrate(pattern, 0);
        //alarmMediaPlater.setDataSource(this, tone);
        //alarmMediaPlater.prepare();
        //alarmMediaPlater.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; //you can not bind to this service
    }
}
