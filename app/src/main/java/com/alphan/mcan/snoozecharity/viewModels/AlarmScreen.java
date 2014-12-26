package com.alphan.mcan.snoozecharity.viewModels;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.alphan.mcan.snoozecharity.R;
import com.alphan.mcan.snoozecharity.data.model.AlarmDataModel;
import com.alphan.mcan.snoozecharity.services.AlarmManagerHelper;

public class AlarmScreen extends Activity {
	
	public final String TAG = this.getClass().getSimpleName();

	private WakeLock mWakeLock;
	private MediaPlayer mPlayer;
    private AlarmDataModel currentAlarm;

	private static final int WAKELOCK_TIMEOUT = 60 * 1000;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        // get an check received alarm:
        long alarmID = getIntent().getLongExtra(AlarmManagerHelper.ID, -1);
        currentAlarm = AlarmManagerHelper.getAlarm(this, alarmID);
        if (currentAlarm == null || currentAlarm.getId() == -1 || !currentAlarm.isEnabled())
            return;
        String name = currentAlarm.getName();
        int timeHour = currentAlarm.getTimeHour();
        int timeMinute = currentAlarm.getTimeMinute();

        // get alarm sound
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
        String strRingtonePreference = preference.getString("ringtone_pref", "DEFAULT_SOUND");
        Uri tone = Settings.System.DEFAULT_ALARM_ALERT_URI;
        if (!strRingtonePreference.equalsIgnoreCase("DEFAULT_SOUND"))
            tone = Uri.parse( strRingtonePreference);

        // setup layout
        this.setContentView(R.layout.activity_alarm_screen);

        TextView tvName = (TextView) findViewById(R.id.alarm_screen_name);
		tvName.setText(name + " -ID: " + alarmID);

		TextView tvTime = (TextView) findViewById(R.id.alarm_screen_time);
		tvTime.setText(String.format("%02d : %02d", timeHour, timeMinute));
		
		Button dismissButton = (Button) findViewById(R.id.alarm_screen_dismiss_button);
		dismissButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
                // if it is found in DB, disable it and update db, or snooze or what ever
                if (currentAlarm != null) {
                    // if the alarm is not weekly then do not disable it
                    if (!currentAlarm.isWeekly()) {
                        currentAlarm.setEnabled(false);
                        AlarmManagerHelper.modifyAlarm(view.getContext(), currentAlarm);
                    }

                    AlarmManagerHelper.resetAlarm(view.getContext(), currentAlarm);
                }
				mPlayer.stop();

                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

                if (mWakeLock != null && mWakeLock.isHeld()) {
                    mWakeLock.release();
                }
                finish();
			}
		});


        Button snoozeButton = (Button) findViewById(R.id.alarm_screen_snooze_button);

        final int charityIndex = preference.getInt(CharityCollectionActivity.DemoObjectFragment.ARG_INDEX, 0);
        final double donationAmount = (Double.parseDouble(preference.getString("donation_snooze_amount", "20")))/100;
        Resources res = getResources();
        String[] charities = res.getStringArray(R.array.charity_array);
        snoozeButton.setText("Snooze [ " + String.format("%.2f", donationAmount) + res.getString(R.string.money_sign) + " to " + charities[charityIndex] + "]");
        snoozeButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                // if it is found in DB, disable it and update db, or snooze or what ever
                if (currentAlarm != null) {
                    // add snooze donation to charity db
                    AlarmManagerHelper.addToPendingDonation(getApplicationContext(), charityIndex, donationAmount);
                    // if the alarm is not weekly then do not disable it
                    if (!currentAlarm.isWeekly()) {
                        currentAlarm.setEnabled(false);
                        AlarmManagerHelper.modifyAlarm(getApplicationContext(), currentAlarm);
                    }
                    AlarmManagerHelper.resetAlarm(getApplicationContext(), currentAlarm);

                    // create a snoozing alarm for 1 min
                    SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(view.getContext());
                    int snooze_duration = Integer.parseInt(preference.getString("snooze_duration","5"));
                    AlarmDataModel snoozeAlarm = AlarmDataModel.createSnoozingAlarm(currentAlarm, snooze_duration, true);
                    AlarmManagerHelper.createNewAlarm(view.getContext(), snoozeAlarm);

                }
                mPlayer.stop();

                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

                if (mWakeLock != null && mWakeLock.isHeld()) {
                    mWakeLock.release();
                }
                finish();
            }
        });

		//Play alarm tone
		mPlayer = new MediaPlayer();
		try {
            if (tone != null) {
                mPlayer.setDataSource(this, tone);
				mPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
				mPlayer.setLooping(true);
				mPlayer.prepare();
				mPlayer.start();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Ensure wakelock release
		Runnable releaseWakelock = new Runnable() {

			@Override
			public void run() {
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

				if (mWakeLock != null && mWakeLock.isHeld()) {
					mWakeLock.release();
				}
			}
		};

		new Handler().postDelayed(releaseWakelock, WAKELOCK_TIMEOUT);
	}

    @Override
    protected void onStart() {
        super.onStart();
    }

    @SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();

		// Set the window to keep screen on
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

		// Acquire wakelock
		PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
		if (mWakeLock == null) {
			mWakeLock = pm.newWakeLock((PowerManager.FULL_WAKE_LOCK | PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), TAG);
		}

		if (!mWakeLock.isHeld()) {
			mWakeLock.acquire();
			Log.i(TAG, "Wakelock aquired!!");
		}

	}

	@Override
	protected void onPause() {
		super.onPause();

		if (mWakeLock != null && mWakeLock.isHeld()) {
			mWakeLock.release();
		}
	}

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
