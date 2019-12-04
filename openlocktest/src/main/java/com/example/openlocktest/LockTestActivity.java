package com.example.openlocktest;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.openlocktest.a1.R;


public class LockTestActivity extends Activity {
    private Locker mLocker = null;
    private EditText boardEditText = null;
    private EditText lockEditText = null;
    private TextView mTextView = null;
    private TextView sendDisplayTextView = null;

    private Switch fanASwitch,fanBSwitch,jrqSwitch,mcSwitch,lightMainBoxSwitch,lightBoxSwitch;
    private Button controlButton;
    private static boolean hasControl = false;
    private Spinner com485Spinner = null;
    private String com485Node;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == 100){
                mTextView.setText(Constant.data);
                Constant.mFlag = false;
            }
        }
    };
    private Runnable runable = new Runnable() {
        @Override
        public void run() {
            while (true){
                if(Constant.mFlag){
                    Log.d("zyz","Constant.data=="+Constant.data);
                    mHandler.sendEmptyMessage(100);
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a1);
        Constant.mFlag = false;
        /*获取控制权按钮逻辑 begin*/
        controlButton = (Button) findViewById(R.id.btnGetControl);
        controlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!hasControl) {
                    controlButton.setText("释放控制权");
                    hasControl = true;
                    if(mLocker == null){
                        Toast.makeText(LockTestActivity.this,"请选择串口节点",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mLocker.getControl(true);
                } else {
                    controlButton.setText("获取控制权");
                    hasControl = false;
                    if(mLocker == null){
                        Toast.makeText(LockTestActivity.this,"请选择串口节点",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mLocker.getControl(false);
                }
            }
        });
        /*获取控制权按钮逻辑 end*/
        /*风扇功能测试 begin*/
        fanASwitch = (Switch) findViewById(R.id.swFSA);
        fanASwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mLocker == null){
                    Toast.makeText(LockTestActivity.this,"请选择串口节点",Toast.LENGTH_SHORT).show();
                    return;
                }
                mLocker.doDebug(12, isChecked);
            }
        });
        fanBSwitch = (Switch) findViewById(R.id.swFSB);
        fanBSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mLocker == null){
                    Toast.makeText(LockTestActivity.this,"请选择串口节点",Toast.LENGTH_SHORT).show();
                    return;
                }
                mLocker.doDebug(25, isChecked);
            }
        });
        /*风扇功能测试 end*/

        /*加热器功能测试 begin*/
        jrqSwitch = (Switch) findViewById(R.id.swJRQ);
        jrqSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mLocker == null){
                    Toast.makeText(LockTestActivity.this,"请选择串口节点",Toast.LENGTH_SHORT).show();
                    return;
                }
                mLocker.doDebug(123,isChecked);
            }
        });
        /*加热器功能测试 end*/

        /*门磁功能测试 begin*/
        mcSwitch = (Switch) findViewById(R.id.swMCD);
        mcSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mLocker == null){
                    Toast.makeText(LockTestActivity.this,"请选择串口节点",Toast.LENGTH_SHORT).show();
                    return;
                }
                mLocker.setMCLamp(isChecked);
            }
        });
        /*门磁功能测试 end*/

        /*灯箱控制测试 begin*/
        lightMainBoxSwitch = (Switch) findViewById(R.id.swMainLightBox);
        lightMainBoxSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mLocker == null){
                    Toast.makeText(LockTestActivity.this,"请选择串口节点",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(isChecked){
                    mLocker.openZlamp();
                }else{
                    mLocker.closeZlamp();
                }
            }
        });
        //辅柜
        lightBoxSwitch = (Switch) findViewById(R.id.swLightBox);
        lightBoxSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mLocker == null){
                    Toast.makeText(LockTestActivity.this,"请选择串口节点",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(isChecked){
                    mLocker.openLamp();
                }else{
                    mLocker.closeLamp();
                }
            }
        });
        /*灯箱控制测试 end*/


        /*开锁功能测试 begin*/
        sendDisplayTextView = (TextView) findViewById(R.id.tv_send_display);
        mTextView = (TextView)findViewById(R.id.tv_display);
        boardEditText = (EditText)findViewById(R.id.et_board_number);
        lockEditText = (EditText)findViewById(R.id.et_lock_number);
        lockEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable input) {
                String boardTmp = boardEditText.getText().toString().trim();
                if(TextUtils.isEmpty(boardTmp)){
                    Toast.makeText(LockTestActivity.this,"板地址不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }
                String lockTmp = input.toString().trim();
                String sendDisplay = getSendData(boardTmp,lockTmp);
                sendDisplayTextView.setText(sendDisplay);
            }
        });

        /*串口节点选择 begin*/
        com485Spinner = (Spinner) findViewById(R.id.sp_485_select);
        com485Spinner.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"请选择串口节点",
                        "/dev/ttymxc4","/dev/ttyUSB0","/dev/ttyS5",
                        "/dev/ttymxc1","/dev/ttymxc2","/dev/ttyS4",
                        "/dev/ttymxc5","/dev/ttymxc6"}));
        com485Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        break;
                    case 1:
                        com485Node = "/dev/ttymxc4";
                        break;
                    case 2:
                        com485Node = "/dev/ttyUSB0";
                        break;
                    case 3:
                        com485Node = "/dev/ttyS5";
                        break;
                    case 4:
                        com485Node = "/dev/ttymxc1";
                        break;
                    case 5:
                        com485Node = "/dev/ttymxc2";
                        break;
                    case 6:
                        com485Node = "/dev/ttyS4";
                        break;
                    case 7:
                        com485Node = "/dev/ttymxc5";
                        break;
                    case 8:
                        com485Node = "/dev/ttymxc6";
                        break;
                    default:
                        break;
                }
                Log.d("zyz","com485Node == "+com485Node);
                if(TextUtils.isEmpty(com485Node)){
                    Toast.makeText(LockTestActivity.this,"请选择串口节点",Toast.LENGTH_SHORT).show();
                    return;
                }
                mLocker = new Locker(LockTestActivity.this,com485Node);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        /*串口节点选择 end*/

        /*发送按钮逻辑 begin*/
        findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String boardStr = boardEditText.getText().toString().trim();
                String lockStr = lockEditText.getText().toString().trim();
                if(boardStr.matches("[a-z]|[A-Z]") || lockStr.matches("[a-z]|[A-Z]") ){
                    Toast.makeText(LockTestActivity.this,"你输入有误，请输入数字，谢谢！",Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    mTextView.setText("");
                    if(mLocker == null){
                        Toast.makeText(LockTestActivity.this,"请选择串口节点",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mLocker.openLock(boardStr,lockStr);
                    new Thread(runable).start();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        /*发送按钮逻辑 end*/
    }
    private String getSendData(String boardStr,String lockStr){
        byte[] lockOrder = new byte[7];
        lockOrder[0] = (byte) 0xAA;
        lockOrder[1] = 0x55;
        lockOrder[2] = 0x03;
        lockOrder[3] = Integer.valueOf(boardStr).byteValue();
        lockOrder[4] = 0x50;
        lockOrder[5] = (byte) (Integer.valueOf(lockStr).byteValue()-1);
        lockOrder[6] = Constant.calcCrc8(lockOrder, 0, 6);
        return Constant.byteToStr(lockOrder.length,lockOrder);
    }

    public void testXhOpenLock(View view) {
        final String boardStr = boardEditText.getText().toString().trim();
        final int lockCount = boardStr.equals("0") ? 8 : 22;
        if(TextUtils.isEmpty(boardStr)){
            Toast.makeText(this,"请输入辅柜板地址",Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(com485Node)){
            Toast.makeText(this,"请选择串口节点",Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    for(int i = 1;i <= lockCount;i ++){
                        try {
                            mLocker.openLock(boardStr,i+"");
                            Thread.sleep(1500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    public void testOpen(View view) {
        for(int i=1;i<3;i++){
            try {
                mLocker.openLock("0",""+i);
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
