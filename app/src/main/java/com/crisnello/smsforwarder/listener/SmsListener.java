package com.crisnello.smsforwarder.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.crisnello.smsforwarder.MainActivity;

public class SmsListener extends BroadcastReceiver {

    private String msgBody;

    OnNewMessageListener onNewMessageListener;
    public SmsListener() {
    }
    public SmsListener(OnNewMessageListener onNewMessageListener) {
        this.onNewMessageListener = onNewMessageListener;
    }

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
                    }

                    if (onNewMessageListener != null){
                        onNewMessageListener.onNewMessageReceived(msg_from,msgBody);
                    }

                    //TODO remove
                    Toast.makeText(context,msg_from + " say: "+msgBody,Toast.LENGTH_SHORT).show();

                }catch(Exception e){
                    Log.d("Exception caught",e.getMessage());
                }
            }
        }
    }
}