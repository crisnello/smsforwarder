package com.crisnello.smsforwarder;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.crisnello.smsforwarder.helper.SmsHelper;
import com.crisnello.smsforwarder.listener.OnNewMessageListener;
import com.crisnello.smsforwarder.listener.SmsListener;
import com.crisnello.smsforwarder.service.BackgroundService;
import com.crisnello.smsforwarder.util.Util;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements OnNewMessageListener {

    private static final String TAG = "MainActivity";

    public static boolean isService = false;

    private int requestPermissionCode = 0;
    private TextView tvStatus;
    private TextView tvAss;
    private TextView tvTarget;
    private RelativeLayout btnHideWindow;
    private int countRequestPermission;
    private SmsListener smsListener;

    @Override
    public void onNewMessageReceived(String from, String msg) {
        String toNumber = hasValidPreConditions();
        if (toNumber != null) {
            SharedPreferences spStore = getSharedPreferences(Constants.spStorage, MODE_PRIVATE);
            String ass =spStore.getString(Constants.signatureKey, "");

            SmsHelper.sendDebugSms(toNumber, ass + " ("+from +") say: "+msg);
            //TODO Debug Mod
            //(new Util(this)).showToast(getString(R.string.toast_sending_sms));
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG,"--> onCreate()");
        countRequestPermission = 0;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvStatus = (TextView) findViewById(R.id.tvStatus);
        tvAss = (TextView) findViewById(R.id.tvAss);
        tvTarget = (TextView) findViewById(R.id.tvTarget);

        btnHideWindow = (RelativeLayout) findViewById(R.id.btn_silent_mode);
        btnHideWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runBackgroung();
            }
        });
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //forceStop();
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            }
        });

    }

    public void launchSmsService() {
        if(!isService) {

            tvStatus.setText("Service is running...");

            isService = true;

            startService(new Intent(getApplicationContext(), BackgroundService.class));
        }
    }

    private void runBackgroung(){
        if(isService){
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
        }else{
            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
        }
    }


    private String hasValidPreConditions() {

        SharedPreferences spStore = getSharedPreferences(Constants.spStorage, MODE_PRIVATE);
        String targetNumber = spStore.getString(Constants.targetNumberKey, "");

        if(TextUtils.isEmpty(targetNumber)){
            (new Util(this)).showToast("Click in Configuration for set Target Number");
            return null;
        }

        if (!SmsHelper.isValidPhoneNumber(targetNumber)) {
            (new Util(this)).showToast( getString(R.string.error_invalid_phone_number));
            return null;
        }
        return targetNumber;
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"--> onResume()");
        SharedPreferences spStore = getSharedPreferences(Constants.spStorage, MODE_PRIVATE);
        tvAss.setText(spStore.getString(Constants.signatureKey, ""));
        String strTarget = spStore.getString(Constants.targetNumberKey,"");
        if(TextUtils.isEmpty(strTarget) || !SmsHelper.isValidPhoneNumber(strTarget)){
            btnHideWindow.setVisibility(View.GONE);
            (new Util(this)).showAlert( getString(R.string.error_invalid_phone_number));
        }else{
            tvTarget.setText("Target : "+strTarget);
            startReply();
        }



    }

    private void startReply(){
        SharedPreferences spStore = getSharedPreferences(Constants.spStorage, MODE_PRIVATE);
        String reply = spStore.getString(Constants.replyKey, Constants.replyKey);
        Log.d(TAG,"--> startReply() : reply is " + reply);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if(reply.equals(Constants.replyKey)) {
                btnHideWindow.setVisibility(View.VISIBLE);
                validateSmsPermission();
            }else{
                btnHideWindow.setVisibility(View.GONE);
                (new Util(this)).showAlert(getString(R.string.alert_reply_off));
            }
        else {
            if(reply.equals(Constants.replyKey)){
                btnHideWindow.setVisibility(View.VISIBLE);
                registerReceive();
            }
            else showMsg(reply);
        }
    }

    private void showMsg(String msg){
        (new Util(this)).showToast(msg);
    }



    @Override
    public void onDestroy() {
        Log.d(TAG, "--> onDestroy()");
        super.onDestroy();

    }


    private void registerReceive(){
        launchSmsService();
    }


    private void validateSmsPermission() {
        Log.d(TAG,"--> validateSmsPermission() - countRequestPermission : "+countRequestPermission);
        if(countRequestPermission > 1 && !isSmsPermission()){ //one Chance for two open's
            Log.e(TAG,"--> information about just one more try and finish in close dialog");
            (new Util(this)).showAlertFinish("Just two times is allowed",new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }else{
            countRequestPermission++;
            if (!isSmsPermission()) { requetPermission();
            }else{ registerReceive(); }
        }
    }

    private void requetPermission(){
        String[] permission_list = new String[]{Manifest.permission.SEND_SMS,Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS};
        ActivityCompat.requestPermissions(this, permission_list, requestPermissionCode);
    }

    private boolean isSmsPermission(){
        return ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try {
            if (requestCode == requestPermissionCode && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                registerReceive();
            } else {
                showMsg("Permission is need for read SMS");
            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }


//    private void forceStop(){
//        Log.d(TAG,"--> forceStop()");
//        unregisterReceive();
//        ComponentName comp = new ComponentName(this,Constants.myBroadcastReceiver);
//        PackageManager pkg = this.getPackageManager();
//        pkg.setComponentEnabledSetting(comp,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);
//        tvStatus.setText("Service is stop.");
//    }
    
}