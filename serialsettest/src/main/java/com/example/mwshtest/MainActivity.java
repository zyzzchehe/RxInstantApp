package com.example.mwshtest;

import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    private EditText mEditText;
    private TextView mTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEditText = (EditText) findViewById(R.id.et_input);
        mTextView = (TextView) findViewById(R.id.tv_serial);
    }

    public void click1(View view) {
        Method setSerialStr = null;
        String inp = mEditText.getText().toString();
        try {
            setSerialStr = Build.class.getDeclaredMethod("setSerialStr",String.class);
            setSerialStr.invoke(Build.class,inp);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void click2(View view) {
        Method getSerialStr = null;
        try {
            getSerialStr = Build.class.getDeclaredMethod("getSerialStr");
            getSerialStr.invoke(Build.class);
            mTextView.setText("读取到的串号--> "+Build.SERIAL);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
