package com.example.qxdmlogget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    static final String TAG = "Qxdmlog";
    static final String getlog = "mount -t vfat -o rw /dev/block/sda1 /storage/udisk2\n" +
            "chmod 777 /dev/ttyUSB*\n" +
            "echo -e \"AT+QCFG=\\\"dbgctl\\\",0\\r\\n \" > /dev/ttyUSB4\n" +
            "sleep 1s\n"+
            "QAndroidLog -p ttyUSB2 -b 115200 -s /storage/udisk2/qxdmlog -f /sdcard/default.cfg &";

    DataReceiver receiver = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(getAppVersionName());
        receiver = new DataReceiver();
        //监听日期变化广播，定期删除log
        //registerReceiver(receiver,new IntentFilter(Intent.ACTION_DATE_CHANGED));
        //copy cfg file to /system/bin
        new Thread(new Runnable() {
            @Override
            public void run() {
                copyFiletoSystem();
            }
        }).start();
    }

    private String getAppVersionName() {
        try {
            PackageManager manager = getPackageManager();
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            return info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "很抱歉，该应用未定义版本号";
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unregisterReceiver(receiver);
    }

    class DataReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Intent.ACTION_DATE_CHANGED)){
                deleteFile("/storage/udisk2/qxdmlog");
            }
        }
    }

    private void copyFiletoSystem() {
        InputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(new File("/sdcard/default.cfg"));
            fileInputStream = getAssets().open("default.cfg");
            byte[] bytes = new byte[1024];
            int len;
            while ((len = fileInputStream.read(bytes)) != -1){
                fileOutputStream.write(bytes,0,len);
                fileOutputStream.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(fileInputStream != null){
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(fileOutputStream != null){
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void getLog(View view) {
        if(!new File("/sdcard/default.cfg").exists()){
            Toast.makeText(this,"需要的default.cfg文件不存在，请重新启动APP",Toast.LENGTH_SHORT).show();
            return;
        }
        doShell(getlog);
    }

    private int doShell(String str) {
        Log.i(TAG, "doShell : " + str);
        int Ret = -1;
        DataOutputStream dos = null;
        try {
            Process su = Runtime.getRuntime().exec("su");
            dos = new DataOutputStream(su.getOutputStream());
            dos.writeBytes(str + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            if (su.waitFor() == 0) {
                Ret = 0;
                Toast.makeText(this,"命令执行成功，开始抓取log",Toast.LENGTH_SHORT).show();
            } else {
                Ret = -1;
                Toast.makeText(this,"命令执行失败",Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Ret = -1;
            e.printStackTrace();
        } finally {
            try {
                if (dos != null) {
                    dos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.i(TAG, "doShell Ret : " + Ret);
        return Ret;
    }
}
