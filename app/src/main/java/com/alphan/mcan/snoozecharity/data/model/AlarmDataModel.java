package com.alphan.mcan.snoozecharity.data.model;

import android.net.Uri;
import android.provider.Settings;

import java.util.Arrays;
import java.util.Calendar;

/**
 * Created by mcni on 11/19/14.
 * representation of an alarm, maybe switch getters and setters for db access?
 */
public class AlarmDataModel {

    // week day definitions for repeat setup
    public static final int SUN = 0;
    public static final int MON = 1;
    public static final int TUE = 2;
    public static final int WED = 3;
    public static final int THU = 4;
    public static final int FRI = 5;
    public static final int SAT = 6;

    private boolean repeatingDays[]; //bool array for selecting repeating days (repeatingDays[Mon] = true..)
    private boolean isWeekly;
    private boolean snoozeAlarm; //TODO: add this fild to DB

    private long id; //db id of the alarm
    private boolean isEnabled;
    private String name;
    private String message;

    private int timeHour;
    private int timeMinute;

    private Uri alarmTone;  //TODO:
                            // acquire using ring tone selector and handle its return in UI
                            // new Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                            // thn handle its return:
                            // onActivityResult(int requestCode, int resultCode, Intent data)
                            // {
                            //    super.onActivityResult(requestCode, resultCode, data);
                            //      ...
                            //      ...
                            //    data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                            // }

    public AlarmDataModel() {
        repeatingDays = new boolean[7];
        Arrays.fill(repeatingDays, true);

        this.setName("Alarm");

        Calendar c = Calendar.getInstance();
        this.setTimeHour(c.get(Calendar.HOUR_OF_DAY));
        this.setTimeMinute(c.get(Calendar.MINUTE));

        this.setEnabled(false);
        this.setId(-1);
        this.setMessage("");
        this.setAlarmTone(Settings.System.DEFAULT_ALARM_ALERT_URI);
        this.setWeeklyRepeat(false);
        this.setSnoozeAlarm(false);
    }

    public AlarmDataModel(String name, int timeHour, int timeMinute) {
        this();

        this.setName(name);
        this.setTimeHour(timeHour);
        this.setTimeMinute(timeMinute);
    }

    // getters and setters
    public void setRepeatingDay(int dayOfWeek, boolean value) {
        repeatingDays[dayOfWeek] = value;
    }

    public boolean getRepeatingDay(int dayOfWeek) {
        return repeatingDays[dayOfWeek];
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean state) {
        this.isEnabled = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getTimeHour() {
        return timeHour;
    }

    public void setTimeHour(int timeHour) {
        this.timeHour = timeHour;
    }

    public int getTimeMinute() {
        return timeMinute;
    }

    public void setTimeMinute(int timeMinute) {
        this.timeMinute = timeMinute;
    }

    public Uri getAlarmTone() {
        return alarmTone;
    }

    public void setAlarmTone(Uri alarmTone) {
        this.alarmTone = alarmTone;
    }

    public boolean isWeekly() {
        return isWeekly;
    }

    public void setWeeklyRepeat(boolean isWeekly) {
        this.isWeekly = isWeekly;
    }

    public boolean isSnoozeAlarm() {
        return snoozeAlarm;
    }

    public void setSnoozeAlarm(boolean snoozeAlarm) {
        this.snoozeAlarm = snoozeAlarm;
    }
}
