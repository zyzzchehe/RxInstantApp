package com.example.runtimedemo.util;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;

public class Utils {

    public static int controlLight(int bit){
        OutputStream outputStream = null;
        try {
            String cmd = "echo "+bit+" > /sys/class/backlight/pwm-backlight.0/brightness\n" +
                    "sync\n" +
                    "exit\n";
            Log.d("yazhou","cmd = "+cmd);
            Process process = Runtime.getRuntime().exec("su");
            outputStream = process.getOutputStream();
            outputStream.write(cmd.getBytes());
            outputStream.flush();
            Log.d("yazhou","process.waitFor()"+process.waitFor());
            return process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return -1;
        }finally {
            try {
                if(outputStream != null) {
                    outputStream.close();
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
