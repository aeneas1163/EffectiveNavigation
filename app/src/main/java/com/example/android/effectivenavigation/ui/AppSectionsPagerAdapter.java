package com.example.android.effectivenavigation.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 *
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
 * sections of the app.
 * Created by Alphan on 16-Nov-14.
 */
public class AppSectionsPagerAdapter extends FragmentPagerAdapter {

    public AppSectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        Bundle args;
        switch (i) {
            case 0:
                Fragment alarmFragment = new AlarmFragment();
                args = new Bundle();
                args.putInt(AlarmFragment.ARG_SECTION_NUMBER, i + 1);
                alarmFragment.setArguments(args);
                return alarmFragment;

            case 1:

                Fragment clockFragment = new ClockFragment();
                args = new Bundle();
                args.putInt(AlarmFragment.ARG_SECTION_NUMBER, i + 1);
                clockFragment.setArguments(args);
                return clockFragment;

            case 2:
                // The first section of the app is the most interesting -- it offers
                // a launchpad into the other demonstrations in this example application.
                return new SettingsFragment();

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "Section " + (position + 1);
    }
}
