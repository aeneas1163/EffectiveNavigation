/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alphan.mcan.snoozecharity.viewModels;

import android.app.ActionBar;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import com.alphan.mcan.snoozecharity.R;
import com.alphan.mcan.snoozecharity.services.AlarmManagerHelper;
import com.alphan.mcan.snoozecharity.viewModels.mainActiviy.AppSectionsPagerAdapter;

public class MainActivity extends FragmentActivity{

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
     * three primary sections of the app. We use a {@link android.support.v4.app.FragmentPagerAdapter}
     * derivative, which will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    AppSectionsPagerAdapter mAppSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will display the three primary sections of the app, one at a
     * time.
     */
    ViewPager mViewPager;

    SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState)     {
        super.onCreate(savedInstanceState);

        initAppPreferences();
        initViewPager();
        initActionBar();

        mViewPager.setCurrentItem(1);
    }

    private void initAppPreferences() {
        // set preferences, moved from app to here
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    private void initViewPager() {
        setContentView(R.layout.activity_main);
        mViewPager = (ViewPager) findViewById(R.id.main);
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(this, mViewPager);
    }

    private void initActionBar() {
        final ActionBar actionBar = getActionBar();
        if (actionBar == null)
            return;

        // Specify that the Home/Up button should not be enabled, since there is no hierarchical
        // parent.
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);

        // Specify that we will be displaying tabs in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        android.app.ActionBar.Tab alarm_tab  =  actionBar.newTab()
                .setIcon(R.drawable.actionbar_alarm_tab_indicator);
        android.app.ActionBar.Tab clock_tab  =  actionBar.newTab()
                .setIcon(R.drawable.actionbar_clock_tab_indicator);
        android.app.ActionBar.Tab settings_tab  =  actionBar.newTab()
                .setIcon(R.drawable.actionbar_settings_tab_indicator);

        actionBar.addTab(alarm_tab.setTabListener(mAppSectionsPagerAdapter));
        actionBar.addTab(clock_tab.setTabListener(mAppSectionsPagerAdapter));
        actionBar.addTab(settings_tab.setTabListener(mAppSectionsPagerAdapter));


    }

    // implemented to catch the first time run of our app
    @Override
    protected void onResume() {
        super.onResume();
        if (prefs.getBoolean("firstrun", true)) {

            firstRunInitialization();

            prefs.edit().putBoolean("firstrun", false).commit();
        }
    }

    private void firstRunInitialization() {
        AlarmManagerHelper.enableDonationCheck(this);
    }

}
