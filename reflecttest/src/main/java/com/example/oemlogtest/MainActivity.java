package com.example.oemlogtest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setProp("persist.sys.main","yes");
        setProp("persist.sys.system","yes");
        setProp("persist.sys.radio","yes");
        setProp("persist.sys.event","yes");
        setProp("persist.sys.kernel","yes");

        setProp("persist.sys.logsize","10240");
        setProp("persist.sys.lognum","10");


        Log.d("yazhou",""+getProp("persist.sys.main","null"));
        Log.d("yazhou",""+getProp("persist.sys.system","null"));
        Log.d("yazhou",""+getProp("persist.sys.radio","null"));
        Log.d("yazhou",""+getProp("persist.sys.event","null"));
        Log.d("yazhou",""+getProp("persist.sys.kernel","null"));
        Log.d("yazhou",""+getProp("persist.sys.logsize","null"));
        Log.d("yazhou",""+getProp("persist.sys.lognum","null"));



    }

    private void setProp(String key,String value){
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method setMethod = c.getMethod("set",String.class,String.class);
            setMethod.invoke(c,key,value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getProp(String key,String defaultVal){
        String val = defaultVal;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method getMethod = c.getMethod("get",String.class,String.class);
            val = (String) getMethod.invoke(c,key,"unknown");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return val;
    }
}
