package com.crisnello.smsforwarder.util;


import android.content.Context;
import android.view.View;
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
        CustomAlert alert = new CustomAlert(context,null);
        alert.setMessage(pMsg);
        alert.show();
    }

    public void showAlertFinish(String pMsg,View.OnClickListener listener){
        CustomAlert alert = new CustomAlert(context,listener);
        alert.setMessage(pMsg);
        alert.show();
    }

}