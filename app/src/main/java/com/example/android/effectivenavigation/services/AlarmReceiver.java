package com.example.android.effectivenavigation.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.android.effectivenavigation.R;

public class AlarmReceiver extends BroadcastReceiver {
    public AlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        long id = intent.getLongExtra("id", 0);
        String msg = intent.getStringExtra("msg");

        //TODO: start an activity instead of a notification?
        Notification n = new Notification(R.drawable.ic_launcher, msg, //TODO: use Notification.builder
                System.currentTimeMillis());
        PendingIntent pi = PendingIntent.getActivity(context, 0, new Intent(), 0);

        n.setLatestEventInfo(context, "Remind Me", msg, pi); //TODO: use not deprecated version
        // TODO check user preferences
        n.defaults |= Notification.DEFAULT_VIBRATE;

        //TODO: acquire ring tome from app
        //n.sound = Uri.parse(RemindMe.getRingtone());

        // n.defaults |= Notification.DEFAULT_SOUND;
        n.flags |= Notification.FLAG_AUTO_CANCEL;

        NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify((int)id, n);
    }
}
