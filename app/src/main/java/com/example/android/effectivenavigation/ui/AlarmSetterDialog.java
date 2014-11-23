package com.example.android.effectivenavigation.ui;

import android.app.TimePickerDialog;
import android.content.Context;

/**
 * Created by Alphan on 23-Nov-14.
 */
public class AlarmSetterDialog extends TimePickerDialog{


    public AlarmSetterDialog(Context context, OnTimeSetListener callBack, int hourOfDay, int minute, boolean is24HourView) {
        super(context, callBack, hourOfDay, minute, is24HourView);
    }

}
