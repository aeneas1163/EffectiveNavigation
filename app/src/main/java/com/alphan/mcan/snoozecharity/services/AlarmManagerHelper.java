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

    // ALARM METHODS:
    // add a completely new alarm and properly set it
    public static boolean createNewAlarm(Context context, AlarmDataModel newAlarm) {

        //try to add alarm to db
        AlarmDBAssistant dbHelper = new AlarmDBAssistant(context);
        boolean added = dbHelper.addAlarmToDB(newAlarm);
        if (!added)
            return false;

        //set alarm if needed
        return setAlarm(context, newAlarm);
    }

    // modify an existing alarm and set it
    public static boolean modifyAlarm(Context context, AlarmDataModel modifiedAlarm) {
        //try to modify the alarm in db
        AlarmDBAssistant dbHelper = new AlarmDBAssistant(context);
        boolean modified = dbHelper.updateAlarmInDB(modifiedAlarm);

        //remove existing alarm if there is one
        cancelPendingAlarm(context, modifiedAlarm);
        setAlarm(context, modifiedAlarm);
        return modified;
    }

    public static boolean resetAlarm(Context context, AlarmDataModel comingAlarm) {
        if (comingAlarm.getId() == -1)
            return false;

        cancelPendingAlarm(context, comingAlarm);
        setAlarm(context, comingAlarm);
        return true;
    }

    // remove an existing alarm
    public static boolean deleteAlarm(Context context, AlarmDataModel deletedAlarm) {

        //first remove existing alarm if there is one (deletion from DB invalidated its ID!)
        cancelPendingAlarm(context, deletedAlarm);

        //try to delete the alarm from db
        AlarmDBAssistant dbHelper = new AlarmDBAssistant(context);
        boolean deleted = dbHelper.removeAlarmFromDB(deletedAlarm);
        if (deleted)
            return true;
        else
            return false;
    }

    public static AlarmDataModel getAlarm(Context context, long iD) {
        AlarmDBAssistant dbHelper = new AlarmDBAssistant(context);
        return dbHelper.getAlarmFromDB(iD);
    }

    public static List<AlarmDataModel> getAlarms(Context context) {
        AlarmDBAssistant dbHelper = new AlarmDBAssistant(context);
        return dbHelper.getAlarmsInDB();
    }


    // DONATION METHODS
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



    // HELPER METHODS
    /**
     * helper method used to create alarm PendingIntents for every enabled alarm in our DB for
     * our service: {@link AlarmBroadcastReceiver}
     */
    private static void setAlarms(Context context) {

        AlarmDBAssistant dbHelper = new AlarmDBAssistant(context);
        List<AlarmDataModel> alarms =  dbHelper.getAlarmsInDB();
        for (AlarmDataModel alarm : alarms) {
            setAlarm(context, alarm);
        }
    }


    /**
     * helper method used to cancel alarm PendingIntents for the given model if there is one
     */
    private static boolean cancelPendingAlarm(Context context, AlarmDataModel alarm) {
        if (alarm.getId() == -1 )
            return false;

        PendingIntent pIntent = createAlarmPendingIntent(context, alarm);
        AlarmManager androidAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        androidAlarmManager.cancel(pIntent);
        return true;
    }


    /**
     * helper method used to create alarm PendingIntents for the given model for the correct time
     */
    private static boolean setAlarm(Context context, AlarmDataModel alarm) {
        if (!alarm.isEnabled())
            return false;

        boolean alarmSet = false;

        PendingIntent pIntent = createAlarmPendingIntent(context, alarm);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, alarm.getTimeHour());
        calendar.set(Calendar.MINUTE, alarm.getTimeMinute());
        calendar.set(Calendar.SECOND, 0);

        //Find next time to set
        final int nowDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        final int nowHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        final int nowMinute = Calendar.getInstance().get(Calendar.MINUTE);

        //First check if it's later in the week
        for (int dayOfWeek = Calendar.SUNDAY; dayOfWeek <= Calendar.SATURDAY; ++dayOfWeek)
            if (alarm.getRepeatingDay(dayOfWeek - 1) && dayOfWeek >= nowDay &&
                    !(dayOfWeek == nowDay && alarm.getTimeHour() < nowHour) &&
                    !(dayOfWeek == nowDay && alarm.getTimeHour() == nowHour && alarm.getTimeMinute() <= nowMinute)) {
                calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
                setPendingAndroidAlarm(context, calendar, pIntent);
                alarmSet = true;
                break;
            }

        //Else check if it's earlier in the week
        if (!alarmSet)
            for (int dayOfWeek = Calendar.SUNDAY; dayOfWeek <= Calendar.SATURDAY; ++dayOfWeek)
                if (alarm.getRepeatingDay(dayOfWeek - 1) && dayOfWeek <= nowDay && alarm.isWeekly()) {
                    calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
                    calendar.add(Calendar.WEEK_OF_YEAR, 1);

                    setPendingAndroidAlarm(context, calendar, pIntent);
                    alarmSet = true;
                    break;
                }

        // if alarm is still not set, check if it is a non-repeating one time alarm
        if (!alarmSet)
            if (!alarm.isWeekly()) { // if the time is less than now, then the alarm should be for tomorrow
                if (alarm.getTimeHour() < nowHour || (alarm.getTimeHour() == nowHour && alarm.getTimeMinute() < nowMinute)) {
                    // if it is for saturday, we should make it next week sunday
                    if (nowDay == Calendar.SATURDAY)
                    {
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                        calendar.add(Calendar.WEEK_OF_YEAR, 1);
                    }
                    // if it is any other day, we should not advance the week
                    else
                    {
                        calendar.add(Calendar.DAY_OF_WEEK, 1);
                    }
                    setPendingAndroidAlarm(context, calendar, pIntent);
                    alarmSet = true;
                }
                else { // else it is for today
                    setPendingAndroidAlarm(context, calendar, pIntent);
                    alarmSet = true;
                }
            }

        return alarmSet;
    }

    /**
     * helper method that schedules a pending alarm intent over android's {@link AlarmManager}
     */
    @SuppressLint("NewApi")
    private static void setPendingAndroidAlarm(Context context, Calendar calendar, PendingIntent pIntent) {
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

