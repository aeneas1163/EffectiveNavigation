package com.alphan.mcan.snoozecharity.data.persistence;

import android.provider.BaseColumns;

/**
 * Alarm colum definitions to be used in sql db
 * Created by mcni on 11/20/14.
 */
public final class AlarmModelContract {


    public static abstract class Alarm implements BaseColumns { //this adds the ID column
        public static final String TABLE_NAME = "alarm";
        public static final String COLUMN_NAME_ALARM_NAME = "name";
        public static final String COLUMN_NAME_ALARM_MESSAGE = "message";
        public static final String COLUMN_NAME_ALARM_TIME_HOUR = "hour";
        public static final String COLUMN_NAME_ALARM_TIME_MINUTE = "minute";
        public static final String COLUMN_NAME_ALARM_REPEAT_DAYS = "days";
        public static final String COLUMN_NAME_ALARM_REPEAT_WEEKLY = "weekly";
        public static final String COLUMN_NAME_ALARM_TONE = "tone";
        public static final String COLUMN_NAME_ALARM_ENABLED = "isEnabled";
        public static final String COLUMN_NAME_ALARM_SNOOZE_ALARM = "snoozeAlarm";
    }

}
