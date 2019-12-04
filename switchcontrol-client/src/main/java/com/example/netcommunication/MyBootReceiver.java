package com.example.netcommunication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

/*装在主工控机上*/
public class MyBootReceiver extends BroadcastReceiver {
    private BufferedWriter mBufferedWriter = null;
    private Socket clientSocket;

    private FileOutputStream fileOutputStream = null;

    private Context mContext;

    int count = 0;
    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            Toast.makeText(context,"接收到开机广播",Toast.LENGTH_LONG).show();  //Toast.LENGTH_LONG
            OutputStreamWriter outputStreamWriter = null;
            BufferedWriter bufferedWriter = null;
            try {
                fileOutputStream = context.openFileOutput("testSwitch.txt",Context.MODE_APPEND);
                outputStreamWriter = new OutputStreamWriter(fileOutputStream);
                bufferedWriter = new BufferedWriter(outputStreamWriter);

                bufferedWriter.write("reboot again");
                bufferedWriter.write("\n");
                bufferedWriter.flush();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    fileOutputStream.close();
                    outputStreamWriter.close();
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            initSocket();
        }
    }

    /*获取mac地址*/
    private String getMacAddress(){
        StringBuilder stringBuilder = new StringBuilder();
        String tmp = do_exec("cat /sys/class/net/eth0/address");
        String[] arr = tmp.split(":");
        String str = stringBuilder.append(arr[arr.length-3]).append(arr[arr.length-2].charAt(0)).append(arr[arr.length-1]).toString();
        return "RSC"+str;
    }

    private String do_exec(String cmd) {
        String s = "";
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                s += line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }

    private void initSocket() {
        new Thread( new Runnable() {
            @Override
            public void run() {
                FileInputStream fileInputStream = null;
                InputStreamReader inputStreamReader = null;
                BufferedReader bufferedReader = null;
                try {
                    fileInputStream = mContext.openFileInput("testSwitch.txt");
                    inputStreamReader = new InputStreamReader(fileInputStream);
                    bufferedReader = new BufferedReader(inputStreamReader);
                    while (!TextUtils.isEmpty(bufferedReader.readLine())){
                        count ++;
                    }
                    //在子线程中初始化Socket对象
                    clientSocket = new Socket("192.168.1.187",8888);
                    mBufferedWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                    //mBufferedWriter.write("RSC65987-20");
                    mBufferedWriter.write(getMacAddress()+"-"+count);
                    mBufferedWriter.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    try {
                        if(mBufferedWriter != null) mBufferedWriter.close();
                        if(fileInputStream != null) fileInputStream.close();
                        if(inputStreamReader != null) inputStreamReader.close();
                        if(bufferedReader != null) bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
