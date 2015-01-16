package com.alphan.mcan.snoozecharity;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import java.util.Locale;

/**
 * Main Application that is used for forground check
 * Created by mcni on 11/18/14.
 */
public class SnoozeApplication extends Application {

    private String lang;

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        if (!lang.isEmpty())
        {
            Locale locale = new Locale(lang);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        }
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        Configuration config = getBaseContext().getResources().getConfiguration();

        TelephonyManager tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String networkOperator = tel.getNetworkOperator();

        int mcc = 0;

        if (!networkOperator.isEmpty()) {
            mcc = Integer.parseInt(networkOperator.substring(0, 3));
        }
        
        switch (mcc) {
            case 286:
                lang = "tr";
                break;
            case 262:
                lang = "de";
                break;
            default:
                lang = "en";
                break;
        }

        if (! "".equals(lang) && ! config.locale.getLanguage().equals(lang))
        {
            Locale locale = new Locale(lang);
            Locale.setDefault(locale);
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
    }

}
