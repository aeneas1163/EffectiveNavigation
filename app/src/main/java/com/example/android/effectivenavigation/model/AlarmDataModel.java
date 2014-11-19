package com.example.android.effectivenavigation.model;

import android.net.Uri;
import java.util.Calendar;

/**
 * Created by mcni on 11/19/14.
 * representation of an alarm, maybe switch getters and setters for db access?
 */
public class AlarmDataModel {

    // week day definitions for repeat setup
    public static final int MON = 0;
    public static final int TUE = 1;
    public static final int WED = 2;
    public static final int THU = 3;
    public static final int FRI = 4;
    public static final int SAT = 5;
    public static final int SUN = 6;
    private boolean repeatingDays[]; //bool array for selecting repeating days (repeatingDays[Mon] = true..)

    private long id; //db id of the alarm
    private boolean isEnabled;
    private String name;
    private String message;

    private int timeHour;
    private int timeMinute;

    private Uri alarmTone;   // acquire using ring tone selector and handle its return in UI
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
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        // by default set an alarm for now
    }

    public AlarmDataModel(String name, int timeHour, int timeMinute) {
        repeatingDays = new boolean[7];
        this.name = name;
        this.setTimeHour(timeHour);
        this.setTimeMinute(timeMinute);
        this.isEnabled = false;
        this.id = -1; //TODO decide on how to get this fella
        this.message = null; //TODO get a default message from settings?
        this.alarmTone = null; //TODO get default alarmTone uri from somewhere? Settings maybe?
    }

    // getters and setters
    public void setRepeatingDay(int dayOfWeek, boolean value) {
        repeatingDays[dayOfWeek] = value;
    }

    public boolean getRepeatingDay(int dayOfWeek) {
        return repeatingDays[dayOfWeek];
    }

    public long getId() {
        //TODO: db look up
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void enableAlarm() {
        this.isEnabled = true;
    }

    public void disableAlarm() {
        this.isEnabled = false;
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
}
