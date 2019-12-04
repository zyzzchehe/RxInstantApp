package com.rocktech.macaddressdemo;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;


public class Constants {
    public static String CAN_DEVICE = "rocktech_panda";
    public static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    // ping 网络地址
    public static final String INTERNET_ADDRESS = /*"192.168.1.1"*/"www.baidu.com";
    public static final String MOBILEPINGADDRESS = "183.232.231.172";//www.baidu.com

    public static boolean recordResult = true;
    public static String recordResultOfItem = null;

    // 压力测试，选项
    public static final String[] PRESSURE_LISTS = {
        "低负载",
        //"较低负载",
        "中负载",
        //"较高负载",
        "高负载",
    };
    // 压力测试，线程数目
    public static final int[] PRESSURE_VALUES = {
        1,
        2,
        3,
        //4,
        //5,
    };

    //测试模式
    public enum TestMode {
        MODE_MANUAL,	//手动测试
        MODE_AUTO,		//自动测试，测试一轮
        MODE_AUTO_LOOP 	//自动测试，循环测试
    }

    //测试结果
    public enum TestResult {
        RESULT_PASSED,	//通过
        RESULT_FAILED	//未通过
    }

    //测试状态
    public enum TestState {
        STATE_UNTEST,	//未测试
        STATE_TESTING,	//测试中
        STATE_PASSED,	//测试通过
        STATE_FAILED	//测试未通过
    }

    //测试项目
    public enum TestItem {
        ITEM_MIN,
        ITEM_WIFI,			//WIFI
        ITEM_USB,			//USB
        ITEM_ETHERNET,		//ETHERNET
        ITEM_SERIALPORT,	//SERIALPORT
        ITEM_TEST422,
        ITEM_CAN,			//CAN
        ITEM_MOBILE,		//3G
        ITEM_SDCARD,		//SDCARD
        ITEM_GPIO,			//GPIO
        //		ITEM_VIDEO_IN,		//VIDEO_IN
        //		ITEM_VIDEO_OUT,		//VIDEO_OUT
        //		ITEM_CAMERA,		//CAMERA
        ITEM_RTC,			//RTC
        ITEM_SYSINFO,
        ITME_MAX
    }

    /**
     * add by yazhou for display host number 20180110
     */
    public static String do_exec(String cmd) {
        String s = "";
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            is = p.getInputStream();
            isr = new InputStreamReader(is);
            br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
                s += line;
                s += "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(is != null)is.close();
                if(isr != null) isr.close();
                if(br != null) br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return s;
    }
    
    //add by yazhou delete file
    public static void deleteFileEx(String data) {
        String cmd = "mount -o remount,rw /system" + "\n"
                + data + "\n"
                + "mount -o remount,ro /system" + "\n"
                + "exit\n";
        OutputStream os = null;
        try {
            Process p = Runtime.getRuntime().exec("/system/xbin/su");
            os = p.getOutputStream();
            os.write(cmd.getBytes());
            os.flush();
            if (p.waitFor() != 0) {
                throw new SecurityException();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if(os != null) os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * wz 获取设备类型 
     * @return 和panda类型相同，返回true
     */
    public static boolean isRocktechPanda(){
        String device = android.os.Build.DEVICE;
        if(!device.isEmpty() && device.equals(Constants.CAN_DEVICE)){
            return true;
        }
        return false;
    }

    /**
     * 获取板卡类型
     */
    public static String AutoObtainBroadType(){
        return android.os.Build.DEVICE;
    }

    /**
     * 获取序列号
     */
    public static String AutoObtainDeviceSerial(){
        return android.os.Build.SERIAL;
    }

    /**
     * 获取厂商 
     */
    public static String AutoObtainManufacturer(){
        return android.os.Build.MANUFACTURER;
    }

    /**
     * 获取ROM版本 
     */
    public static String AutoObtainROMID(){
        return android.os.Build.ID;
    }

    /**
     * 获取Android版本
     */
    public static String AutoObtainAndroidVersion(){
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * 获取产品型号
     */
    public static String AutoObtainModelType(){
        return android.os.Build.MODEL;
    }


    public static void byteToHex(String string, int length, byte[] msg) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            byte b = msg[i];
            String str = Integer.toHexString(0xFF & b);
            if (str.length() == 1) {
                str = " 0" + str;
            } else {
                str = " " + str;
            }
            sb.append(str);
        }

        Log.e("zyz", string + " : " +sb.toString() + " ;length == "+length);
        // 解析串口收发的所有数据
    }

}
