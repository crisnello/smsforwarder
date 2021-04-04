package com.crisnello.smsforwarder.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.crisnello.smsforwarder.MainActivity;

public class SmsListener extends BroadcastReceiver {

    private String msgBody;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
            Bundle bundle = intent.getExtras();
            SmsMessage[] msgs = null;
            String msg_from = "";
            if (bundle != null){

                try{
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];
                    for(int i=0; i<msgs.length; i++){
                        msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i], bundle.getString("format"));
                        msg_from = msgs[i].getOriginatingAddress();
                        msgBody = msgs[i].getMessageBody();
                        MainActivity.handleMessage(msgBody);
                    }

                    Toast.makeText(context,msg_from + " say: "+msgBody,Toast.LENGTH_SHORT).show();

                }catch(Exception e){
                    Log.d("Exception caught",e.getMessage());
                }
            }
        }
    }
}