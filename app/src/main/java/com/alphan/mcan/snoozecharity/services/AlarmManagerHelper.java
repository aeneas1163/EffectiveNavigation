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
public class AlarmManagerHelper extends BroadcastReceiver {
    public AlarmManagerHelper() {
    }

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String MESSAGE = "message";
    public static final String TIME_HOUR = "timeHour";
    public static final String TIME_MINUTE = "timeMinute";
    public static final String TONE = "alarmTone";

    // set alarms again on reboot
    @Override
    public void onReceive(Context context, Intent intent) {
        setAlarms(context);
    }

    public static void triggerAlarmModelUpdate(Context context, AlarmDataModel alarm ) {
        AlarmDBAssistant dbHelper = new AlarmDBAssistant(context);

        AlarmManagerHelper.cancelAlarms(context);

        if (alarm.getId() < 0)
            dbHelper.addAlarm(alarm);
        else
            dbHelper.updateAlarm(alarm);

        AlarmManagerHelper.setAlarms(context);
    }

    // sets all of the required alarms from DB
    public static void setAlarms(Context context) {
        cancelAlarms(context); //cancel every alarm we have set as they will be reset.

        AlarmDBAssistant dbHelper = new AlarmDBAssistant(context);

        List<AlarmDataModel> alarms =  dbHelper.getAlarms();

        for (AlarmDataModel alarm : alarms)
            if (alarm.isEnabled()) {

                PendingIntent pIntent = createPendingIntent(context, alarm);

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
                            break;
                        }
            }

    }

    // cancels the alarms that as been set
    public static void cancelAlarms(Context context) {
        AlarmDBAssistant dbHelper = new AlarmDBAssistant(context);

        List<AlarmDataModel> alarms =  dbHelper.getAlarms();
        if (alarms != null) for (AlarmDataModel alarm : alarms)
            if (alarm.isEnabled()) {
                PendingIntent pIntent = createPendingIntent(context, alarm);
                AlarmManager androidAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                androidAlarmManager.cancel(pIntent);
            }
    }

    /**
     * helper method used to create PendingIntents for our service: {@link AlarmReceiverService}
    */
    private static PendingIntent createPendingIntent(Context context, AlarmDataModel model) {
        Intent intent = new Intent(context, AlarmReceiverService.class);
        intent.putExtra(ID, model.getId());
        intent.putExtra(NAME, model.getName());
        intent.putExtra(MESSAGE, model.getMessage());
        intent.putExtra(TIME_HOUR, model.getTimeHour());
        intent.putExtra(TIME_MINUTE, model.getTimeMinute());
        intent.putExtra(TONE, model.getAlarmTone());

        return PendingIntent.getService(context, (int)model.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * helper method that schedules a pending intent over android's {@link AlarmManager}
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

}

