package com.alphan.mcan.snoozecharity.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.alphan.mcan.snoozecharity.R;
import com.alphan.mcan.snoozecharity.data.model.AlarmDataModel;
import com.alphan.mcan.snoozecharity.data.model.PendingDonationDataModel;
import com.alphan.mcan.snoozecharity.viewModels.MainActivity;

import java.util.ArrayList;
import java.util.List;


// class that handles call backs from androids alarm manager api
public class AlarmBroadcastReceiver extends BroadcastReceiver {


    // actions and params
    private static final String DELETE_ALARM = "snoozecharity.services.action.DELETE.ALARM";

    public final String TAG = this.getClass().getSimpleName();

    /**
     * Helper method to create a pending-intent for this service which to delete an alarm
     */
    public static PendingIntent getDeleteAlarmPendingIntent (Context context, long alarmID) {
        // generate pendingIntent
        Intent intent = getDeleteAlarmIntent(context, alarmID);
        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pIntent;
    }

    private static Intent getDeleteAlarmIntent(Context context, long alarmID) {
        Intent intent = new Intent(context, AlarmBroadcastReceiver.class);
        intent.setAction(DELETE_ALARM);
        intent.putExtra(AlarmManagerHelper.ID, alarmID);
        return intent;
    }


    @Override
    public void onReceive(Context context, Intent intent) {

        // intents from androids alarm manager:
        int intentType = intent.getIntExtra(AlarmManagerHelper.INTENT_TYPE, -1);
        switch (intentType) {
            case AlarmManagerHelper.ALARM_INTENT:
                handleAlarmIntent(context, intent);
                break;
            case AlarmManagerHelper.DONATION_INTENT:
                handleDonationCheckIntent(context, intent);
                break;
            default:
                break;
        }

        // internal intents used for alarm management
        final long alarmID = intent.getLongExtra(AlarmManagerHelper.ID, -1);
        final String action = intent.getAction();

        if (action != null) {
            if (action.equals(DELETE_ALARM))
                handleDeleteAlarmIntent(context, alarmID);
        }
    }

    // handler for alarm manager call back
    private void handleAlarmIntent(final Context context, final Intent intent) {
        long alarmID = intent.getLongExtra(AlarmManagerHelper.ID, -1);
        AlarmDataModel receivedAlarm = AlarmManagerHelper.getAlarm(context, alarmID);
        if ( !isValidAlarm(receivedAlarm))
            return;

        //reschedule next alarm:
        AlarmManagerHelper.resetAlarm(context, receivedAlarm);

        AlarmRingService.startRingAlarmIntent(context, alarmID);
    }

    private boolean isValidAlarm(AlarmDataModel receivedAlarm) {
        if (receivedAlarm == null)
            return false;
        //TODO: check time of the received alarm versus current time, handle if it is too different.
        if (receivedAlarm.isEnabled())
            return true;
        return false;
    }

    private void handleDonationCheckIntent(Context context, Intent intent) {
        //TODO:
        //get the largest pending donation from db
        //check if it larger than warning threshold
        //if it is show notification to the user
        //profit.

        List<PendingDonationDataModel> donations =  AlarmManagerHelper.getPendingDonations(context);
        List<PendingDonationDataModel> donationsToBePaid = new ArrayList<PendingDonationDataModel>();
        Resources res = context.getResources();
        String[] charities = res.getStringArray(R.array.charity_array);
        String[] charitySMSamount = res.getStringArray(R.array.charity_sms_donation_amount);
        String[] charitiesSnooze = res.getStringArray(R.array.charity_snooze_text);

        for (PendingDonationDataModel donation_iter : donations)
        {
            if (donation_iter.getPendingAmount().doubleValue() >= Double.parseDouble(charitySMSamount[donation_iter.getCharityIndex()]))
            {
                donationsToBePaid.add(donation_iter);
            }
        }
        String notificationMessage;
        if (!donationsToBePaid.isEmpty())
        {
            StringBuilder strBuilder = new StringBuilder();
            Double totalAmount = 0.0;
            for (int index = 0; index < donationsToBePaid.size(); index ++)
            {
                PendingDonationDataModel pendingDon = donationsToBePaid.get(index);
                strBuilder.append(charitiesSnooze[pendingDon.getCharityIndex()]);
                if (index == donationsToBePaid.size() - 2)
                    strBuilder.append(" " + res.getString(R.string.and) + " ");
                else if (index != donationsToBePaid.size() - 1)
                    strBuilder.append(", ");

                totalAmount = totalAmount + pendingDon.getPendingAmount();
            }

            String pendingAmount = String.format("%.2f", totalAmount);
            notificationMessage = res.getString(R.string.charity_needs_payment, pendingAmount, strBuilder.toString(), res.getString(R.string.money_sign));
            fireDonationNotification(context, notificationMessage);
        }
    }

    // internal handlers
    private void handleDeleteAlarmIntent(Context context, long alarmID) {
        Log.d(TAG, "Received Delete Alarm Intent...");

        //get and check alarm"
        AlarmDataModel alarmToDelete = AlarmManagerHelper.getAlarm(context, alarmID);
        if(alarmToDelete == null) {
            Log.d(TAG, "Delete Intent received for invalid alarm, ignoring!");
            return;
        }

        //try and check delete
        Boolean deleted = AlarmManagerHelper.deleteAlarm(context, alarmToDelete);
        if(!deleted) {
            Log.d(TAG, "Alarm deleted failed! (id:  " + alarmID + ")");
            return;
        }

        // remove alarm notification if there is one
        NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(AlarmRingService.getNotificationID(alarmToDelete));
        Log.d(TAG, "Alarm deleted: " + alarmID);
    }

    private static void fireDonationNotification(Context context, String message) {
        Intent resultIntent = new Intent(context, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);

        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder  = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setGroup(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setContentIntent(resultPendingIntent);

        // message
        builder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(message));

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(0, builder.build());
    }
}
