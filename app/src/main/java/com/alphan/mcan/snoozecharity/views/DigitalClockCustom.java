package com.alphan.mcan.snoozecharity.views;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.widget.TextView;

import com.alphan.mcan.snoozecharity.R;

import java.util.Calendar;

/**
 * Created by mcni on 1/14/15.
 *
 * Digital clock widget which allows you to change the time type between 24h/12h
 *
 */
public class DigitalClockCustom extends TextView {

    Calendar calendar;

    private Runnable ticker;

    private Handler handler;

    private boolean tickerStopped = false;

    private boolean is24h;

    private Context mContext;

    public DigitalClockCustom(Context context) {
        super(context);
        initClock(context);
        mContext = context;
    }

    public DigitalClockCustom(Context context, AttributeSet attrs) {
        super(context, attrs);
        initClock(context);
        mContext = context;
    }

    private void initClock(Context context) {
        if (calendar == null)
            calendar = Calendar.getInstance();

        is24h = android.text.format.DateFormat.is24HourFormat(context);
    }

    @Override
    protected void onAttachedToWindow() {

        tickerStopped = false;
        super.onAttachedToWindow();
        handler = new Handler();

        /**
         * requests a tick on the next hard-second boundary
         */
        ticker = new Runnable() {
            public void run() {
                // if we are not ticking
                if (tickerStopped)
                    return;

                // set time to now
                calendar.setTimeInMillis(System.currentTimeMillis());

                // get minute and hour
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                // create string for 12h
                String timeText;
                if (is24h) {
                    timeText = String.format("%02d:%02d", hour, minute);
                } else {
                    final Boolean isPM = (hour > 12);
                    if (hour == 0)
                        hour = 12;
                    final String format12H = String.format("%02d:%02d", (isPM) ? (hour - 12) : hour, minute);
                    if (mContext != null)
                        timeText = format12H + " " + (isPM ? mContext.getString(R.string.pm) : mContext.getString(R.string.am));
                    else
                        timeText = String.format("%02d:%02d", hour, minute);
                }

                setText(timeText);
                invalidate(); // update the view

                // post the next tick
                long now = SystemClock.uptimeMillis();
                long next = now + (1000 - now % 1000);
                handler.postAtTime(ticker, next);
            }
        };
        ticker.run();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        tickerStopped = true;
    }

    public void set24Hmode(Boolean enable) {
        is24h = enable;
    }

}
