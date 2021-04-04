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

//    OnNewMessageListener onNewMessageListener;
//    public SmsListener() {
//    }
//    public SmsListener(OnNewMessageListener onNewMessageListener) {
//        this.onNewMessageListener = onNewMessageListener;
//    }
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
//            Bundle extras = intent.getExtras();
//            if (extras != null) {
//                Status status = (Status) extras.get(SmsRetriever.EXTRA_STATUS);
//
//                if (status != null)
//                    switch (status.getStatusCode()) {
//                        case CommonStatusCodes.SUCCESS:
//                            // Get SMS message contents
//                            String message = (String) extras.get(SmsRetriever.EXTRA_SMS_MESSAGE);
//                            // Extract one-time code from the message and complete verification
//                            // by sending the code back to your server.
//                            if (!TextUtils.isEmpty(message)) {
//                                String activationCode = null;
//                                Pattern p = Pattern.compile("your pattern like \\b\\d{4}\\b");
//                                Matcher m = p.matcher(message);
//                                if (m.find()) {
//                                    activationCode = (m.group(0));  // The matched substring
//                                }
//
//                                if (onNewMessageListener != null && !TextUtils.isEmpty(activationCode))
//                                    onNewMessageListener.onNewMessageReceived(activationCode);
//                            }
//                            break;
//                        case CommonStatusCodes.TIMEOUT:
//                            // Waiting for SMS timed out (5 minutes)
//                            // Handle the error ...
//                            break;
//                    }
//            }
//        }
//    }


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