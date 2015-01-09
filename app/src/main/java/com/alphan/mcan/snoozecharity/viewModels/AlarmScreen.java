package com.alphan.mcan.snoozecharity.viewModels;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.alphan.mcan.snoozecharity.R;
import com.alphan.mcan.snoozecharity.data.model.AlarmDataModel;
import com.alphan.mcan.snoozecharity.services.AlarmManagerHelper;
import com.alphan.mcan.snoozecharity.services.AlarmRingService;
import com.alphan.mcan.snoozecharity.views.SlideToDismissButton;

public class AlarmScreen extends Activity {
	
	public final String TAG = this.getClass().getSimpleName();
    private static final String KILL_ALARM_SCREEN = "snoozecharity.activity.action.KILL_ALARM_SCREEN";

	private WakeLock mWakeLock;
    private AlarmDataModel currentAlarm;
    private int snoozeDurationInMinutes;

    // internal broadcast receiver used to kill this activity from outside
    private final BroadcastReceiver killReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    /**
     * Helper method to creates and shows the alarm screen for the given context
     */
    public static void showAlarmScreen(Context context, long alarmID) {
        Intent alarmScreenIntent = new Intent(context, AlarmScreen.class);
        alarmScreenIntent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                            | Intent.FLAG_ACTIVITY_NO_HISTORY);
        alarmScreenIntent.putExtra(AlarmManagerHelper.ID, alarmID);
        context.startActivity(alarmScreenIntent);
    }

    /**
     * Helper method which kills the alarm screen using internal broadcast receiver
     */
    public static void dismissAlarmScreen(Context context) {
        context.sendBroadcast(new Intent(KILL_ALARM_SCREEN));
    }


    /**
     * Helper method which creates a pending intent to run this activity
     */
    public static PendingIntent getShowAlarmScreenPendingIntent(Context context, long alarmID) {
        // generate pendingIntent
        Intent alarmScreenIntent = new Intent(context, AlarmScreen.class);
        alarmScreenIntent.addFlags( Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                                  | Intent.FLAG_ACTIVITY_NO_HISTORY);
        alarmScreenIntent.putExtra(AlarmManagerHelper.ID, alarmID);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, alarmScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pIntent;
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        // get and check received alarm:
        final long alarmID = getIntent().getLongExtra(AlarmManagerHelper.ID, -1);
        currentAlarm = AlarmManagerHelper.getAlarm(this, alarmID);
        if (currentAlarm == null || currentAlarm.getId() == -1 ) {
            Log.d(TAG, "Recived null Alarm! AlarmScreen will not be shown");
            finish();
            return;
        }

        // snooze duration
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
        snoozeDurationInMinutes = Integer.parseInt(preference.getString("snooze_duration","5"));

        // setup layout
        this.setContentView(R.layout.activity_alarm_screen);
        TextView tvTitle = (TextView) findViewById(R.id.alarm_screen_title);
        tvTitle.setText(currentAlarm.getName());
		TextView tvTime = (TextView) findViewById(R.id.alarm_screen_time);
        Typeface face = Typeface.createFromAsset(getAssets(),"fonts/clock.ttf");
        tvTime.setTypeface(face);
		tvTime.setText(currentAlarm.toString());

        // dismiss button
		Button dismissButton = (Button) findViewById(R.id.alarm_screen_dismiss_button);
        dismissButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                AlarmRingService.startDismissAlarmIntent(view.getContext(), currentAlarm.getId());
                finish();
            }
        });

        // dismiss slider
        SlideToDismissButton slideDismissButton = (SlideToDismissButton) findViewById(R.id.dismiss_slide_button);
        slideDismissButton.setSlideButtonListener(new SlideToDismissButton.SlideToDismissButtonListener() {
            @Override
            public void handleSlide(View view) {
                AlarmRingService.startDismissAlarmIntent(view.getContext(), currentAlarm.getId());
                finish();
            }
        });

        // snooze button
        Button snoozeButton = (Button) findViewById(R.id.alarm_screen_snooze_button);
        Resources res = getResources();

        final int charityIndex = preference.getInt(CharityCollectionActivity.DemoObjectFragment.ARG_INDEX, 0);
        final double donationAmount = (Double.parseDouble(preference.getString("donation_snooze_amount", "20")))/100;
        String[] charitiesSnooze = res.getStringArray(R.array.charity_snooze_text);

        String snoozeWord = res.getString(R.string.snooze);
        String snoozeSubText = res.getString(R.string.snooze_button_text, String.format("%.2f", donationAmount), charitiesSnooze[charityIndex], res.getString(R.string.money_sign));
        int n = snoozeWord.length();
        int m = snoozeSubText.length();

        Spannable span = new SpannableString(snoozeWord + "\n" +  snoozeSubText);
        //Big font till you find `\n`
        span.setSpan(new RelativeSizeSpan(1.0f), 0, n, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //Small font from `\n` to the end
        span.setSpan(new RelativeSizeSpan(0.5f), n, (n+m+1), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        snoozeButton.setText(span);
        snoozeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                AlarmRingService.startSnoozeAlarmIntent(view.getContext(), currentAlarm.getId(), snoozeDurationInMinutes);
                finish();
            }
        });

        // activity kill broadcast receiver
        registerReceiver(killReceiver, new IntentFilter(KILL_ALARM_SCREEN));

	}

    @SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
        super.onResume();

        // window flags:
        Log.d(TAG, "Updating window params");
        getWindow().addFlags( WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// Acquire wakelock
		PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		if (mWakeLock == null) {
			mWakeLock = pm.newWakeLock((PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE), TAG);
		}

		if (!mWakeLock.isHeld()) {
			mWakeLock.acquire();
			Log.i(TAG, "Wakelock aquired!!");
		}
	}

    @Override
    protected void onDestroy() {
        // stop listening to kill signal
        unregisterReceiver(killReceiver);

        // release lock
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }

        // remove window flags
        getWindow().clearFlags( WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                              | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                              | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                              | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN  || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
            Log.d(TAG, "Volume down event caught!");
            AlarmRingService.changeAlarmVolumeIntent(this, currentAlarm.getId(), 0);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            Log.d(TAG, "Volume up event caught!");
            AlarmRingService.changeAlarmVolumeIntent(this, currentAlarm.getId(), 1);
            return true;
        } else
            return super.onKeyDown(keyCode, event);
    }
}
