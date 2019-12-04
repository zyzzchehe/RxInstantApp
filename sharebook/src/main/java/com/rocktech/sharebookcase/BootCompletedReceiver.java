package com.rocktech.sharebookcase;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class BootCompletedReceiver extends BroadcastReceiver {

    private static final String TAG = WebSocketService.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "recevie boot completed ... ");
        //context.startService(new Intent(context, WebSocketService.class));
        //context.startActivity( new Intent(context, MainActivity.class));

        Intent it=new Intent(context,MainActivity.class);
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(it);
        Toast.makeText(context,"我自启动成功了哈",Toast.LENGTH_LONG).show();

    }

}
