package com.example.openlocktest.sendbin;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.example.openlocktest.Constant;
import com.example.openlocktest.Locker;
import com.example.openlocktest.a1.R;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends Activity {
    private EditText mEditText;
    StringBuilder stringBuilder = new StringBuilder();
    int[] tmpData = new int[64];
    private Locker locker = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RuntimeException runtimeException = new RuntimeException("zyz");
        runtimeException.fillInStackTrace();
        Log.d("zyz","zyz",runtimeException);
        setContentView(R.layout.activity_main);
        locker = new Locker(this,"");
        mEditText = (EditText) findViewById(R.id.etDisplay);
    }

    public void testSend(View view) {
        new Thread(mRunnable).start();
    }

    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            readFileByBytes();
        }
    };

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mEditText.setText(stringBuilder.toString());
        }
    };
    public  void readFileByBytes() {
        InputStream in = null;
        try {
            Log.d("zyz","以字节为单位读取文件内容，一次读一个字节：");
            // 一次读一个字节
            in = getAssets().open("update.bin");
            int totalSize = in.available();//总大小
            int pkgTmp =  totalSize%64;
            int pkgCount = pkgTmp == 0 ? totalSize/64 : totalSize/64+1;
            int tempbyte;
            int count = 0;
            int currentPkg = 0;
            while ((tempbyte = in.read()) != -1) {
                tmpData[count] = tempbyte;
                //stringBuilder.append(tmpData[count]+"——");
                count ++;
                if(count % 64 == 0 && Constant.aFlag){
                    locker.openLockForWukang(pkgCount,currentPkg,tmpData);
                    currentPkg ++;
                    tmpData = null;
                    tmpData = new int[64];
                    //stringBuilder.append("换行了 \n");
                    Log.d("zyz","您读取了64个字节了 pkgCount == "+pkgCount+"; currentPkg == "+currentPkg);
                    //mHandler.sendEmptyMessage(100);
                    count = 0;
                    Constant.aFlag = false;
                    //break;
                }
            }
            if(pkgCount > currentPkg){
                Log.d("zyz","非64的倍数出现");
                locker.openLockForWukang(pkgCount,currentPkg,tmpData);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } finally {
            try {
                if(in != null) in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void readDataFromNativeFile(){
        InputStream inputStream = null;
        try {
            inputStream = getAssets().open("update.bin");
            Log.d("zyz","inputStream.available() == "+inputStream.available());
            byte[] b = new byte[1];
            for(int i=1;i<inputStream.available();i++){
                int tmp = inputStream.read(b);
                stringBuilder.append(tmp+"");
                if(i%64 == 0){
                    Log.d("zyz","开始发送");
                    mEditText.setText(stringBuilder.toString());
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(inputStream != null) inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
