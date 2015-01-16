package com.alphan.mcan.snoozecharity.views;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
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

                //adjust font of the clock
                Typeface face = Typeface.createFromAsset(mContext.getAssets(), "fonts/clock.ttf");
                setTypeface(face);

                // create string for 12h
                String timeText;
                if (is24h) {
                    setText(String.format("%02d:%02d", hour, minute));
                } else {
                    final Boolean isPM = (hour > 12);
                    if (hour == 0)
                        hour = 12;
                    final String format12H = String.format("%02d:%02d", (isPM) ? (hour - 12) : hour, minute);
                    final String amPm = (isPM ? mContext.getString(R.string.pm) : mContext.getString(R.string.am));
                    int n = format12H.length();
                    int m = amPm.length();

                    Spannable span = new SpannableString(format12H + " " +  amPm);
                    //Big font till you find `\n`
                    span.setSpan(new RelativeSizeSpan(1.0f), 0, n, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    //Small font from `\n` to the end
                    span.setSpan(new RelativeSizeSpan(0.5f), n, (n+m+1), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    if (mContext != null)
                        setText(span);
                    else
                        setText(String.format("%02d:%02d", hour, minute));
                }
                invalidate(); // update the view

                // post the next tick
                long now = SystemClock.uptimeMillis();
                long next = now + (60000 - now % 60000); // set the 60000 milliseoncds = 1 minute
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
