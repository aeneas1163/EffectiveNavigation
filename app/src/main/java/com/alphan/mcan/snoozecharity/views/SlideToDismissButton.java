package com.alphan.mcan.snoozecharity.views;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;

/**
 * Widget to be used by alarmScreen to dismiss the alarm
 * Created by mcni on 1/6/15.
 */
public class SlideToDismissButton extends SeekBar {


    private static String TAG = "SlideButton";

    private static int SLIDE_ACCEPT_PERCENTAGE_THRESHOLD = 90;
    private Drawable thumb;
    private SlideToDismissButtonListener listener;
    private Boolean swipeInProgress;

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

        if (event.getAction() == MotionEvent.ACTION_DOWN) { // button down, if we hit the thumb start swipe
            Log.d(TAG, "Action DOWN recognized!");

            if (thumb.getBounds().contains((int) event.getX(), (int) event.getY())) {
                Log.d(TAG, "Action Down within bounds! SWIPE IN PROGRESS!");
                swipeInProgress = true;
                super.onTouchEvent(event);
            } else {
                Log.d(TAG, "Action Down not within bounds! IGNORING");
                return false; // ignore if we did not hit the thumb
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) { // we are moving the swipe button
            Log.d(TAG, "Action MOVE recognized!");
            if (swipeInProgress) { // we are already swiping
                Log.d(TAG, "swipe was in progress, setting progress:");

                float posX = event.getX();
                Log.d(TAG, "event X: " +posX);
                Rect rectf = new Rect();
                this.getLocalVisibleRect(rectf); // get position of the view on screen

                float distance = posX - rectf.left;
                Log.d(TAG, "event distance: " + distance);

                if (distance < 0)
                    distance = 0;

                float ratio = distance/rectf.right;
                Log.d(TAG, "ratio: " +ratio);
                if (ratio >= 1)
                    ratio = 1;

                Log.d(TAG, "setting progress to: " + (int)(100/ratio));
                setProgress((int)(100/ratio));

                if (getProgress() > SLIDE_ACCEPT_PERCENTAGE_THRESHOLD)
                    handleSlide(this);
                setProgress(0);
                swipeInProgress = false;

            }
            else { //we are not swiping yet
                if (thumb.getBounds().contains((int) event.getX(), (int) event.getY())) { // trigger swipe when we hit the button
                    swipeInProgress = true;
                    super.onTouchEvent(event);
                }
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            Log.d(TAG, "Action UP recognized!");
            if (getProgress() > SLIDE_ACCEPT_PERCENTAGE_THRESHOLD)
                handleSlide(this);
            setProgress(0);
            swipeInProgress = false;
        }
        else {
            Log.d(TAG, "Unhandled Action recognized: " + event.getAction());
            super.onTouchEvent(event);
        }

        return true;
    }

    private void handleSlide(View view) {
        if(listener != null)
            listener.handleSlide(view);
    }

}
