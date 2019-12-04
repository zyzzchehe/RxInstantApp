package com.rocktech.sharebookcase;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Admin on 2018/8/28.
 *
 */

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent alarmService = new Intent(context, AlarmService.class);
        context.startService(alarmService);
        Log.i("AlarmReceiver === ", "@@@ 启动书柜盘点的service...");
    }
}
