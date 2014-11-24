package com.alphan.mcan.snoozecharity.data.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.alphan.mcan.snoozecharity.data.model.AlarmDataModel;
import com.alphan.mcan.snoozecharity.data.model.CharityDataModel;
import com.alphan.mcan.snoozecharity.data.model.DonationDataModel;

import java.util.ArrayList;
import java.util.List;

/**
 * DB access and stuff
 * Created by mcni on 11/20/14.
 */
public class AlarmDBAssistant extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "snoozeFTW.db";

    //TODO: modify for charity and donation
    private static final String SQL_CREATE_ALARM_TABLE =
            "CREATE TABLE " + AlarmModelContract.Alarm.TABLE_NAME + " (" +
                    AlarmModelContract.Alarm._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    AlarmModelContract.Alarm.COLUMN_NAME_ALARM_NAME + " TEXT," +
                    AlarmModelContract.Alarm.COLUMN_NAME_ALARM_MESSAGE + " TEXT," +
                    AlarmModelContract.Alarm.COLUMN_NAME_ALARM_TIME_HOUR + " INTEGER," +
                    AlarmModelContract.Alarm.COLUMN_NAME_ALARM_TIME_MINUTE + " INTEGER," +
                    AlarmModelContract.Alarm.COLUMN_NAME_ALARM_REPEAT_DAYS + " TEXT," +
                    AlarmModelContract.Alarm.COLUMN_NAME_ALARM_REPEAT_WEEKLY + " BOOLEAN," +
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


    /**
     DB alarm access methods
     */
    public long addAlarm(AlarmDataModel model) {
        ContentValues values = generateAlarmContentValues(model);
        return getWritableDatabase().insert(AlarmModelContract.Alarm.TABLE_NAME, null, values);
    }

    public AlarmDataModel getAlarm(long id) {
        if (id == -1)
            return null;

        SQLiteDatabase db = this.getReadableDatabase();
        String select = "SELECT * FROM " + AlarmModelContract.Alarm.TABLE_NAME + " WHERE " + AlarmModelContract.Alarm._ID + " = " + id;
        Cursor c = db.rawQuery(select, null);
        if (c.moveToNext())
            return generateAlarmModel(c);
        else
            return null;

    }

    public long updateAlarm(AlarmDataModel model) {
        ContentValues values = generateAlarmContentValues(model);
        return getWritableDatabase().update(AlarmModelContract.Alarm.TABLE_NAME,
                values, AlarmModelContract.Alarm._ID + " = ?", new String[] { String.valueOf(model.getId()) });
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
            alarmList.add(generateAlarmModel(c));
        if (!alarmList.isEmpty()) {
            return alarmList;
        }

        return null;
    }


    /**
     DB donation access methods
     */
    public long addDonation(DonationDataModel donation) {
        //TODO
        throw new UnsupportedOperationException("Not Implemented!");
    }

    public DonationDataModel getDonation(long id) {
        //TODO
        throw new UnsupportedOperationException("Not Implemented!");
    }

    public long updateDonation(DonationDataModel model) {
        //TODO
        throw new UnsupportedOperationException("Not Implemented!");
    }

    public int deleteDonation(long id) {
        //TODO
        throw new UnsupportedOperationException("Not Implemented!");
    }

    public List<DonationDataModel> getDonations() {
        List<DonationDataModel> pendingDonations = getPendingDonations();
        pendingDonations.addAll(getSubmittedDonations());
        return pendingDonations;
    }

    public List<DonationDataModel> getSubmittedDonations() {
        //TODO
        throw new UnsupportedOperationException("Not Implemented!");
    }
    public List<DonationDataModel> getPendingDonations() {
        //TODO
        throw new UnsupportedOperationException("Not Implemented!");
    }


    /**
     DB charity access methods
     */
    public long addCharity(CharityDataModel charity) {
        //TODO
        throw new UnsupportedOperationException("Not Implemented!");
    }

    public CharityDataModel getCharity(long id) {
        //TODO
        throw new UnsupportedOperationException("Not Implemented!");
    }

    public int deleteCharity(long id) {
        //TODO
        throw new UnsupportedOperationException("Not Implemented!");
    }

    public List<CharityDataModel> getCharities() {
        throw new UnsupportedOperationException("Not Implemented!");
    }


    /**
     *
     * Helper DB generation methods
     */
    private AlarmDataModel generateAlarmModel(Cursor c) {
        AlarmDataModel model = new AlarmDataModel();

        model.setId(c.getLong(c.getColumnIndex(AlarmModelContract.Alarm._ID)));
        model.setName(c.getString(c.getColumnIndex(AlarmModelContract.Alarm.COLUMN_NAME_ALARM_NAME)));
        model.setMessage(c.getString(c.getColumnIndex(AlarmModelContract.Alarm.COLUMN_NAME_ALARM_MESSAGE)));
        model.setTimeHour(c.getInt(c.getColumnIndex(AlarmModelContract.Alarm.COLUMN_NAME_ALARM_TIME_HOUR)));
        model.setTimeMinute(c.getInt(c.getColumnIndex(AlarmModelContract.Alarm.COLUMN_NAME_ALARM_TIME_MINUTE)));
        model.setWeeklyRepeat(c.getInt(c.getColumnIndex(AlarmModelContract.Alarm.COLUMN_NAME_ALARM_REPEAT_WEEKLY)) == 0 ? false : true);

        model.setAlarmTone(c.getString(c.getColumnIndex(AlarmModelContract.Alarm.COLUMN_NAME_ALARM_TONE)) != ""
                ? Uri.parse(c.getString(c.getColumnIndex(AlarmModelContract.Alarm.COLUMN_NAME_ALARM_TONE)))
                : null);
        model.setEnabled(c.getInt(c.getColumnIndex(AlarmModelContract.Alarm.COLUMN_NAME_ALARM_ENABLED)) == 0 ? false : true);

        String[] repeatingDays = c.getString(c.getColumnIndex((AlarmModelContract.Alarm.COLUMN_NAME_ALARM_REPEAT_DAYS))).split(",");
        for (int i = 0; i < repeatingDays.length; ++i) {
            model.setRepeatingDay(i, repeatingDays[i].equals("false") ? false : true);
        }

        return model;
    }

    private ContentValues generateAlarmContentValues(AlarmDataModel model) {
        ContentValues values = new ContentValues();

        values.put(AlarmModelContract.Alarm.COLUMN_NAME_ALARM_NAME, model.getName());
        values.put(AlarmModelContract.Alarm.COLUMN_NAME_ALARM_MESSAGE, model.getMessage());
        values.put(AlarmModelContract.Alarm.COLUMN_NAME_ALARM_TIME_HOUR, model.getTimeHour());
        values.put(AlarmModelContract.Alarm.COLUMN_NAME_ALARM_TIME_MINUTE, model.getTimeMinute());
        values.put(AlarmModelContract.Alarm.COLUMN_NAME_ALARM_TONE, model.getAlarmTone().toString());
        values.put(AlarmModelContract.Alarm.COLUMN_NAME_ALARM_ENABLED, model.isEnabled() ? 1 : 0 );
        values.put(AlarmModelContract.Alarm.COLUMN_NAME_ALARM_REPEAT_WEEKLY, model.isWeekly());

        String repeatingDays = "";
        for (int i = 0; i < 7; ++i) {
            repeatingDays += model.getRepeatingDay(i) + ",";
        }
        values.put(AlarmModelContract.Alarm.COLUMN_NAME_ALARM_REPEAT_DAYS, repeatingDays);

        return values;
    }


    private DonationDataModel generateDonationModel(Cursor c) {
        //TODO
        throw new UnsupportedOperationException("Not Implemented!");
    }

    private ContentValues generateDonationContentValues(DonationDataModel model) {
        //TODO
        throw new UnsupportedOperationException("Not Implemented!");
    }


    private CharityDataModel generateCharityModel(Cursor c) {
        //TODO
        throw new UnsupportedOperationException("Not Implemented!");
    }

    private ContentValues generateCharityContentValues(CharityDataModel model) {
        //TODO
        throw new UnsupportedOperationException("Not Implemented!");
    }


}
