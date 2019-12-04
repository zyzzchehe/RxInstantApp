package com.example.broadcast_receive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class StaticRegisterReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Log.e("yazhou","intent.getAction() = "+intent.getAction());
        if(intent.getAction().equals("android.intent.action.custom.yazhou")){
            Toast.makeText(context,"receive a broadcast success",Toast.LENGTH_SHORT).show();
        }else if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            Toast.makeText(context,"receive ACTION_BOOT_COMPLETED",Toast.LENGTH_LONG).show();
        }
    }
}
