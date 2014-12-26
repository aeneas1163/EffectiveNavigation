package com.alphan.mcan.snoozecharity.viewModels.mainActiviy;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.alphan.mcan.snoozecharity.R;
import com.alphan.mcan.snoozecharity.data.model.DonationDataModel;

import java.util.List;

/**
 * Created by Alphan on 26-Dec-14.
 */
public class DonationListAdapter extends ArrayAdapter<DonationDataModel> {

    private Context mContext;

    public DonationListAdapter(Context context, List<DonationDataModel> pendingDonations) {
        super(context, R.layout.pending_donation_list_item, pendingDonations);
        mContext = context;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.pending_donation_list_item, parent, false);
        }

        final DonationDataModel model =  getItem(position);

        TextView charityName = (TextView) view.findViewById(R.id.charity_name);
        Resources res = mContext.getResources();
        String[] charities = res.getStringArray(R.array.charity_array);
        charityName.setText(charities[model.getCharityIndex()]);

        TextView txtName = (TextView) view.findViewById(R.id.donation_amount);
        txtName.setText(String.format("%.2f", model.getPendingAmount()) + res.getString(R.string.money_sign));

        TextView donationName = (TextView) view.findViewById(R.id.donation_db_id);
        donationName.setText("Donation DB ID: " + model.getId());

        return view;
    }
}
