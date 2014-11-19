package com.example.android.effectivenavigation;

import android.app.Application;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;

/**
 * Main Application that contains activities and holds database access if needed
 * Created by mcni on 11/18/14.
 */
public class SnoozeFTW extends Application {

    public static SQLiteDatabase db;
    public static SharedPreferences sp;

    @Override
    public void onCreate() {
        super.onCreate();

        //TODO: acquire default values after settings sml is properly generated
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sp = PreferenceManager.getDefaultSharedPreferences(this);

        //TODO: initializa db acces here some how
        // db = ...
    }

    public static String getRingtone() {
        //TODO: return default ringtone from settints after it is defined
        //return sp.getString(SnozeFTW.RINGTONE_PREF, DEFAULT_NOTIFICATION_URI.toString());
        return "";
    }

}
