package com.alphan.mcan.snoozecharity.viewModels.mainActiviy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
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
		txtTime.setText(String.format("%02d : %02d", model.getTimeHour(), model.getTimeMinute()));

		TextView txtName = (TextView) view.findViewById(R.id.alarm_item_name);
		txtName.setText(model.getName() + model.getId());

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

		view.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View view) {

                AlertDialog.Builder adb=new AlertDialog.Builder(mContext);
                adb.setTitle("Delete?");
                adb.setMessage("Are you sure you want to delete " +  model.getName());
                adb.setNegativeButton("Cancel", null);
                adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        AlarmDBAssistant dbHelper = new AlarmDBAssistant(mContext);
                        remove(model);
                        dbHelper.deleteAlarm(model.getId());
                    }});
                adb.show();
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

}
