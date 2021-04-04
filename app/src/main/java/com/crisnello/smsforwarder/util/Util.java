package com.crisnello.smsforwarder.util;


import android.content.Context;
import android.widget.Toast;

public class Util {

    private Context context;

    public Util(Context pContext){
        context = pContext;

    }

    public void showToast(String pMsg){
        Toast.makeText(context, pMsg, Toast.LENGTH_LONG).show();
    }


    public void showAlert(String pMsg){
        CustomAlert alert = new CustomAlert(context);
        alert.setMessage(pMsg);
        alert.show();
    }

}