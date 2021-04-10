package com.crisnello.smsforwarder.service;


import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.crisnello.smsforwarder.Constants;
import com.crisnello.smsforwarder.MainActivity;
import com.crisnello.smsforwarder.R;
import com.crisnello.smsforwarder.SettingsActivity;
import com.crisnello.smsforwarder.helper.SmsHelper;
import com.crisnello.smsforwarder.listener.OnNewMessageListener;
import com.crisnello.smsforwarder.listener.Restarter;
import com.crisnello.smsforwarder.listener.SmsListener;
import com.crisnello.smsforwarder.util.Util;

import java.util.Timer;
import java.util.TimerTask;

public class BackgroundService extends Service implements OnNewMessageListener {
    public int counter=0;
    private static final String TAG = "BackgroundService";

    private SmsListener smsListener;



    private final IBinder mBinder = new LocalBinder();
    public class LocalBinder extends Binder {
        BackgroundService getService() {
            return BackgroundService.this;
        }
    }

    private String newtext = "SMS Forwarder Service Running";

    @Override
    public void onCreate() {
        registerReceive();

        super.onCreate();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground()
    {
        String NOTIFICATION_CHANNEL_ID = "sms.forwarder";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle(newtext)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    @Override
    public void onNewMessageReceived(String from, String msg) {
        String toNumber = hasValidPreConditions();
        if (toNumber != null) {
            SharedPreferences spStore = getSharedPreferences(Constants.spStorage, MODE_PRIVATE);
            String ass =spStore.getString(Constants.signatureKey, "");

            SmsHelper.sendDebugSms(toNumber, ass + " ("+from +") say: "+msg);
            //(new Util(this)).showToast(getString(R.string.toast_sending_sms));
        }
    }

    private String hasValidPreConditions() {

        SharedPreferences spStore = getSharedPreferences(Constants.spStorage, MODE_PRIVATE);
        String targetNumber = spStore.getString(Constants.targetNumberKey, "");

        if(TextUtils.isEmpty(targetNumber)){
            (new Util(this)).showToast("Click in Plus for set Target Number");
            return null;
        }

        if (!SmsHelper.isValidPhoneNumber(targetNumber)) {
            (new Util(this)).showToast( getString(R.string.error_invalid_phone_number));
            return null;
        }
        return targetNumber;
    }


    private void registerReceive(){
        if(smsListener == null) {
            Log.e(TAG, "--> registerReceive()");
            smsListener = new SmsListener();
            smsListener.setListener(this);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
            registerReceiver(smsListener, intentFilter);
            Log.e(TAG, "--> Service is running...");
        }
    }

    private void unregisterReceive(){
        Log.d(TAG,"--> unregisterReceive()");
        try {
            MainActivity.isService = false;
            unregisterReceiver(smsListener);
            smsListener = null;
        } catch (Exception ignored) {
            smsListener = null;
            ignored.printStackTrace();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceive();
        forceStop();
        stoptimertask();
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, Restarter.class);
        this.sendBroadcast(broadcastIntent);
    }

    public void stoptimertask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void forceStop(){
        Log.d(TAG,"--> forceStop()");
        ComponentName comp = new ComponentName(this,Constants.myBroadcastReceiver);
        PackageManager pkg = this.getPackageManager();
        pkg.setComponentEnabledSetting(comp,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);
        MainActivity.isService = false;
        Log.d(TAG,"-->  MainActivity.isService = " + MainActivity.isService );
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startTimer();
        return START_STICKY;
    }

    private Timer timer;
    private TimerTask timerTask;
    public void startTimer() {
        timer = new Timer();
        timerTask = new TimerTask() {
            public void run() {
                //Log.i("Count", "=========  "+ (counter++));
            }
        };
        timer.schedule(timerTask, 2000, 2000);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());

        PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartServicePendingIntent);

        super.onTaskRemoved(rootIntent);
    }
}
