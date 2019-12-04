package com.rocktech.macaddressdemo;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class MacAddr extends Activity implements OnClickListener {
    String TAG = "MacAddr";
    private EditText et_Barcode = null;
    private EditText et_mac = null;
    private EditText et_sys_mac = null;
    private Button btn_write;
    private Button btn_read;
    private Button btn_exit;

    private byte m_iMacAddr0 = (byte) 0xC8;
    private byte m_iMacAddr1 = 0x32;
    private byte m_iMacAddr2 = 0x55;
    private byte m_iMacAddr3 = 0x00;
    private byte m_iMacAddr4 = 0x00;
    private byte m_iMacAddr5 = 0x00;

    private int HW_OCOTP_MAC1;
    private int HW_OCOTP_MAC0;

    // mac地址是否可写
    private boolean canWrite = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mac_addr);
        if(Constants.AutoObtainBroadType().equals("rocktech_xinbeiyang_zitigui")){
            Settings.Secure.putString(getContentResolver(),
                    Settings.Secure.DEFAULT_INPUT_METHOD,"com.android.inputmethod.latin/.LatinIME");
        }
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(Constants.AutoObtainBroadType().equals("rocktech_xinbeiyang_zitigui")){
            Settings.Secure.putString(getContentResolver(),
                    Settings.Secure.DEFAULT_INPUT_METHOD,"com.sohu.inputmethod.sogou/.SogouIME");
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    //int selectionStart,selectionEnd;
    private void initView() {
        btn_write = (Button) findViewById(R.id.button1);
        btn_read = (Button) findViewById(R.id.button2);
        btn_exit = (Button) findViewById(R.id.button3);
        btn_write.setOnClickListener(this);
        btn_read.setOnClickListener(this);
        btn_exit.setOnClickListener(this);
        //btn_read.performClick();
        //btn_read.setEnabled(false);
        et_Barcode = (EditText) findViewById(R.id.et_barcode);
        et_mac = (EditText) findViewById(R.id.et_mac);
        et_sys_mac = (EditText) findViewById(R.id.et_sys_mac);
        et_mac.setEnabled(false);
        et_sys_mac.setEnabled(false);
        //et_Barcode.setEnabled(true);
        //et_Barcode.setInputType(InputType.TYPE_NULL);
        // 给条形码输入框设置监听器
        et_Barcode.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                String strCode = s.toString();
                /*add by yazhou for RSC313  魔盒 begin*/
                if(Constants.AutoObtainBroadType().equals("rocktech_rsc313_root")){
                    //do some things
                    et_Barcode.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});//动态设置et长度
                    if(strCode.length() >= 12){
                        //容错性处理
                        String mac = doExce();
                        Log.d(TAG,"mac == "+mac);
                        if(!TextUtils.isEmpty(mac)){
                            Toast.makeText(MacAddr.this, "mac地址不能重复写入", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if(strCode.substring(0, 8).equals("00D051B1")){
                            StringBuilder sb = new StringBuilder();
                            String mac1 = "0x"+strCode.substring(0,2);
                            String mac2 = ",0x"+strCode.substring(2,4);
                            String mac3 = ",0x"+strCode.substring(4,6);
                            String mac4 = ",0x"+strCode.substring(6,8);
                            String mac5 = ",0x"+strCode.substring(8,10);
                            String mac6 = ",0x"+strCode.substring(10,12);
                            String result = sb.append(mac1).append(mac2).append(mac3)
                                    .append(mac4).append(mac5).append(mac6).toString();
                            Log.d(TAG,"result == "+result);
                            Process su = null;
                            OutputStream os = null;
                            try {
                                su = Runtime.getRuntime().exec("/system/xbin/su");
                                os = su.getOutputStream();
                                String cmd = "mount -o remount,rw /device\n" 
                                        + "echo fec.macaddr=" + result + " >/device/mac.ini\n"
                                        + "echo bootargs=console=ttymxc0,115200 vmalloc=400M init=/init video_mode=extension video=mxcfb0:dev=lcd,1024x600,if=RGB24,bpp=32 video=mxcfb1:dev=hdmi,1920x1080@60M,if=RGB24,bpp=32 video=mxcfb2:off video=mxcfb3:off fbmem=32M,32M androidboot.console=ttymxc0 androidboot.hardware=freescale >/device/boot.ini\n"
                                        + "sync\n"
                                        + "chmod 666 /device/boot.ini\n"
                                        + "mount -o remount,ro /device\n"
                                        + "exit\n";
                                os.write(cmd.getBytes());
                                os.flush();
                                if (su.waitFor() != 0) {
                                    throw new SecurityException();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } finally{
                                try {
                                    if(os != null) os.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }else{
                            Toast.makeText(MacAddr.this, "mac地址格式错误，请重新扫描...", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                /*add by yazhou for RSC313 魔盒 end*/

                /**panji zhilai xinbeiyang*/
                else{
                    et_Barcode.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});//动态设置et长度
                    if (strCode.length() >= 8) {
                        btn_write.setEnabled(true);
                        Log.d(TAG, "strCode == "+strCode);
                        String strMacAddr, strFmtAddr;
                        String szCode1, szCode2, szCode3;
                        strMacAddr = strCode.substring(3, strCode.length());
                        Log.d(TAG,"strMacAddr bef== "+strMacAddr);
                        if (strCode.substring(0, 3).equals("RCT")) {
                            strMacAddr = "0" + strMacAddr;
                        } else if (strCode.substring(0, 3).equals("RCC")) {
                            strMacAddr = "1" + strMacAddr;
                        } else if (!strCode.substring(0,3).equals("RSC")
                                ||!strCode.substring(3,4).matches("[0-9]|[A-Z]")
                                ||(!strCode.substring(4).matches("\\d")&&!strCode.substring(4).matches("\\d+")))
                        {//add by yazhou for avoid scan code error
                            canWrite = false;
                            Toast.makeText(MacAddr.this,"Mac地址有误，请扫描正确的Mac号码！", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Log.d(TAG,"strMacAddr == "+strMacAddr);
                        szCode1 = strMacAddr.substring(0, 2);
                        szCode2 = strMacAddr.substring(2, 4);
                        szCode3 = strMacAddr.substring(strMacAddr.length() - 2, strMacAddr.length());

                        Log.d(TAG,"go one "+szCode1 + "---" + szCode2 + "---" + szCode3);

                        m_iMacAddr3 = (byte) Integer.parseInt(szCode1, 16);
                        m_iMacAddr4 = (byte) Integer.parseInt(szCode2, 16);
                        m_iMacAddr5 = (byte) Integer.parseInt(szCode3, 16);

                        Log.d(TAG,"go two "+m_iMacAddr3 + "+++" + m_iMacAddr4 + "+++" + m_iMacAddr5);

                        strFmtAddr = byte2Hex(m_iMacAddr0, false) + ":" + byte2Hex(m_iMacAddr1, false) + ":"
                                + byte2Hex(m_iMacAddr2, false) + ":" + szCode1 + ":" + szCode2 + ":" + szCode3;

                        Log.d(TAG,"go three "+ strFmtAddr);
                        et_mac.setText(strFmtAddr);
                        //                    et_Barcode.setSelection(selectionEnd);
                        HW_OCOTP_MAC1 = (((m_iMacAddr0 & 0xFF) << 8) | (m_iMacAddr1 & 0xFF));
                        HW_OCOTP_MAC0 = (((m_iMacAddr2 & 0xFF) << 24) | ((m_iMacAddr3 & 0xFF) << 16)
                                | ((m_iMacAddr4 & 0xFF) << 8) | (m_iMacAddr5 & 0xFF));
                        // 写入按键可点击
                        if(canWrite){
                            btn_write.setEnabled(true);
                            btn_write.performClick();
                        }else{
                            Toast.makeText(getApplicationContext(), "Mac地址不可重复写入", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
    }

    private String doExce(){
        String s = ""; 
        OutputStream os = null;
        BufferedReader br = null;
        try {
            Process p  = Runtime.getRuntime().exec("/system/xbin/su");
            os = p.getOutputStream();
            String cmd = "cat /device/mac.ini\n"
                    + "exit\n";
            os.write(cmd.getBytes());
            os.flush();
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = br.readLine()) != null) {
                s += line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "s == "+s);
        return s;
    }


    public String byte2Hex(byte byt, boolean b) {
        String str = Integer.toHexString(0xFF & byt);
        if (b) {
            if (str.length() == 1) {
                str = " 0x0" + str;
            } else {
                str = " 0x" + str;
            }
        } else {
            if (str.length() == 1 && b) {
                str = "0" + str;
            }
        }
        return str;
    }

    // 检查权限
    private void checkQuanxian(String s) {
        File file = new File(s);
        if (!file.canRead() || !file.canWrite()) {
            DataOutputStream dos = null;
            try {
                if(isRoot()){//如果设备可以root
                    Process su = Runtime.getRuntime().exec("sh");
                    dos = new DataOutputStream(su.getOutputStream());
                    String cmd = "chmod 777 " + s + "\n" + "exit\n";
                    dos.writeBytes(cmd);
                    dos.flush();

                    Log.d(TAG, "su back for == "+su.waitFor());

                    if (su.waitFor() != 0) {
                        Toast.makeText(MacAddr.this, s + "没读写权限,chmod 失败", Toast.LENGTH_SHORT).show();
                        return;
                    }  
                }else{
                    Toast.makeText(MacAddr.this,"设备不能root", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {//changhua
                try {
                    if(dos != null)  dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }else{
            //有读写权限
            Log.d(TAG,"HW_OCOTP_MAC0 and HW_OCOTP_MAC1 有读写权限！");
        }
    }
    private boolean isRoot() {
        String binPath = "/system/bin/su";
        String xBinPath = "/system/xbin/su";
        if (new File(binPath).exists()||new File(xBinPath).exists()){
            return true;
        } else {
            return false;
        }
    }

    private String do_exec(String cmd) {
        String s = "";
        BufferedReader in = null;
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                s += line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {//changhua
            if(in != null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.e("RockTech do_exec "+ cmd + "ret:", s);
        return s;
    }

    // 设置mac命令：
    // echo HW_OCOTP_MAC0 > /sys/fsl_otp/HW_OCOTP_MAC0
    // echo HW_OCOTP_MAC1 > /sys/fsl_otp/HW_OCOTP_MAC1

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
        case R.id.button1://写入

            checkQuanxian("/sys/fsl_otp/HW_OCOTP_MAC0");
            checkQuanxian("/sys/fsl_otp/HW_OCOTP_MAC1");

            DataOutputStream dos = null;
            try {
                Process process = Runtime.getRuntime().exec("sh");
                dos = new DataOutputStream(process.getOutputStream());

                String mac0 = "0x" + Integer.toHexString(HW_OCOTP_MAC0);
                String mac1 = "0x" + Integer.toHexString(HW_OCOTP_MAC1);

                String cmd = 
                        "busybox echo " + mac0 + " > /sys/fsl_otp/HW_OCOTP_MAC0\n" + 
                                "busybox echo " + mac1 + " > /sys/fsl_otp/HW_OCOTP_MAC1\n"+"exit\n";
                dos.writeBytes(cmd);

                Log.i(TAG, "busybox echo " + mac0 + " > /sys/fsl_otp/HW_OCOTP_MAC0");
                Log.i(TAG, "busybox echo " + mac1 + " > /sys/fsl_otp/HW_OCOTP_MAC1");

                dos.flush();

                Log.d(TAG, "process back for == "+process.waitFor());
                if(process.waitFor() == 0){
                    canWrite = false;//已经写入，不能在写
                }else{
                   Toast.makeText(MacAddr.this, "mac address write fail", Toast.LENGTH_SHORT).show(); 
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.v(TAG, "write fail...");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (dos != null) {
                    try {
                        dos.close();
                    } catch (IOException ee) {
                        ee.printStackTrace();
                    }
                }
            }
            btn_write.setEnabled(false);
            break;
        case R.id.button2://读取
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    String sys_mac = do_exec("cat /sys/class/net/eth0/address");
                    if(sys_mac.substring(0, 2).equals("c8")){//已经写入了 不能在写
                        canWrite = false;
                    }
                    if(TextUtils.isEmpty(sys_mac)){
                        Toast.makeText(MacAddr.this, "请先烧写MAC地址", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    et_sys_mac.setText(sys_mac);//系统mac
                }
            });
            break;
        case R.id.button3://退出
            finish();
            break;
        }
    }
}
