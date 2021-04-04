package com.crisnello.smsforwarder.helper;

import android.telephony.SmsManager;

/**
    helper class
 */

public class SmsHelper {


    public static boolean isValidPhoneNumber(String phoneNumber) {
        return android.util.Patterns.PHONE.matcher(phoneNumber).matches();
    }

    public static void sendDebugSms(String number, String smsBody) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(number, null, smsBody, null, null);
    }
}
