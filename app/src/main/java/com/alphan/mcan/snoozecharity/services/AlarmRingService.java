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
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.alphan.mcan.snoozecharity.data.model.AlarmDataModel;
import com.alphan.mcan.snoozecharity.R;

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
    private AlarmDataModel snoozeAlarm = null;
    private int defaultSnoozeDuration = -1;
    private PendingIntent timeOutIntent = null;
    private Notification alarmNotification = null;
    private int alarmNotificationID = -1;

    // interactive members
    private Vibrator alarmVibrator = null;
    private final long[] vibrationPattern = new long[]{500, 200, 1000};  //TODO: make vibration pattern setting based?
    // with half second delay, Vibrate for 200 milliseconds, Sleep for 1000 milliseconds
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
        Intent snoozeIntent = getSnoozeAlarmIntent(context, alarmID, snoozeDurationInMins);
        context.startService(snoozeIntent);
    }

    private static Intent getSnoozeAlarmIntent(Context context, long alarmID, int snoozeDurationInMins) {
        Intent intent = new Intent(context, AlarmRingService.class);
        intent.setAction(SNOOZE_ALARM);
        intent.putExtra(AlarmManagerHelper.ID, alarmID);
        intent.putExtra(PARAM_SNOOZE_DURATION ,snoozeDurationInMins);
        return intent;
    }

    /**
     * Helper method to create an intent for this service which cancels the currently
     * ringing alarm (supplied ID and the ID of the ringing alarm muss match)
     */
    public static void startDismissAlarmIntent (Context context, long alarmID) {
        Intent dismissAlarmIntent = getDismissAlarmIntent(context, alarmID);
        context.startService(dismissAlarmIntent);
    }

    private static Intent getDismissAlarmIntent (Context context, long alarmID) {
        Intent intent = new Intent(context, AlarmRingService.class);
        intent.setAction(DISMISS_ALARM);
        intent.putExtra(AlarmManagerHelper.ID, alarmID);
        return intent;
    }

    /**
     * Helper method to create an pendingIntent for this service which cancels the currently
     * ringing alarm (supplied ID and the ID of the ringing alarm muss match)
     */
    private static PendingIntent startTimeOutAlarmPendingIntent (Context context, AlarmDataModel alarm,
                                                                 int timeOutDurationInMins) {

        // generate pendingIntent
        Intent intent = new Intent(context, AlarmRingService.class);
        intent.setAction(TIME_OUT_ALARM);
        intent.putExtra(AlarmManagerHelper.ID, alarm.getId());

        PendingIntent pIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // post pendingIntent
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long durationInMilliSeconds = SystemClock.elapsedRealtime() + (60 * 1000 * timeOutDurationInMins);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT)
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, durationInMilliSeconds, pIntent);
        else
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, durationInMilliSeconds, pIntent);

        return pIntent;
    }

    private static int getNotificationID(AlarmDataModel alarm) {
        //TODO: for snooze alarms get ID of the parent
        return (int) alarm.getId();
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

        // settings
        // update snooze duration
        if (defaultSnoozeDuration == -1) { // if snooze duration is invalid update from settings
            SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
            defaultSnoozeDuration = Integer.parseInt(preference.getString("snooze_duration", "5"));
        }

        //TODO: below can possibly be removed
        // prepare wakelock
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(( PowerManager.FULL_WAKE_LOCK
                                  | PowerManager.SCREEN_BRIGHT_WAKE_LOCK //TODO: these are possibly not needed
                                  | PowerManager.ACQUIRE_CAUSES_WAKEUP), TAG);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) { //This should never happen here
            Log.w(TAG, "Received null intent!");
            return START_NOT_STICKY;
        }

        // get params
        final long alarmID = intent.getLongExtra(AlarmManagerHelper.ID, -1);
        final int snoozeDurationInMinutes = intent.getIntExtra(PARAM_SNOOZE_DURATION, -1);
        Log.d(TAG, "Received Intent for AlarmID: " + alarmID);

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

        Log.d(TAG, "Creating snooze alarm in " + snoozeDurationInMinutes + " mins.");
        createAndPostSnoozeAlarm(snoozeDurationInMinutes);
        dismissCurrent(DismissReasons.SNOOZE);
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
        int timeOutDurationInMin = 3; //TODO: make this setting based
        timeOutIntent = startTimeOutAlarmPendingIntent(this, ringingAlarm, timeOutDurationInMin);
        Log.i(TAG, "generated auto time-out for alarm: " + alarmID + " time out int: " + timeOutDurationInMin + " mins.");

        // create notification
        alarmNotification = createNotification(ringingAlarm);
        alarmNotificationID = getNotificationID(ringingAlarm);
        startForeground(alarmNotificationID, alarmNotification);
        Log.i(TAG, "Notification created, and service moved to foreground with ID: " + alarmNotificationID);

        // trigger vibration
        if (alarmVibrator != null) {
            alarmVibrator.vibrate(vibrationPattern, 0);
            Log.i(TAG, "Vibration Started!");
        }

        // prepare media player
        alarmMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mP) {
                Log.i(TAG, "Starting media player!");
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

        Log.i(TAG, "Preparing media player for alarm!");
        alarmMediaPlayer.prepareAsync();
    }

    private void dismissCurrent(DismissReasons reason) {

        // remove ringing alarm
        Log.i(TAG, "dismissing alarm: " + ringingAlarm.getId());
        AlarmDataModel dismissedAlarm = ringingAlarm;
        ringingAlarm = null;
        if (timeOutIntent != null)
            timeOutIntent.cancel();
        timeOutIntent = null;

        // stop vibration & media player
        if (alarmMediaPlayer != null)
            alarmMediaPlayer.stop();
        if (alarmVibrator != null)
            alarmVibrator.cancel();
        Log.i(TAG, "Media and Vibrator stopped");

        stopForeground(true);
        Log.i(TAG, "Service moved to background");

        // based on reason for dismiss update/remove notification and endService if needed
        switch (reason) {
            case SNOOZE:
                Log.i(TAG, "Dismissed due SNOOZE: updating snooze notification, and ending service.");
                updateSnoozeNotification(snoozeAlarm);
                endService();
                break;
            case TIMEOUT:
                Log.i(TAG, "Dismissed due TIMEOUT: updating missed alarm notification, and ending service.");
                updateMissedAlarmNotification(dismissedAlarm);
                endService();
                break;
            case NEW_ALARM:
                Log.i(TAG, "Dismissed due NEW_ALARM: updating missed alarm notification.");
                updateMissedAlarmNotification(dismissedAlarm);
                break;
            case DISMISS:
                Log.i(TAG, "Dismissed due DISMISS action: removing notification and ending service.");
                dismissAlarmNotification();
                endService();
                break;
        }

    }

    // notification management:
    private Notification createNotification(AlarmDataModel alarmToNotify) {

        // initial notification
        NotificationCompat.Builder builder  = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setGroup(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(false)
                .setContentIntent(null); //TODO: this should open alarmActivity;

        // message
        String notificationMessage = alarmToNotify.getName()
                + (alarmToNotify.getMessage() != ""
                ? (" " + alarmToNotify.getMessage())
                : "Wake Up!");
        builder.setContentText(notificationMessage);

        // snooze action
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
        int snoozeDuration = Integer.parseInt(preference.getString("snooze_duration","5"));
        PendingIntent snoozePendingIntent = PendingIntent.getService(this, 0,
                getSnoozeAlarmIntent(this, alarmToNotify.getId(), snoozeDuration),
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(R.drawable.clock, "Snooze", snoozePendingIntent); // #0

        // dismiss action
        PendingIntent dismissPendingIntent = PendingIntent.getService(this, 0,
                getDismissAlarmIntent(this, alarmToNotify.getId()),
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(R.drawable.delete, "Dismiss", dismissPendingIntent) ; // #1

        return builder.build();       // throw new UnsupportedOperationException("Not yet Implemented.!");
    }

    private void updateSnoozeNotification(AlarmDataModel snoozeAlarm){
        // initial notification
        NotificationCompat.Builder builder  = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setGroup(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(null); //TODO: this should open alarmActivity;

        // dismiss action
        //TODO: create a pending intent which cancels: snoozeAlarm
        PendingIntent dismissPendingIntent = null;
        builder.addAction(R.drawable.delete, "Dismiss", dismissPendingIntent) ; // #1

        // message
        String formedHour = (snoozeAlarm.getTimeHour() < 10) ? "0" + snoozeAlarm.getTimeHour() : String.valueOf(snoozeAlarm.getTimeHour());
        String formedMin = (snoozeAlarm.getTimeMinute() < 10) ? "0" + snoozeAlarm.getTimeMinute() : String.valueOf(snoozeAlarm.getTimeMinute());
        String snoozeMessage = "Snoozed alarm until " + formedHour + ":" + formedMin;
        builder.setContentText(snoozeMessage);

        // push updated
        alarmNotification = builder.build();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(alarmNotificationID, alarmNotification);
    }

    private void updateMissedAlarmNotification(AlarmDataModel missedAlarm) {
        // initial notification
        NotificationCompat.Builder builder  = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setGroup(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setContentIntent(null); //TODO: this should open alarmActivity;

        // message
        String snoozeMessage = "Missed an alarm from: " + missedAlarm.getTimeHour() + ":" + missedAlarm.getTimeMinute();
        builder.setContentText(snoozeMessage);

        // push updated
        alarmNotification = builder.build();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(alarmNotificationID, alarmNotification);
    }

    private void dismissAlarmNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(alarmNotificationID);

        alarmNotification = null;
        alarmNotificationID = -1;
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
        Log.d(TAG, "created snooze alarm: " + snoozeAlarm.getId() + " at: " +
                snoozeAlarm.getTimeHour() + ":"  + snoozeAlarm.getTimeMinute());
        this.snoozeAlarm = snoozeAlarm;
    }

    private void endService() {
        Log.i(TAG, "Ending Alarm Service");

        // alarm data
        ringingAlarm = null;
        if (timeOutIntent != null)
            timeOutIntent.cancel();
        timeOutIntent = null;
        alarmNotification = null;
        alarmNotificationID = -1;
        Log.i(TAG, "removed alarm data, timeout, and notification");

        // vibrator and media player
        if (alarmVibrator != null) {
            alarmVibrator.cancel();
            alarmVibrator = null;
            Log.i(TAG, "removed alarm vibrator");
        }
        if (alarmMediaPlayer != null) {
            alarmMediaPlayer.setOnErrorListener(null);
            alarmMediaPlayer.setOnPreparedListener(null);
            alarmMediaPlayer.stop();
            alarmMediaPlayer.release();
            alarmMediaPlayer = null;
            Log.i(TAG, "removed alarm media player");
        }

        // wakelog
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            Log.i(TAG, "released wakelock!");
        }
        wakeLock = null;

        Log.i(TAG, "Stopped service!");
        this.stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("you can not bind to this service!");
        //return null; //you can not bind to this service
    }
}
