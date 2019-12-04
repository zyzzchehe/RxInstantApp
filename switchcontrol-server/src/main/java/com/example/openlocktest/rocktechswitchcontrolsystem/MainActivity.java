package com.example.openlocktest.rocktechswitchcontrolsystem;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/*装在主机上*/
public class MainActivity extends AppCompatActivity {

    private static final int SOCKET_PORT = 8888;
    private boolean flag = true;
    public Socket clientSocket;

    private static int count = 0;
    private static int mcount = 0;

    static ServerSocket serverSocket = null;

    public LinearLayout mLinearLayout = null;
    private Spinner timeSpinner;
    private EditText givePowerEt,shutPowerEt;
    private TextView executeCountTextView,restCountTextView,textView3,textView4;
    private Button beginButton,cancelButton;

    private int time;
    private Locker mLocker = null;
    private int MSG_TIME_OUT_FLAG = 201;
    private EditText editText;
    private int testCount,executeCount,restCount,givePower,shutPower;

    private static int zong_shu = 0;
    private static int failure_count = 0;
    private float f;
    private static final int MSG_UPDATE_UI = 0x10;
    private static final int MSG_BEGIN_INVISIBLE = 0x11;
    private static final int MSG_CANCEL_INVISIBLE = 0x12;
    private Thread testThread = null;
    private TestRunnable testRunnable = null;
    private boolean isExecute = false;

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == MSG_TIME_OUT_FLAG){
                failure_count++;
                executeCountTextView.setText("总次数"+ zong_shu);
                textView3.setText("失败次数"+failure_count);
                f =(float)failure_count/zong_shu*100;
                f= (float) Math.round(f);
                textView4.setText("失败率"+f+"%");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        initSocket();
                    }
                }).start();
            } else if (msg.what == 100) {
                mHandler.removeMessages(MSG_TIME_OUT_FLAG);
                if(count >= 10){
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mLinearLayout.removeAllViews();
                    count = 0;
                }

                String res = (String)msg.obj;
                Log.d("zyz","进入 Handler res == "+res);
                String[] a = res.split("-");
                View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_cus, null);

                TextView mTextView = view.findViewById(R.id.tv_device);
                mTextView.setTextSize(40f);
                mTextView.setTextColor(Color.RED);
                mTextView.setText(a[0]+"启动了   ");

                EditText editText = (EditText) view.findViewById(R.id.et_count);
                editText.setTextColor(Color.BLUE);
                editText.setTextSize(60f);
                editText.setText(a[1]);
                mLinearLayout.addView(view);
                count ++ ;
                showControl();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        initSocket();
                    }
                }).start();
            }else if(msg.what==2){
                zong_shu ++;
                executeCountTextView.setText("总次数"+ zong_shu);
                f =(float)failure_count/zong_shu*100;
                f= (float) Math.round(f);
                textView4=(TextView)findViewById(R.id.textView4);
                textView4.setText("失败率"+f+"%");
            } else if(msg.what == MSG_UPDATE_UI) {
                executeCountTextView.setText("已重启次数："+executeCount);
                restCountTextView.setText("剩余次数："+restCount);
            } else if(msg.what == MSG_BEGIN_INVISIBLE) {
                beginButton.setVisibility(View.INVISIBLE);
                cancelButton.setVisibility(View.VISIBLE);
            } else if(msg.what == MSG_CANCEL_INVISIBLE) {
                cancelButton.setVisibility(View.INVISIBLE);
                beginButton.setVisibility(View.VISIBLE);
            } else {
                //do something
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);

        mLocker = new Locker(this);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        beginButton = (Button) findViewById(R.id.bt_begin);
        cancelButton = (Button) findViewById(R.id.bt_cancel);
        cancelButton.setVisibility(View.INVISIBLE);
        executeCountTextView= (TextView)findViewById(R.id.tv_execute_count);
        restCountTextView = (TextView) findViewById(R.id.tv_rest_count);
        textView3=(TextView)findViewById(R.id.textView3);
        textView4=(TextView)findViewById(R.id.textView4);

        mLinearLayout = (LinearLayout) findViewById(R.id.line_parent);
        editText= (EditText) findViewById(R.id.editText);

        givePowerEt  = (EditText) findViewById(R.id.et_give_power);
        shutPowerEt = (EditText) findViewById(R.id.et_shut_power);

        beginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String number = editText.getText().toString();
                String givePowerTime = givePowerEt.getText().toString();
                String shutPowerTime = shutPowerEt.getText().toString();
                if(TextUtils.isEmpty(number)){
                    Toast.makeText(MainActivity.this,"重启次数为空，请输入……",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(givePowerTime)){
                    Toast.makeText(MainActivity.this,"给电持续时间为空，请输入……",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(shutPowerTime)){
                    Toast.makeText(MainActivity.this,"关机持续时间为空，请输入……",Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    testCount = Integer.parseInt(number);
                    givePower = Integer.parseInt(givePowerTime);
                    shutPower = Integer.parseInt(shutPowerTime);
                    if(givePower > 255 || givePower < 0){
                        Toast.makeText(MainActivity.this,"给电持续时间只能在0—255s之间",Toast.LENGTH_SHORT).show();
                        return;
                    }else {
                        if(!isExecute){
                            testRunnable = new TestRunnable();
                            testThread = new Thread(testRunnable);
                            testThread.start();
                            isExecute = true;
                            //mHandler.sendEmptyMessage(MSG_BEGIN_INVISIBLE);
                        }else {
                            Toast.makeText(MainActivity.this,"重复开关机循环已启动，请勿重复启动，谢谢配合……",Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHandler.sendEmptyMessage(MSG_CANCEL_INVISIBLE);
                isExecute = false;
                testThread.interrupt();
                testRunnable = null;
                testThread = null;
            }
        });

        timeSpinner = (Spinner) findViewById(R.id.sp_select_time);
        timeSpinner.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"请选择开机时间(单位s)","255","240","180","120","90","40","10"}));
        timeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                switch (position){
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7://40s
                        time = Integer.valueOf(((TextView)view).getText().toString());
                        break;
                    default:
                        time = 60;
                        break;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d("zyz","onNothingSelected  ===  方法");
            }
        });

        /*new Thread(new Runnable() {
            @Override
            public void run() {
                initSocket();
            }
        }).start();*/
    }

    public void initSocket() {
        try {
            while (flag) {
                clientSocket=getInstance().accept();
                new SocketThread(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class TestRunnable implements Runnable{
        @Override
        public void run() {
            for (int i=0;i<testCount;i++){
                //amountShow();
                try {
                    //运行时的线程被中断后，只会修改中断标记，不会抛出异常
                    while(Thread.currentThread().isInterrupted()){
                        //中断线程
                    }
                    Log.d("zyz","givePower --> "+givePower+" s ; shutPower --> "+shutPower + "ms");
                    mLocker.givePower(givePower);
                    executeCount ++;
                    restCount = testCount - executeCount;
                    mHandler.sendEmptyMessage(MSG_UPDATE_UI);
                    Thread.sleep(givePower*1000+shutPower);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static ServerSocket getInstance(){
        if(serverSocket == null){
            try {
                serverSocket = new ServerSocket(SOCKET_PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return serverSocket;
    }

    private void amountShow() {
        Message msg = new Message();
        msg.what = 2;
        mHandler.sendMessage(msg);
        mHandler.sendEmptyMessageDelayed(MSG_TIME_OUT_FLAG,50000);
    }
    //成功显示次数
    private void showControl() {
        executeCountTextView.setText("成功次数"+mcount);
    }

    public class SocketThread extends Thread {
        private Socket socket;
        byte[] bytes = new byte[512];
        int len = 0;
        public SocketThread(Socket clientSocket) {
            this.socket = clientSocket;
        }
        @Override
        public void run() {
            super.run();
            InputStream inputStream;
            try {
                inputStream = socket.getInputStream();
                while ((len = inputStream.read(bytes)) != -1) {
                    mcount++;
                    String data = new String(bytes, 0, len,"UTF-8");
                    Message message = Message.obtain();
                    message.what = 100;
                    message.obj = data;
                    mHandler.sendMessage(message);
                }
                inputStream.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
