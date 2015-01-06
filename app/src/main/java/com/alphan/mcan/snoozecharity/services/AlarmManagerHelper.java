package com.alphan.mcan.snoozecharity.services;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

import com.alphan.mcan.snoozecharity.R;
import com.alphan.mcan.snoozecharity.data.model.AlarmDataModel;
import com.alphan.mcan.snoozecharity.data.model.PaidDonationDataModel;
import com.alphan.mcan.snoozecharity.data.model.PendingDonationDataModel;
import com.alphan.mcan.snoozecharity.data.persistence.AlarmDBAssistant;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;


/**
 * An {@link android.app.AlarmManager} helper class used to set new alarms, modify existing alarms
 * as well as resetting alarms. Access to existing alarms is done over this fella as well.
 * In addition this also handles resetting alarms in case device reboots.
 */
public class AlarmManagerHelper extends BroadcastReceiver {
    public AlarmManagerHelper() {
    }

    // intent params:
    public static final String INTENT_TYPE = "intentType";
    public static final int ALARM_INTENT = 0;
    public static final int DONATION_INTENT = 1;
    // alarm params:
    public static final String ID = "id";

    // set alarms again on reboot
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            setAlarms(context);
            //TODO: if (donationReminderIsEnabled)
            // get if from SnoozeApplication.sp
            enableDonationCheck(context);
        }
    }

    // ALARM METHODS:
    // add a completely new alarm and properly set it
    public static boolean createNewAlarm(Context context, AlarmDataModel newAlarm) {

        //try to add alarm to db
        AlarmDBAssistant dbHelper = new AlarmDBAssistant(context);
        boolean added = dbHelper.addAlarmToDB(newAlarm);
        if (!added)
            return false;

        //set alarm if needed
        return setAlarm(context, newAlarm);
    }

    // modify an existing alarm and set it
    public static boolean modifyAlarm(Context context, AlarmDataModel modifiedAlarm) {
        //try to modify the alarm in db
        AlarmDBAssistant dbHelper = new AlarmDBAssistant(context);
        boolean modified = dbHelper.updateAlarmInDB(modifiedAlarm);

        //remove existing alarm if there is one
        cancelPendingAlarm(context, modifiedAlarm);
        setAlarm(context, modifiedAlarm);
        return modified;
    }

    // cancel pending intent for the given alarm if there is one, and set the next alarm for it
    public static boolean resetAlarm(Context context, AlarmDataModel comingAlarm) {
        if (comingAlarm.getId() == -1)
            return false;

        cancelPendingAlarm(context, comingAlarm);
        setAlarm(context, comingAlarm);
        return true;
    }

    // remove an existing alarm
    public static boolean deleteAlarm(Context context, AlarmDataModel deletedAlarm) {

        //first remove existing alarm if there is one (deletion from DB invalidated its ID!)
        cancelPendingAlarm(context, deletedAlarm);

        //try to delete the alarm from db
        AlarmDBAssistant dbHelper = new AlarmDBAssistant(context);
        return dbHelper.removeAlarmFromDB(deletedAlarm);
    }

    // acquire the alarm with the given ID, can return null!!
    public static AlarmDataModel getAlarm(Context context, long iD) {
        AlarmDBAssistant dbHelper = new AlarmDBAssistant(context);
        return dbHelper.getAlarmFromDB(iD);
    }

    // get alarms in DB, filters snooze alarms out.
    public static List<AlarmDataModel> getAlarms(Context context) {
        AlarmDBAssistant dbHelper = new AlarmDBAssistant(context);

        // filter snooze alarms out:
        List<AlarmDataModel> alarms = dbHelper.getAlarmsInDB();
        for (Iterator<AlarmDataModel> alarmIterator  = alarms.iterator(); alarmIterator.hasNext();) {
            AlarmDataModel alarm = alarmIterator.next();
            if (alarm.isSnoozeAlarm())
                alarmIterator.remove();
        }

        // sort alarms
        Collections.sort(alarms, new Comparator<AlarmDataModel>() {
            @Override
            public int compare(AlarmDataModel alarm1, AlarmDataModel alarm2) {
                int totalMinutes1 = (alarm1.getTimeHour()*60) + alarm1.getTimeMinute();
                int totalMinutes2 = (alarm2.getTimeHour()*60) + alarm2.getTimeMinute();

                return totalMinutes1 - totalMinutes2;
            }
        });

        return alarms;
    }


    // DONATION METHODS
    // enable a donation reminder check alarm (every 2 days)
    public static void enableDonationCheck(Context context) {

        PendingIntent pIntent = createDonationPendingIntent(context);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // set a reminder to check donation status every 2 days (not counting today)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 20);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY * 2, pIntent);
    }

    // cancel the donation reminder
    public static void disableDonationCheck(Context context) {
        PendingIntent pIntent = createDonationPendingIntent(context);
        AlarmManager androidAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        androidAlarmManager.cancel(pIntent);
    }



    // HELPER METHODS
    /**
     * helper method used to create alarm PendingIntents for every enabled alarm in our DB for
     * our service: {@link AlarmBroadcastReceiver}
     */
    private static void setAlarms(Context context) {

        AlarmDBAssistant dbHelper = new AlarmDBAssistant(context);
        List<AlarmDataModel> alarms =  dbHelper.getAlarmsInDB();
        for (AlarmDataModel alarm : alarms) {
            setAlarm(context, alarm);
        }
    }


    /**
     * helper method used to cancel alarm PendingIntents for the given model if there is one
     */
    private static boolean cancelPendingAlarm(Context context, AlarmDataModel alarm) {
        if (alarm.getId() == -1 )
            return false;

        PendingIntent pIntent = createAlarmPendingIntent(context, alarm);
        AlarmManager androidAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        androidAlarmManager.cancel(pIntent);
        return true;
    }


    /**
     * helper method used to create alarm PendingIntents for the given model for the correct time
     */
    private static boolean setAlarm(Context context, AlarmDataModel alarm) {
        if (!alarm.isEnabled())
            return false;

        boolean alarmSet = false;

        PendingIntent pIntent = createAlarmPendingIntent(context, alarm);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, alarm.getTimeHour());
        calendar.set(Calendar.MINUTE, alarm.getTimeMinute());
        calendar.set(Calendar.SECOND, 0);

        //Find next time to set
        final int nowDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        final int nowHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        final int nowMinute = Calendar.getInstance().get(Calendar.MINUTE);

        //First check if it's later in the week
        for (int dayOfWeek = Calendar.SUNDAY; dayOfWeek <= Calendar.SATURDAY; ++dayOfWeek)
            if (alarm.getRepeatingDay(dayOfWeek - 1) && dayOfWeek >= nowDay &&
                    !(dayOfWeek == nowDay && alarm.getTimeHour() < nowHour) &&
                    !(dayOfWeek == nowDay && alarm.getTimeHour() == nowHour && alarm.getTimeMinute() <= nowMinute)) {
                calendar.add(Calendar.DAY_OF_WEEK, dayOfWeek - nowDay);
                setPendingAndroidAlarm(context, calendar, pIntent);
                alarmSet = true;
                break;
            }

        //Else check if it's earlier in the week
        if (!alarmSet)
            for (int dayOfWeek = Calendar.SUNDAY; dayOfWeek <= Calendar.SATURDAY; ++dayOfWeek)
                if (alarm.getRepeatingDay(dayOfWeek - 1) && dayOfWeek <= nowDay && alarm.isWeekly()) {
                    calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
                    calendar.add(Calendar.WEEK_OF_YEAR, 1);

                    setPendingAndroidAlarm(context, calendar, pIntent);
                    alarmSet = true;
                    break;
                }

        // if alarm is still not set, check if it is a non-repeating one time alarm
        if (!alarmSet)
            if (!alarm.isWeekly()) { // if the time is less than or equal to now, then the alarm should be for tomorrow
                if (alarm.getTimeHour() < nowHour || (alarm.getTimeHour() == nowHour && alarm.getTimeMinute() <= nowMinute)) {
                    // if it is for saturday, we should make it next week sunday
                    if (nowDay == Calendar.SATURDAY)
                    {
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                        calendar.add(Calendar.WEEK_OF_YEAR, 1);
                    }
                    // if it is any other day, we should not advance the week
                    else
                    {
                        calendar.add(Calendar.DAY_OF_WEEK, 1);
                    }
                    setPendingAndroidAlarm(context, calendar, pIntent);
                    alarmSet = true;
                }
                else { // else it is for today
                    setPendingAndroidAlarm(context, calendar, pIntent);
                    alarmSet = true;
                }
            }

        return alarmSet;
    }

    /**
     * helper method that schedules a pending alarm intent over android's {@link AlarmManager}
     */
    @SuppressLint("NewApi")
    private static void setPendingAndroidAlarm(Context context, Calendar calendar, PendingIntent pIntent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pIntent);
        }
    }

    /**
     * helper method used to create an Alarm PendingIntents for our service: {@link AlarmBroadcastReceiver}
    */
    private static PendingIntent createAlarmPendingIntent(Context context, AlarmDataModel model) {

        Intent intent = new Intent(context, AlarmBroadcastReceiver.class);
        intent.putExtra(INTENT_TYPE, ALARM_INTENT);
        intent.putExtra(ID, model.getId());

        return PendingIntent.getBroadcast(context, (int) model.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * helper method used to create an Donation check PendingIntent for our service: {@link AlarmBroadcastReceiver}
     */
    private static PendingIntent createDonationPendingIntent(Context context) {

        Intent intent = new Intent(context, AlarmBroadcastReceiver.class);
        intent.putExtra(INTENT_TYPE, DONATION_INTENT);

        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    // DONATION METHODS:
    // add or update pending donations
    public static boolean addToPendingDonation(Context context, int charityIndex, double amount) {
        //try to add alarm to db
        AlarmDBAssistant dbHelper = new AlarmDBAssistant(context);
        PendingDonationDataModel model = dbHelper.getPendingDonationByCharityIndex(charityIndex);
        if(model != null)
        {
            model.increasePendingAmount(amount);
            return dbHelper.updatePendingDonation(model);
        }
        else {
            model = new PendingDonationDataModel(charityIndex, amount);
            return dbHelper.addPendingDonation(model);
        }
    }

    // modify an existing alarm and set it
    private static boolean modifyPendingDonation(Context context, PendingDonationDataModel modifiedPendingDonation) {
        //try to modify the alarm in db
        AlarmDBAssistant dbHelper = new AlarmDBAssistant(context);
        return dbHelper.updatePendingDonation(modifiedPendingDonation);
    }

    // remove an existing alarm
    private static boolean deletePendingDonation(Context context, PendingDonationDataModel deletedPendingDonation) {
        //try to delete the alarm from db
        AlarmDBAssistant dbHelper = new AlarmDBAssistant(context);
        return dbHelper.deletePendingDonation(deletedPendingDonation);
    }

    // acquire the alarm with the given ID, can return null!!
    private static PendingDonationDataModel getPendingDonation(Context context, int id) {
        AlarmDBAssistant dbHelper = new AlarmDBAssistant(context);
        return dbHelper.getPendingDonationByCharityIndex(id);
    }

    // get alarms in DB, filters snooze alarms out.
    public static List<PendingDonationDataModel> getPendingDonations(Context context) {
        AlarmDBAssistant dbHelper = new AlarmDBAssistant(context);

        // filter snooze alarms out:
        List<PendingDonationDataModel> pendingDonations = dbHelper.getPendingDonations();
        // sort alarms
        Collections.sort(pendingDonations, new Comparator<PendingDonationDataModel>() {
            @Override
            public int compare(PendingDonationDataModel pendon1, PendingDonationDataModel pendon2) {
                return ((int)(pendon2.getPendingAmount()*100 - pendon1.getPendingAmount()*100));
            }
        });

        return pendingDonations;
    }


    public static PaidDonationDataModel transferToPaidDonation(Context context, PendingDonationDataModel pendingModel) {
        //try to add alarm to db
        AlarmDBAssistant dbHelper = new AlarmDBAssistant(context);
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate = df.format(calendar.getTime());
        PaidDonationDataModel paidModel =  new PaidDonationDataModel(pendingModel, formattedDate);
        //TODO maybe just return a boolean
        if (dbHelper.addPaidDonation(paidModel) && dbHelper.deletePendingDonation(pendingModel))
            return paidModel;
        return null;
    }

    public static Double addPaidSMSDonation(Context context, PendingDonationDataModel pendingModel, Double smsAmount) {
        AlarmDBAssistant dbHelper = new AlarmDBAssistant(context);
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate = df.format(calendar.getTime());
        PaidDonationDataModel paidModel =  new PaidDonationDataModel(pendingModel.getCharityIndex(), smsAmount , formattedDate);
        Double pendingAmount = pendingModel.getPendingAmount();

        if (smsAmount.doubleValue() >= pendingAmount.doubleValue())
        {
            if (dbHelper.addPaidDonation(paidModel) && dbHelper.deletePendingDonation(pendingModel))
                return 0.0;
        }
        else {
            pendingModel.setPendingAmount(pendingAmount - smsAmount);
            if (dbHelper.addPaidDonation(paidModel) && dbHelper.updatePendingDonation(pendingModel))
                return pendingAmount - smsAmount;

        }
        return 0.0;
    }

    // get alarms in DB, filters snooze alarms out.
    public static List<PaidDonationDataModel> getPaidDonations(Context context) {
        AlarmDBAssistant dbHelper = new AlarmDBAssistant(context);

        // filter snooze alarms out:
        List<PaidDonationDataModel> paidDonations = dbHelper.getPaidDonations();
        // sort alarms
        Collections.sort(paidDonations, new Comparator<PaidDonationDataModel>() {
            @Override
            public int compare(PaidDonationDataModel pendon1, PaidDonationDataModel pendon2) {
                return ((int) (pendon2.getId() - pendon1.getId()));
            }
        });

        return paidDonations;
    }

    // get alarms in DB, filters snooze alarms out.
    public static List<PaidDonationDataModel> getTotalDonations(Context context) {
        AlarmDBAssistant dbHelper = new AlarmDBAssistant(context);

        // filter snooze alarms out:
        List<PaidDonationDataModel> paidDonations = dbHelper.getPaidDonations();

        List<PaidDonationDataModel> totalPaidDonations = new ArrayList<PaidDonationDataModel>();

        Resources res = context.getResources();
        String[] charities = res.getStringArray(R.array.charity_array);

        for (int i = 0; i < charities.length; i++)
        {
            totalPaidDonations.add(new PaidDonationDataModel(i, 0.0, ""));
        }


        for (PaidDonationDataModel donationIter : paidDonations)
        {
            for (PaidDonationDataModel totalIter: totalPaidDonations)
            {
                if (donationIter.getCharityIndex() == totalIter.getCharityIndex())
                {
                    Double previousTotal = totalIter.getPaidAmount();
                    totalIter.setPaidAmount(previousTotal + donationIter.getPaidAmount());
                }
            }
        }

        Iterator<PaidDonationDataModel> totalIter = totalPaidDonations.iterator();

        while (totalIter.hasNext())
        {
            PaidDonationDataModel  current = totalIter.next();
            if (current.getPaidAmount() == 0.0)
            {
                totalIter.remove();
            }
        }

        // sort alarms
        Collections.sort(totalPaidDonations, new Comparator<PaidDonationDataModel>() {
            @Override
            public int compare(PaidDonationDataModel pendon1, PaidDonationDataModel pendon2) {
                return ((int)(pendon2.getPaidAmount()*100 - pendon1.getPaidAmount()*100));
            }
        });

        return totalPaidDonations;
    }

}

