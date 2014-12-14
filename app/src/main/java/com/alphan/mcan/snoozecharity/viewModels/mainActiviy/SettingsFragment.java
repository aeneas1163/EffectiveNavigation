package com.alphan.mcan.snoozecharity.viewModels.mainActiviy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alphan.mcan.snoozecharity.R;
import com.alphan.mcan.snoozecharity.viewModels.CharityCollectionActivity;
import com.alphan.mcan.snoozecharity.data.model.AlarmDataModel;
import com.alphan.mcan.snoozecharity.services.AlarmManagerHelper;
import com.alphan.mcan.snoozecharity.viewModels.AppPreferencesActivity;

import java.util.Calendar;

/**
 * A fragment that launches other parts of the demo application.
 *
 * Created by Alphan on 16-Nov-14.
 */
public class SettingsFragment extends Fragment{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_section_settings, container, false);

        // Demonstration of a collection-browsing activity.
        rootView.findViewById(R.id.demo_collection_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), CharityCollectionActivity.class);
                        startActivity(intent);
                    }
                });

        // Demonstration of navigating to external activities.
        rootView.findViewById(R.id.demo_external_activity)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), AppPreferencesActivity.class);
                        startActivity(intent);
                    }
                });

        rootView.findViewById(R.id.demo_alarm)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final Calendar c = Calendar.getInstance();
                        int hour = c.get(Calendar.HOUR_OF_DAY);
                        int minute = c.get(Calendar.MINUTE);
                        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        String strRingtonePreference = preference.getString("ringtone_pref", "DEFAULT_SOUND");
                        Uri ringtoneUri = Settings.System.DEFAULT_ALARM_ALERT_URI;
                        if (!strRingtonePreference.equalsIgnoreCase("DEFAULT_SOUND"))
                        {
                            ringtoneUri = Uri.parse( strRingtonePreference);
                        }
                        AlarmDataModel alarmData = new AlarmDataModel("WAAAKE UP!!", hour, minute + 1, ringtoneUri);
                        alarmData.setEnabled(true);
                        AlarmManagerHelper.createNewAlarm(getActivity(), alarmData);
                    }
                });

        return rootView;
    }
}


