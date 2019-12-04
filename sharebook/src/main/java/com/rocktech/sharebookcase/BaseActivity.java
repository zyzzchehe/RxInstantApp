package com.rocktech.sharebookcase;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by wxs on 16/8/17.
 *
 * 这个基类里面就是注册了一下receiver以及注销receiver
 * 里面有一个抽象方法,每次activity收到消息后都会调用这个抽象方法
 * 只要继承这个类的都能收到消息
 *
 *
 */
public abstract class BaseActivity extends Activity {
    private static final String TAG = "BaseActivity";
    private static final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";

    private BroadcastReceiver imReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e(TAG,"action == "+action);
            if (WebSocketService.WEBSOCKET_ACTION.equals(action)) {
                if (intent != null) {
                    String msg = intent.getStringExtra("message");
                    if (!TextUtils.isEmpty(msg)){
                        getMessage(msg);
                    }
                }
            }

            if (ACTION_BOOT.equals(action)) {
                Log.i(TAG, "=== @@@开机广播, imReceiver ACTION_BOOT, Do thing!");
                // RegisterTerminal();
                String msg = intent.getStringExtra("message");
            }
        }
    };

    protected abstract void RegisterTerminal();

    protected abstract void getMessage(String msg);

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(WebSocketService.WEBSOCKET_ACTION);
        registerReceiver(imReceiver,filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(imReceiver);
    }
}
