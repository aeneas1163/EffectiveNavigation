package com.alphan.mcan.snoozecharity.viewModels.mainActiviy;

import android.app.AlertDialog;
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
import android.widget.ListView;

import com.alphan.mcan.snoozecharity.R;
import com.alphan.mcan.snoozecharity.data.model.AlarmDataModel;
import com.alphan.mcan.snoozecharity.data.model.PaidDonationDataModel;
import com.alphan.mcan.snoozecharity.services.AlarmManagerHelper;
import com.alphan.mcan.snoozecharity.viewModels.AppPreferencesActivity;
import com.alphan.mcan.snoozecharity.viewModels.CharityCollectionActivity;

import java.util.Calendar;
import java.util.List;

/**
 * A fragment that launches other parts of the demo application.
 *
 * Created by Alphan on 16-Nov-14.
 */
public class SettingsFragment extends Fragment{

    private PaidDonationListAdapter paidDonationListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_section_settings, container, false);

        List<PaidDonationDataModel> donations =  AlarmManagerHelper.getPaidDonations(getActivity());

        if (donations.isEmpty())
            rootView.findViewById(R.id.paid_donation_textview).setVisibility(View.GONE);

        paidDonationListAdapter = new PaidDonationListAdapter(getActivity(), donations);

        final ListView donationListView = (ListView) rootView.findViewById(R.id.paid_donation_list);

        donationListView.setAdapter(paidDonationListAdapter);

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

        rootView.findViewById(R.id.about_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder adb = new AlertDialog.Builder(view.getContext());
                adb.setTitle("MCan?");
                adb.setMessage("Asagidakilerden hangisi MCan'in herhangi bir t aninda saglik durumudur?");
                adb.setNegativeButton("Hasta", null);
                adb.setPositiveButton("Cok Hasta", null);
                adb.show();
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (paidDonationListAdapter != null)
        {
            paidDonationListAdapter.clear();
            List<PaidDonationDataModel> donations =  AlarmManagerHelper.getPaidDonations(getActivity());
            paidDonationListAdapter.addAll(donations);
        }
    }
}


