package com.crisnello.smsforwarder.util;


import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.crisnello.smsforwarder.R;

/** by crisnello */

public class CustomAlert extends Dialog
{
    public CustomAlert(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_alert);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().setStatusBarColor(getContext().getResources().getColor(R.color.design_default_color_background));

        setButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        }, ButtonOptions.CLOSE);
    }

    public void setMessage(String message)
    {
        ((TextView)getWindow().findViewById(R.id.txtMessage)).setText(message);
    }

    public void setButtonListener(View.OnClickListener listener,ButtonOptions buttonOptions){
        (getWindow().findViewById(R.id.btn)).setOnClickListener(listener);

        if(buttonOptions== ButtonOptions.YesORNo)
        {
            (getWindow().findViewById(R.id.btn2)).setVisibility(View.VISIBLE);
            ((TextView)getWindow().findViewById(R.id.txtBtn2)).setText("Não");
            ((TextView)getWindow().findViewById(R.id.txtBtn)).setText("Sim");

            (getWindow().findViewById(R.id.btn2)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }else
        if(buttonOptions== ButtonOptions.ExitOrCancel)
        {
            (getWindow().findViewById(R.id.btn2)).setVisibility(View.VISIBLE);
            ((TextView)getWindow().findViewById(R.id.txtBtn)).setText("Cancelar");
            ((TextView)getWindow().findViewById(R.id.txtBtn2)).setText("Sair");

            (getWindow().findViewById(R.id.btn2)).setOnClickListener(listener);

            (getWindow().findViewById(R.id.btn)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }
    }

    public enum ButtonOptions
    {
        YesORNo,CLOSE,ExitOrCancel
    }

}