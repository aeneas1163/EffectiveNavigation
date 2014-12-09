package com.alphan.mcan.snoozecharity.viewModels.mainActiviy;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import com.alphan.mcan.snoozecharity.R;
import com.alphan.mcan.snoozecharity.data.model.AlarmDataModel;
import com.alphan.mcan.snoozecharity.data.persistence.AlarmDBAssistant;
import com.alphan.mcan.snoozecharity.services.AlarmManagerHelper;

import java.util.Iterator;
import java.util.List;

/**
 * A dummy fragment representing a section of the app, but that simply displays dummy text.
 *
 * Created by Alphan on 16-Nov-14.
 */
public class AlarmFragment extends Fragment{

    public static final String ARG_SECTION_NUMBER = "section_number";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle args = getArguments();

        View rootView = inflater.inflate(R.layout.fragment_section_alarm, container, false);

        final AlarmDBAssistant dbHelper = new AlarmDBAssistant(getActivity());
        List<AlarmDataModel> alarms =  dbHelper.getAlarms();

        // filter snooze alarms out:
        for (Iterator<AlarmDataModel> alarmIterator  = alarms.iterator(); alarmIterator.hasNext();) {
            AlarmDataModel alarm = alarmIterator.next();
            if (alarm.isSnoozeAlarm())
                alarmIterator.remove();
        }

        AlarmListAdapter alarmListAdapter = new AlarmListAdapter(getActivity(), alarms);

        final ListView alarmListView = (ListView) rootView.findViewById(R.id.alarm_list);

        if (alarms != null) {
            alarmListView.setAdapter(alarmListAdapter);
        }


