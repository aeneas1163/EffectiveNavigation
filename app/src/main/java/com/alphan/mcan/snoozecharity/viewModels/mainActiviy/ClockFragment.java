package com.alphan.mcan.snoozecharity.viewModels.mainActiviy;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.alphan.mcan.snoozecharity.R;
import com.alphan.mcan.snoozecharity.data.model.DonationDataModel;
import com.alphan.mcan.snoozecharity.services.AlarmManagerHelper;

import java.util.List;

/**
 * A dummy fragment representing a section of the app, but that simply displays dummy text.
 *
 * Created by Alphan on 16-Nov-14.
 */
public class ClockFragment extends Fragment {

    public static final String ARG_SECTION_NUMBER = "section_number";

    private DonationListAdapter donationListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle args = getArguments();

        View rootView = inflater.inflate(R.layout.fragment_section_clock, container, false);

        List<DonationDataModel> donations =  AlarmManagerHelper.getPendingDonations(getActivity());

        if (donations.isEmpty())
            rootView.findViewById(R.id.pending_donation_textview).setVisibility(View.GONE);

        donationListAdapter = new DonationListAdapter(getActivity(), donations);

        final ListView donationListView = (ListView) rootView.findViewById(R.id.pending_donation_list);

        donationListView.setAdapter(donationListAdapter);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (donationListAdapter != null)
        {
            donationListAdapter.clear();
            List<DonationDataModel> donations =  AlarmManagerHelper.getPendingDonations(getActivity());
            donationListAdapter.addAll(donations);
        }

    }
}