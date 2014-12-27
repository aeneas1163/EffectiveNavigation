package com.alphan.mcan.snoozecharity.viewModels.mainActiviy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.alphan.mcan.snoozecharity.R;
import com.alphan.mcan.snoozecharity.data.model.PendingDonationDataModel;
import com.alphan.mcan.snoozecharity.services.AlarmManagerHelper;

import java.util.List;

/**
 * Created by Alphan on 26-Dec-14.
 */
public class PendingDonationListAdapter extends ArrayAdapter<PendingDonationDataModel> {

    private Context mContext;

    public PendingDonationListAdapter(Context context, List<PendingDonationDataModel> pendingDonations) {
        super(context, R.layout.pending_donation_list_item, pendingDonations);
        mContext = context;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.pending_donation_list_item, parent, false);
        }

        final PendingDonationDataModel model =  getItem(position);

        TextView charityName = (TextView) view.findViewById(R.id.charity_name);
        final Resources res = mContext.getResources();
        final String[] charities = res.getStringArray(R.array.charity_array);
        final String[] charityWebsites = res.getStringArray(R.array.charity_website_array);
        final String[] charitiesSnooze = res.getStringArray(R.array.charity_snooze_text);
        charityName.setText(charities[model.getCharityIndex()]);

        TextView txtName = (TextView) view.findViewById(R.id.donation_amount);
        txtName.setText(String.format("%.2f", model.getPendingAmount()) + res.getString(R.string.money_sign));

        TextView donationName = (TextView) view.findViewById(R.id.donation_db_id);
        donationName.setText("Donation DB ID: " + model.getId());

        Button payButton = (Button) view.findViewById(R.id.donation_pay_button);

        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder adb = new AlertDialog.Builder(view.getContext());
                adb.setTitle(res.getString(R.string.pay_now));
                adb.setMessage(res.getString(R.string.donate_dialog_text, String.format("%.2f", model.getPendingAmount()), charitiesSnooze[model.getCharityIndex()], res.getString(R.string.money_sign)));
                adb.setNegativeButton(res.getString(R.string.no), null);
                adb.setPositiveButton(res.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AlarmManagerHelper.transferToPaidDonation(mContext, model);
                        remove(model);
                        notifyDataSetChanged();
                        String url = "http://" + charityWebsites[model.getCharityIndex()];
                        Intent goToDonationWebsite = new Intent(Intent.ACTION_VIEW);
                        goToDonationWebsite.setData(Uri.parse(url));
                        mContext.startActivity(goToDonationWebsite);
                    }
                });
                adb.show();
            }
        });

        return view;
    }
}
