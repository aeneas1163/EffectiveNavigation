package com.example.android.effectivenavigation.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.android.effectivenavigation.model.AlarmDataModel;
import com.example.android.effectivenavigation.persistence.AlarmDBAssistant;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;


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
    public static final String TIME_HOUR = "timeHour";
    public static final String TIME_MINUTE = "timeMinute";
    public static final String TONE = "alarmTone";

    // set alarms again on reboot
    @Override
    public void onReceive(Context context, Intent intent) {
        setAlarms(context);
    }


    // test method to set alarm to 1 minute in future
    public static void ring(Context context)
    {
        AlarmDataModel fake = new AlarmDataModel();
        Random r = new Random();
        fake.setId(r.nextInt(1000));
        fake.setMessage("Test Alarm " + fake.getId());
        fake.setName("Test Alarm");

        PendingIntent pIntent = createPendingIntent(context, fake);

        Calendar nowCal = new GregorianCalendar();
        nowCal.setTimeInMillis(System.currentTimeMillis());
        int day = nowCal.get(Calendar.DAY_OF_WEEK);
        int hour = nowCal.get(Calendar.HOUR_OF_DAY);
        int minute = nowCal.get(Calendar.MINUTE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(nowCal.getTimeInMillis());
        calendar.set(Calendar.DAY_OF_WEEK, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute+1);

        setAlarm(context, calendar, pIntent);

    }


    // sets all of the required alarms from DB
    public static void setAlarms(Context context) {
        /*
        cancelAlarms(context); //cancel every alarm we have set as they will be reset.

        AlarmDBAssistant dbHelper = new AlarmDBAssistant(context);

        List<AlarmDataModel> alarms =  dbHelper.getAlarms();
        for (AlarmDataModel alarm : alarms) {
            if (alarm.isEnabled()) {

                PendingIntent pIntent = createPendingIntent(context, alarm);

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, alarm.getTimeHour());
                calendar.set(Calendar.MINUTE, alarm.getTimeMinute());
                calendar.set(Calendar.SECOND, 00);

                //TODO: this can only set alarms for today, do a loop to set alarms for other days
                setAlarm(context, calendar, pIntent);

            }
        }*/
    }

    // cancels the alarms that as been set
    public static void cancelAlarms(Context context) {
        /*
        AlarmDBAssistant dbHelper = new AlarmDBAssistant(context);
        List<AlarmDataModel> alarms =  dbHelper.getAlarms();
        if (alarms != null) {
            for (AlarmDataModel alarm : alarms) {
                if (alarm.isEnabled()) {
                    PendingIntent pIntent = createPendingIntent(context, alarm);
                    AlarmManager androidAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    androidAlarmManager.cancel(pIntent);
                }
            }
        }
        */
    }

    /**
     * helper method used to create PendingIntents for our service: {@link AlarmReceiverService}
    */
    private static PendingIntent createPendingIntent(Context context, AlarmDataModel model) {
        Intent intent = new Intent(context, AlarmReceiverService.class);
        intent.putExtra(ID, model.getId());
        intent.putExtra(NAME, model.getName());
        intent.putExtra(TIME_HOUR, model.getTimeHour());
        intent.putExtra(TIME_MINUTE, model.getTimeMinute());
        intent.putExtra(TONE, model.getAlarmTone());

        return PendingIntent.getService(context, (int)model.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * helper method that schedules a pending intent over android's {@link AlarmManager}
     */
    private static void setAlarm(Context context, Calendar calendar, PendingIntent pIntent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pIntent);
        }
    }

}

