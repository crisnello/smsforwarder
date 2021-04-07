package com.crisnello.smsforwarder.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.crisnello.smsforwarder.service.BackgroundService;

public class Restarter extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("Broadcast Listened", "Service tried to stop");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, BackgroundService.class));
        } else {
            context.startService(new Intent(context, BackgroundService.class));
        }
    }
}
