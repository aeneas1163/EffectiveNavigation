package com.alphan.mcan.snoozecharity.viewModels.mainActiviy;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alphan.mcan.snoozecharity.R;
import com.alphan.mcan.snoozecharity.viewModels.MainActivity;

/**
 * Preferences fragment that uses PreferenceScreen.xml in res/values in order to allow user to edit
 * preferences
 *
 * Created by Alphan on 18-Nov-14.
 */
public class PrefsFragment extends PreferenceFragment {

    private int color;
    private boolean clockType;
    private boolean hour24type;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.setBackgroundColor(getResources().getColor(android.R.color.black));

        // get default that should trigger activity recreation
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getActivity());
        color = preference.getInt("dash_colorkey", getActivity().getResources().getColor(R.color.default_color));
        clockType = preference.getBoolean("clock_type",  true);
        hour24type = preference.getBoolean("24hour_option", android.text.format.DateFormat.is24HourFormat(getActivity()));

        final ActionBar  actionBar = getActivity().getActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        final ActionBar  actionBar = getActivity().getActionBar();
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        Intent myIntent = new Intent(getActivity(), MainActivity.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // check if there is need to refresh views
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int new_color = preference.getInt("dash_colorkey", getActivity().getResources().getColor(R.color.default_color));
        boolean new_clockType = preference.getBoolean("clock_type",  true);
        boolean new_hour24type = preference.getBoolean("24hour_option", android.text.format.DateFormat.is24HourFormat(getActivity()));

        // clock type of color theme changed:
        if (color != new_color || clockType != new_clockType)
        {
            myIntent.putExtra(MainActivity.WHICH_PAGE_INT, 2); //Optional parameters
            getActivity().startActivity(myIntent);
        }

        // for digital clock 12h/24h setting changes
        if(clockType == false  && hour24type != new_hour24type)
        {
            myIntent.putExtra(MainActivity.WHICH_PAGE_INT, 2); //Optional parameters
            getActivity().startActivity(myIntent);
        }
    }


}