        rootView.findViewById(R.id.set_alarm_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        final Dialog dialog = new Dialog(getActivity());

                        dialog.setContentView(R.layout.alarm_setter_dialog);

                        final TimePicker tp = (TimePicker) dialog.findViewById(R.id.timePicker);

                        tp.setIs24HourView(true);

                        Button okButton = (Button) dialog.findViewById(R.id.setterOk);

                        CheckBox satView = (CheckBox)dialog.findViewById(R.id.alarm_repeat_check);

                        satView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                            @Override
                            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                                if (isChecked){
                                    AlarmFragment.enableToggleRepeat(dialog);
                                }
                                else {
                                    AlarmFragment.disableToggleRepeat(dialog);
                                }
                            }

                        });

                        okButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                int selectedHour = tp.getCurrentHour();
                                int selectedMinute = tp.getCurrentMinute();
                                SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getActivity());
                                String strRingtonePreference = preference.getString("ringtone_pref", "DEFAULT_SOUND");
                                Uri ringtoneUri = Settings.System.DEFAULT_ALARM_ALERT_URI;
                                if (!strRingtonePreference.equalsIgnoreCase("DEFAULT_SOUND"))
                                {
                                    ringtoneUri = Uri.parse( strRingtonePreference);
                                }
                                AlarmDataModel alarmData = new AlarmDataModel("Test Alarm", selectedHour, selectedMinute, ringtoneUri);
                                alarmData.setEnabled(true);
                                AlarmFragment.setRepeatingDays(dialog, alarmData);
                                AlarmManagerHelper.createOrModifyAlarmPendingIntent(getActivity(), alarmData);

                                // if this is the first alarm then set up the adapter which will autoatically add
                                // first alarm to the list
                                List<AlarmDataModel> alarms =  dbHelper.getAlarms();
                                if (alarmListView.getAdapter() == null && alarms.size() == 1) {

                                    AlarmListAdapter alarmListAdapter = new AlarmListAdapter(getActivity(), alarms);
                                    alarmListView.setAdapter(alarmListAdapter);
                                }
                                // if not then just get the current adapter and add the new alarm
                                else {
                                    AlarmListAdapter currentAdapter = (AlarmListAdapter) alarmListView.getAdapter();

                                    if (currentAdapter != null)
                                    {
                                        currentAdapter.clear();

                                        if (alarms != null){

                                            for (AlarmDataModel alarm_iter : alarms) {

                                                currentAdapter.add(alarm_iter);
                                            }
                                        }

                                        currentAdapter.notifyDataSetChanged();
                                    }
                                }


                                dialog.dismiss();
                            }
                        });

                        Button cancelButton = (Button) dialog.findViewById(R.id.setterCancel);

                        cancelButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                            }
                        });

                        dialog.setTitle("Set Alarm");

                        dialog.show();
                    }
                });

        return rootView;
    }

    public static void enableToggleRepeat(Dialog dialog) {
        ToggleButton togSunday = ((ToggleButton)dialog.findViewById(R.id.toggle_sunday));
        ToggleButton togMonday = ((ToggleButton)dialog.findViewById(R.id.toggle_monday));
        ToggleButton togTuesday = ((ToggleButton)dialog.findViewById(R.id.toggle_tuesday));
        ToggleButton togWednesday = ((ToggleButton)dialog.findViewById(R.id.toggle_wednesday));
        ToggleButton togThursday = ((ToggleButton)dialog.findViewById(R.id.toggle_thursday));
        ToggleButton togFriday = ((ToggleButton)dialog.findViewById(R.id.toggle_friday));
        ToggleButton togSaturday = ((ToggleButton)dialog.findViewById(R.id.toggle_saturday));
        togSunday.setEnabled(true);
        togMonday.setEnabled(true);
        togTuesday.setEnabled(true);
        togWednesday.setEnabled(true);
        togThursday.setEnabled(true);
        togFriday.setEnabled(true);
        togSaturday.setEnabled(true);
        togSunday.setClickable(true);
        togMonday.setClickable(true);
        togTuesday.setClickable(true);
        togWednesday.setClickable(true);
        togThursday.setClickable(true);
        togFriday.setClickable(true);
        togSaturday.setClickable(true);
        togSunday.setChecked(false);
        togMonday.setChecked(true);
        togTuesday.setChecked(true);
        togWednesday.setChecked(true);
        togThursday.setChecked(true);
        togFriday.setChecked(true);
        togSaturday.setChecked(false);
    }

    public static void disableToggleRepeat(Dialog dialog) {
        ToggleButton togSunday = ((ToggleButton)dialog.findViewById(R.id.toggle_sunday));
        ToggleButton togMonday = ((ToggleButton)dialog.findViewById(R.id.toggle_monday));
        ToggleButton togTuesday = ((ToggleButton)dialog.findViewById(R.id.toggle_tuesday));
        ToggleButton togWednesday = ((ToggleButton)dialog.findViewById(R.id.toggle_wednesday));
        ToggleButton togThursday = ((ToggleButton)dialog.findViewById(R.id.toggle_thursday));
        ToggleButton togFriday = ((ToggleButton)dialog.findViewById(R.id.toggle_friday));
        ToggleButton togSaturday = ((ToggleButton)dialog.findViewById(R.id.toggle_saturday));
        togSunday.setEnabled(false);
        togMonday.setEnabled(false);
        togTuesday.setEnabled(false);
        togWednesday.setEnabled(false);
        togThursday.setEnabled(false);
        togFriday.setEnabled(false);
        togSaturday.setEnabled(false);
        togSunday.setClickable(false);
        togMonday.setClickable(false);
        togTuesday.setClickable(false);
        togWednesday.setClickable(false);
        togThursday.setClickable(false);
        togFriday.setClickable(false);
        togSaturday.setClickable(false);
        togSunday.setChecked(false);
        togMonday.setChecked(false);
        togTuesday.setChecked(false);
        togWednesday.setChecked(false);
        togThursday.setChecked(false);
        togFriday.setChecked(false);
        togSaturday.setChecked(false);
    }

    public static void setRepeatingDays(Dialog dialog, AlarmDataModel alarmData) {
        alarmData.setWeeklyRepeat(((CheckBox)dialog.findViewById(R.id.alarm_repeat_check)).isChecked());
        alarmData.setRepeatingDay(0, ((ToggleButton)dialog.findViewById(R.id.toggle_sunday)).isChecked());
        alarmData.setRepeatingDay(1, ((ToggleButton)dialog.findViewById(R.id.toggle_monday)).isChecked());
        alarmData.setRepeatingDay(2, ((ToggleButton)dialog.findViewById(R.id.toggle_tuesday)).isChecked());
        alarmData.setRepeatingDay(3, ((ToggleButton)dialog.findViewById(R.id.toggle_wednesday)).isChecked());
        alarmData.setRepeatingDay(4, ((ToggleButton)dialog.findViewById(R.id.toggle_thursday)).isChecked());
        alarmData.setRepeatingDay(5, ((ToggleButton)dialog.findViewById(R.id.toggle_friday)).isChecked());
        alarmData.setRepeatingDay(6, ((ToggleButton)dialog.findViewById(R.id.toggle_saturday)).isChecked());
    }



}
