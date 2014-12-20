package com.alphan.mcan.snoozecharity.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AlarmRingService extends Service {
    public AlarmRingService() {
    }

    @Override
    public void onCreate() {
        //TODO:
        throw new UnsupportedOperationException("Not yet Implemented.!");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO:
        throw new UnsupportedOperationException("Not yet Implemented.!");
        //return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        //TODO
        throw new UnsupportedOperationException("Not yet Implemented.!");
        //super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; //you can not bind to this service
    }
}
