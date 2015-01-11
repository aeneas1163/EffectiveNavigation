package com.alphan.mcan.snoozecharity.viewModels.mainActiviy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import com.alphan.mcan.snoozecharity.R;
import com.alphan.mcan.snoozecharity.data.model.AlarmDataModel;
import com.alphan.mcan.snoozecharity.services.AlarmManagerHelper;
import com.alphan.mcan.snoozecharity.views.ColorPreference;

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

        View rootView = inflater.inflate(R.layout.fragment_section_alarm, container, false);

        View background = rootView.findViewById(R.id.alarm);

        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int color = preference.getInt("dash_colorkey", getActivity().getResources().getColor(R.color.default_color));

        int lightColor = ColorPreference.getLightColor(color, getActivity());

        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[] {color,lightColor});
        gd.setCornerRadius(0f);

        background.setBackgroundDrawable(gd);

        List<AlarmDataModel> alarms =  AlarmManagerHelper.getAlarms(getActivity());

        final AlarmListAdapter alarmListAdapter = new AlarmListAdapter(getActivity(), alarms);

        final ListView alarmListView = (ListView) rootView.findViewById(R.id.alarm_list);

        alarmListView.setAdapter(alarmListAdapter);

        alarmListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> adapterView, View view, int position, long l) {

                final AlarmDataModel model = (AlarmDataModel) adapterView.getItemAtPosition(position);
                final Context mContext = getActivity();
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                final View dialog = inflater.inflate(R.layout.alarm_setter_dialog, null);


                final TextView alarm_name = (TextView) dialog.findViewById(R.id.alarm_name);

                alarm_name.setText(model.getName());

                final TimePicker tp = (TimePicker) dialog.findViewById(R.id.timePicker);
                final SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(mContext);
                tp.setIs24HourView(preference.getBoolean("24hour_option", true));
                tp.setCurrentHour(model.getTimeHour());
                tp.setCurrentMinute(model.getTimeMinute());



                CheckBox satView = (CheckBox) dialog.findViewById(R.id.alarm_repeat_check);

                // if the alarm is repeating we should initialize the toggle buttons appropriately
                if (model.isWeekly()) {
                    satView.setChecked(true);
                    AlarmFragment.enableToggleRepeat(dialog);
                    updateRepeatingDays(dialog, model);
                }

                satView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            AlarmFragment.enableToggleRepeat(dialog);
                        } else {
                            AlarmFragment.disableToggleRepeat(dialog);
                        }
                    }

                });

                dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        alarmListAdapter.clear();
                        int selectedHour = tp.getCurrentHour();
                        int selectedMinute = tp.getCurrentMinute();
                        model.setName(alarm_name.getText().toString());
                        model.setTimeHour(selectedHour);
                        model.setTimeMinute(selectedMinute);
                        model.setEnabled(true);
                        AlarmFragment.setRepeatingDays(dialog, model);
                        AlarmManagerHelper.modifyAlarm(mContext, model);
                        List<AlarmDataModel> alarms = AlarmManagerHelper.getAlarms(mContext);
                        if (alarms != null) {
                            for (AlarmDataModel alarm_iter : alarms) {
                                alarmListAdapter.add(alarm_iter);
                            }
                        }
                        alarmListAdapter.notifyDataSetChanged();
                    }
                });

                dialogBuilder.setNegativeButton(R.string.cancel, null);

                dialogBuilder.setView(dialog);

                final AlertDialog dialogToLaunch = dialogBuilder.create();

                ImageButton deleteButton = (ImageButton) dialog.findViewById(R.id.deleteAlarm);

                deleteButton.setVisibility(Button.VISIBLE);
                deleteButton.setClickable(true);

                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlarmManagerHelper.deleteAlarm(mContext, model);
                        alarmListAdapter.remove(model);
                        dialogToLaunch.dismiss();
                    }
                });

                dialogToLaunch.setCanceledOnTouchOutside(true);
                dialogToLaunch.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialogToLaunch.show();
            }
        });

