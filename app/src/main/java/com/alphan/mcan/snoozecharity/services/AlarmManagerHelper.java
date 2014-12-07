package com.alphan.mcan.snoozecharity.services;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.alphan.mcan.snoozecharity.data.model.AlarmDataModel;
import com.alphan.mcan.snoozecharity.data.persistence.AlarmDBAssistant;

import java.util.Calendar;
import java.util.List;


/**
 * An {@link android.app.AlarmManager} helper class used to set intents using this service to
 * set and cancel alarms. This is also responsible for restarting alarms on reboot
 * <p/>
 */
public class AlarmManagerHelper extends BroadcastReceiver { //TODO convert to local-broadcast receiver?
    public AlarmManagerHelper() {
    }

    // intent params:
    public static final String INTENT_TYPE = "intentType";
    public static final int ALARM_INTENT = 0;
    public static final int DONATION_INTENT = 1;
    // alarm params:
    public static final String ID = "id";

    // set alarms again on reboot
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            setAlarms(context);
            //TODO: if (donationReminderIsEnabled)
            // get if from SnoozeApplication.sp
            enableDonationCheck(context);
        }
    }

    // add/modify an existing alarm and set it
    public static void createOrModifyAlarmPendingIntent(Context context, AlarmDataModel newAlarm) {
        AlarmDBAssistant dbHelper = new AlarmDBAssistant(context);

        AlarmManagerHelper.cancelPendingAlarms(context);

        if (newAlarm.getId() < 0)
            dbHelper.addAlarm(newAlarm);
        else
            dbHelper.updateAlarm(newAlarm);

        AlarmManagerHelper.setAlarms(context);
    }

    // cancels all of the pending alarm intents
    public static void cancelPendingAlarms(Context context) {
        AlarmDBAssistant dbHelper = new AlarmDBAssistant(context);

        List<AlarmDataModel> alarms =  dbHelper.getAlarms();
        if (alarms != null) for (AlarmDataModel alarm : alarms)
            if (alarm.isEnabled()) {
                PendingIntent pIntent = createAlarmPendingIntent(context, alarm);
                AlarmManager androidAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                androidAlarmManager.cancel(pIntent);
            }
    }

    // enable a donation reminder check alarm (every 2 days)
    public static void enableDonationCheck(Context context) {

        PendingIntent pIntent = createDonationPendingIntent(context);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // set a reminder to check donation status every 2 days (not counting today)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 20);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY * 2, pIntent);
    }

    // cancel the donation reminder
    public static void disableDonationCheck(Context context) {
        PendingIntent pIntent = createDonationPendingIntent(context);
        AlarmManager androidAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        androidAlarmManager.cancel(pIntent);
    }

    /**
     * helper method used to create alarm PendingIntents for every enabled alarm in our DB for
     * our service: {@link AlarmBroadcastReceiver}
     */
    private static void setAlarms(Context context) {

        AlarmDBAssistant dbHelper = new AlarmDBAssistant(context);
        List<AlarmDataModel> alarms =  dbHelper.getAlarms();
        for (AlarmDataModel alarm : alarms)
            if (alarm.isEnabled()) {

                PendingIntent pIntent = createAlarmPendingIntent(context, alarm);

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, alarm.getTimeHour());
                calendar.set(Calendar.MINUTE, alarm.getTimeMinute());
                calendar.set(Calendar.SECOND, 00);

                //Find next time to set
                final int nowDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
                final int nowHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                final int nowMinute = Calendar.getInstance().get(Calendar.MINUTE);
                boolean alarmSet = false;

                //First check if it's later in the week
                for (int dayOfWeek = Calendar.SUNDAY; dayOfWeek <= Calendar.SATURDAY; ++dayOfWeek)
                    if (alarm.getRepeatingDay(dayOfWeek - 1) && dayOfWeek >= nowDay &&
                            !(dayOfWeek == nowDay && alarm.getTimeHour() < nowHour) &&
                            !(dayOfWeek == nowDay && alarm.getTimeHour() == nowHour && alarm.getTimeMinute() <= nowMinute)) {
                        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);

                        setAlarm(context, calendar, pIntent);
                        alarmSet = true;
                        break;
                    }

                //Else check if it's earlier in the week
                if (!alarmSet)
                    for (int dayOfWeek = Calendar.SUNDAY; dayOfWeek <= Calendar.SATURDAY; ++dayOfWeek)
                        if (alarm.getRepeatingDay(dayOfWeek - 1) && dayOfWeek <= nowDay && alarm.isWeekly()) {
                            calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
                            calendar.add(Calendar.WEEK_OF_YEAR, 1);

                            setAlarm(context, calendar, pIntent);
                            alarmSet = true;
                            break;
                        }
                // if alarm is still not set, check if it is a non-repeating one time alarm
                if (!alarmSet)
                    if (!alarm.isWeekly()) {
                        setAlarm(context, calendar, pIntent);
                    }
            }

    }

    /**
     * helper method that schedules a pending alarm intent over android's {@link AlarmManager}
     */
    @SuppressLint("NewApi")
    private static void setAlarm(Context context, Calendar calendar, PendingIntent pIntent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pIntent);
        }
    }

    /**
     * helper method used to create an Alarm PendingIntents for our service: {@link AlarmBroadcastReceiver}
    */
    private static PendingIntent createAlarmPendingIntent(Context context, AlarmDataModel model) {

        Intent intent = new Intent(context, AlarmBroadcastReceiver.class);
        intent.putExtra(INTENT_TYPE, ALARM_INTENT);
        intent.putExtra(ID, model.getId());

        return PendingIntent.getBroadcast(context, (int) model.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * helper method used to create an Donation check PendingIntent for our service: {@link AlarmBroadcastReceiver}
     */
    private static PendingIntent createDonationPendingIntent(Context context) {

        Intent intent = new Intent(context, AlarmBroadcastReceiver.class);
        intent.putExtra(INTENT_TYPE, DONATION_INTENT);

        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

}

