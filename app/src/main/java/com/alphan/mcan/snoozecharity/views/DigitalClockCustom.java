package com.alphan.mcan.snoozecharity.views;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.Calendar;

/**
 * Created by mcni on 1/14/15.
 *
 * Digital clock widget which allows you to change the time type between 24h/12h
 *
 */
public class DigitalClockCustom extends TextView {

    Calendar calendar;

    private final static String mode12h = "h:mm aa";
    private final static String mode24h = "k:mm";

    private Runnable ticker;
    private Handler handler;

    private boolean tickerStopped = false;

    private boolean is24h;

    String timeFormat;

    public DigitalClockCustom(Context context) {
        super(context);
        initClock(context);
    }

    public DigitalClockCustom(Context context, AttributeSet attrs) {
        super(context, attrs);
        initClock(context);
    }

    private void initClock(Context context) {
        if (calendar == null)
            calendar = Calendar.getInstance();

        is24h = android.text.format.DateFormat.is24HourFormat(context);
        setFormat();
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
                setText(DateFormat.format(timeFormat, calendar));
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
        setFormat();
    }

    private void setFormat() {
        if (is24h)
            timeFormat = mode24h;
        else
            timeFormat = mode12h;
    }


}
