package com.alphan.mcan.snoozecharity.viewModels.mainActiviy;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import com.alphan.mcan.snoozecharity.R;
import com.alphan.mcan.snoozecharity.data.model.AlarmDataModel;
import com.alphan.mcan.snoozecharity.services.AlarmManagerHelper;

import java.util.List;

public class AlarmListAdapter extends ArrayAdapter<AlarmDataModel> {

	private Context mContext;

	public AlarmListAdapter(Context context, List<AlarmDataModel> alarms) {
        super(context, R.layout.alarm_list_item, alarms);
		mContext = context;
	}

	@Override
	public View getView(final int position, View view, ViewGroup parent) {
//        Log.e("view","entered pos: " + position);

		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.alarm_list_item, parent, false);
		}

		final AlarmDataModel model =  getItem(position);

		TextView txtTime = (TextView) view.findViewById(R.id.alarm_item_time);
		txtTime.setText(String.format("%02d:%02d", model.getTimeHour(), model.getTimeMinute()));

		TextView txtName = (TextView) view.findViewById(R.id.alarm_item_name);
		txtName.setText(model.getName());

        TextView donationName = (TextView) view.findViewById(R.id.alarm_donation_name);
        donationName.setText("Alarm DB ID: " + model.getId());

		updateTextColor((TextView) view.findViewById(R.id.alarm_item_sunday), model.getRepeatingDay(AlarmDataModel.SUN));
		updateTextColor((TextView) view.findViewById(R.id.alarm_item_monday), model.getRepeatingDay(AlarmDataModel.MON));
		updateTextColor((TextView) view.findViewById(R.id.alarm_item_tuesday), model.getRepeatingDay(AlarmDataModel.TUE));
		updateTextColor((TextView) view.findViewById(R.id.alarm_item_wednesday), model.getRepeatingDay(AlarmDataModel.WED));
		updateTextColor((TextView) view.findViewById(R.id.alarm_item_thursday), model.getRepeatingDay(AlarmDataModel.THU));
		updateTextColor((TextView) view.findViewById(R.id.alarm_item_friday), model.getRepeatingDay(AlarmDataModel.FRI));
		updateTextColor((TextView) view.findViewById(R.id.alarm_item_saturday), model.getRepeatingDay(AlarmDataModel.SAT));

		ToggleButton btnToggle = (ToggleButton) view.findViewById(R.id.alarm_item_toggle);
		btnToggle.setTag(model.getId());
		btnToggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                Log.e("togglebutton","entered pos: " + position + "with checked: "+ isChecked);
                model.setEnabled(isChecked);
                AlarmManagerHelper.modifyAlarm(mContext, model);
//                Log.e("togglebutton","exited pos: " + position);
			}
		});

        btnToggle.setChecked(model.isEnabled());


//		view.setTag(model.getId());
//		view.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View view) {
//
//			}
//		});

        // edit alarm dialog when the item is long clicked
		view.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {

                final Dialog dialog = new Dialog(mContext);
                dialog.setCanceledOnTouchOutside(true);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.alarm_setter_dialog);

                final TextView alarm_name = (TextView) dialog.findViewById(R.id.alarm_name);

                alarm_name.setText(model.getName());

                final TimePicker tp = (TimePicker) dialog.findViewById(R.id.timePicker);
                final SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(mContext);
                tp.setIs24HourView(preference.getBoolean("24hour_option", true));
                tp.setCurrentHour(model.getTimeHour());
                tp.setCurrentMinute(model.getTimeMinute());

                Button okButton = (Button) dialog.findViewById(R.id.setterOk);

                ImageButton deleteButton = (ImageButton) dialog.findViewById(R.id.deleteAlarm);

                deleteButton.setVisibility(Button.VISIBLE);
                deleteButton.setClickable(true);

                deleteButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlarmManagerHelper.deleteAlarm(mContext, model);
                        remove(model);
                        dialog.dismiss();
                    }
                });

                CheckBox satView = (CheckBox) dialog.findViewById(R.id.alarm_repeat_check);

                // if the alarm is repeating we should initialize the toggle buttons appropriately
                if (model.isWeekly()) {
                    satView.setChecked(true);
                    AlarmFragment.enableToggleRepeat(dialog);
                    AlarmListAdapter.updateRepeatingDays(dialog, model);
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

                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int selectedHour = tp.getCurrentHour();
                        int selectedMinute = tp.getCurrentMinute();
                        model.setName(alarm_name.getText().toString());
                        model.setTimeHour(selectedHour);
                        model.setTimeMinute(selectedMinute);
                        model.setEnabled(true);
                        AlarmFragment.setRepeatingDays(dialog, model);
                        AlarmManagerHelper.modifyAlarm(mContext, model);
                        notifyDataSetChanged();
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

                dialog.setTitle("Edit Alarm");

                dialog.show();

//                AlertDialog.Builder adb=new AlertDialog.Builder(mContext);
//                adb.setTitle("Delete?");
//                adb.setMessage("Are you sure you want to delete " +  model.getName() + "?");
//                adb.setNegativeButton("Cancel", null);
//                adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        model.setEnabled(false);
//                        AlarmManagerHelper.createOrModifyAlarmPendingIntent(mContext, model);
//                        AlarmDBAssistant dbHelper = new AlarmDBAssistant(mContext);
//                        remove(model);
//                        dbHelper.deleteAlarm(model.getId());
//                    }});
//                adb.show();
            }
        });

//        Log.e("view","exited pos: " + position);
		return view;
	}

	private void updateTextColor(TextView view, boolean isOn) {
		if (isOn) {
			view.setTextColor(Color.GREEN);
		} else {
			view.setTextColor(Color.GRAY);
		}
	}

    public static void updateRepeatingDays(Dialog dialog, AlarmDataModel alarmData) {
        ((ToggleButton)dialog.findViewById(R.id.toggle_sunday)).setChecked(alarmData.getRepeatingDay(0));
        ((ToggleButton)dialog.findViewById(R.id.toggle_monday)).setChecked(alarmData.getRepeatingDay(1));
        ((ToggleButton)dialog.findViewById(R.id.toggle_tuesday)).setChecked(alarmData.getRepeatingDay(2));
        ((ToggleButton)dialog.findViewById(R.id.toggle_wednesday)).setChecked(alarmData.getRepeatingDay(3));
        ((ToggleButton)dialog.findViewById(R.id.toggle_thursday)).setChecked(alarmData.getRepeatingDay(4));
        ((ToggleButton)dialog.findViewById(R.id.toggle_friday)).setChecked(alarmData.getRepeatingDay(5));
        ((ToggleButton)dialog.findViewById(R.id.toggle_saturday)).setChecked(alarmData.getRepeatingDay(6));
    }

}
