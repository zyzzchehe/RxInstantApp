package com.example.openlocktest.rxinstantapp;

import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView mTextView;
    private CustomProgressDialog dialog = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Log.d("zyz","onCreate start");
//        mTextView = (TextView) findViewById(R.id.tv);
//        String timeStr = String.format(getResources().getString(R.string.timer_format), -209/60, -209%60);
//        mTextView.setText(timeStr);
//        System.currentTimeMillis();
//        SystemClock.currentThreadTimeMillis();
        dialog = new CustomProgressDialog(this);
        dialog.setMessage("正在加载……");
        dialog.show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//        Log.d("zyz","newConfig str = "+newConfig.toString() + "; newConfig = "+newConfig);
    }
}