//        rootView.findViewById(R.id.demo_alarm)
//                .setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        final Calendar c = Calendar.getInstance();
//                        int hour = c.get(Calendar.HOUR_OF_DAY);
//                        int minute = c.get(Calendar.MINUTE);
//                        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getActivity());
//                        String strRingtonePreference = preference.getString("ringtone_pref", "DEFAULT_SOUND");
//                        Uri ringtoneUri = Settings.System.DEFAULT_ALARM_ALERT_URI;
//                        if (!strRingtonePreference.equalsIgnoreCase("DEFAULT_SOUND"))
//                        {
//                            ringtoneUri = Uri.parse( strRingtonePreference);
//                        }
//                        AlarmDataModel alarmData = new AlarmDataModel("WAAAKE UP!!", hour, minute + 1, ringtoneUri);
//                        alarmData.setEnabled(true);
//                        AlarmManagerHelper.createNewAlarm(getActivity(), alarmData);
//                    }
//                });


        rootView.findViewById(R.id.set_alarm_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        final View dialog = inflater.inflate(R.layout.alarm_setter_dialog, null);

                        final TimePicker tp = (TimePicker) dialog.findViewById(R.id.timePicker);

                        final SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        tp.setIs24HourView(preference.getBoolean("24hour_option", true));

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

                        dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                int selectedHour = tp.getCurrentHour();
                                int selectedMinute = tp.getCurrentMinute();

                                String strRingtonePreference = preference.getString("ringtone_pref", "DEFAULT_SOUND");
                                Uri ringtoneUri = Settings.System.DEFAULT_ALARM_ALERT_URI;
                                if (!strRingtonePreference.equalsIgnoreCase("DEFAULT_SOUND"))
                                {
                                    ringtoneUri = Uri.parse( strRingtonePreference);
                                }

                                TextView alarm_name = (TextView) dialog.findViewById(R.id.alarm_name);
                                AlarmDataModel alarmData = new AlarmDataModel(alarm_name.getText().toString(), selectedHour, selectedMinute, ringtoneUri);
                                alarmData.setEnabled(true);
                                AlarmFragment.setRepeatingDays(dialog, alarmData);
                                AlarmManagerHelper.createNewAlarm(getActivity(), alarmData);

                                // if this is the first alarm then set up the adapter which will automatically add
                                // first alarm to the list
                                List<AlarmDataModel> alarms =  AlarmManagerHelper.getAlarms(getActivity());
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
                                                if (!alarm_iter.isSnoozeAlarm())
                                                    currentAdapter.add(alarm_iter);
                                            }
                                        }

                                        currentAdapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        });

                        dialogBuilder.setNegativeButton(R.string.cancel, null);


                        dialogBuilder.setView(dialog);

                        final AlertDialog dialogToLaunch = dialogBuilder.create();

                        dialogToLaunch.setCanceledOnTouchOutside(true);
                        dialogToLaunch.requestWindowFeature(Window.FEATURE_NO_TITLE);

                        dialogToLaunch.show();
                    }
                });

        return rootView;
    }

    public static void enableToggleRepeat(View dialog) {
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

    public static void disableToggleRepeat(View dialog) {
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

    public static void setRepeatingDays(View dialog, AlarmDataModel alarmData) {
        alarmData.setWeeklyRepeat(((CheckBox)dialog.findViewById(R.id.alarm_repeat_check)).isChecked());
        alarmData.setRepeatingDay(0, ((ToggleButton)dialog.findViewById(R.id.toggle_sunday)).isChecked());
        alarmData.setRepeatingDay(1, ((ToggleButton)dialog.findViewById(R.id.toggle_monday)).isChecked());
        alarmData.setRepeatingDay(2, ((ToggleButton)dialog.findViewById(R.id.toggle_tuesday)).isChecked());
        alarmData.setRepeatingDay(3, ((ToggleButton)dialog.findViewById(R.id.toggle_wednesday)).isChecked());
        alarmData.setRepeatingDay(4, ((ToggleButton)dialog.findViewById(R.id.toggle_thursday)).isChecked());
        alarmData.setRepeatingDay(5, ((ToggleButton)dialog.findViewById(R.id.toggle_friday)).isChecked());
        alarmData.setRepeatingDay(6, ((ToggleButton)dialog.findViewById(R.id.toggle_saturday)).isChecked());
    }

    public static void updateRepeatingDays(View dialog, AlarmDataModel alarmData) {
        ((ToggleButton)dialog.findViewById(R.id.toggle_sunday)).setChecked(alarmData.getRepeatingDay(0));
        ((ToggleButton)dialog.findViewById(R.id.toggle_monday)).setChecked(alarmData.getRepeatingDay(1));
        ((ToggleButton)dialog.findViewById(R.id.toggle_tuesday)).setChecked(alarmData.getRepeatingDay(2));
        ((ToggleButton)dialog.findViewById(R.id.toggle_wednesday)).setChecked(alarmData.getRepeatingDay(3));
        ((ToggleButton)dialog.findViewById(R.id.toggle_thursday)).setChecked(alarmData.getRepeatingDay(4));
        ((ToggleButton)dialog.findViewById(R.id.toggle_friday)).setChecked(alarmData.getRepeatingDay(5));
        ((ToggleButton)dialog.findViewById(R.id.toggle_saturday)).setChecked(alarmData.getRepeatingDay(6));
    }


}
