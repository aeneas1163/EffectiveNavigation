package com.alphan.mcan.snoozecharity.viewModels.mainActiviy;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
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

		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.alarm_list_item, parent, false);
		}

		final AlarmDataModel model =  getItem(position);

		TextView txtTime = (TextView) view.findViewById(R.id.alarm_item_time);
		txtTime.setText(String.format("%02d:%02d", model.getTimeHour(), model.getTimeMinute()));

		TextView txtName = (TextView) view.findViewById(R.id.alarm_item_name);
		txtName.setText(model.getName());

//        TextView donationName = (TextView) view.findViewById(R.id.alarm_donation_name);
//        donationName.setText("Alarm DB ID: " + model.getId());

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
                // update the model & database only if the current condition of the alarm is different
                if (model.isEnabled() != isChecked) {
                    model.setEnabled(isChecked);
                    AlarmManagerHelper.modifyAlarm(mContext, model);
                }
			}
		});

        btnToggle.setChecked(model.isEnabled());

		return view;
	}



	private void updateTextColor(TextView view, boolean isOn) {
		if (isOn) {
			view.setTextColor(view.getResources().getColor(R.color.android_yellow));
		} else {
			view.setTextColor(Color.GRAY);
		}
	}

}
