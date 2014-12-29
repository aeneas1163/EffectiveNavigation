package com.alphan.mcan.snoozecharity.viewModels.mainActiviy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.alphan.mcan.snoozecharity.R;
import com.alphan.mcan.snoozecharity.data.model.PendingDonationDataModel;
import com.alphan.mcan.snoozecharity.services.AlarmManagerHelper;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

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

        final PendingDonationDataModel model = getItem(position);

        TextView charityName = (TextView) view.findViewById(R.id.charity_name);
        final Resources res = mContext.getResources();
        final String[] charities = res.getStringArray(R.array.charity_array);
        final String[] charityWebsites = res.getStringArray(R.array.charity_website_array);
        final String[] charitySnooze = res.getStringArray(R.array.charity_snooze_text);
        final String[] charitySMSnumber = res.getStringArray(R.array.charity_sms_number);
        final String[] charitySMStext = res.getStringArray(R.array.charity_sms_text);
        final String[] charitySMSamount = res.getStringArray(R.array.charity_sms_donation_amount);
        final String[] charityWEBsupport = res.getStringArray(R.array.charity_web_donation_support);
        charityName.setText(charities[model.getCharityIndex()]);

        TextView txtName = (TextView) view.findViewById(R.id.donation_amount);
        txtName.setText(String.format("%.2f", model.getPendingAmount()) + res.getString(R.string.money_sign));

//        TextView donationName = (TextView) view.findViewById(R.id.donation_db_id);
//        donationName.setText("Donation DB ID: " + model.getId());

        Button payButton = (Button) view.findViewById(R.id.donation_pay_button);

        final int charityIndex = model.getCharityIndex();

        final Double pendingAmount = model.getPendingAmount();

        final Double currentSMSAmount = Double.parseDouble(charitySMSamount[charityIndex]);

        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder adb = new AlertDialog.Builder(mContext);
                SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(mContext);
                final Boolean isSMS = preference.getBoolean("payment_option", true);
                adb.setTitle(res.getString(R.string.pay_now));
                if (isSMS) {
                    if (pendingAmount.doubleValue() == currentSMSAmount.doubleValue())
                        adb.setMessage(res.getString(R.string.donate_dialog_text_sms, String.format("%.2f", pendingAmount), charitySnooze[charityIndex], res.getString(R.string.money_sign)));
                    else
                        adb.setMessage(res.getString(R.string.donate_sure_text_sms, String.format("%.2f", pendingAmount), charitySnooze[charityIndex], res.getString(R.string.money_sign), String.format("%.2f", currentSMSAmount)));
                } else
                    adb.setMessage(res.getString(R.string.donate_dialog_text_web, String.format("%.2f", pendingAmount), charitySnooze[charityIndex], res.getString(R.string.money_sign)));
                adb.setNegativeButton(res.getString(R.string.no), null);
                adb.setPositiveButton(res.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (isSMS || !Boolean.parseBoolean(charityWEBsupport[charityIndex])) {
                            if (pendingAmount < currentSMSAmount) {

                                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                                sendIntent.setData(Uri.parse("sms:" + charitySMSnumber[charityIndex] + "?body=" + charitySMStext[charityIndex]));
                                mContext.startActivity(sendIntent);
                                AlarmManagerHelper.addPaidSMSDonation(mContext, model, currentSMSAmount);
                                remove(model);
                                notifyDataSetChanged();
                                sendMail(currentSMSAmount, charities[charityIndex]);

                            } else {
                                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                                sendIntent.setData(Uri.parse("sms:" + charitySMSnumber[charityIndex] + "?body=" + charitySMStext[charityIndex]));
                                mContext.startActivity(sendIntent);
                                Double leftoverAmount = AlarmManagerHelper.addPaidSMSDonation(mContext, model, currentSMSAmount);
                                if (pendingAmount.doubleValue() == currentSMSAmount.doubleValue()) {
                                    remove(model);
                                } else {
                                    model.setPendingAmount(leftoverAmount);
                                }
                                notifyDataSetChanged();
                                sendMail(currentSMSAmount, charities[charityIndex]);
                            }

                        } else {
                            AlarmManagerHelper.transferToPaidDonation(mContext, model);
                            remove(model);
                            notifyDataSetChanged();
                            sendMail(model.getPendingAmount(), charities[charityIndex]);
                            String url = charityWebsites[model.getCharityIndex()];
                            Intent goToDonationWebsite = new Intent(Intent.ACTION_VIEW);
                            goToDonationWebsite.setData(Uri.parse(url));
                            mContext.startActivity(goToDonationWebsite);
                        }
                    }
                });
                adb.show();
            }
        });

        return view;
    }

    private void sendMail(Double amount, String charity) {
        Session session = createSessionObject();

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate = df.format(calendar.getTime());

        String text = charity + " " + String.format("%.2f", amount) + " " + mContext.getString(R.string.money_sign) + " " + formattedDate;

        try {
            Message message = createMessage(mContext.getString(R.string.username), text, text, session);
            new SendMailTask().execute(message);
        } catch (AddressException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private Message createMessage(String email, String subject, String messageBody, Session session) throws MessagingException, UnsupportedEncodingException {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(mContext.getString(R.string.username), mContext.getString(R.string.app_name)));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(email, email));
        message.setSubject(subject);
        message.setText(messageBody);
        return message;
    }

    private Session createSessionObject() {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        return Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mContext.getString(R.string.username), mContext.getString(R.string.password));
            }
        });
    }

    private class SendMailTask extends AsyncTask<Message, Void, Void> {

        @Override
        protected Void doInBackground(Message... messages) {
            try {
                Transport.send(messages[0]);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
