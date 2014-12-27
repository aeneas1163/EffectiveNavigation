package com.alphan.mcan.snoozecharity.viewModels.mainActiviy;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.alphan.mcan.snoozecharity.R;
import com.alphan.mcan.snoozecharity.data.model.PaidDonationDataModel;
import com.alphan.mcan.snoozecharity.services.AlarmManagerHelper;
import com.alphan.mcan.snoozecharity.viewModels.AppPreferencesActivity;
import com.alphan.mcan.snoozecharity.viewModels.CharityCollectionActivity;

import java.util.List;

/**
 * A fragment that launches other parts of the demo application.
 *
 * Created by Alphan on 16-Nov-14.
 */
public class SettingsFragment extends Fragment{

    private PaidDonationListAdapter totalDonationListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_section_settings, container, false);

        List<PaidDonationDataModel> donations =  AlarmManagerHelper.getTotalDonations(getActivity());



        totalDonationListAdapter = new PaidDonationListAdapter(getActivity(), donations);

        final ListView donationListView = (ListView) rootView.findViewById(R.id.paid_donation_list);

        donationListView.setAdapter(totalDonationListAdapter);

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

        rootView.findViewById(R.id.donation_history)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        List<PaidDonationDataModel> donationsMade =  AlarmManagerHelper.getPaidDonations(getActivity());

                        ListView donationList = new ListView(getActivity());

                        PaidDonationListAdapter paidDonations = new PaidDonationListAdapter(getActivity(), donationsMade);

                        donationList.setAdapter(paidDonations);

                        builder.setTitle("Your Donation History:")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //do things
                                    }
                                });

                        builder.setView(donationList);
                        AlertDialog alert = builder.create();
                        alert.show();
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
        if (totalDonationListAdapter != null)
        {
            totalDonationListAdapter.clear();
            List<PaidDonationDataModel> donations =  AlarmManagerHelper.getTotalDonations(getActivity());
            if (donations.isEmpty())
                getActivity().findViewById(R.id.paid_donation_textview).setVisibility(View.GONE);
            else
                getActivity().findViewById(R.id.paid_donation_textview).setVisibility(View.VISIBLE);
            totalDonationListAdapter.addAll(donations);
        }
    }
}


