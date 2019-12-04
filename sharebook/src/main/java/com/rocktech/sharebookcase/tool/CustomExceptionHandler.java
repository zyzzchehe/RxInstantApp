package com.rocktech.sharebookcase.tool;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.rocktech.sharebookcase.MainActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by maodongwei on 2018/8/13.
 * 自定义crash log打印类
 */

public class CustomExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "CustomExceptionHandler";
    private Context mContext;
    private static CustomExceptionHandler sInstance;

    private File file;
    private File fileLog;
    private final int SDCARD_TYPE = 0; // 存储在SD卡
    private final int MEMORY_TYPE = 1; // 存储在手机内存中

    private int CURR_LOG_TYPE = SDCARD_TYPE; // 日志记录类型
    private String LOG_PATH_MEMORY_DIR; // 日志文件在内存中的路径(日志文件在安装目录中的路径)
    private String LOG_PATH_SDCARD_DIR; // 日志文件在sdcard中的路径
    private static String CURR_INSTALL_LOG_NAME;
    private String logDocName = "crashLog.txt";

    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private PendingIntent intent; //APP退出后, PendingIntent还存在,设置3秒之后,重启主Activity


    private CustomExceptionHandler(Context context){
        mContext = context;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        intent = PendingIntent.getActivity(mContext, 0,new Intent(mContext, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    public static synchronized CustomExceptionHandler registerInstance(Context context){
        if(null == sInstance){
            sInstance = new CustomExceptionHandler(context.getApplicationContext());
        }
        return sInstance;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        try {
            printExceptionToSDCard(ex);//把crash日志写入SD卡
            ex.printStackTrace();
            AlarmManager mgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 3000, intent);
            mDefaultHandler.uncaughtException(thread, ex);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    //异常信息打印到SD卡
    private void printExceptionToSDCard(Throwable e) throws IOException {
        initLogConfig(mContext);
        try{
            PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter(fileLog)));
            long current = System.currentTimeMillis();
            String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(current));
            printWriter.println(time);
            printDeviceInfo(printWriter);
            printWriter.println();
            e.printStackTrace(printWriter);
            printWriter.close();
        }catch (Exception exception){
            Log.e(TAG, "dump crash into failed");
        }
    }

    //打印设备信息
    private void printDeviceInfo(PrintWriter printWriter) throws PackageManager.NameNotFoundException{
        PackageManager packageManager = mContext.getPackageManager();
        PackageInfo packageInfo = packageManager.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
        printWriter.print("APP Version: ");
        printWriter.print(packageInfo.versionName);
        printWriter.print('_');
        printWriter.println(packageInfo.versionCode);
        //Android 版本号
        printWriter.print("OS Version: ");
        printWriter.print(Build.VERSION.RELEASE);
        printWriter.print('_');
        printWriter.println(Build.VERSION.SDK_INT);
        //手机制造商
        printWriter.print("Vendor: ");
        printWriter.println(Build.MANUFACTURER);
        //手机型号
        printWriter.print("Model: ");
        printWriter.println(Build.MODEL);
        //CPU架构
        printWriter.print("CPU ABI: ");
        printWriter.println(Build.CPU_ABI);
    }

    private void initLogConfig(Context context) {
        LOG_PATH_MEMORY_DIR = context.getFilesDir().getAbsolutePath() + File.separator + "rocktech_crash"+ File.separator + "log";
        LOG_PATH_SDCARD_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "rocktech_crash" + File.separator + "log";
        CURR_LOG_TYPE = getCurrLogType();
        createLogDir();
    }

    public int getCurrLogType() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_REMOVED)) {
            Log.e(TAG, "MEMORY_TYPE");
            return MEMORY_TYPE;
        } else {
            Log.e(TAG, "SDCARD_TYPE");
            return SDCARD_TYPE;
        }
    }

    private void createLogDir() {
        String filename = CURR_LOG_TYPE == MEMORY_TYPE ? LOG_PATH_MEMORY_DIR: LOG_PATH_SDCARD_DIR;
        Log.e(TAG, "filename: "+filename);

        file = new File(filename);
        boolean mkOk;
        if (!file.isDirectory()) {
            mkOk = file.mkdirs();
            if (!mkOk) {
                mkOk = file.mkdirs();
            }
        }

        CURR_INSTALL_LOG_NAME = filename + File.separator + logDocName;
        fileLog = new File(CURR_INSTALL_LOG_NAME);
    }
}
