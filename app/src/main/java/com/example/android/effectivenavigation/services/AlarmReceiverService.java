package com.example.android.effectivenavigation.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.example.android.effectivenavigation.alarm.AlarmScreen;

public class AlarmReceiverService extends Service {

    public static String TAG = AlarmReceiverService.class.getSimpleName();

    @Override
    public IBinder onBind(Intent intent) {
        return null; // you can not bind to this one
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        /////////////////////////////
        // received an alarm callback:
//        long id = intent.getIntExtra(AlarmManagerHelper.ID, 0);
//        String msg = intent.getStringExtra(AlarmManagerHelper.NAME);
//
//        Notification n = new Notification(R.drawable.ic_launcher, msg,
//                System.currentTimeMillis());
//        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(), 0);
//
//        n.setLatestEventInfo(this, "WAKEEEEE UPPPPP! :)", msg, pi);
//        // TODO check user preferences
//        n.defaults |= Notification.DEFAULT_VIBRATE;
//        //n.sound = Uri.parse(RemindMe.getRingtone());
////      n.defaults |= Notification.DEFAULT_SOUND;
//        n.flags |= Notification.FLAG_AUTO_CANCEL;
//
//        NotificationManager nm = (NotificationManager)this.getSystemService(this.NOTIFICATION_SERVICE);
//        nm.notify((int)id, n);
//        ///////////////////////////////////
//
//        //TODO:
//        //AlarmDataModel newModel = //Model after snooze, time shifted 5 minutes, etc.. or we are done with it?
//        //if snoozed:
//        //AlarmManagerHelper.triggerAlarmModelUpdate(newModel);
//        //else:
//        //AlarmManagerHelper.setAlarms(this); //this one starts the alarms
//        return super.onStartCommand(intent, flags, startId);

        Intent alarmIntent = new Intent(getBaseContext(), AlarmScreen.class);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        alarmIntent.putExtras(intent);
        getApplication().startActivity(alarmIntent);

        AlarmManagerHelper.setAlarms(this);

        return super.onStartCommand(intent, flags, startId);
    }

}
