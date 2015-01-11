package com.alphan.mcan.snoozecharity.viewModels.mainActiviy;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.alphan.mcan.snoozecharity.R;
import com.alphan.mcan.snoozecharity.data.model.PaidDonationDataModel;
import com.alphan.mcan.snoozecharity.services.AlarmManagerHelper;
import com.alphan.mcan.snoozecharity.viewModels.AppPreferencesActivity;
import com.alphan.mcan.snoozecharity.viewModels.CharityCollectionActivity;
import com.alphan.mcan.snoozecharity.views.ColorPreference;

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

        View background = rootView.findViewById(R.id.settings_frag);

        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int color = preference.getInt("dash_colorkey", getActivity().getResources().getColor(R.color.default_color));
        int lightColor = ColorPreference.getLightColor(color, getActivity());

        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[] {color,lightColor});
        gd.setCornerRadius(0f);

        background.setBackgroundDrawable(gd);

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

                        Resources res = view.getResources();

                        if (donationsMade.isEmpty())
                        {
                            builder.setMessage(res.getString(R.string.no_donations_yet));
                        }
                        else {

                            ListView donationList = new ListView(getActivity());

                            PaidDonationListAdapter paidDonations = new PaidDonationListAdapter(getActivity(), donationsMade);

                            donationList.setAdapter(paidDonations);

                            builder.setView(donationList);
                        }

                        builder.setTitle(res.getString(R.string.donation_history_title))
                                .setCancelable(false)
                                .setPositiveButton(res.getString(R.string.ok), null);

                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                });

        rootView.findViewById(R.id.about_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder adb = new AlertDialog.Builder(view.getContext());

                final Resources res = view.getResources();
                adb.setTitle(res.getString(R.string.about_us));
                adb.setMessage(res.getString(R.string.about_us_message));
                adb.setNegativeButton(res.getString(R.string.cancel), null);
                adb.setPositiveButton(res.getString(R.string.rate_us), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        //Try Google play
                        intent.setData(Uri.parse("market://details?id=" + res.getString(R.string.app_id)));
                        if (!MyStartActivity(intent)) {
                            //Market (Google play) app seems not installed, let's try to open a webbrowser
                            intent.setData(Uri.parse("https://play.google.com/store/apps/details?" + res.getString(R.string.app_id)));
                            if (!MyStartActivity(intent)) {
                                //Well if this also fails, we have run out of options, inform the user.
                                Toast.makeText(getActivity(), res.getString(R.string.could_not_open_market), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
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

    private boolean MyStartActivity(Intent aIntent) {
        try
        {
            startActivity(aIntent);
            return true;
        }
        catch (ActivityNotFoundException e)
        {
            return false;
        }
    }
}


