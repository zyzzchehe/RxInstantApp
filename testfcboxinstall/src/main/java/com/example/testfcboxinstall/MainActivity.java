package com.example.testfcboxinstall;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity {

    private Switch mSwitch;
    private boolean isSilent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSwitch = (Switch) findViewById(R.id.sw_silent_install);
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isSilent = b;
            }
        });
    }

    public void doClick(View view) {
        Intent intent = new Intent("android.intent.action.ACTION_SILENCE_INSTALL");
        // com.fcbox.locker.extra.APK_PATH 要升级的apk文件的绝对路径
        intent.putExtra("com.fcbox.locker.extra.APK_PATH", "/sdcard/test.apk");
        // com.fcbox.locker.extra.SILENT 是否静默安装 true - 静默安装，不需要任何提示  false -安装时显示提示界面，界面无要求，只要提示就ok
        intent.putExtra("com.fcbox.locker.extra.SILENT", isSilent);
        // com.fcbox.locker.extra.PKG_NAME * 需要启动组件的包名 可能为空
        intent.putExtra("com.fcbox.locker.extra.PKG_NAME", "com.example.mwshtest");
        //com.fcbox.locker.extra.CLS_NAME * 需要启动组件的类名 可能为空
        intent.putExtra("com.fcbox.locker.extra.CLS_NAME", "com.example.mwshtest.MainActivity");
        sendBroadcast(intent);
    }
}
