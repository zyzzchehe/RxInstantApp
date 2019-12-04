package com.rocktech.recyclelogprint;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    public BootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // an Intent broadcast.
        String action = intent.getAction();
        if(action.equals(Intent.ACTION_BOOT_COMPLETED)){
            Intent mIntent = new Intent(context,PrintActivity.class);
            mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mIntent);
        }
    }
}
