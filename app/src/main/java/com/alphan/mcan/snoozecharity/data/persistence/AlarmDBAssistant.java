package com.alphan.mcan.snoozecharity.data.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.alphan.mcan.snoozecharity.data.model.AlarmDataModel;
import com.alphan.mcan.snoozecharity.data.model.PaidDonationDataModel;
import com.alphan.mcan.snoozecharity.data.model.PendingDonationDataModel;

import java.util.ArrayList;
import java.util.List;

/**
 * DB access and stuff
 * Created by mcni on 11/20/14.
 */
public class AlarmDBAssistant extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "snoozeFTW.db";

    public static final String ALARM_TABLE_CHANGED = "snoozecharity.database.action.ALARM_TABLE_CHANGED";
    public static final String DONATION_TABLE_CHANGED = "snoozecharity.database.action.CHARITY_TABLE_CHANGED";

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
                    AlarmModelContract.Alarm.COLUMN_NAME_ALARM_SNOOZE_PARENT_ID + " INTEGER," +
                    AlarmModelContract.Alarm.COLUMN_NAME_ALARM_ENABLED + " BOOLEAN" + " )";

    private static final String SQL_CREATE_PENDING_DONATION_TABLE =
            "CREATE TABLE " + DonationModelContract.PendingDonation.TABLE_NAME + " (" +
                    DonationModelContract.PendingDonation._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DonationModelContract.PendingDonation.COLUMN_NAME_CHARITY_INDEX + " INTEGER," +
                    DonationModelContract.PendingDonation.COLUMN_NAME_DONATION_AMOUNT + " REAL" + " )";

    private static final String SQL_CREATE_PAID_DONATION_TABLE =
            "CREATE TABLE " + DonationModelContract.PaidDonation.TABLE_NAME + " (" +
                    DonationModelContract.PaidDonation._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DonationModelContract.PaidDonation.COLUMN_NAME_CHARITY_INDEX + " INTEGER," +
                    DonationModelContract.PaidDonation.COLUMN_NAME_DONATION_AMOUNT + " REAL," +
                    DonationModelContract.PaidDonation.COLUMN_NAME_DONATION_DATE + " TEXT" + " )";

    private static final String SQL_DELETE_ALARM_TABLE =
            "DROP TABLE IF EXISTS " + AlarmModelContract.Alarm.TABLE_NAME;

    private static final String SQL_DELETE_PENDING_DONATION_TABLE =
            "DROP TABLE IF EXISTS " + DonationModelContract.PendingDonation.TABLE_NAME;

    private static final String SQL_DELETE_PAID_DONATION_TABLE =
            "DROP TABLE IF EXISTS " + DonationModelContract.PaidDonation.TABLE_NAME;

    private Context context = null;

    public AlarmDBAssistant(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ALARM_TABLE);
        db.execSQL(SQL_CREATE_PENDING_DONATION_TABLE);
        db.execSQL(SQL_CREATE_PAID_DONATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ALARM_TABLE);
        db.execSQL(SQL_DELETE_PENDING_DONATION_TABLE);
        db.execSQL(SQL_DELETE_PAID_DONATION_TABLE);
        onCreate(db);
        //TODO: Not sure what the hell this one does, search a bit?
    }


    /**
     DB alarm access methods
     */
    public Boolean addAlarmToDB(AlarmDataModel model) {
        if (model.getId() != -1)
            return false; // only new alarms can be added

        //insert and update id
        ContentValues values = generateAlarmContentValues(model);
        SQLiteDatabase db = this.getWritableDatabase();
        model.setId(db.insert(AlarmModelContract.Alarm.TABLE_NAME, null, values));

        db.close();
        // check for success
        if (model.getId() == -1)
            return false;
        else {
            context.sendBroadcast(new Intent(ALARM_TABLE_CHANGED));
            return true;
        }
    }

    public boolean removeAlarmFromDB(AlarmDataModel model) {
        if (model.getId() == -1)
            return false;

        SQLiteDatabase db = this.getWritableDatabase();
        int effectedRows = db.delete(AlarmModelContract.Alarm.TABLE_NAME,
                AlarmModelContract.Alarm._ID + " = ?", new String[]{String.valueOf(model.getId())});

        model.setId(-1);
        db.close();
        // check success
        if (effectedRows != 0) {
            context.sendBroadcast(new Intent(ALARM_TABLE_CHANGED));
            return true;
        }
        else
            return false;
    }

    public boolean updateAlarmInDB(AlarmDataModel model) {
        if (model.getId() == -1) // use add in this case
            return false;

        // update rows of DB
        ContentValues values = generateAlarmContentValues(model);
        SQLiteDatabase db = this.getWritableDatabase();
        int changedRows =  db.update(AlarmModelContract.Alarm.TABLE_NAME, values,
                AlarmModelContract.Alarm._ID + " = ?", new String[]{String.valueOf(model.getId())});

        db.close();
        // check for success
        if (changedRows != 0) {
            context.sendBroadcast(new Intent(ALARM_TABLE_CHANGED));
            return true;
        }
        else
            return false;
    }

    public AlarmDataModel getAlarmFromDB(long id) {
        if (id == -1)
            return null;

        AlarmDataModel alarm = null;
        SQLiteDatabase db = this.getReadableDatabase();
        String select = "SELECT * FROM " + AlarmModelContract.Alarm.TABLE_NAME + " WHERE " + AlarmModelContract.Alarm._ID + " = " + id;
        Cursor c = db.rawQuery(select, null);
        if (c.moveToNext())
            alarm = generateAlarmModel(c);

        c.close();
        db.close();
        return alarm;
    }

    public List<AlarmDataModel> getAlarmsInDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        String select = "SELECT * FROM " + AlarmModelContract.Alarm.TABLE_NAME;
        Cursor c = db.rawQuery(select, null);

        List<AlarmDataModel> alarmList = new ArrayList<AlarmDataModel>();
        while (c.moveToNext())
            alarmList.add(generateAlarmModel(c));

        c.close();
        db.close();
        return alarmList;
    }


    /**
     DB donation access methods
     */
    public Boolean addPendingDonation(PendingDonationDataModel donation) {
        if (donation.getId() != -1)
            return false; // only new donations can be added

        //insert and update id
        ContentValues values = generatePendingDonationContentValues(donation);
        SQLiteDatabase db = this.getWritableDatabase();
        donation.setId(db.insert(DonationModelContract.PendingDonation.TABLE_NAME, null, values));

        db.close();
        // check for success
        if (donation.getId() == -1)
            return false;
        else {
            context.sendBroadcast(new Intent(DONATION_TABLE_CHANGED));
            return true;
        }
    }

    public PendingDonationDataModel getPendingDonationByCharityIndex(int id) {
        if (id == -1)
            return null;

        PendingDonationDataModel donation = null;
        SQLiteDatabase db = this.getReadableDatabase();
        String select = "SELECT * FROM " + DonationModelContract.PendingDonation.TABLE_NAME + " WHERE "
                                         + DonationModelContract.PendingDonation.COLUMN_NAME_CHARITY_INDEX+ " = " + id;
        Cursor c = db.rawQuery(select, null);
        if (c.moveToNext())
            donation = generatePendingDonationModel(c);

        c.close();
        db.close();
        return donation;
    }

    public boolean updatePendingDonation(PendingDonationDataModel model) {
        if (model.getId() == -1) // use add in this case
            return false;

        SQLiteDatabase db = this.getWritableDatabase();
        // update rows of DB
        ContentValues values = generatePendingDonationContentValues(model);
        int changedRows =  db.update(DonationModelContract.PendingDonation.TABLE_NAME, values,
                DonationModelContract.PendingDonation._ID + " = ?", new String[]{String.valueOf(model.getId())});

        db.close();
        // check for success
        if (changedRows != 0) {
            context.sendBroadcast(new Intent(DONATION_TABLE_CHANGED));
            return true;
        }
        else
            return false;
    }

    public boolean deletePendingDonation(PendingDonationDataModel model) {
        if (model.getId() == -1)
            return false;
        SQLiteDatabase db = this.getWritableDatabase();
        int effectedRows = db.delete(DonationModelContract.PendingDonation.TABLE_NAME,
                DonationModelContract.PendingDonation._ID + " = ?", new String[]{String.valueOf(model.getId())});

        model.setId(-1);
        db.close();
        // check success
        if (effectedRows != 0) {
            context.sendBroadcast(new Intent(DONATION_TABLE_CHANGED));
            return true;
        }
        else
            return false;
    }

    public List<PendingDonationDataModel> getPendingDonations() {
        SQLiteDatabase db = this.getReadableDatabase();
        String select = "SELECT * FROM " + DonationModelContract.PendingDonation.TABLE_NAME;
        Cursor c = db.rawQuery(select, null);

        List<PendingDonationDataModel> pendingDonationList = new ArrayList<PendingDonationDataModel>();
        while (c.moveToNext())
            pendingDonationList.add(generatePendingDonationModel(c));

        c.close();
        db.close();
        return pendingDonationList;
    }

    /**
     DB donation access methods
     */
    public Boolean addPaidDonation(PaidDonationDataModel donation) {
        if (donation.getId() != -1)
            return false; // only new donations can be added

        //insert and update id
        ContentValues values = generatePaidDonationContentValues(donation);
        SQLiteDatabase db = this.getWritableDatabase();
        donation.setId(db.insert(DonationModelContract.PaidDonation.TABLE_NAME, null, values));

        db.close();
        // check for success
        if (donation.getId() == -1)
            return false;
        else {
            context.sendBroadcast(new Intent(DONATION_TABLE_CHANGED));
            return true;
        }
    }

    public PaidDonationDataModel getPaidDonationByCharityIndex(int id) {
        if (id == -1)
            return null;

        PaidDonationDataModel donation = null;
        SQLiteDatabase db = this.getReadableDatabase();
        String select = "SELECT * FROM " + DonationModelContract.PaidDonation.TABLE_NAME + " WHERE "
                                         + DonationModelContract.PaidDonation.COLUMN_NAME_CHARITY_INDEX+ " = " + id;
        Cursor c = db.rawQuery(select, null);
        if (c.moveToNext())
            donation = generatePaidDonationModel(c);

        c.close();
        db.close();
        return donation;
    }

    public boolean updatePaidDonation(PaidDonationDataModel model) {
        if (model.getId() == -1) // use add in this case
            return false;

        SQLiteDatabase db = this.getWritableDatabase();
        // update rows of DB
        ContentValues values = generatePaidDonationContentValues(model);
        int changedRows =  db.update(DonationModelContract.PaidDonation.TABLE_NAME, values,
                DonationModelContract.PaidDonation._ID + " = ?", new String[]{String.valueOf(model.getId())});

        db.close();
        // check for success
        if (changedRows != 0) {
            context.sendBroadcast(new Intent(DONATION_TABLE_CHANGED));
            return true;
        }
        else
            return false;
    }

    public boolean deletePaidDonation(PaidDonationDataModel model) {
        if (model.getId() == -1)
            return false;
        SQLiteDatabase db = this.getWritableDatabase();
        int effectedRows = db.delete(DonationModelContract.PaidDonation.TABLE_NAME,
                DonationModelContract.PaidDonation._ID + " = ?", new String[]{String.valueOf(model.getId())});

        model.setId(-1);
        db.close();
        // check success
        if (effectedRows != 0) {
            context.sendBroadcast(new Intent(DONATION_TABLE_CHANGED));
            return true;
        }
        else
            return false;
    }

    public List<PaidDonationDataModel> getPaidDonations() {
        SQLiteDatabase db = this.getReadableDatabase();
        String select = "SELECT * FROM " + DonationModelContract.PaidDonation.TABLE_NAME;
        Cursor c = db.rawQuery(select, null);

        List<PaidDonationDataModel> paidDonationList = new ArrayList<PaidDonationDataModel>();
        while (c.moveToNext())
            paidDonationList.add(generatePaidDonationModel(c));

        c.close();
        db.close();
        return paidDonationList;
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
        model.setAlarmTone(c.getString(c.getColumnIndex(AlarmModelContract.Alarm.COLUMN_NAME_ALARM_TONE)) != ""
                ? Uri.parse(c.getString(c.getColumnIndex(AlarmModelContract.Alarm.COLUMN_NAME_ALARM_TONE)))
                : null);
        model.setEnabled(c.getInt(c.getColumnIndex(AlarmModelContract.Alarm.COLUMN_NAME_ALARM_ENABLED)) == 0 ? false : true);
        model.setSnoozeAlarm(c.getLong(c.getColumnIndex(AlarmModelContract.Alarm.COLUMN_NAME_ALARM_SNOOZE_PARENT_ID)));
        model.setWeeklyRepeat(c.getInt(c.getColumnIndex(AlarmModelContract.Alarm.COLUMN_NAME_ALARM_REPEAT_WEEKLY)) == 0 ? false : true);
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
        values.put(AlarmModelContract.Alarm.COLUMN_NAME_ALARM_SNOOZE_PARENT_ID, model.getParentAlarmID());
        values.put(AlarmModelContract.Alarm.COLUMN_NAME_ALARM_REPEAT_WEEKLY, model.isWeekly());
        String repeatingDays = "";
        for (int i = 0; i < 7; ++i) {
            repeatingDays += model.getRepeatingDay(i) + ",";
        }
        values.put(AlarmModelContract.Alarm.COLUMN_NAME_ALARM_REPEAT_DAYS, repeatingDays);

        return values;
    }


    private PendingDonationDataModel generatePendingDonationModel(Cursor c) {

        long id = c.getLong(c.getColumnIndex(DonationModelContract.PendingDonation._ID));
        int charityIndex = c.getInt(c.getColumnIndex(DonationModelContract.PendingDonation.COLUMN_NAME_CHARITY_INDEX));
        double amount = c.getDouble(c.getColumnIndex(DonationModelContract.PendingDonation.COLUMN_NAME_DONATION_AMOUNT));

        PendingDonationDataModel model = new PendingDonationDataModel(charityIndex, amount);
        model.setId(id);

        return model;
    }

    private ContentValues generatePendingDonationContentValues(PendingDonationDataModel donation) {
        ContentValues values = new ContentValues();

        values.put(DonationModelContract.PendingDonation.COLUMN_NAME_CHARITY_INDEX, donation.getCharityIndex());
        values.put(DonationModelContract.PendingDonation.COLUMN_NAME_DONATION_AMOUNT, donation.getPendingAmount());

        return values;
    }

    private PaidDonationDataModel generatePaidDonationModel(Cursor c) {

        long id = c.getLong(c.getColumnIndex(DonationModelContract.PaidDonation._ID));
        int charityIndex = c.getInt(c.getColumnIndex(DonationModelContract.PaidDonation.COLUMN_NAME_CHARITY_INDEX));
        double amount = c.getDouble(c.getColumnIndex(DonationModelContract.PaidDonation.COLUMN_NAME_DONATION_AMOUNT));
        String date = c.getString(c.getColumnIndex(DonationModelContract.PaidDonation.COLUMN_NAME_DONATION_DATE));

        PaidDonationDataModel model = new PaidDonationDataModel(charityIndex, amount, date);
        model.setId(id);

        return model;
    }

    private ContentValues generatePaidDonationContentValues(PaidDonationDataModel donation) {
        ContentValues values = new ContentValues();

        values.put(DonationModelContract.PaidDonation.COLUMN_NAME_CHARITY_INDEX, donation.getCharityIndex());
        values.put(DonationModelContract.PaidDonation.COLUMN_NAME_DONATION_AMOUNT, donation.getPaidAmount());
        values.put(DonationModelContract.PaidDonation.COLUMN_NAME_DONATION_DATE, donation.getPaymentDate());

        return values;
    }
}
