package com.rocktech.sutestdemo;

import android.app.Activity;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends Activity {

    TextView display;

    Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        display = (TextView) findViewById(R.id.tv_display);
        findViewById(R.id.bt_excu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        OutputStream os = null;
                        try {
                            Log.d("MainActivity","点击了按钮");
                            Process process = Runtime.getRuntime().exec("/system/xbin/su");
                            os = process.getOutputStream();
                            String cmd = "touch /data/1.txt " + "\n" + "exit\n";
                            os.write(cmd.getBytes());
                            os.flush();
                            int res = process.waitFor();
                            Log.d("MainActivity","res == "+res);
                            if(res == 0){
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        display.setText("执行OK");
                                    }
                                });
                            }else{
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        display.setText("执行失败");
                                    }
                                });
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }finally {
                            try {
                                if (os != null) os.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();

            }
        });
    }



}
