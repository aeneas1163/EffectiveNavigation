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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.alphan.mcan.snoozecharity.R;
import com.alphan.mcan.snoozecharity.views.ColorPreference;

public class CharityCollectionActivity extends FragmentActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments representing
     * each object in a collection. We use a {@link android.support.v4.app.FragmentStatePagerAdapter}
     * derivative, which will destroy and re-create fragments as needed, saving and restoring their
     * state in the process. This is important to conserve memory and is a best practice when
     * allowing navigation between objects in a potentially large collection.
     */
    DemoCollectionPagerAdapter mDemoCollectionPagerAdapter;

    /**
     * The {@link android.support.v4.view.ViewPager} that will display the object collection.
     */
    ViewPager mViewPager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charity_selection);

        // Create an adapter that when requested, will return a fragment representing an object in
        // the collection.
        // 
        // ViewPager and its adapters use support library fragments, so we must use
        // getSupportFragmentManager.
        mDemoCollectionPagerAdapter = new DemoCollectionPagerAdapter(getSupportFragmentManager(), this);

        PagerTabStrip pagerTabStrip = (PagerTabStrip) findViewById(R.id.pager_title_strip);

        // Set up action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
        int color = preference.getInt("dash_colorkey", getResources().getColor(R.color.turquiose));
        int lightColor = ColorPreference.getLightColor(color, this);

        pagerTabStrip.setBackgroundColor(lightColor);


        // Specify that the Home button should show an "Up" caret, indicating that touching the
        // button will take the user one step up in the application's hierarchy.
        //actionBar.setDisplayHomeAsUpEnabled(true);

        // Set up the ViewPager, attaching the adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mDemoCollectionPagerAdapter);
        mViewPager.setCurrentItem(preference.getInt(DemoObjectFragment.ARG_INDEX, 0));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This is called when the Home (Up) button is pressed in the action bar.
                // Create a simple intent that starts the hierarchical parent activity and
                // use NavUtils in the Support Package to ensure proper handling of Up.
                Intent upIntent = new Intent(this, MainActivity.class);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    // This activity is not part of the application's task, so create a new task
                    // with a synthesized back stack.
                    TaskStackBuilder.from(this)
                            // If there are ancestor activities, they should be added here.
                            .addNextIntent(upIntent)
                            .startActivities();
                    finish();
                } else {
                    // This activity is part of the application's task, so simply
                    // navigate up to the hierarchical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link android.support.v4.app.FragmentStatePagerAdapter} that returns a fragment
     * representing an object in the collection.
     */
    public static class DemoCollectionPagerAdapter extends FragmentStatePagerAdapter {

        private Context _context;

        public DemoCollectionPagerAdapter(FragmentManager fm, Context c) {
            super(fm);
            _context = c;
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new DemoObjectFragment();
            Bundle args = new Bundle();
            args.putInt(DemoObjectFragment.ARG_INDEX, i); // Our object is just an integer :-P
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            // For this contrived example, we have a 100-object collection.
            Resources res = _context.getResources();
            String[] charities = res.getStringArray(R.array.charity_array);
            return charities.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Resources res = _context.getResources();
            String[] charities = res.getStringArray(R.array.charity_array);
            return charities[position];
        }
    }

    /**
     * A dummy fragment representing a section of the app, but that simply displays dummy text.
     */
    public static class DemoObjectFragment extends Fragment {

        public static final String ARG_INDEX = "charityindex";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_charity_object, container, false);
            Bundle args = getArguments();
            Resources res = getActivity().getResources();
            SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getActivity());
            int charity_selected = preference.getInt(DemoObjectFragment.ARG_INDEX, 0);
            String[] charityDesc = res.getStringArray(R.array.charity_description_array);
            String[] charityName = res.getStringArray(R.array.charity_array);
            String[] charitiesSelectionText = res.getStringArray(R.array.charity_selection_text);

            final int index = args.getInt(ARG_INDEX);

            TextView  descText = (TextView) rootView.findViewById(R.id.donation_description);
            descText.setText(charityDesc[index]);

            int color = preference.getInt("dash_colorkey", res.getColor(R.color.turquiose));
            int lightColor = ColorPreference.getLightColor(color, getActivity());
            descText.setTextColor(lightColor);

            Button selectButton = ((Button) rootView.findViewById(R.id.select_charity));

            if (charity_selected == index)
            {
                selectButton.setText(res.getString(R.string.selected_charity_button_text, charitiesSelectionText[index]));
                selectButton.setEnabled(false);
            }
            else
                selectButton.setText(res.getString(R.string.select_charity_button_text, charitiesSelectionText[index]));

            TypedArray imgs = getResources().obtainTypedArray(R.array.charity_image_array);

            ImageView imageView = (ImageView) rootView.findViewById(R.id.charity_imageview);

            imageView.setImageResource(imgs.getResourceId(index, -1));

            imgs.recycle();

            selectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());

                    SharedPreferences.Editor editor = settings.edit();

                    editor.putInt(ARG_INDEX, index);

                    editor.commit();

                    getActivity().onBackPressed();
                }
            });
            return rootView;
        }
    }
}
