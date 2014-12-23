package com.alphan.mcan.snoozecharity.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;

import com.alphan.mcan.snoozecharity.data.model.AlarmDataModel;

import java.io.IOException;

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
    private Notification alarmNotification = null;
    private int alarmNotificationID = -1;

    // interactive members
    private Vibrator alarmVibrator = null;
    private final long[] vibrationPattern = new long[]{0, 100, 1000};  //TODO: make vibration pattern setting based?
    // without a delay, Vibrate for 100 milliseconds, Sleep for 1000 milliseconds
    private MediaPlayer alarmMediaPlayer = null;
    private PowerManager.WakeLock wakeLock = null; //TODO: this is possibly not needed here

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
        alarmMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK
                                                            | PowerManager.ACQUIRE_CAUSES_WAKEUP);

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
            Log.w(TAG, "received unrecognized intent: " + action);

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
            Log.i(TAG, ringingAlarm == null
                    ? "received snooze request while there is no ringing alarm! received ID:" + alarmID
                    : "received snooze request for an alarm which is not ringing! received ID:"+ alarmID);
            return; // do nothing since snooze request is not valid
        }

        createAndPostSnoozeAlarm(snoozeDurationInMinutes);
        dismissCurrent(DismissReasons.SNOOZE);
        endService();
    }

    private synchronized void handleActionDismissAlarm(long alarmID) {
        if (ringingAlarm == null) {
            endService();
        } else if (ringingAlarm .getId() != alarmID) {
            Log.i(TAG, "received dismiss request for alarm which is not ringing! ID-" + alarmID);
            return;
        } else {
            dismissCurrent(DismissReasons.DISMISS);
        }
    }

    private synchronized void handleActionTimeOutAlarm(long alarmID) {
        if (ringingAlarm == null) {
            endService();
        } else if (ringingAlarm .getId() != alarmID) {
            Log.i(TAG, "received dismiss request for alarm which is not ringing! ID-" + alarmID);
            return;
        } else {
            dismissCurrent(DismissReasons.TIMEOUT);
        }
    }

    private void startRinging(long alarmID) {

        // get alarm to ring
        ringingAlarm = AlarmManagerHelper.getAlarm(this, alarmID);
        if (ringingAlarm == null) {
            Log.w(TAG, "Alarm not found, can not ring!");
            endService();
            return;
        }

        // set time out for alarm
        int timeOutDurationInMin = 5; //TODO: make this setting based
        timeOutIntent = startTimeOutAlarmPendingIntent(this, alarmID, timeOutDurationInMin);

        // create notification
        alarmNotification = createNotification(ringingAlarm);
        startForeground((int)ringingAlarm.getId(), alarmNotification);

        // trigger vibration
        if (alarmVibrator != null)
            alarmVibrator.vibrate(vibrationPattern, 0);

        // prepare media player
        alarmMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mP) {
                mP.start();
            }});
        alarmMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mP, int what, int extra) {
                Log.d(TAG, "MediaPlayer issue: " + what + "-" + extra);
                endService();
                return false;
            }});

        try {
            alarmMediaPlayer.setDataSource(this, ringingAlarm.getAlarmTone());
        } catch (IOException e) {
            endService();
            Log.d(TAG, "MediaPlayer input issue!");
            return;
        }
        alarmMediaPlayer.prepareAsync();
    }

    private void dismissCurrent(DismissReasons reason) {

        // remove ringing alarm
        ringingAlarm = null;
        if (timeOutIntent != null)
            timeOutIntent.cancel();
        timeOutIntent = null;

        // stop vibration & media player
        if (alarmMediaPlayer != null)
            alarmMediaPlayer.stop();
        if (alarmVibrator != null)
            alarmVibrator.cancel();

        stopForeground(false);

        // based on reason for dismiss update/remove notification and endService if needed
        switch (reason) {
            case SNOOZE:
                updateSnoozeNotification(alarmNotification);
                endService();
                break;
            case TIMEOUT:
                updateMissedAlarmNotification(alarmNotification);
                endService();
                break;
            case NEW_ALARM:
                updateMissedAlarmNotification(alarmNotification);
                break;
            case DISMISS:
                dismissAlarmNotification(alarmNotification);
                endService();
                break;
        }

    }

    // notification management:
    private Notification createNotification(AlarmDataModel alarmToNotify) {
        throw new UnsupportedOperationException("Not yet Implemented.!");
        // TODO:
    }

    private void updateSnoozeNotification(Notification currentNotification){
        throw new UnsupportedOperationException("Not yet Implemented.!");
        // TODO:
    }

    private void updateMissedAlarmNotification(Notification currentNotification) {
        throw new UnsupportedOperationException("Not yet Implemented.!");
        // TODO:
    }

    private void dismissAlarmNotification(Notification currentNotification) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        throw new UnsupportedOperationException("Not yet Implemented.!");
        // TODO:
    }

    private void createAndPostSnoozeAlarm(int snoozeDurationInMinutes) {
        if (ringingAlarm == null) {
            Log.d(TAG, "invalid internal state: Tried to snooze an alarm which is not ringing!");
            return;
        }

        // update snooze duration
        if (snoozeDurationInMinutes == -1) { // if snooze duration is invalid update from settings
            SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
            snoozeDurationInMinutes = Integer.parseInt(preference.getString("snooze_duration", "5"));
        }

        // create snooze alarm and push
        AlarmDataModel snoozeAlarm = AlarmDataModel.createSnoozingAlarm(ringingAlarm, snoozeDurationInMinutes, true);
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
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("you can not bind to this service!");
        //return null; //you can not bind to this service
    }
}
