package com.example.android.effectivenavigation.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.effectivenavigation.CharityCollectionActivity;
import com.example.android.effectivenavigation.R;
import com.example.android.effectivenavigation.services.AlarmManagerHelper;

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
                        AlarmManagerHelper.ring(view.getContext());
                    }
                });

        return rootView;
    }
}


