package com.alphan.mcan.snoozecharity.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import com.alphan.mcan.snoozecharity.data.model.AlarmDataModel;

import java.util.Calendar;

public class AlarmRingService extends Service {

    // actions and params
    private static final String RING_ALARM = "snoozecharity.services.action.RING.ALARM";
    private static final String SNOOZE_ALARM = "snoozecharity.services.action.SNOOZE.ALARM";
    private static final String DISMISS_ALARM = "snoozecharity.services.action.DISMISS.ALARM";
    private static final String TIME_OUT_ALARM = "snoozecharity.services.action.TIMEOUT.ALARM";
    private static final String PARAM_SNOOZE_DURATION = "snoozecharity.services.param.SNOOZE.DURATION";

    private enum DismissReasons {
        TIMEOUT, NEW_ALARM, SNOOZE, DISMISS
    };

    // alarm members:
    private AlarmDataModel ringingAlarm = null;
    private PendingIntent timeOutIntent = null;

    private Vibrator alarmVibrator = null;
    private final long[] vibrationPattern = new long[]{0, 100, 1000};  //TODO: make vibration pattern setting based?
    // without a delay, Vibrate for 100 milliseconds, Sleep for 1000 milliseconds
    private MediaPlayer alarmMediaPlayer = null;
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
        Intent intent = new Intent(context, AlarmRingService.class);
        intent.setAction(DISMISS_ALARM);
        intent.putExtra(AlarmManagerHelper.ID, alarmID);
        context.startService(intent);
    }

    /**
     * Helper method to create an pendingIntent for this service which cancels the currently
     * ringing alarm (supplied ID and the ID of the ringing alarm muss match)
     */
    private static PendingIntent startTimeOutAlarmPendingIntent (Context context, long alarmID, int timeOutDurationInMins) {

        // generate pendingIntent
        Intent intent = new Intent(context, AlarmRingService.class);
        intent.setAction(TIME_OUT_ALARM);
        intent.putExtra(AlarmManagerHelper.ID, alarmID);
        PendingIntent pIntent = PendingIntent.getBroadcast(context, (int) alarmID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // post pendingIntent
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long durationInMilliSeconds = SystemClock.elapsedRealtime() + (60 * 1000 * timeOutDurationInMins);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT)
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, durationInMilliSeconds, pIntent);
        else
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, durationInMilliSeconds, pIntent);

        return pIntent;
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
        alarmMediaPlayer = new MediaPlayer();
        alarmMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
        alarmMediaPlayer.setLooping(true);

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

        // get params
        final long alarmID = intent.getLongExtra(AlarmManagerHelper.ID, -1);
        final int snoozeDurationInMinutes = intent.getIntExtra(PARAM_SNOOZE_DURATION, -1);

        // handle actions
        final String action = intent.getAction();
        if (action.equals(RING_ALARM))
            handleActionRingAlarm(alarmID);
        else if (action.equals(SNOOZE_ALARM))
            handleActionSnoozeAlarm(alarmID, snoozeDurationInMinutes);
        else if (action.equals(DISMISS_ALARM))
            handleActionDismissAlarm(alarmID);
        else if (action.equals(TIME_OUT_ALARM))
            handleActionTimeOutAlarm(alarmID);
        else
            Log.d(TAG, "received unrecognized intent: " + action);

        // means if we are terminated for whatever reason system should not restart this service
        return START_NOT_STICKY;
    }

    private synchronized void handleActionRingAlarm(long alarmID) {

        if (ringingAlarm == null)
            startRinging(alarmID);
        else if (ringingAlarm.getId() == alarmID) {
            Log.d(TAG, "received double ring request for alarm: ID-" + alarmID);
            return; //we are requested to ring an alarm that is already ringing: do nothing
        } else { // dismiss/timeout previous alarm and ring this one
           dismissCurrent(DismissReasons.NEW_ALARM);
           startRinging(alarmID);
        }
    }

    private synchronized void handleActionSnoozeAlarm(long alarmID, int snoozeDurationInMinutes) {
        if (ringingAlarm == null || ringingAlarm.getId() != alarmID) {
            Log.d(TAG, ringingAlarm == null
                    ? "received snooze request while there is no ringing alarm! received ID:" + alarmID
                    : "received snooze request for an alarm which is not ringing! received ID:"+ alarmID);
            return; // do nothing since snooze request is not valid
        }

        createAndPostSnoozeAlarm();
        dismissCurrent(DismissReasons.SNOOZE);
        endService();
    }

    private synchronized void handleActionDismissAlarm(long alarmID) {
        if (ringingAlarm == null) {
            endService();
        } else if (ringingAlarm .getId() != alarmID) {
            Log.d(TAG, "received dismiss request for alarm which is not ringing! ID-" + alarmID);
            return;
        } else {
            dismissCurrent(DismissReasons.DISMISS);
        }
    }

    private synchronized void handleActionTimeOutAlarm(long alarmID) {
        if (ringingAlarm == null) {
            endService();
        } else if (ringingAlarm .getId() != alarmID) {
            Log.d(TAG, "received dismiss request for alarm which is not ringing! ID-" + alarmID);
            return;
        } else {
            dismissCurrent(DismissReasons.TIMEOUT);
        }
    }

    private void startRinging(long alarmID) {
        throw new UnsupportedOperationException("Not yet Implemented.!");

        // TODO:
        // 1 validity check, endService() if it fails
        // 2 get alarm from db
        // 3 set ringingAlarm =
        // 4 create PendingIntent for timeout (dismiss intent)
        // 5 trigger media player
        // 6 trigger vibrator
        // 7 create notification
        // 8 bring to foreground
    }

    private void dismissCurrent(DismissReasons reason) {
        throw new UnsupportedOperationException("Not yet Implemented.!");

        // TODO:
        // 1 validity check
        // 3 remove ringingAlarm
        // 4 cancel PendingIntent for auto cancel if needed (based on reason)
        // 5 stop media player
        // 6 stop vibrator
        // 7 update/remove notification (based on reason)
        // 6 remove from foreground
        // endService() if needed (based on reason)
    }

    private void createAndPostSnoozeAlarm() {
        if (ringingAlarm == null) {
            Log.d(TAG, "invalid internal state: Tried to snooze an alarm which is not ringing!");
            return;
        }
        // get snooze duration & create snooze alarm
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
        int snoozeDuration_InMinutes = Integer.parseInt(preference.getString("snooze_duration","5"));
        AlarmDataModel snoozeAlarm = AlarmDataModel.createSnoozingAlarm(ringingAlarm, snoozeDuration_InMinutes, true);

        AlarmManagerHelper.createNewAlarm(this, snoozeAlarm);
    }

    private void endService() {
        throw new UnsupportedOperationException("Not yet Implemented.!");

        // TODO:
        // 1 remove ringingAlarm
        // 2 remove PendingIntent for auto cancel if needed (based on reason)
        // 3 stop media player
        // 4 stop vibrator
        // 5 update/remove notification (based on reason)
        // 6 remove from foreground
        // 7 this.stopSelf();
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
        throw new UnsupportedOperationException("you can not bind to this service!");
        //return null; //you can not bind to this service
    }
}
