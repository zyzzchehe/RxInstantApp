package com.example.broadcast_receive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class DynamicRegisterActivity extends AppCompatActivity{
    DtReceiver receiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        receiver = new DtReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.custom.yazhou");
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    class DtReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("yazhou","intent.getAction() = "+intent.getAction());
            if(intent.getAction().equals("android.intent.action.custom.yazhou")){
                Toast.makeText(DynamicRegisterActivity.this,"receive the dynamic broadcast",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
