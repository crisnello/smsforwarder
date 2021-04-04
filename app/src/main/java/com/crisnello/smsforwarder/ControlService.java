package com.crisnello.smsforwarder;

import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.util.Log;

import com.crisnello.smsforwarder.listener.SmsListener;

public class ControlService {

    private Context context;

    public ControlService(Context context) {
        this.context = context;
    }

    public final String TAG = "ControlService";

    private SmsListener smsListener;

    public void register(){
        if(smsListener == null) {
            Log.e(TAG, "--> register()");
            smsListener = new SmsListener();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
            context.registerReceiver(smsListener, intentFilter);

        }
    }

    public void unregister(){
        try {
            context.unregisterReceiver(smsListener);
            smsListener = null;
        } catch (Exception ignored) {
            smsListener = null;
            ignored.printStackTrace();
        }
    }

    public void stopService(){
        this.unregister();
        ComponentName comp = new ComponentName(context,"com.crisnello.smsforwarder.listener.SmsListener");
        PackageManager pkg = context.getPackageManager();
        pkg.setComponentEnabledSetting(comp,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);
    }

}
