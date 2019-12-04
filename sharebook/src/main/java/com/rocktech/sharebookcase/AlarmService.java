package com.rocktech.sharebookcase;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by Admin on 2018/8/24.
 *
 */

public class AlarmService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //发送书柜盘点的广播消息
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("com.rocktech.sharebookcase.inventory");
        sendBroadcast(broadcastIntent);
        Log.i("AlarmService === ", "@@@ 发送书柜盘点的广播消息...");
        return super.onStartCommand(intent, flags, startId);
    }

}
