package com.alphan.mcan.snoozecharity.viewModels.mainActiviy;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;

import com.alphan.mcan.snoozecharity.viewModels.mainActiviy.AlarmSetterDialog;
import com.example.android.effectivenavigation.R;
import com.alphan.mcan.snoozecharity.data.model.AlarmDataModel;
import com.alphan.mcan.snoozecharity.services.AlarmManagerHelper;

import java.util.Calendar;

/**
 * A dummy fragment representing a section of the app, but that simply displays dummy text.
 *
 * Created by Alphan on 16-Nov-14.
 */
public class AlarmFragment extends Fragment {

    public static final String ARG_SECTION_NUMBER = "section_number";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle args = getArguments();

        View rootView = inflater.inflate(R.layout.fragment_section_alarm, container, false);

        rootView.findViewById(R.id.set_alarm_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        final Calendar c = Calendar.getInstance();
                        int hour = c.get(Calendar.HOUR_OF_DAY);
                        int minute = c.get(Calendar.MINUTE);

                        // Create a new instance of TimePickerDialog and return it
                        AlarmSetterDialog alarmSetter = new AlarmSetterDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {

                            int callCount = 0;
                            @Override
                            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                                if(callCount == 1)    // On second call
                                {
                                    AlarmDataModel alarmData = new AlarmDataModel("WAAAAKE UPPP!!", selectedHour, selectedMinute);
                                    alarmData.setEnabled(true);
                                    AlarmManagerHelper.triggerAlarmModelUpdate(getActivity(), alarmData);
                                }
                                callCount++;
                            }
                        }, hour, minute, true);
                        alarmSetter.show();
                    }
                });

        return rootView;
    }



}
