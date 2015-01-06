package com.alphan.mcan.snoozecharity.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;

/**
 * Widget to be used by alarmScreen to dismiss the alarm
 * Created by mcni on 1/6/15.
 */
public class SlideToDismissButton extends SeekBar {


    private static int SLIDE_ACCEPT_PERCENTAGE_THRESHOLD = 90;
    private Drawable thumb;
    private SlideToDismissButtonListener listener;

    public interface SlideToDismissButtonListener {
        public void handleSlide(View view);
    }

    public SlideToDismissButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setSlideButtonListener(SlideToDismissButtonListener listener) {
        this.listener = listener;
    }

    @Override
    public void setThumb(Drawable thumb) {
        super.setThumb(thumb);
        this.thumb = thumb;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (thumb.getBounds().contains((int) event.getX(), (int) event.getY())) {
                super.onTouchEvent(event);
            } else
                return false;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (getProgress() > SLIDE_ACCEPT_PERCENTAGE_THRESHOLD)
                handleSlide(this);
            setProgress(0);
        } else
            super.onTouchEvent(event);

        return true;
    }

    private void handleSlide(View view) {
        if(listener != null)
            listener.handleSlide(view);
    }

}
