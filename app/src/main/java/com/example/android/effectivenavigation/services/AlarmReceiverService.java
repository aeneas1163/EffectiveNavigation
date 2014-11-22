package com.example.android.effectivenavigation.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.example.android.effectivenavigation.R;

public class AlarmReceiverService extends Service {


    @Override
    public IBinder onBind(Intent intent) {
        return null; // you can not bind to this one
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        /////////////////////////////
        // received an alarm callback:
        long id = intent.getIntExtra(AlarmManagerHelper.ID, 0);
        String msg = intent.getStringExtra(AlarmManagerHelper.NAME);

        Notification n = new Notification(R.drawable.ic_launcher, msg,
                System.currentTimeMillis());
        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(), 0);

        n.setLatestEventInfo(this, "WAKEEEEE UPPPPP! :)", msg, pi);
        // TODO check user preferences
        n.defaults |= Notification.DEFAULT_VIBRATE;
        //n.sound = Uri.parse(RemindMe.getRingtone());
//      n.defaults |= Notification.DEFAULT_SOUND;
        n.flags |= Notification.FLAG_AUTO_CANCEL;

        NotificationManager nm = (NotificationManager)this.getSystemService(this.NOTIFICATION_SERVICE);
        nm.notify((int)id, n);
        ///////////////////////////////////


        AlarmManagerHelper.setAlarms(this); //this one starts the alarms
        return super.onStartCommand(intent, flags, startId);
    }

}
