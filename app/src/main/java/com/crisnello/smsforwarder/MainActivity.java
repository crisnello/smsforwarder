package com.crisnello.smsforwarder;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
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
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import static java.net.Proxy.Type.HTTP;

public class MainActivity extends AppCompatActivity implements OnNewMessageListener {

    private static final String TAG = "MainActivity";

    public static boolean isService;

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

        try{
            //because  isService = false at BackrgoundService not work
            stopService(new Intent(getApplicationContext(), BackgroundService.class));
            isService = false;
        }catch(Exception e){
            e.printStackTrace();
        }


        String newString;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {newString= null;
            } else { newString= extras.getString("STRING_I_NEED"); }
        } else { newString= (String) savedInstanceState.getSerializable("STRING_I_NEED"); }
        if(newString!=null) {
            Intent sendIntent = new Intent(Intent.ACTION_VIEW);
            sendIntent.putExtra("sms_body", newString);
            sendIntent.setType("vnd.android-dir/mms-sms");
            startActivity(sendIntent);
        }

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

        FloatingActionButton fabNewSms = findViewById(R.id.fabNewSms);
        fabNewSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Message");
                FrameLayout mainLayout = (FrameLayout) findViewById(R.id.activity_main_layout);
                View viewInflated = LayoutInflater.from(MainActivity.this).inflate(R.layout.input_value, mainLayout , false);
                final EditText input = (EditText) viewInflated.findViewById(R.id.input);
                builder.setView(viewInflated);
                builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        String msg = input.getText().toString();
                        String target = hasValidPreConditions();
                        if (target != null){
                            SmsHelper.sendDebugSms(target, msg);
                            (new Util(MainActivity.this)).showToast("SMS Sent with success!");
                        }else{
                            (new Util(MainActivity.this)).showAlert("Set target number fisrt");
                        }
//                          Intent sendIntent = new Intent(Intent.ACTION_SEND);
//                          sendIntent.putExtra("STRING_I_NEED", target + " - " + msg);
//                          startActivity(sendIntent);
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();

            }
        });
    }

    public void launchSmsService() {
        if(!isService) {
            tvStatus.setText("Service is running...");
            isService = true;
            startService(new Intent(getApplicationContext(), BackgroundService.class));
        }else{
            Log.d(TAG,"--> launchSmsService isService is true");
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
            //(new Util(this)).showAlert( getString(R.string.error_invalid_phone_number));
            tvTarget.setText("Service just work with target number");
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
    protected void onPause() {
        Log.d(TAG, "--> onPause()");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "--> onStop()");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "--> onDestroy()");
        super.onDestroy();

    }

    private void registerReceive(){
        Log.d(TAG, "--> registerReceive()");
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

    private void sendNotification(String message123) {

        Intent intent;
        intent = new Intent(this, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);


        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            long[] pattern = {500, 500, 500, 500, 500, 500, 500, 500, 500};
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setLargeIcon(largeIcon)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(message123)
                    .setAutoCancel(true)
                    .setSound(alarmSound)
                    .setVibrate(pattern)
                    .setContentIntent(pendingIntent);
            notificationManager.notify(0, notificationBuilder.build());
        } else {
//            Logger.e("**android.os.Build.VERSION.SDK_INT : " + android.os.Build.VERSION.SDK_INT);
            try {
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);
                long[] pattern = {500, 500, 500, 500, 500, 500, 500, 500, 500};
                Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);

                String channelId = "android_channel_id";
                String channelDescription = "Default Channel";
                NotificationCompat.Builder notificationBuilder = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel(channelId, channelDescription, NotificationManager.IMPORTANCE_DEFAULT);
                    notificationManager.createNotificationChannel(channel);
                    notificationBuilder = new NotificationCompat.Builder(this,channelId)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setLargeIcon(largeIcon)
                            .setContentTitle(getString(R.string.local_service_started))
                            .setContentText(message123)
                            .setAutoCancel(true)
                            .setColor(Color.parseColor("#d7ab0f"))
                            .setSound(alarmSound)
                            .setVibrate(pattern)
                            .setContentIntent(pendingIntent);
                }else{
                    notificationBuilder = new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setLargeIcon(largeIcon)
                            .setContentTitle(getString(R.string.local_service_started))
                            .setContentText(message123)
                            .setAutoCancel(true)
                            .setColor(Color.parseColor("#d7ab0f"))
                            .setSound(alarmSound)
                            .setVibrate(pattern)
                            .setContentIntent(pendingIntent);
                }
                notificationManager.notify(0, notificationBuilder.build());

            }catch (Exception e){
                Log.e(TAG,e.getMessage(),e);
            }
        }
    }
    
}