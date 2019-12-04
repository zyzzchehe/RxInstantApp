package com.example.broadcast_send;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import java.lang.reflect.Method;

public class StatusNaviBarTestActivity extends AppCompatActivity {

    private Button btSendBroadcast;
    private Button btSendBroadcast2;

    private Switch swStatus;
    private Switch swNavi;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btSendBroadcast = findViewById(R.id.bt_send_broadcast);
        btSendBroadcast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendBroadcast(new Intent("android.intent.action.custom.yazhou"));
            }
        });
        btSendBroadcast2 = findViewById(R.id.bt_send_broadcast2);
        btSendBroadcast2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendOrderedBroadcast(new Intent("android.intent.action.custom.yazhou"),null);
            }
        });
        swStatus = findViewById(R.id.sw_status);
        swNavi = findViewById(R.id.sw_navi);
        swStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    sendBroadcast(new Intent("android.receive.status.bar").putExtra("cmd","show"));
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(StatusNaviBarTestActivity.this,"show persist.sys.StatusBar = "+getProperty("persist.sys.StatusBar","1"),Toast.LENGTH_SHORT).show();
                }else{
                    sendBroadcast(new Intent("android.receive.status.bar").putExtra("cmd","close"));
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(StatusNaviBarTestActivity.this,"close persist.sys.StatusBar = "+getProperty("persist.sys.StatusBar","1"),Toast.LENGTH_SHORT).show();
                }
            }
        });

        swNavi.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    sendBroadcast(new Intent("android.receive.navigation.bar").putExtra("cmd","show"));
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(StatusNaviBarTestActivity.this,"show persist.sys.NavigationBar = "+getProperty("persist.sys.NavigationBar","1"),Toast.LENGTH_SHORT).show();
                }else{
                    sendBroadcast(new Intent("android.receive.navigation.bar").putExtra("cmd","close"));
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(StatusNaviBarTestActivity.this,"close persist.sys.NavigationBar = "+getProperty("persist.sys.NavigationBar","1"),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    String getProperty(String key, String defaultValue) {
        String value = defaultValue;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            value = (String)(get.invoke(c, key, "unknown" ));
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            return value;
        }
    }

    void setProperty(String key, String value) {
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method set = c.getMethod("set", String.class, String.class);
            set.invoke(c, key, value );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
