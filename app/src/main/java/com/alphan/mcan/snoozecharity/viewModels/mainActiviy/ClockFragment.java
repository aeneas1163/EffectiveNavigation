package com.alphan.mcan.snoozecharity.viewModels.mainActiviy;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.alphan.mcan.snoozecharity.R;
import com.alphan.mcan.snoozecharity.data.model.PendingDonationDataModel;
import com.alphan.mcan.snoozecharity.services.AlarmManagerHelper;

import java.util.List;

/**
 * A dummy fragment representing a section of the app, but that simply displays dummy text.
 *
 * Created by Alphan on 16-Nov-14.
 */
public class ClockFragment extends Fragment {

    public static final String ARG_SECTION_NUMBER = "section_number";

    private PendingDonationListAdapter pendingDonationListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle args = getArguments();

        View rootView = inflater.inflate(R.layout.fragment_section_clock, container, false);

        List<PendingDonationDataModel> donations =  AlarmManagerHelper.getPendingDonations(getActivity());

        pendingDonationListAdapter = new PendingDonationListAdapter(getActivity(), donations);

        final ListView donationListView = (ListView) rootView.findViewById(R.id.pending_donation_list);

        donationListView.setAdapter(pendingDonationListAdapter);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (pendingDonationListAdapter != null)
        {
            pendingDonationListAdapter.clear();
            List<PendingDonationDataModel> donations =  AlarmManagerHelper.getPendingDonations(getActivity());

            if (donations.isEmpty())
                getActivity().findViewById(R.id.pending_donation_textview).setVisibility(View.GONE);
            else
                getActivity().findViewById(R.id.pending_donation_textview).setVisibility(View.VISIBLE);

            pendingDonationListAdapter.addAll(donations);
        }

    }
}