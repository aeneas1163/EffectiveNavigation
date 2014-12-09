package com.alphan.mcan.snoozecharity.viewModels.mainActiviy;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import com.alphan.mcan.snoozecharity.R;
import com.alphan.mcan.snoozecharity.data.model.AlarmDataModel;
import com.alphan.mcan.snoozecharity.data.persistence.AlarmDBAssistant;
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
		btnToggle.setChecked(model.isEnabled());
		btnToggle.setTag(Long.valueOf(model.getId()));
		btnToggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                model.setEnabled(isChecked);
                AlarmManagerHelper.createOrModifyAlarmPendingIntent(mContext, model);
			}
		});

		view.setTag(Long.valueOf(model.getId()));
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {

			}
		});

        // edit alarm dialog when the item is long clicked
		view.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View view) {

                final Dialog dialog = new Dialog(mContext);

                dialog.setContentView(R.layout.alarm_setter_dialog);

                final TimePicker tp = (TimePicker) dialog.findViewById(R.id.timePicker);

                tp.setCurrentHour(model.getTimeHour());
                tp.setCurrentMinute(model.getTimeMinute());

                tp.setIs24HourView(true);

                Button okButton = (Button) dialog.findViewById(R.id.setterOk);

                Button deleteButton = (Button) dialog.findViewById(R.id.deleteAlarm);

                deleteButton.setVisibility(Button.VISIBLE);
                deleteButton.setClickable(true);

                deleteButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        model.setEnabled(false);
                        AlarmManagerHelper.createOrModifyAlarmPendingIntent(mContext, model);
                        AlarmDBAssistant dbHelper = new AlarmDBAssistant(mContext);
                        remove(model);
                        dbHelper.deleteAlarm(model.getId());
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
                        model.setTimeHour(selectedHour);
                        model.setTimeMinute(selectedMinute);
                        model.setEnabled(true);
                        AlarmFragment.setRepeatingDays(dialog, model);
                        AlarmManagerHelper.createOrModifyAlarmPendingIntent(mContext, model);
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
                return true;
            }
        });

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
