package com.alphan.mcan.snoozecharity.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AlarmRingService extends Service {
    public AlarmRingService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
