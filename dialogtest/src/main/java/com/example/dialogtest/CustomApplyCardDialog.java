package com.example.dialogtest;

import android.app.Dialog;
import android.content.Context;
import android.os.CountDownTimer;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


/**
 * Created by 33400 on 2018-11-22.
 */

public class CustomApplyCardDialog extends Dialog {

    private static int mTheme = R.style.CustomDialog;
    private Window window = null;
    private Context mContext;
    private ImageView dialog_productintroduction_image;
    private  CountDownTimer mCountDownTimer;

    public CustomApplyCardDialog(@NonNull Context context) {
        super(context,mTheme);
        setContentView(R.layout.dialog_applycard_custom);
        dialog_productintroduction_image = (ImageView) findViewById(R.id.card_image);
        dialog_productintroduction_image.setImageDrawable(context.getDrawable(R.drawable.apply_card));
    }


    public void showDialog(){
//        presenter.getQrcodeMessage();//请求二维码
//        pro_loading.setVisibility(View.VISIBLE);
//        dialog_text_loading.setText("加载中，请稍等！！！");
        mCountDownTimer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }
            @Override
            public void onFinish() {
                dismiss();
                mCountDownTimer.cancel();
            }
        };
        mCountDownTimer.start();
        show();
    }


    public CustomApplyCardDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected CustomApplyCardDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }


}
