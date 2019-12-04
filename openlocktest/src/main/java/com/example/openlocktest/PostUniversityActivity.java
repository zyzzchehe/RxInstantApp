package com.example.openlocktest;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.openlocktest.a1.R;

public class PostUniversityActivity extends Activity implements View.OnClickListener{
    private Button openButton1;
    private Button openButton2;
    private Button openButton3;
    private Button openButton4;
    private Button openButton5;
    private Button openButton6;
    private Button openButton7;
    private Button openButton8;
    private Locker locker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_you_dian_zhan_hui);
        locker = new Locker(this,"/dev/ttyUSB0");
        openButton1 = (Button) findViewById(R.id.btn_open1);
        openButton2 = (Button) findViewById(R.id.btn_open2);
        openButton3 = (Button) findViewById(R.id.btn_open3);
        openButton4 = (Button) findViewById(R.id.btn_open4);
        openButton5 = (Button) findViewById(R.id.btn_open5);
        openButton6 = (Button) findViewById(R.id.btn_open6);
        openButton7 = (Button) findViewById(R.id.btn_open7);
        openButton8 = (Button) findViewById(R.id.btn_open8);
        openButton1.setOnClickListener(this);
        openButton2.setOnClickListener(this);
        openButton3.setOnClickListener(this);
        openButton4.setOnClickListener(this);
        openButton5.setOnClickListener(this);
        openButton6.setOnClickListener(this);
        openButton7.setOnClickListener(this);
        openButton8.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_open1:
                try {
                    locker.openLock("0","8");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_open2:
                try {
                    locker.openLock("0","1");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_open3:
                try {
                    locker.openLock("0","2");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_open4:
                try {
                    locker.openLock("0","3");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_open5:
                try {
                    locker.openLock("0","4");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_open6:
                try {
                    locker.openLock("0","5");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_open7:
                try {
                    locker.openLock("0","6");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_open8:
                try {
                    locker.openLock("0","7");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }
}
