package com.crisnello.smsforwarder;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.crisnello.smsforwarder.listener.OnNewMessageListener;
import com.crisnello.smsforwarder.listener.SmsListener;
import com.crisnello.smsforwarder.util.Util;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private TextView tvStatus;
    private TextView tvAss;

    private int countRequestPermission;

    private static final String TAG = "MainActivity";

    String permission = Manifest.permission.RECEIVE_SMS;

    private int requestPermissionCode = 0;
    
    private SmsListener smsListener;

    private class SendSms implements OnNewMessageListener{
        @Override
        public void onNewMessageReceived(String from, String msg) {

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.e(TAG,"--> onCreate()");
        countRequestPermission = 0;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvStatus = (TextView) findViewById(R.id.tvStatus);
        tvAss = (TextView) findViewById(R.id.tvAss);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                forceStop();
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG,"--> onResume()");
        SharedPreferences spStore = getSharedPreferences(Constants.spStorage, MODE_PRIVATE);
        tvAss.setText(spStore.getString(Constants.signatureKey, ""));
        startReply();
    }

    private void startReply(){
        SharedPreferences spStore = getSharedPreferences(Constants.spStorage, MODE_PRIVATE);
        String reply = spStore.getString(Constants.replyKey, Constants.replyKey);
        Log.e(TAG,"--> startReply() : reply is " + reply);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if(reply.equals(Constants.replyKey)) validateSmsPermission();
            else showMsg(reply);
        else {
            if(reply.equals(Constants.replyKey)) registerReceive();
            else showMsg(reply);
        }
    }

    private void showMsg(String msg){
        (new Util(this)).showToast(msg);
    }



    @Override
    public void onDestroy() {
        Log.e(TAG, "--> onDestroy()");
        super.onDestroy();

    }


    private void registerReceive(){
        if(smsListener == null) {
            Log.e(TAG, "--> registerReceive()");
            smsListener = new SmsListener();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
            registerReceiver(smsListener, intentFilter);
            tvStatus.setText("Service is running...");
        }
    }

    private void unregisterReceive(){
        Log.e(TAG,"--> unregisterReceive() : in Samsung s20 Plus not work, just forcestop");
        try {
            unregisterReceiver(smsListener);
            smsListener = null;
        } catch (Exception ignored) {
            smsListener = null;
            ignored.printStackTrace();
        }
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        Log.e(TAG,"--> onPause()");
//    }

    private void validateSmsPermission() {
        Log.e(TAG,"--> validateSmsPermission() - countRequestPermission : "+countRequestPermission);
        if(countRequestPermission > 1 && !isSmsPermission()){ //one Chance for two open's
            //finish(); //put dialog information about just one more try and finish in close dialog
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
        String[] permission_list = new String[1];
        permission_list[0] = permission;
        ActivityCompat.requestPermissions(this, permission_list, requestPermissionCode);
    }

    private boolean isSmsPermission(){
        int grant = ContextCompat.checkSelfPermission(this, permission);
        return grant == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try {
            if (requestCode == requestPermissionCode && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                registerReceive();
            } else {
                showMsg("Permission is need for read SMS");
            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }

//    @Override
//    public void onStop() {
//        super.onStop();
//        Log.e(TAG,"--> onStop()");
//        forceStop();
//    }

    private void forceStop(){
        Log.e(TAG,"--> forceStop()");
        unregisterReceive();
        ComponentName comp = new ComponentName(this,"com.crisnello.smsforwarder.listener.SmsListener");
        PackageManager pkg = this.getPackageManager();
        pkg.setComponentEnabledSetting(comp,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);
        tvStatus.setText("Service is stop.");
    }
    
}