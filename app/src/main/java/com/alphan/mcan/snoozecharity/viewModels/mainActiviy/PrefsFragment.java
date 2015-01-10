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

        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getActivity());
        color = preference.getInt("dash_colorkey", getActivity().getResources().getColor(R.color.turquiose));

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
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int new_color = preference.getInt("dash_colorkey", getActivity().getResources().getColor(R.color.turquiose));
        if (color != new_color)
        {
            myIntent.putExtra(MainActivity.WHICH_PAGE_INT, 2); //Optional parameters
            getActivity().startActivity(myIntent);
        }
    }


}
