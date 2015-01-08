package com.alphan.mcan.snoozecharity.data.model;

import android.net.Uri;

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
    private long parentAlarmID;

    private long id; //db id of the alarm
    private boolean isEnabled;
    private String name;
    private String message;

    private int timeHour;
    private int timeMinute;

    private Uri alarmTone;

    public static AlarmDataModel createSnoozingAlarm(AlarmDataModel alarmToSnooze, int snoozeDuration_InMinutes,
                                                     Boolean snoozeFromNow) {
        // basic model
        AlarmDataModel snoozeAlarm = new AlarmDataModel();
        snoozeAlarm.setName(alarmToSnooze.getName());
        snoozeAlarm.setMessage(alarmToSnooze.getMessage());
        snoozeAlarm.setAlarmTone(alarmToSnooze.getAlarmTone());
        snoozeAlarm.setEnabled(true);
        snoozeAlarm.setSnoozeAlarm(alarmToSnooze);
        if (!snoozeFromNow) { // if we are not snoozing from now on use time of the alarm supplied instead
            snoozeAlarm.setTimeHour(alarmToSnooze.getTimeHour());
            snoozeAlarm.setTimeMinute(alarmToSnooze.getTimeMinute());
        }

        // shift time
        if (snoozeAlarm.getTimeMinute() >= 60 - snoozeDuration_InMinutes) {
            snoozeAlarm.setTimeHour(snoozeAlarm.getTimeHour()+1);
            snoozeAlarm.setTimeMinute((snoozeAlarm.getTimeMinute()+ snoozeDuration_InMinutes)%60);
        } else {
            snoozeAlarm.setTimeHour(snoozeAlarm.getTimeHour());
            snoozeAlarm.setTimeMinute(snoozeAlarm.getTimeMinute()+ snoozeDuration_InMinutes);
        }

        return snoozeAlarm;
    }

    public AlarmDataModel() {
        repeatingDays = new boolean[7];
        Arrays.fill(repeatingDays, false);

        this.setName("Alarm");

        Calendar c = Calendar.getInstance();
        this.setTimeHour(c.get(Calendar.HOUR_OF_DAY));
        this.setTimeMinute(c.get(Calendar.MINUTE));

        this.setEnabled(false);
        this.setId(-1);
        this.setMessage("");
        this.setWeeklyRepeat(false);
        this.setSnoozeAlarm(-1);
    }

    public AlarmDataModel(String name, int timeHour, int timeMinute, Uri ringtone) {
        this();

        this.setName(name);
        this.setTimeHour(timeHour);
        this.setTimeMinute(timeMinute);
        this.setAlarmTone(ringtone);
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
        return parentAlarmID == -1 ? false : true;
    }

    public void setSnoozeAlarm(AlarmDataModel parentAlarm) {
        if (parentAlarm.isSnoozeAlarm())
            this.parentAlarmID = parentAlarm.getParentAlarmID();
        else
            this.parentAlarmID = parentAlarm.getId();
    }

    public void setSnoozeAlarm(long parentAlarmID) {
        this.parentAlarmID = parentAlarmID;
    }

    public long getParentAlarmID() {
        return parentAlarmID;
    }

    @Override
    public String toString() {
        return String.format("%02d:%02d", timeHour, timeMinute);
    }
}
