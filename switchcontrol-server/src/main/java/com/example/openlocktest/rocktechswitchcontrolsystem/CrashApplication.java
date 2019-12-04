package com.example.openlocktest.rocktechswitchcontrolsystem;


import android.app.Application;
import android.content.Context;

public class CrashApplication extends Application {
    private static Context context;
    private static boolean isBusy = false;

    public static boolean getBusy() {
        return isBusy;
    }

    public static void setBusy(boolean b) {
        isBusy = b;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
    }
    /**
     * 获取全局上下文
     */
    public static Context getContext() {
        return context;
    }
}
