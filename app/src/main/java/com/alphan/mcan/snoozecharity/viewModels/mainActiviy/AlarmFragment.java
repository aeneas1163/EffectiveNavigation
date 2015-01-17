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

        initViewBackground(rootView); // set background color according to theme

        ListView alarmListView = initAlarmListView(rootView);

        prepareAlarmItemOnClick(alarmListView);

        prepareNewAlarmButtonOnClick(rootView, alarmListView);

        return rootView;
    }

    private void initViewBackground(View rootView) {
        View background = rootView.findViewById(R.id.alarm);
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int color = preference.getInt("dash_colorkey", getActivity().getResources().getColor(R.color.default_color));
        int lightColor = ColorPreference.getLightColor(color, getActivity());
        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[] {color,lightColor});
        gd.setCornerRadius(0f);
        background.setBackgroundDrawable(gd);
    }

    private ListView initAlarmListView(View rootView) {
        // get alarms and set alarm list adapter
        List<AlarmDataModel> alarms =  AlarmManagerHelper.getAlarms(getActivity());
        final AlarmListAdapter alarmListAdapter = new AlarmListAdapter(getActivity(), alarms);
        ListView alarmListView = (ListView) rootView.findViewById(R.id.alarm_list);

        // set adaptor
        alarmListView.setAdapter(alarmListAdapter);
        return alarmListView;
    }

    private void prepareAlarmItemOnClick(ListView alarmListView) {

        final AlarmListAdapter alarmListAdapter = (AlarmListAdapter)alarmListView.getAdapter();

        // on click listener for alarms
        alarmListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> adapterView, View view, int position, long l) {

                // prepare view of the dialog to be shown:
                final Context mContext = getActivity();
                LayoutInflater inflater = getActivity().getLayoutInflater();
                final View dialog = inflater.inflate(R.layout.alarm_setter_dialog, null);

                // prepare time picker tool
                final TimePicker tp = (TimePicker) dialog.findViewById(R.id.timePicker);
                final SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(mContext);
                tp.setIs24HourView(preference.getBoolean("24hour_option", true));

                // get model params to set time picker time
                final AlarmDataModel model = (AlarmDataModel) adapterView.getItemAtPosition(position);
                tp.setCurrentHour(model.getTimeHour());
                tp.setCurrentMinute(model.getTimeMinute());

                //set alarm title from model
                final TextView alarm_name = (TextView) dialog.findViewById(R.id.alarm_name);
                alarm_name.setText(model.getName());

                // prepare and set repeating checkbox
                CheckBox repeatCheck = (CheckBox) dialog.findViewById(R.id.alarm_repeat_check);
                // if the alarm is repeating we should initialize the toggle buttons appropriately
                if (model.isWeekly()) {
                    repeatCheck.setChecked(true);
                    AlarmFragment.enableToggleRepeat(dialog);
                    updateRepeatingDays(dialog, model);
                }

                setRepeatToggleButtonHandler(dialog);

                setDailyToggleButtonHandlers(dialog);

                // prepare and get dialog to be shown on click
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
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

                // prepare and set delete button
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

                // show dialog!
                dialogToLaunch.setCanceledOnTouchOutside(true);
                dialogToLaunch.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialogToLaunch.show();
            }
        });

    }

    private void prepareNewAlarmButtonOnClick(View rootView,final ListView alarmListView) {
        // button to set a new alarm
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

                        setRepeatToggleButtonHandler(dialog);

                        setDailyToggleButtonHandlers(dialog);

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
    }

    private void setRepeatToggleButtonHandler(final View dialog) {
        CheckBox repeatCheck = (CheckBox)dialog.findViewById(R.id.alarm_repeat_check);
        repeatCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    AlarmFragment.enableToggleRepeat(dialog);
                else
                    AlarmFragment.disableToggleRepeat(dialog);
            }
        });
    }

    private void setDailyToggleButtonHandlers(final View dialog) {

        ToggleButton togSunday = ((ToggleButton)dialog.findViewById(R.id.toggle_sunday));
        ToggleButton togMonday = ((ToggleButton)dialog.findViewById(R.id.toggle_monday));
        ToggleButton togTuesday = ((ToggleButton)dialog.findViewById(R.id.toggle_tuesday));
        ToggleButton togWednesday = ((ToggleButton)dialog.findViewById(R.id.toggle_wednesday));
        ToggleButton togThursday = ((ToggleButton)dialog.findViewById(R.id.toggle_thursday));
        ToggleButton togFriday = ((ToggleButton)dialog.findViewById(R.id.toggle_friday));
        ToggleButton togSaturday = ((ToggleButton)dialog.findViewById(R.id.toggle_saturday));

        final CheckBox toggleRepeat = (CheckBox) dialog.findViewById(R.id.alarm_repeat_check);

        CompoundButton.OnCheckedChangeListener toggleButtonClickhandler = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    toggleRepeat.setChecked(true);
                } else {

                    ToggleButton togSunday = ((ToggleButton)dialog.findViewById(R.id.toggle_sunday));
                    ToggleButton togMonday = ((ToggleButton)dialog.findViewById(R.id.toggle_monday));
                    ToggleButton togTuesday = ((ToggleButton)dialog.findViewById(R.id.toggle_tuesday));
                    ToggleButton togWednesday = ((ToggleButton)dialog.findViewById(R.id.toggle_wednesday));
                    ToggleButton togThursday = ((ToggleButton)dialog.findViewById(R.id.toggle_thursday));
                    ToggleButton togFriday = ((ToggleButton)dialog.findViewById(R.id.toggle_friday));
                    ToggleButton togSaturday = ((ToggleButton)dialog.findViewById(R.id.toggle_saturday));

                    Boolean noneIsChecked = !togSunday.isChecked() && !togMonday.isChecked() && !togTuesday.isChecked()
                            && !togWednesday.isChecked() && !togThursday.isChecked() && !togFriday.isChecked() && !togSaturday.isChecked();

                    if (noneIsChecked) // if none is checked now, set repeat to disabled
                        toggleRepeat.setChecked(false);
                }

            }
        };

        togSunday.setOnCheckedChangeListener(toggleButtonClickhandler);
        togMonday.setOnCheckedChangeListener(toggleButtonClickhandler);
        togTuesday.setOnCheckedChangeListener(toggleButtonClickhandler);
        togWednesday.setOnCheckedChangeListener(toggleButtonClickhandler);
        togThursday.setOnCheckedChangeListener(toggleButtonClickhandler);
        togFriday.setOnCheckedChangeListener(toggleButtonClickhandler);
        togSaturday.setOnCheckedChangeListener(toggleButtonClickhandler);
    }

    public static void enableToggleRepeat(View dialog) {
        ToggleButton togSunday = ((ToggleButton)dialog.findViewById(R.id.toggle_sunday));
        ToggleButton togMonday = ((ToggleButton)dialog.findViewById(R.id.toggle_monday));
        ToggleButton togTuesday = ((ToggleButton)dialog.findViewById(R.id.toggle_tuesday));
        ToggleButton togWednesday = ((ToggleButton)dialog.findViewById(R.id.toggle_wednesday));
        ToggleButton togThursday = ((ToggleButton)dialog.findViewById(R.id.toggle_thursday));
        ToggleButton togFriday = ((ToggleButton)dialog.findViewById(R.id.toggle_friday));
        ToggleButton togSaturday = ((ToggleButton)dialog.findViewById(R.id.toggle_saturday));


        Boolean oneAlreadyChecked = togSunday.isChecked() || togMonday.isChecked() || togTuesday.isChecked()
                || togWednesday.isChecked() || togThursday.isChecked() || togFriday.isChecked() || togSaturday.isChecked();
        if (!oneAlreadyChecked) { // if one of them is already checked means this is getting automaticly enabled!
            togSunday.setChecked(false);
            togMonday.setChecked(true);
            togTuesday.setChecked(true);
            togWednesday.setChecked(true);
            togThursday.setChecked(true);
            togFriday.setChecked(true);
            togSaturday.setChecked(false);
        }
    }

    public static void disableToggleRepeat(View dialog) {
        ToggleButton togSunday = ((ToggleButton)dialog.findViewById(R.id.toggle_sunday));
        ToggleButton togMonday = ((ToggleButton)dialog.findViewById(R.id.toggle_monday));
        ToggleButton togTuesday = ((ToggleButton)dialog.findViewById(R.id.toggle_tuesday));
        ToggleButton togWednesday = ((ToggleButton)dialog.findViewById(R.id.toggle_wednesday));
        ToggleButton togThursday = ((ToggleButton)dialog.findViewById(R.id.toggle_thursday));
        ToggleButton togFriday = ((ToggleButton)dialog.findViewById(R.id.toggle_friday));
        ToggleButton togSaturday = ((ToggleButton)dialog.findViewById(R.id.toggle_saturday));

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
        alarmData.setRepeatingDay(0, ((ToggleButton) dialog.findViewById(R.id.toggle_sunday)).isChecked());
        alarmData.setRepeatingDay(1, ((ToggleButton) dialog.findViewById(R.id.toggle_monday)).isChecked());
        alarmData.setRepeatingDay(2, ((ToggleButton) dialog.findViewById(R.id.toggle_tuesday)).isChecked());
        alarmData.setRepeatingDay(3, ((ToggleButton) dialog.findViewById(R.id.toggle_wednesday)).isChecked());
        alarmData.setRepeatingDay(4, ((ToggleButton) dialog.findViewById(R.id.toggle_thursday)).isChecked());
        alarmData.setRepeatingDay(5, ((ToggleButton) dialog.findViewById(R.id.toggle_friday)).isChecked());
        alarmData.setRepeatingDay(6, ((ToggleButton) dialog.findViewById(R.id.toggle_saturday)).isChecked());

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
