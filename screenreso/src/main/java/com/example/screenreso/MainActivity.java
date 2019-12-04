package com.example.screenreso;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*DisplayMetrics dm = getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;*/

        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getRealMetrics(dm);//获取真实分辨率
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        Log.d("yz","screenWidth = "+screenWidth+"; screenHeight = "+screenHeight);
    }
}
