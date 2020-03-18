package com.example.runtimedemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.SeekBar;

import com.example.runtimedemo.util.Utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class MainActivity extends AppCompatActivity {
    private SeekBar seekBar;
    ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        seekBar = findViewById(R.id.seek);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
                cachedThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        int result =  Utils.controlLight(progress);
                        Log.d("yazhou","process result == "+result);
                    }
                });
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d("yazhou","onStartTrackingTouch");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d("yazhou","onStopTrackingTouch");
            }
        });
    }



}
