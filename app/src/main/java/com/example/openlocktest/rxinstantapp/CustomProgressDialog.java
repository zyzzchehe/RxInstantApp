package com.example.openlocktest.rxinstantapp;

import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;


/**
 * <p> Title: CustomProgressDialog.java </p>
 * <p> Description: </p>
 * <p> Copyright：Copyrigth (c) 2014 </p>
 * <p> Company:Monda Group </P>
 *
 * @version 1.0.0
 * @Time 2014年11月19日
 */
public class CustomProgressDialog extends Dialog {

    public CustomProgressDialog(Context context) {
        super(context, R.style.custom_dialog);
        setContentView(R.layout.progress_dialog);
        setCancelable(true);
    }

    public void setMessage(CharSequence message) {
        ((TextView) findViewById(R.id.progress_dialog_msg)).setText(message);
    }
}