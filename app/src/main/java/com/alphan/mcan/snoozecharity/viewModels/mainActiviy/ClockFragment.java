package com.alphan.mcan.snoozecharity.viewModels.mainActiviy;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alphan.mcan.snoozecharity.R;

/**
 * A dummy fragment representing a section of the app, but that simply displays dummy text.
 *
 * Created by Alphan on 16-Nov-14.
 */
public class ClockFragment extends Fragment {

    public static final String ARG_SECTION_NUMBER = "section_number";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle args = getArguments();

        View rootView = inflater.inflate(R.layout.fragment_section_clock, container, false);

        return rootView;
    }
}