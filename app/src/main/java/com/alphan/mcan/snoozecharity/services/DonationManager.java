package com.alphan.mcan.snoozecharity.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class DonationManager extends Service {

    public static String TAG = AlarmReceiverService.class.getSimpleName();

    public DonationManager() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO
        return super.onStartCommand(intent, flags, startId);
    }
}
