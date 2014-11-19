package com.example.android.effectivenavigation.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.example.android.effectivenavigation.model.AlarmDataModel;

import java.util.ArrayList;
import java.util.List;

/**
 * DB access and stuff
 * Created by mcni on 11/20/14.
 */
public class AlarmDBAssistant extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "snoozeFTW.db";

    private static final String SQL_CREATE_ALARM_TABLE =
            "CREATE TABLE " + AlarmModelContract.Alarm.TABLE_NAME + " (" +
                    AlarmModelContract.Alarm._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    AlarmModelContract.Alarm.COLUMN_NAME_ALARM_NAME + " TEXT," +
                    AlarmModelContract.Alarm.COLUMN_NAME_ALARM_MESSAGE + " TEXT," +
                    AlarmModelContract.Alarm.COLUMN_NAME_ALARM_TIME_HOUR + " INTEGER," +
                    AlarmModelContract.Alarm.COLUMN_NAME_ALARM_TIME_MINUTE + " INTEGER," +
                    AlarmModelContract.Alarm.COLUMN_NAME_ALARM_REPEAT_DAYS + " TEXT," +
                    AlarmModelContract.Alarm.COLUMN_NAME_ALARM_TONE + " TEXT," +
                    AlarmModelContract.Alarm.COLUMN_NAME_ALARM_ENABLED + " BOOLEAN" + " )";

    private static final String SQL_DELETE_ALARM_TABLE =
            "DROP TABLE IF EXISTS " + AlarmModelContract.Alarm.TABLE_NAME;

    public AlarmDBAssistant(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ALARM_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ALARM_TABLE);
        onCreate(db);
        //TODO: Not sure what the hell this one does, search a bit?
    }

    //DB access methods
    public long addAlarm(AlarmDataModel model) {
        ContentValues values = generateContentValues(model);
        return getWritableDatabase().insert(AlarmModelContract.Alarm.TABLE_NAME, null, values);
    }

    public AlarmDataModel getAlarm(long id) {
        if (id == -1)
            return null;

        SQLiteDatabase db = this.getReadableDatabase();
        String select = "SELECT * FROM " + AlarmModelContract.Alarm.TABLE_NAME + " WHERE " + AlarmModelContract.Alarm._ID + " = " + id;
        Cursor c = db.rawQuery(select, null);
        if (c.moveToNext())
            return generateDataModel(c);
        else
            return null;

    }

    public int deleteAlarm(long id) {
        return getWritableDatabase().delete(AlarmModelContract.Alarm.TABLE_NAME, AlarmModelContract.Alarm._ID + " = ?", new String[] { String.valueOf(id) });
    }

    public List<AlarmDataModel> getAlarms() {
        SQLiteDatabase db = this.getReadableDatabase();
        String select = "SELECT * FROM " + AlarmModelContract.Alarm.TABLE_NAME;
        Cursor c = db.rawQuery(select, null);

        List<AlarmDataModel> alarmList = new ArrayList<AlarmDataModel>();
        while (c.moveToNext())
            alarmList.add(generateDataModel(c));
        if (!alarmList.isEmpty())
            return alarmList;

        return null;
    }


    //helper method to generate model out of DB cursor:
    private AlarmDataModel generateDataModel(Cursor c) {
        AlarmDataModel model = new AlarmDataModel();

        model.setId(c.getLong(c.getColumnIndex(AlarmModelContract.Alarm._ID)));
        model.setName(c.getString(c.getColumnIndex(AlarmModelContract.Alarm.COLUMN_NAME_ALARM_NAME)));
        model.setMessage(c.getString(c.getColumnIndex(AlarmModelContract.Alarm.COLUMN_NAME_ALARM_MESSAGE)));
        model.setTimeHour(c.getInt(c.getColumnIndex(AlarmModelContract.Alarm.COLUMN_NAME_ALARM_TIME_HOUR)));
        model.setTimeMinute(c.getInt(c.getColumnIndex(AlarmModelContract.Alarm.COLUMN_NAME_ALARM_TIME_MINUTE)));
        model.setAlarmTone(Uri.parse(c.getString(c.getColumnIndex(AlarmModelContract.Alarm.COLUMN_NAME_ALARM_TONE))));

        if (c.getInt(c.getColumnIndex(AlarmModelContract.Alarm.COLUMN_NAME_ALARM_ENABLED)) != 0)
            model.enableAlarm();
        else
            model.disableAlarm();

        //TODO:  do the parsing for COLUMN_NAME_ALARM_REPEAT_DAYS to get repeart days


        return model;
    }

    //helper method to push model to DB:
    private ContentValues generateContentValues(AlarmDataModel model) {
        ContentValues values = new ContentValues();

        values.put(AlarmModelContract.Alarm.COLUMN_NAME_ALARM_NAME, model.getName());
        values.put(AlarmModelContract.Alarm.COLUMN_NAME_ALARM_MESSAGE, model.getMessage());
        values.put(AlarmModelContract.Alarm.COLUMN_NAME_ALARM_TIME_HOUR, model.getTimeHour());
        values.put(AlarmModelContract.Alarm.COLUMN_NAME_ALARM_TIME_MINUTE, model.getTimeMinute());
        values.put(AlarmModelContract.Alarm.COLUMN_NAME_ALARM_TONE, model.getAlarmTone().toString());
        values.put(AlarmModelContract.Alarm.COLUMN_NAME_ALARM_ENABLED, model.isEnabled() ? 1 : 0 );

        String repeatingDays = "";
        //TODO: do parsing code to create an array of repeat days "MON, TUE, FRI" ..
        values.put(AlarmModelContract.Alarm.COLUMN_NAME_ALARM_REPEAT_DAYS, repeatingDays);

        return values;
    }
}
