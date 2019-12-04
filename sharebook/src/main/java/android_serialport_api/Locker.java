package android_serialport_api;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import androidx.core.content.LocalBroadcastManager;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.rfid.RFIDReaderHelper;
import com.rocktech.sharebookcase.WebSocketService;
import com.rocktech.sharebookcase.msgdata.ShareBookMessage;
import com.rocktech.sharebookcase.msgdata.bizObject;
import com.rocktech.sharebookcase.msgdata.bodyMsg;
import com.rocktech.sharebookcase.msgdata.headMsg;
import com.rocktech.sharebookcase.tool.Constant;
import com.rocktech.sharebookcase.tool.RequestManager;
import com.rocktech.sharebookcase.tool.SpHelper;
import com.tools.ExcelUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;

import android_serialport_api.SerialPort.onDataReceivedListener;

public class Locker implements Serializable {
    private Context mBase;
    private static final String TAG = "Locker";
    private static SerialPort lock;

    // 锁控版命令
    private static byte[] lockOrder = null;

    private static String boxId = "";
    private static String doorId = "";

    static private String[] batchBoxId;
    static private boolean[] HasOpened;

    private static int n = -1;

    private static int iNum;

    // 是否正在进行其他操作
    private boolean isBusy = false;

    private static ArrayList<String> list = new ArrayList<String>();
    private static String[] boxOpened;

    private static ArrayList<String> code1 = new ArrayList<String>();
    private static ArrayList<String> code2 = new ArrayList<String>();

    private static byte[] LISHIZHI = null;

    // 485抄表
    private final static String Query485 = "pj.xing.meter";

    // 查询温度
    private final static String Temp = "pj.xing.temp";
    // 查询当前温度补偿
    private final static String queryTemp = "pj.xing.queryTemp";

    // 查询湿度
    private final static String Shidu = "pj.xing.shidu";
    // 查询当前湿度补偿
    private final static String queryShidu = "pj.xing.queryShidu";

    // 查询抄表485设置
    private final static String query485SET = "pj.xing.query485Setting";

    // 查询风扇A设置温度
    private final static String query12SET = "pj.xing.query12Setting";
    // 查询风扇B设置温度
    private final static String query25SET = "pj.xing.query25Setting";
    // 查询加热器设置温度
    private final static String query26SET = "pj.xing.query26Setting";
    // 门磁监测设置
    private final static String queryMCSET = "pj.xing.queryMCSetting";

    private final static Integer MAX_DOOR_NUM = 3;

    // 485抄表
    private static byte[] LINSHI485 = null;

    // 检查log
    private static boolean checkLogFile = true;

    // 锁控版固件路径
    // private static final String mPath = "/sdcard/lock.hex";
    private static final String mPath = "/sdcard/LOCK.BIN";
    // 测试开锁
    private static String[] sBox = {"Z", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "0"};
    private static String sBox2 = null;
    private static StringBuffer sBuffer = new StringBuffer();
    private static int currentI = 0;
    private static String currentBox = null;


    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 1: {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            for (String str : sBox) {
                                currentI++;
                                currentBox = str;
                                Log.v(TAG, "currentI = " + currentI);
                                queryLockTest(str + "01");
                                try {
                                    Thread.sleep(1300);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).start();
                    break;
                }
                case 2: {
                    try {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while (sBox2.length() >= 1) {
                                    Log.v(TAG, "========== + " + sBox2);
                                    String s2 = sBox2.substring(0, 1);
                                    if (s2.equals("Z")) {
                                        for (int inum = 1; inum < 9; inum++) {
                                            openLock(s2 + "0" + inum);
                                            try {
                                                Thread.sleep(1300);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    } else {
                                        for (int inum = 1; inum < 23; inum++) {
                                            if (inum < 10) {
                                                openLock(s2 + "0" + inum);
                                            } else {
                                                openLock(s2 + inum);
                                            }
                                            try {
                                                Thread.sleep(1300);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                    sBox2 = sBox2.substring(1);
                                }
                                Intent intent = new Intent("pj.xing.locktest");
                                mBase.sendBroadcast(intent);
                                isBusy = false;
                            }
                        }).start();
                    } catch (Exception e) {
                        Intent intent = new Intent("pj.xing.locktest");
                        mBase.sendBroadcast(intent);
                        isBusy = false;
                    }

                    break;
                }

                case -1: {
                    break;
                }

                default:
                    break;
            }
        }

        ;
    };

    private boolean hasSendOpendoorMsg;//是否把已经开门成功的信息发送给server

    public void doLockTest() {
        isBusy = true;
        mHandler.sendEmptyMessage(1);
    }

    private Timer timer;

    private RFIDReaderHelper mReader;
    private LocalBroadcastManager localBroadcastManager;

    public Locker(final Context context, Timer timer, LocalBroadcastManager localBroadcastManager) throws Exception {
        this.timer = timer;
        mBase = context;
        this.localBroadcastManager = localBroadcastManager;
        try {
            Log.e(TAG, "Locker, new SerialPort === 串口节点：" + Constant.COM_LOCKER + ",  波特率：" + Constant.LOCKER_baudrate);
            lock = new SerialPort(new File(Constant.COM_LOCKER), Constant.LOCKER_baudrate, 0,
                    new onDataReceivedListener() {
                        @Override
                        public void onDataReceived(byte[] buffer, int size) {
                            if (LISHIZHI == null) {
                                LISHIZHI = subBytes(buffer, 0, size);
                            } else {
                                LISHIZHI = byteMerger(LISHIZHI, subBytes(buffer, 0, size));
                            }

                            if (LISHIZHI.length == 1 && buffer[0] == 5) {
                                // 如果接收的是超时反馈值，则执行解析
                                toAnalysis(n);
                                return;
                            }

                            while (true) {
                                // 首字节不等于0xAA 丢弃
                                while ((LISHIZHI.length > 2) && (LISHIZHI[0] != -86) && (LISHIZHI[1] != 0x55)) {
                                    LISHIZHI = subBytes(LISHIZHI, 1, LISHIZHI.length - 1);
                                }

                                if (LISHIZHI.length < 3) {
                                    // 接收的长度小于3
                                    lock.start();
                                    break;
                                } else {
                                    if (n == 11) {
                                        while ((LISHIZHI.length >= 7) && (LISHIZHI.length >= (LISHIZHI[2] + 4))) {
                                            byte[] linshi = subBytes(LISHIZHI, 5, LISHIZHI[2] - 2);

                                            LISHIZHI = subBytes(LISHIZHI, (LISHIZHI[2] + 4), LISHIZHI.length - (LISHIZHI[2] + 4));

                                            if (LINSHI485 == null) {
                                                LINSHI485 = new byte[linshi.length];
                                                for (int i = 0; i < linshi.length; i++) {
                                                    LINSHI485[i] = linshi[i];
                                                }
                                            } else {
                                                LINSHI485 = byteMerger(LINSHI485, linshi);
                                            }
                                        }
                                        if (LINSHI485 == null || LINSHI485.length != 22) {
                                            lock.start();
                                            return;
                                        } else {
                                            toAnalysis(n);
                                            break;
                                        }
                                    } else {
                                        // 接收到完整指令则解析
                                        if (LISHIZHI[2] == (LISHIZHI.length - 4)) {
                                            // 计算CRC8校验，并进行校验
                                            byte crc8 = Constant.calcCrc8(LISHIZHI, 0, LISHIZHI.length - 1);
                                            if (LISHIZHI[LISHIZHI.length - 1] == crc8) {
                                                // crc8校验通过
                                                toAnalysis(n);
                                                break;
                                            } else {
                                                // crc8校验错误，丢弃接收数据的第一个字节，并重新扫描
                                                LISHIZHI = subBytes(LISHIZHI, 1, LISHIZHI.length - 1);
                                            }
                                        } else if (LISHIZHI[2] > (LISHIZHI.length - 4)) {
                                            // 指令不完整，继续读取
                                            lock.start();
                                            break;
                                        } else {
                                            // 接收的数据长度超过完整指令长度
                                            Constant.byteToHex("Locker, 接收的数据长度超过完整指令长度=== ", LISHIZHI.length, LISHIZHI);
                                            LISHIZHI = subBytes(LISHIZHI, 0, LISHIZHI[2] + 4);
                                        }
                                    }
                                }
                            }
                        }
                    }, 1300);
            Log.e(TAG, "===锁控版初始化成功!====");

        } catch (Exception e) {
            Constant.byteToHex("发生异常 ：", LISHIZHI.length, LISHIZHI);
            lock.stop();
            LISHIZHI = null;
        }

    }


    private void toAnalysis(int n) {
        switch (n) {
            case FC_GET_TEMP_FOR_OTHER: {//获取温度
                if (LISHIZHI.length == 8) {
                    byte t1 = LISHIZHI[5];
                    byte t2 = LISHIZHI[6];
                    int i1, i2;
                    if (t1 < 0) {
                        i1 = (256 + t1) << 8;
                    } else {
                        i1 = t1 << 8;
                    }
                    if (t2 < 0) {
                        i2 = 256 + t2;
                    } else {
                        i2 = t2;
                    }
                    Log.v(TAG, i1 + "/" + i2);
                    double f = (i1 + i2 - 500) / 10.0;
                    BigDecimal b = new BigDecimal(f);
                    double f1 = b.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                    Log.v(TAG, "温度 = " + f1);
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.hal.sys.result");
                    String mock = "[\n" +
                            "{\n" +
                            "\"key\": \"temp.mainboard\",\n" +
                            "\"value\":" + f1 + "\n" +
                            "},\n" +
                            "{\n" +
                            "\"key\": \"anotherKeyName\",\n" +
                            "\"value\":\"anotherValue\"\n" +
                            "}\n" +
                            "]\n";
                    intent.putExtra("result", mock);
                    mBase.sendBroadcast(intent);
                }
                isBusy = false;
                LISHIZHI = null;
                break;
            }
            case 1: {
                if (LISHIZHI != null && LISHIZHI.length != 1) {
                    if (doCheckResultsharebook(LISHIZHI)) { //执行关门成功
                        // 调用盘存动作
                        // ch13 返回结果 aa 55 03 00 55 00 d4
                        // ch12  AA 55 03 00 52 00 ba
                        // ch25  AA 55 03 00 53 00 7E
                        ExcelUtils.isFirstInventy = false;
                        ExcelUtils.checklockstate = false;

                        hasSendOpendoorMsg = false;

                        Log.i("zyz", "doCheckResultsharebook( LISHIZHI) = true, 此时 ExcelUtils.isFirstInventy = false");

                        if (ExcelUtils.sharebookdoornum == 0) { //ch12 副柜
                            Log.v(TAG, "door0 close ");
                            Intent intent = new Intent("com.share.switch.antenna");
                            intent.putExtra("id", ExcelUtils.sharebookdoornum);
                            // 发送本地广播。
                            localBroadcastManager.sendBroadcast(intent);
                            Log.i(TAG, "toAnalysis(), localBroadcastManager sendBroadcast com.share.switch.antenna");

                            // mReader.setWorkAntenna((byte) 0xFF, (byte) 0);
                        } else if (ExcelUtils.sharebookdoornum == 1) { //ch25 副柜
                            Log.v(TAG, "door1 close ");
                            Intent intent = new Intent("com.share.switch.antenna");
                            intent.putExtra("id", ExcelUtils.sharebookdoornum);

                            // 发送本地广播。
                            localBroadcastManager.sendBroadcast(intent);

                            // mReader.setWorkAntenna((byte) 0xFF, (byte) 0);
                        } else if (ExcelUtils.sharebookdoornum == 2) { //ch13 主柜
                            Log.v(TAG, "door2 close ");
                            Intent intent = new Intent("com.share.switch.antenna");
                            intent.putExtra("id", ExcelUtils.sharebookdoornum);

                            // 发送本地广播。
                            localBroadcastManager.sendBroadcast(intent);
                            // Log.e(TAG, "toAnalysis: to be done by someone ");
                            // mReader.setWorkAntenna((byte) 0xFF, (byte) 6);
                            // mReader2.setWorkAntenna((byte) 0xFF, (byte) 6);
                        } else {

                        }

                        //timer.cancel();
                        //timer = null;

                    } else {
                        if (!hasSendOpendoorMsg) {
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            sendOpendoorMsg();
                            hasSendOpendoorMsg = true;
                        }
                    }
                }

                LISHIZHI = null;
                isBusy = false;
                break;
            }
            case 3: {
                // 多路开锁/查询
                Log.v("pj", "======== iNum = " + iNum);
                try {
                    if (doCheckResult(LISHIZHI)) {
                        HasOpened[iNum] = true;
                    } else {
                        HasOpened[iNum] = false;
                    }
                } catch (Exception e) {
                    e.getStackTrace();
                }
                LISHIZHI = null;
                if (iNum >= batchBoxId.length - 1) {
                    Intent intent = new Intent("android.intent.action.hal.iocontroller.batchopen.result");
                    intent.putExtra("batchboxid", batchBoxId);
                    intent.putExtra("opened", HasOpened);
                    mBase.sendBroadcast(intent);
                    LISHIZHI = null;
                    isBusy = false;
                }
                break;
            }
            case 5: {
                // 已打开格口
                System.out.println("i = " + iNum);
                if (LISHIZHI.length != 1) {
                    if (doCheckResult(LISHIZHI)) {
                        Log.v(TAG, " = ");
                        list.add(batchBoxId[iNum]);
                    }
                }
                LISHIZHI = null;
                if (iNum >= batchBoxId.length - 1) {
                    boxOpened = list.toArray(new String[list.size()]);
                    Intent intent = new Intent("android.intent.action.hal.iocontroller.queryAllData");
                    intent.putExtra("openedBoxes", boxOpened);
                    mBase.sendBroadcast(intent);
                    isBusy = false;
                    LISHIZHI = null;
                }
                break;
            }
            case 6: {
                // 查询资产编码
    /*            if (LISHIZHI.length == 30) {
                    try {
                        String ss = new String(subBytes(LISHIZHI, 8, 21), "UTF-8");
                        if (ss.endsWith("N")) { // "*** * ** * ** 00 ******* NNN"
                            code1.add(ss.substring(0, 18));
                        } else {
                            code1.add(ss);
                        }
                        code2.add(new String(subBytes(LISHIZHI, 5, 3), "UTF-8"));
                        Log.e("zyz", "ss1 == " + ss + "; ss2 == " + new String(subBytes(LISHIZHI, 5, 3), "UTF-8"));
                        Log.v("zyz", "code1 size == " + code1.size() + ": code2 size == " + code2.size());
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                LISHIZHI = null;

                if (iNum >= *//*8*//*12) {
                    assetCodeArrays = code1.toArray(new String[code1.size()]);
                    boxIdArrays = code2.toArray(new String[code2.size()]);
                    Log.e("zyz", "assetCodeArrays == " + Arrays.toString(assetCodeArrays) + "; boxIdArrays == " + Arrays.toString(boxIdArrays));
                    Intent intent = new Intent("android.intent.action.hal.boxinfo.result");
                    if (assetCodeArrays != null && assetCodeArrays.length != 0) {
                        Log.e("zyz", "TEST assetCodeArrays == " + Arrays.toString(assetCodeArrays));
                        intent.putExtra("array.assetCode", assetCodeArrays);
                    }
                    if (boxIdArrays != null && boxIdArrays.length != 0) {
                        Log.e("zyz", "TEST boxIdArrays == " + Arrays.toString(boxIdArrays));
                        intent.putExtra("array.boxId", boxIdArrays);
                    }
                    mBase.sendBroadcast(intent);
                    isBusy = false;
                    LISHIZHI = null;
                }*/

                if(null != LISHIZHI){
                    String receiveData = Constant.byteToHex("@@@读取资产编码返回", LISHIZHI.length, LISHIZHI).replace(" ", "");
                    String deviceCode = Constant.hexToStr(receiveData.substring(10, receiveData.length() - 2));
                    SpHelper.setStringValue("deviceCode", deviceCode);
                }
                LISHIZHI = null;

                break;
            }
            case 7: {
                // 写入资产编码
                break;
            }
            case 8: {
                // 门磁监测
                if (LISHIZHI.length != 1) {
                    if (isReceivedDataEquals(LISHIZHI)) {
                        Intent intent = new Intent("android.intent.action.hal.guard.event");
                        intent.putExtra("eventType", "BoxCoreOpened");
                        intent.putExtra("eventArg", "");
                        mBase.sendBroadcast(intent);
                    }
                }

                LISHIZHI = null;

                break;
            }

            case 11: {
                // 485抄表
                Intent intent = new Intent(Query485);
                if ((LINSHI485 != null) && (LINSHI485.length == 22)) {
                    if ((LINSHI485[0] != -2) && (LINSHI485[21] != 0x16)) {
                        intent.putExtra("meter", "**");
                    } else {
                        intent.putExtra("meter", bcd2Str(subBytes(LINSHI485, 16, 4)));
                    }
                } else {
                    intent.putExtra("meter", "**");
                }
                mBase.sendBroadcast(intent);
                isBusy = false;
                LINSHI485 = null;
                LISHIZHI = null;
                break;
            }

            case 12: {
                // 获取温度
                if (LISHIZHI.length == 8) {
                    byte t1 = LISHIZHI[5];
                    byte t2 = LISHIZHI[6];
                    int i1, i2;
                    if (t1 < 0) {
                        i1 = (256 + t1) << 8;
                    } else {
                        i1 = t1 << 8;
                    }
                    if (t2 < 0) {
                        i2 = 256 + t2;
                    } else {
                        i2 = t2;
                    }

                    Log.v(TAG, i1 + "/" + i2);

                    double f = (i1 + i2 - 500) / 10.0;
                    BigDecimal b = new BigDecimal(f);
                    double f1 = b.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();

                    Log.v(TAG, "温度 = " + f1);

                    Intent intent = new Intent(Temp);
                    intent.putExtra("temp", f1 + "");
                    mBase.sendBroadcast(intent);
                }
                isBusy = false;
                LISHIZHI = null;
                break;
            }

            case 13: {
                // 湿度
                isBusy = false;
                LISHIZHI = null;
                break;
            }

            case 14: {
                // 查询当前温度补偿
                if (LISHIZHI.length == 7) {
                    byte b = LISHIZHI[5];
                    Intent intent = new Intent(queryTemp);
                    if ((b & 0x80) == 0x80) {
                        intent.putExtra("temp", "-" + ((b & 0x7F) / 10));
                    } else {
                        intent.putExtra("temp", (b / 10) + "");
                    }
                    mBase.sendBroadcast(intent);
                }
                isBusy = false;
                LISHIZHI = null;
                break;
            }

            case 15: {
                // 查询当前湿度补偿
                if (LISHIZHI.length == 7) {
                    byte b = LISHIZHI[5];
                    Intent intent = new Intent(queryShidu);
                    intent.putExtra("shidu", b + "");
                    mBase.sendBroadcast(intent);
                }
                isBusy = false;
                LISHIZHI = null;
                break;
            }

            case 16: {
                if (LISHIZHI.length != 1) {
                    sBuffer.append(currentBox);
                    Log.v(TAG, "sBuffers = " + sBuffer.toString());
                }
                if (currentI > 15) {
                    if (sBuffer.length() > 0) {
                        sBox2 = sBuffer.toString();
                        sBuffer.setLength(0);
                        currentI = 0;
                        Log.v(TAG, "发送开锁命令");
                        mHandler.sendEmptyMessage(2);
                    } else {
                        Intent intent = new Intent("pj.xing.locktest");
                        mBase.sendBroadcast(intent);
                        isBusy = false;
                    }
                }
                LISHIZHI = null;
                break;
            }

            case 17: {
                if (LISHIZHI.length == 9) {
                    Constant.Scom_parity = LISHIZHI[5] + 1;
                    Constant.Scom_baudrate = LISHIZHI[6] + 1;
                    Intent intent = new Intent(query485SET);
                    mBase.sendBroadcast(intent);
                }
                isBusy = false;
                LISHIZHI = null;
                break;
            }

            case 18: {
                if (LISHIZHI.length == 10) {
                    Intent intent = null;
                    DecimalFormat df = new DecimalFormat("###.0");
                    String temOpen = df
                            .format((Integer.parseInt(byteToHexStr(2, subBytes(LISHIZHI, 5, 2)), 16) - 500) / 10.0);
                    String temClose = df
                            .format((Integer.parseInt(byteToHexStr(2, subBytes(LISHIZHI, 7, 2)), 16) - 500) / 10.0);
                    switch (LISHIZHI[4]) {
                        case 0x69:
                            intent = new Intent(query12SET);
                            intent.putExtra("temOpen", temOpen);
                            intent.putExtra("temClose", temClose);
                            mBase.sendBroadcast(intent);
                            break;
                        case 0x6A:
                            intent = new Intent(query25SET);
                            intent.putExtra("temOpen", temOpen);
                            intent.putExtra("temClose", temClose);
                            mBase.sendBroadcast(intent);
                            break;
                        case 0x6B:
                            intent = new Intent(query26SET);
                            intent.putExtra("temOpen", temOpen);
                            intent.putExtra("temClose", temClose);
                            mBase.sendBroadcast(intent);
                            break;
                        default:
                            break;
                    }
                }
                isBusy = false;
                LISHIZHI = null;
                break;
            }

            case 19: {
                if (LISHIZHI.length == 7) {
                    Intent intent = new Intent(queryMCSET);
                    if (LISHIZHI[5] == 0x00) {
                        intent.putExtra("mcSet", true);
                    } else {
                        intent.putExtra("mcSet", false);
                    }
                    mBase.sendBroadcast(intent);
                }
                isBusy = false;
                LISHIZHI = null;
                break;
            }
            case -1: {
                isBusy = false;
                LISHIZHI = null;
            }
            default:
                isBusy = false;
                LISHIZHI = null;
                break;
        }
    }

    /**
     * 开门成功，告诉server，服务器用来进行倒计时等操作
     */
    private void sendOpendoorMsg() {
        try {
            ShareBookMessage msg = new ShareBookMessage();
            msg.head = new headMsg();
            msg.body = new bodyMsg();
            msg.body.bizObject = new bizObject();

            msg.body.bizObject.setSender(SpHelper.getIntValue("receiver"));
            msg.body.bizObject.setReceiver(SpHelper.getIntValue("sender"));

            msg.body.bizObject.setMessage("action=openDoor&ok");

            msg.body.bizObject.setChatType(0);

            String chatToken = msg.body.bizObject.getSender() + msg.body.bizObject.getReceiver()+ "" ;
            msg.body.bizObject.setChatToken(chatToken);

            String message = JSON.toJSONString(msg, SerializerFeature.WriteMapNullValue);

            Log.e(TAG, "开门成功，告诉server === jsonStrMsg : " + message);
            WebSocketService.sendMsg(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @功能: BCD码转为10进制串(阿拉伯数据)
     * @参数: BCD码
     * @结果: 10进制串
     */
    public static String bcd2Str(byte[] bytes) {
        StringBuffer temp = new StringBuffer(bytes.length * 2);
        for (int i = bytes.length - 1; i >= 0; i--) {
            temp.append((byte) (((bytes[i] - 0x33) & 0xf0) >>> 4));
            temp.append((byte) ((bytes[i] - 0x33) & 0x0f));
            if (i == 1) {
                temp.append(".");
            }
        }
        String str2 = temp.toString();
        DecimalFormat df = new DecimalFormat("0.00");
        double d = Double.parseDouble(str2);
        return df.format(d);
    }

    private String byteToHexStr(int length, byte[] msg) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            byte b = msg[i];
            String str = Integer.toHexString(0xFF & b);
            if (str.length() == 1) {
                str = "0" + str;
            } else {
                str = "" + str;
            }

            sb.append(str);
        }
        Log.i(TAG, "length = " + length + "," + sb.toString());
        return sb.toString();
    }

    /**
     * 处理门磁查询返回值,兼容12路和18路主柜锁控板
     *
     * @param -1-开，0为关
     * @return 开返回true, 关返回false
     */
    private boolean isReceivedDataEquals(byte[] data) {
        if (data.length == 9) {
            if (!Constant.buzEnableOne) {
                if (((data[6] >> 7) & 1) == 1) {
                    return true;
                }
            } else {
                if (((data[6] >> 7) & 1) == 0) {
                    return true;
                }
            }
            return false;
        } else {
            if ((data[6] == 0x0f) || (data[6] == 0x0b)) {
                if (!Constant.buzEnableOne) {
                    if ((data[6] & 4) == 4) {
                        return true;
                    }
                } else {
                    if ((data[6] & 4) == 0) {
                        return true;
                    }
                }
            } else {
                if (!Constant.buzEnableOne) {
                    if ((data[6] & 2) == 2) {
                        return true;
                    }
                } else {
                    if ((data[6] & 2) == 0) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    /**
     * 解析锁状态、兼容18路和12路主柜鎖控板
     *
     * @param bt
     * @return 0为开-true,1为关-false
     */
    private boolean doCheckResultsharebook(byte[] bt) {
        int num = calculateLock(boxId) - 1; // 计算锁编号
        boolean b = Constant.lockStats;
        if (boxId.equals("Z18")) {
            num = 18;
        }

        if (false) {
            Log.d(TAG, "doCheckResultsharebook:bt[5] = " + bt[5]);
            Log.d(TAG, "doCheckResultsharebook:bt[5]>>1 = " + bt[5]);

            if ((((bt[5] >> 5) & 1) == 0))
                Log.d(TAG, "bt[5] bit == 0");  //ch15
            if ((((bt[6] >> 6) & 1) == 0))
                Log.d(TAG, " bt[6] bit == 0");

            if ((((bt[5] >> 5) & 1) == 0) && (((bt[6] >> 6) & 1) == 0)) {
                Log.d(TAG, " door is closed all");
                return true;
            }
            return false;

        }


        if (bt.length == 9) { // 18路锁控板主柜查询返回值是9
            if (doorId.equals("door1")) {
                //检查对应的比特位
                //副柜2
                //ch 25
                if ((((bt[7] >> 0) & 1) == 0))
                    Log.d(TAG, "bt[7] bit0 == 0");
                if ((((bt[5] >> 6) & 1) == 0))
                    Log.d(TAG, " bt[5] bit6 == 0");  //ch16

                if ((((bt[5] >> 6) & 1) == 0) && (((bt[7] >> 0) & 1) == 0)) {
                    Log.d(TAG, " door1 is closed all");
                    ExcelUtils.sharebookdoornum = 1;
                    return true;
                }

                return false;
            } else if (doorId.equals("door0")) {
                //副柜
                //ch12
                if ((((bt[5] >> 5) & 1) == 0))
                    Log.d(TAG, " bt [5]  == 0");

                if ((((bt[6] >> 6) & 1) == 0))
                    Log.d(TAG, " bt [5]  == 0");


                if ((((bt[5] >> 5) & 1) == 0) && (((bt[6] >> 6) & 1) == 0)) {
                    Log.d(TAG, " door0 is closed all");
                    ExcelUtils.sharebookdoornum = 0;
                    return true;
                }
            } else if (doorId.equals("door2")) {
                //ch13 主柜
                if ((((bt[6] >> 7) & 1) == 0) && (((bt[7] >> 1) & 1) == 0)) {
                    Log.d(TAG, " door2 is closed all");
                    ExcelUtils.sharebookdoornum = 2;
                    return true;
                }
            }

        }

        return false;

    }

    /**
     * 解析锁状态、兼容18路和12路主柜鎖控板
     *
     * @param bt
     * @return 0为开-true,1为关-false
     */
    private boolean doCheckResult(byte[] bt) {
        Log.d("zyz", "boxId == " + boxId);
        int num = calculateLock(boxId) - 1; // 计算锁编号
        Log.d("zyz", "num == " + num);
        boolean b = Constant.lockStats;
        if (boxId.equals("Z18")) {
            num = 18;
        }

        if (bt.length == 9) { // 18路锁控板主柜查询返回值是9
            if (boxId.startsWith("Z")) {
                if (boxId.equalsIgnoreCase("Z00")) {
                    if (!Constant.buzEnableOne) {
                        if (((bt[6] >> 7) & 1) == 1) {
                            return true;
                        }
                    } else {
                        if (((bt[6] >> 7) & 1) == 0) {
                            return true;
                        }
                    }
                    return false;
                } else {
                    return (((bt[5] >> num) & 1) == 0) ? (!b) : b;
                }
            }
        }

        switch (num / 8) {
            case 0:
                return (((bt[5] >> (num % 8)) & 1) == 0) ? (!b) : b;
            case 1:
                return (((bt[6] >> (num % 8)) & 1) == 0) ? (!b) : b;
            case 2:
                return (((bt[7] >> (num % 8)) & 1) == 0) ? (!b) : b;
            default:
                return false;
        }

    }

    /**
     * 设置门磁监测，高/低电平 默认00 低电平 蜂鸣器叫, 01 高电平
     */
    public void setMCcheck(boolean b) {
        // aa 55 03 00 71 00 2e
        n = -1;

        lockOrder = new byte[7];

        lockOrder[0] = (byte) 0xAA;
        lockOrder[1] = 0x55;
        lockOrder[2] = 0x03;
        lockOrder[3] = 0x00;
        lockOrder[4] = 0x71;
        if (b) {
            lockOrder[5] = 0x00;
        } else {
            lockOrder[5] = 0x01;
        }
        lockOrder[6] = Constant.calcCrc8(lockOrder, 0, 6);

        lock.start();
        lock.sendData(lockOrder);
    }

    /**
     * 门磁小灯控制，
     *
     * @param b=true时，开启小灯
     */
    public void setMCLamp(boolean b) {
        // aa 55 03 00 55 00 D4
        n = -1;

        lockOrder = new byte[7];

        lockOrder[0] = (byte) 0xAA;
        lockOrder[1] = 0x55;
        lockOrder[2] = 0x03;
        lockOrder[3] = 0x00;
        lockOrder[4] = 0x55;
        if (b) {
            lockOrder[5] = 0x00;
        } else {
            lockOrder[5] = 0x01;
        }
        lockOrder[6] = Constant.calcCrc8(lockOrder, 0, 6);

        lock.start();
        lock.sendData(lockOrder);
    }

    /**
     * 设置485抄表校验位和波特率
     *
     * @param check
     * @param baudrate
     */
    public void set485(String check, String baudrate) {
        n = -1;

        lockOrder = new byte[8];

        lockOrder[0] = (byte) 0xAA;
        lockOrder[1] = 0x55;
        lockOrder[2] = 0x04;
        lockOrder[3] = 0x00;
        lockOrder[4] = 0x61;
        lockOrder[5] = (byte) (Byte.parseByte(check) - 1);
        lockOrder[6] = (byte) (Byte.parseByte(baudrate) - 1);
        lockOrder[7] = Constant.calcCrc8(lockOrder, 0, 7);

        lock.start();
        lock.sendData(lockOrder);
    }

    /**
     * 获取当前湿度
     */
    public void getHumidity() {
        n = 13;
        isBusy = true;
        lockOrder = new byte[6];
        lockOrder[0] = (byte) 0xAA;
        lockOrder[1] = 0x55;
        lockOrder[2] = 0x02;
        lockOrder[3] = 0x00;
        lockOrder[4] = 0x5C;
        lockOrder[5] = Constant.calcCrc8(lockOrder, 0, 5);
        lock.start();
        lock.sendData(lockOrder);
    }

    /**
     * 获取当前温度值
     */
    public void getTemp() {
        n = 12;
        isBusy = true;
        lockOrder = new byte[6];
        lockOrder[0] = (byte) 0xAA;
        lockOrder[1] = 0x55;
        lockOrder[2] = 0x02;
        lockOrder[3] = 0x00;
        lockOrder[4] = 0x5B;
        lockOrder[5] = Constant.calcCrc8(lockOrder, 0, 5);
        lock.start();
        lock.sendData(lockOrder);
    }

    /**
     * 获取主板温度值
     */
    private static final int FC_GET_TEMP_FOR_OTHER = 20;

    public void getTempEx() {
        n = FC_GET_TEMP_FOR_OTHER;
        isBusy = true;
        lockOrder = new byte[6];
        lockOrder[0] = (byte) 0xAA;
        lockOrder[1] = 0x55;
        lockOrder[2] = 0x02;
        lockOrder[3] = 0x00;
        lockOrder[4] = 0x5B;
        lockOrder[5] = Constant.calcCrc8(lockOrder, 0, 5);
        lock.start();
        lock.sendData(lockOrder);
    }

    /**
     * 设置温度补偿
     */
    public void setWDBC(byte b) {
        // aa 55 03 00 6C 01 15
        n = -1;
        isBusy = true;
        lockOrder = new byte[7];
        lockOrder[0] = (byte) 0xAA;
        lockOrder[1] = 0x55;
        lockOrder[2] = 0x03;
        lockOrder[3] = 0x00;
        lockOrder[4] = 0x6C;
        lockOrder[5] = b;
        lockOrder[6] = Constant.calcCrc8(lockOrder, 0, 6);
        lock.start();
        lock.sendData(lockOrder);
    }

    /**
     * 查询温度补偿
     */
    public void querWDBC() {
        // aa 55 02 00 6E E2;
        n = 14;
        isBusy = true;
        lockOrder = new byte[6];
        lockOrder[0] = (byte) 0xAA;
        lockOrder[1] = 0x55;
        lockOrder[2] = 0x02;
        lockOrder[3] = 0x00;
        lockOrder[4] = 0x6E;
        lockOrder[5] = Constant.calcCrc8(lockOrder, 0, 5);
        lock.start();
        lock.sendData(lockOrder);
    }

    /**
     * 查询当前湿度补偿
     */
    public void querySDBC() {
        // AA 55 02 00 6F BC
        n = 15;
        isBusy = true;
        lockOrder = new byte[6];
        lockOrder[0] = (byte) 0xAA;
        lockOrder[1] = 0x55;
        lockOrder[2] = 0x02;
        lockOrder[3] = 0x00;
        lockOrder[4] = 0x6F;
        lockOrder[5] = Constant.calcCrc8(lockOrder, 0, 5);
        lock.start();
        lock.sendData(lockOrder);
    }

    /**
     * 设置湿度补偿
     */
    public void setSDBC(byte b) {
        // aa 55 03 00 6C 01 15
        n = -1;
        isBusy = true;
        lockOrder = new byte[7];
        lockOrder[0] = (byte) 0xAA;
        lockOrder[1] = 0x55;
        lockOrder[2] = 0x03;
        lockOrder[3] = 0x00;
        lockOrder[4] = 0x6D;
        lockOrder[5] = b;
        lockOrder[6] = Constant.calcCrc8(lockOrder, 0, 6);
        lock.start();
        lock.sendData(lockOrder);
    }

    /**
     * 获取锁孔板风扇，加热器控制权，用于调试这些接口
     *
     * @param b=true时，获取控制权
     */
    public void getControl(boolean b) {
        n = -1;
        lockOrder = new byte[7];
        lockOrder[0] = (byte) 0xAA;
        lockOrder[1] = 0x55;
        lockOrder[2] = 0x03;
        lockOrder[3] = 0x00;
        lockOrder[4] = 0x5D;
        if (b) {
            lockOrder[5] = 0x00; // 0 = 获取控制权
        } else {
            lockOrder[5] = 0x01;
        }
        lockOrder[6] = (byte) 0xa2;//Constant.calcCrc8(lockOrder, 0, 6);

        lockOrder[6] = Constant.calcCrc8(lockOrder, 0, 6);
        Constant.byteToHex("获取控制权", 7, lockOrder);

        lock.start();
        lock.sendData(lockOrder);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * 风扇A，风扇B，加热器 手动控制
     *
     * @param channal 风扇A=12，风扇B=25，加热器=123
     */
    public void doDebug(int channal, boolean b) {
        n = -1;
        lockOrder = new byte[7];

        lockOrder[0] = (byte) 0xAA;
        lockOrder[1] = 0x55;
        lockOrder[2] = 0x03;
        lockOrder[3] = 0x00;
        switch (channal) {
            case 12:
                lockOrder[4] = 0x52;
                break;
            case 25:
                lockOrder[4] = 0x53;
                break;
            case 123:
                lockOrder[4] = 0x57;
                break;
            default:
                return;
        }
        lockOrder[5] = (byte) (b ? 0x00 : 0x01); // 0 代表 打开
        lockOrder[6] = Constant.calcCrc8(lockOrder, 0, 6);

        lock.start();
        lock.sendData(lockOrder);
    }

    /**
     * 485抄表
     */
    public void queryPower() {
        isBusy = true;
        n = 11;
        byte[] byte1 = {(byte) 0xAA, 0x55, 0x15, 0x00, 0x64};
        byte[] byte2 = {(byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, 0x68, (byte) 0xAA,
                (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, 0x68, 0x01, 0x02, 0x43, (byte) 0xC3,
                (byte) 0xD5, 0x16};
        byte[] byte3 = byteMerger(byte1, byte2);
        byte[] byte4 = {0x00};
        byte[] byte5 = byteMerger(byte3, byte4);
        byte5[byte5.length - 1] = Constant.calcCrc8(byte5, 0, byte5.length - 1);

        lock.start();
        lock.sendData(byte5);

    }

    public void close485Transfor() {
        isBusy = true;
        n = -1;

        lockOrder = new byte[6];

        lockOrder[0] = (byte) 0xAA;
        lockOrder[1] = 0x55;
        lockOrder[2] = 0x02;
        lockOrder[3] = 0x00;
        lockOrder[4] = 0x65;
        lockOrder[5] = Constant.calcCrc8(lockOrder, 0, 5);

        lock.start();
        lock.sendData(lockOrder);
    }

    public void openLock(String str) {
        boxId = str;
        isBusy = true;
        n = 1;

        Log.e(TAG, "openLock(String str): n = " + n);

        lockOrder = new byte[7];

        lockOrder[0] = (byte) 0xAA;
        lockOrder[1] = 0x55;
        lockOrder[2] = 0x03;
        lockOrder[3] = 0x00;       //lockOrder[3] = calculateBoard(str);
        lockOrder[4] = 0x50;
        lockOrder[5] = (byte) (calculateLock(str) - 1);
        lockOrder[6] = Constant.calcCrc8(lockOrder, 0, 6);

        lock.start();
        lock.sendData(lockOrder);
    }

    /**
     * 设置/烧录 资产编码
     */
    public void setDeviceCode(String code) {
        isBusy = true;
        n = -1;
        lockOrder = new byte[27];

        lockOrder[0] = (byte) 0xAA;
        lockOrder[1] = 0x55;
        lockOrder[2] = 0x17;
        lockOrder[3] = 0x00;
        lockOrder[4] = 0x5F;

        // 029000003001805310000  固定资产编码（原始字符串形式）
        String codeHexString = Constant.strToHex(code);
        // 303239303030303033303031383035333130303030（16进制）

        for(int i = 0; i < codeHexString.length()/2; i++){
            lockOrder[i + 5] = Constant.hexToByte(codeHexString)[i];
        }

        lockOrder[26] = Constant.calcCrc8(lockOrder, 0, 26);
        Constant.byteToHex("设置资产编码：", 27, lockOrder);

        lock.start();
        lock.sendData(lockOrder);
    }

    /**
     * 查询/读取 资产编码
     */
    public void readCodes() {
        n = 6;
        isBusy = true;
        code1.clear();
        code2.clear();
        assetCodeArrays = null;
        boxIdArrays = null;

//        for (iNum = 0; iNum < 3; iNum++) {

            lockOrder = new byte[7];

            lockOrder[0] = (byte) 0xAA;
            lockOrder[1] = 0x55;
            lockOrder[2] = 0x03;
            lockOrder[3] = 0x00;
            lockOrder[4] = 0x60;
            lockOrder[5] = 0x15;
            lockOrder[6] = Constant.calcCrc8(lockOrder, 0, 6);

            Constant.byteToHex("查询资产编码", 7, lockOrder);

            lock.start();
            lock.sendData(lockOrder);

//            try {
//                Thread.sleep(3000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
    }

    /**
     * 开/关 备用小锁
     *
     * @param doorid
     * @param flag
     */
    public void opencloseStandbyDoor(Integer doorid, boolean flag) {
        isBusy = true;
        n = -1;
        lockOrder = new byte[8];

        if ((doorid < 0) || (doorid > MAX_DOOR_NUM)) {
            Log.e(TAG, "opendoor: doorid is Beyond the scope ");
        }

        lockOrder[0] = (byte) 0xAA;
        lockOrder[1] = 0x55;
        lockOrder[2] = 0x04;
        lockOrder[3] = 0x00;
        lockOrder[4] = 0x50;

        switch (doorid) {
            case 0:
                lockOrder[5] = 0x00;
                break;
            case 1:
                lockOrder[5] = 0x01;
                break;
            case 2:
                lockOrder[5] = 0x02;
                break;
            default:
                break;
        }
        lockOrder[6] = 0x05;//5s
        lockOrder[7] = Constant.calcCrc8(lockOrder, 0, 7);

        lock.start();
        lock.sendData(lockOrder);

    }

    /**
     * 开/关 扫码锁
     *
     * @param doorid
     * @param flag   true:open ; false:close
     */
    public void openclosedoor(Integer doorid, boolean flag) {
        isBusy = true;
        n = -1;
        lockOrder = new byte[7];

        if ((doorid < 0) || (doorid > MAX_DOOR_NUM)) {
            Log.e(TAG, "opendoor: doorid is Beyond the scope ");
        }

        lockOrder[0] = (byte) 0xAA;
        lockOrder[1] = 0x55;
        lockOrder[2] = 0x03;
        lockOrder[3] = 0x00;       //lockOrder[3] = calculateBoard(str);

        switch (doorid) {
            case 0:
                lockOrder[4] = 0x52;
                //boxId = "Z00";
                doorId = "door0"; //ch12 副柜
                break;

            case 1:
                lockOrder[4] = 0x53;
                //boxId = "Z18";
                doorId = "door1"; //ch25 副柜
                break;

            case 2:
                lockOrder[4] = 0x55;
                //boxId = "Z00";
                doorId = "door2"; //ch13 主柜
                break;
            default:
                break;
        }

        if (true == flag) {
            lockOrder[5] = (byte) 0x01;

        } else {
            lockOrder[5] = (byte) 0x00;

        }
        lockOrder[6] = Constant.calcCrc8(lockOrder, 0, 6);

        Constant.byteToHex("开关锁：", 7, lockOrder);

        lock.start();
        lock.sendData(lockOrder);


    }

    // 多路开锁
    public void openLock(String[] batchboxid) {
        n = 3;
        isBusy = true;
        batchBoxId = null;
        batchBoxId = new String[batchboxid.length];

        HasOpened = null;
        HasOpened = new boolean[batchboxid.length];
        iNum = 0;
        for (String s : batchboxid) {
            boxId = s;
            batchBoxId[iNum] = s;
            HasOpened[iNum] = false;

            lockOrder = new byte[7];

            lockOrder[0] = (byte) 0xAA;
            lockOrder[1] = 0x55;
            lockOrder[2] = 0x03;
            lockOrder[3] = calculateBoard(s);
            lockOrder[4] = 0x50;
            lockOrder[5] = (byte) (calculateLock(s) - 1);
            lockOrder[6] = Constant.calcCrc8(lockOrder, 0, 6);

            if (!s.equals("Z08")) {
                lock.start();
                lock.sendData(lockOrder);
            }

            try {
                Thread.sleep(1300);
            } catch (InterruptedException e) {
                Log.v(TAG, "sleep(1000)异常");
            }
            // if (iNum < batchboxid.length - 1) {
            // iNum++;
            // }
            iNum++;
        }
    }

    // 查询锁状态
    public void queryLock(String str) {
        n = 1;
        isBusy = true;
        boxId = str;

        lockOrder = new byte[6];

        lockOrder[0] = (byte) 0xAA;
        lockOrder[1] = 0x55;
        lockOrder[2] = 0x02;
        lockOrder[3] = calculateBoard(str);
        lockOrder[4] = 0x51;
        lockOrder[5] = Constant.calcCrc8(lockOrder, 0, 5);

        lock.start();
        lock.sendData(lockOrder);
    }

    public void queryLockTest(String str) {
        n = 16;
        isBusy = true;
        boxId = str;

        lockOrder = new byte[6];

        lockOrder[0] = (byte) 0xAA;
        lockOrder[1] = 0x55;
        lockOrder[2] = 0x02;
        lockOrder[3] = calculateBoard(str);
        lockOrder[4] = 0x51;
        lockOrder[5] = Constant.calcCrc8(lockOrder, 0, 5);

        lock.start();
        lock.sendData(lockOrder);
    }

    // 多路查询
    public void queryLock(String[] batchboxid) {
        n = 3;
        isBusy = true;
        batchBoxId = null;
        batchBoxId = new String[batchboxid.length];

        HasOpened = null;
        HasOpened = new boolean[batchboxid.length];

        iNum = 0;
        for (String s : batchboxid) {
            boxId = s;
            Constant.writeLog("多路查询----> " + s);
            batchBoxId[iNum] = s;
            HasOpened[iNum] = false;

            lockOrder = new byte[6];

            lockOrder[0] = (byte) 0xAA;
            lockOrder[1] = 0x55;
            lockOrder[2] = 0x02;
            lockOrder[3] = calculateBoard(s);
            lockOrder[4] = 0x51;
            lockOrder[5] = Constant.calcCrc8(lockOrder, 0, 5);

            lock.start();
            lock.sendData(lockOrder);
            try {
                Thread.sleep(130);
            } catch (InterruptedException e) {
                Log.v(TAG, "sleep(1000)异常");
            }
            iNum++;
        }
    }

    // 已打开格口测试
    public void queryAll(String s) {
        n = 5;
        isBusy = true;
        list.clear();
        String str[] = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16",
                "17", "18", "19", "20", "21", "22"};

        ArrayList<String> listBoxId = new ArrayList<String>();

        if (s.length() % 3 != 0) {
            return;
        }

        while (true) {
            String s1 = s.substring(0, 1);
            if (s1.equalsIgnoreCase("Z")) {
                for (int s5 = 1; s5 < 8; s5++) {
                    listBoxId.add("Z0" + s5);
                }
            } else {
                for (int s5 = 0; s5 < 22; s5++) {
                    listBoxId.add(s1 + str[s5]);
                }
            }
            if (s.length() == 3) {
                break;
            } else {
                s = s.substring(3, s.length());
            }
        }

        batchBoxId = null;
        batchBoxId = listBoxId.toArray(new String[listBoxId.size()]);

        for (iNum = 0; iNum <= batchBoxId.length - 1; iNum++) {
            boxId = batchBoxId[iNum];

            lockOrder = new byte[6];

            lockOrder[0] = (byte) 0xAA;
            lockOrder[1] = 0x55;
            lockOrder[2] = 0x02;
            lockOrder[3] = calculateBoard(batchBoxId[iNum]);
            lockOrder[4] = 0x51;
            lockOrder[5] = Constant.calcCrc8(lockOrder, 0, 5);

            lock.start();
            lock.sendData(lockOrder);
            try {
                Thread.sleep(130);
            } catch (InterruptedException e) {
                Log.v(TAG, "sleep(1000)异常");
            }
        }
    }

    // 门磁状态监测
    public static boolean status = false;

    public void queryStatus() {
        status = true;
    }

    public void concelQueryStatus() {
        status = false;
    }

    public void ifDoorOpened() {
        n = 8;

        lockOrder = new byte[6];

        lockOrder[0] = (byte) 0xAA;
        lockOrder[1] = 0x55;
        lockOrder[2] = 0x02;
        lockOrder[3] = 0x00; // 板地址
        lockOrder[4] = 0x56;
        lockOrder[5] = Constant.calcCrc8(lockOrder, 0, 5);

        lock.start();
        lock.sendData(lockOrder);
    }

    // 主柜 00
    public void openZlamp() {
        isBusy = true;
        n = -1;

        lockOrder = new byte[7];

        lockOrder[0] = (byte) 0xAA;
        lockOrder[1] = 0x55;
        lockOrder[2] = 0x03;
        lockOrder[3] = 0x00; // 板地址
        lockOrder[4] = 0x54;
        lockOrder[5] = 0x00; // 0为开灯，1为关灯
        lockOrder[6] = Constant.calcCrc8(lockOrder, 0, 6);

        lock.start();
        lock.sendData(lockOrder);
    }

    public void closeZlamp() {
        isBusy = true;
        n = -1;

        lockOrder = new byte[7];

        lockOrder[0] = (byte) 0xAA;
        lockOrder[1] = 0x55;
        lockOrder[2] = 0x03;
        lockOrder[3] = 0x00; // 板地址
        lockOrder[4] = 0x54;
        lockOrder[5] = 0x01; // 0为开灯，1为关灯
        lockOrder[6] = Constant.calcCrc8(lockOrder, 0, 6);

        lock.start();
        lock.sendData(lockOrder);
    }

    private byte calculateBoard(String string) {
        String str = string.substring(0, 1);
        if (str.equals("Z")) {
            return 0x00;
        } else if (str.equals("A")) {
            return 0x01;
        } else if (str.equals("B")) {
            return 0x02;
        } else if (str.equals("C")) {
            return 0x03;
        } else if (str.equals("D")) {
            return 0x04;
        } else if (str.equals("E")) {
            return 0x05;
        } else if (str.equals("F")) {
            return 0x06;
        } else if (str.equals("G")) {
            return 0x07;
        } else if (str.equals("H")) {
            return 0x08;
        } else if (str.equals("I")) {//add by yazhou for one conn twelve
            return 0x09;
        } else if (str.equals("J")) {
            return 0x0a;
        } else if (str.equals("K")) {
            return 0x0b;
        } else if (str.equals("L")) {
            return 0x0c;
        } else {
            return 0x0f;
        }
    }

    private byte calculateLock(String string) {
        if (string.substring(0, 1).equalsIgnoreCase("Z")) {
            if (Integer.parseInt(string.substring(1, 3)) == 99) {
                return 8;
            } else if (Integer.parseInt(string.substring(1, 3)) == 0) {
                return 0x0d;
            } else {
                return (byte) Integer.parseInt(string.substring(1, 3));
            }
        } else {
            return (byte) Integer.parseInt(string.substring(1, 3));
        }
    }

    public boolean getIsBusy() {
        return isBusy;
    }

    // 开灯
    public void openLamp() {
        n = -1;
        isBusy = true;
        for (iNum = 1; iNum < /*9*/13; iNum++) {//modify by yazhou
            lockOrder = new byte[7];

            lockOrder[0] = (byte) 0xAA;
            lockOrder[1] = 0x55;
            lockOrder[2] = 0x03;
            lockOrder[3] = (byte) iNum; // 板地址
            lockOrder[4] = 0x54;
            lockOrder[5] = 0x00;
            lockOrder[6] = Constant.calcCrc8(lockOrder, 0, 6);

            lock.start();
            lock.sendData(lockOrder);
            try {
                Thread.sleep(120);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        isBusy = false;

        // 开副柜灯的时候，检查log文件，超过15天的删除
        if (checkLogFile) {
            checkLogFile = false;
            try {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        // 删除过期的串口log
                        deleteExpirationFile("/storage/sdcard0/rocktech_log");
                        // 删除过期的crash信息
                        deleteExpirationFile("/storage/sdcard0/rocktech_crash");
                        // 如果老版本路径下的carsh信息，删除
                        File crashLog = new File("/storage/sdcard0/rocktech_log/crash");
                        if (crashLog.exists()) {
                            if (crashLog.isDirectory()) {
                                File[] files = crashLog.listFiles();
                                for (File f : files) {
                                    f.delete();
                                }
                            }
                            crashLog.delete();
                        }
                    }
                }).start();
            } catch (Exception e) {
                // TODO: handle exception
                checkLogFile = true;
            }
            checkLogFile = true;
        }
    }

    /**
     * 文件排序
     *
     * @param fs
     */
    public static void orderByDate(File[] fs) {
        Arrays.sort(fs, new Comparator<File>() {
            public int compare(File f1, File f2) {
                long diff = f1.lastModified() - f2.lastModified();
                if (diff > 0)
                    return 1;
                else if (diff == 0)
                    return 0;
                else
                    return -1;
            }

            public boolean equals(Object obj) {
                return true;
            }

        });
    }

    /*
     * 只保留15天的log信息
     */
    private static void deleteExpirationFile(String str) {
        File dir = new File(str);
        File[] files = dir.listFiles();
        long expirationTime = 15 * 24 * 3600 * 1000;
        long nowTime = System.currentTimeMillis();
        List<File> deleteList = new ArrayList<File>();

        if (getFileSize(dir) >= 1024) {
            while (true) {
                orderByDate(files);
                files[0].delete();
                if (getFileSize(dir) < 800) {
                    break;
                }
            }
        }

        if (dir.isDirectory()) {
            for (File f : files) {
                if (f.exists()) {
                    long time = f.lastModified();
                    if (nowTime - time >= expirationTime) {
                        deleteList.add(f);
                    }
                }
            }
            for (File f : deleteList) {
                f.delete();
            }
        }

    }

    /**
     * 获取文件夹大小
     *
     * @param f
     * @return MB
     */
    private static long getFileSize(File f) {
        long size = 0;
        File flist[] = f.listFiles();
        if (flist != null) {
            for (int i = 0; i < flist.length; i++) {
                if (flist[i].isDirectory()) {
                    size = size + getFileSize(flist[i]);
                } else {
                    size = size + flist[i].length();
                }
            }
            return size / 1048576;
        } else {
            return 0;
        }
    }

    // 关灯
    public void closeLamp() {
        n = -1;
        isBusy = true;
        for (iNum = 1; iNum < /*9*/13; iNum++) {//modify by yazhou

            lockOrder = new byte[7];

            lockOrder[0] = (byte) 0xAA;
            lockOrder[1] = 0x55;
            lockOrder[2] = 0x03;
            lockOrder[3] = (byte) iNum; // 板地址
            lockOrder[4] = 0x54;
            lockOrder[5] = 0x01;
            lockOrder[6] = Constant.calcCrc8(lockOrder, 0, 6);

            lock.start();
            lock.sendData(lockOrder);
            try {
                Thread.sleep(120);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    String[] assetCodeArrays;
    String[] boxIdArrays;

    public void writeCode(byte[] codes) {
        n = -1;
        isBusy = true;
        lock.start();
        lock.sendData(codes);
    }

    public static byte[] subBytes(byte[] src, int begin, int count) {
//         if (count <= 0) {
//         return null;
//         }

        byte[] bs = new byte[16];//默认数组长度暂设为16
        if (count >= 0) {
            bs = new byte[count];
            for (int i = begin; i < begin + count; i++) {
                bs[i - begin] = src[i];
            }
        }

        return bs;
    }

    public void writeCodes(String[] assetCodeArrays2, String[] boxIdArrays2) {
        n = 7;
        isBusy = true;
        int j = 1;
        code1.clear();
        code2.clear();
        assetCodeArrays = null;
        boxIdArrays = null;

        String[] assetCodeArrays3 = assetCodeArrays2;
        String[] boxIdArrays3 = boxIdArrays2;

        for (int i = 0; i < boxIdArrays3.length; i++) {
            if (Integer.parseInt(assetCodeArrays3[i].substring(1, 3)) != 0x00) {
                byte[] code1 = {(byte) 0xAA, (byte) j, 0x1D, 0x07, 0x00, 0x18,
                        boxIdArrays3[i].substring(0, 1).getBytes()[0], boxIdArrays3[i].substring(1, 2).getBytes()[0],
                        boxIdArrays3[i].substring(2, 3).getBytes()[0]};
                byte[] code2 = assetCodeArrays3[i].getBytes();
                byte[] codes = byteMerger(code1, code2);
                byte[] code3 = {Constant.calcCrc8(codes, 0, 29, (byte) 0), (byte) 0xff};
                byte[] codes_fl = byteMerger(codes, code3);
                // Constant.byteToHex("send", codes_fl.length, codes_fl);
                lock.start();
                lock.sendData(codes_fl);
                try {
                    System.out.println(new String(subBytes(codes_fl, 6, 3), "UTF-8"));
                    System.out.println(new String(subBytes(codes_fl, 9, 21), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                j++;
            } else {
                byte[] code1 = {(byte) 0xAA, 0x00, 0x1D, 0x08, 0x18, boxIdArrays3[i].substring(0, 1).getBytes()[0],
                        boxIdArrays3[i].substring(1, 2).getBytes()[0], boxIdArrays3[i].substring(2, 3).getBytes()[0]};
                byte[] code2 = assetCodeArrays3[i].getBytes();
                byte[] codes = byteMerger(code1, code2);
                byte[] code3 = {Constant.calcCrc8(codes, 0, 29, (byte) 0), (byte) 0xff};
                byte[] codes_fl = byteMerger(codes, code3);
                // Constant.byteToHex("send", codes_fl.length, codes_fl);
                lock.start();
                lock.sendData(codes_fl);
                try {
                    System.out.println(new String(subBytes(codes_fl, 5, 3), "UTF-8"));
                    System.out.println(new String(subBytes(codes_fl, 8, 21), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            try {
                Thread.sleep(120);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static byte[] byteMerger(byte[] byte_1, byte[] byte_2) {
        byte[] byte_3 = new byte[byte_1.length + byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }

    /**
     * 打开/关闭 蜂鸣器
     *
     * @param b-true，打开
     */
    public void setBuzzer(boolean b) {
        n = -1;

        lockOrder = new byte[7];

        lockOrder[0] = (byte) 0xAA;
        lockOrder[1] = 0x55;
        lockOrder[2] = 0x03;
        lockOrder[3] = 0x00;
        lockOrder[4] = 0x5E;
        lockOrder[5] = (byte) (b ? 1 : 0);
        lockOrder[6] = Constant.calcCrc8(lockOrder, 0, 6);

        lock.start();
        lock.sendData(lockOrder);
    }

    /**
     * 设置 温度上阀值，下阀值
     *
     * @param i     58=ch12风扇,59=ch25风扇,5A=加热器
     * @param open  2个字节的打开温度
     * @param close 2个字节的关闭温度
     */
    public void setControl(int i, String open, String close) throws Exception {
        n = -1;
        int btO = (int) (Double.parseDouble(open) * 10 + 500);
        int btC = (int) (Double.parseDouble(close) * 10 + 500);

        if (btO >= 0 && btO <= 1500 && btC >= 0 && btC <= 1500) {
            lockOrder = new byte[10];

            lockOrder[0] = (byte) 0xAA;
            lockOrder[1] = 0x55;
            lockOrder[2] = 0x06;
            lockOrder[3] = 00;
            lockOrder[4] = (byte) i;
            lockOrder[5] = (byte) (btO >> 8);
            lockOrder[6] = (byte) (btO & 0xFF);
            lockOrder[7] = (byte) (btC >> 8);
            lockOrder[8] = (byte) (btC & 0xFF);

            lockOrder[9] = Constant.calcCrc8(lockOrder, 0, 9);

            lock.start();
            lock.sendData(lockOrder);
        } else {
            throw new Exception();
        }

    }

    public void query485Setting() {
        n = 17;

        lockOrder = new byte[6];

        lockOrder[0] = (byte) 0xAA;
        lockOrder[1] = 0x55;
        lockOrder[2] = 0x02;
        lockOrder[3] = 0x00;
        lockOrder[4] = 0x70;
        lockOrder[5] = Constant.calcCrc8(lockOrder, 0, 5);

        lock.start();
        lock.sendData(lockOrder);
    }

    public void querySetting(int i) {
        n = 18;

        lockOrder = new byte[6];

        lockOrder[0] = (byte) 0xAA;
        lockOrder[1] = 0x55;
        lockOrder[2] = 0x02;
        lockOrder[3] = 0x00;
        lockOrder[4] = (byte) i;
        lockOrder[5] = Constant.calcCrc8(lockOrder, 0, 5);

        lock.start();
        lock.sendData(lockOrder);
    }

    public void queryMCsetting() {
        n = 19;

        lockOrder = new byte[6];

        lockOrder[0] = (byte) 0xAA;
        lockOrder[1] = 0x55;
        lockOrder[2] = 0x02;
        lockOrder[3] = 0x00;
        lockOrder[4] = 0x72;
        lockOrder[5] = Constant.calcCrc8(lockOrder, 0, 5);

        lock.start();
        lock.sendData(lockOrder);
    }

    public void SwitchWorkMode() {
        n = 20;
        lock.start();
        lock.sendData(new byte[]{(byte) 0xaa, 0x55, 0x02, 0x00, 0x73, (byte) 0x82});
    }

    /**
     * 将升级固件转化为byte[]
     *
     * @return byte[] or null
     */
    private byte[] binToByte() {
        File file = new File(mPath);
        if (!file.exists()) {
            return null;
        }

        FileInputStream fis = null;
        byte[] a = new byte[(int) file.length()];
        try {
            fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            bis.read(a);

            bis.close();
            return a;
        } catch (Exception e) {
            // TODO
            return null;
        }
    }

    private static byte[] total = null;
    private static int transportNum = 0;

    public void doUpdate() {
        n = 21;

        if (total == null) {
            total = binToByte();
            transportNum = 0;
        }

        byte[] b1 = new byte[7];
        byte[] b4 = {0x00};
        byte[] b3;

        if (total != null) {
            Constant.writeLog("total != null");
            if (total.length > 512) {
                b1[0] = 0x07;
                b1[1] = 0x0e;
                b1[2] = 0x02;
                b1[3] = 0x03;
                b1[4] = 0x55;
                b1[5] = (byte) 0xaa;
                b1[6] = (byte) transportNum;

                byte[] b2 = subBytes(total, 0, 512);
                for (byte b : b2) {
                    b4[0] += b;
                }
                b4[0] = (byte) ((b1[2] * 256 + b1[3] + b1[4] + b1[5] + b1[6] + b4[0]) & 0xFF);
                b3 = byteMerger(b1, b2);

                lock.start();
                lock.sendData(byteMerger(b3, b4));

            } else {
                int length = total.length + 3;
                b1[0] = 0x07;
                b1[1] = 0x0e;
                b1[2] = (byte) (length >> 8);
                b1[3] = (byte) (length & 0xff);
                b1[4] = 0x55;
                b1[5] = (byte) 0xaa;
                b1[6] = (byte) transportNum;

                byte[] b2 = total;
                for (byte b : b2) {
                    b4[0] += b;
                }

                b4[0] = (byte) ((b1[2] * 256 + b1[3] + b1[4] + b1[5] + b1[6] + b4[0]) & 0xFF);
                b3 = byteMerger(b1, b2);

                lock.start();
                lock.sendData(byteMerger(b3, b4));
            }
        }
    }

    public static Integer getMaxDoorNum() {
        return MAX_DOOR_NUM;
    }

    /**
     * 获取当前app分配内存的使用情况
     * @return
     */
    public String getAppAvailMemory() {
        float maxMemory = (float) (Runtime.getRuntime().maxMemory() * 1.0/ (1024 * 1024));
        //当前分配的总内存
        float totalMemory = (float) (Runtime.getRuntime().totalMemory() * 1.0/ (1024 * 1024));
        //剩余内存
        float freeMemory = (float) (Runtime.getRuntime().freeMemory() * 1.0/ (1024 * 1024));

        Log.i(TAG, "maxMemory : "+maxMemory+"; totalMemory : "+totalMemory+"; freeMemory : "+freeMemory);

        return String.valueOf(freeMemory/totalMemory);// 将获取的内存大小规格化
    }

    private  long getAvailableMemory() {
        ActivityManager am = (ActivityManager) mBase.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        return mi.availMem;
    }

    /**
     * 获取手机内存使用情况
     */
    private String getMemoryUsePenter() {
        String dir = "/proc/meminfo";
        try {
            FileReader fr = new FileReader(dir);
            BufferedReader br = new BufferedReader(fr, 2048);
            String memoryLine = br.readLine();
            String subMemoryLine = memoryLine.substring(memoryLine.indexOf("MemTotal:"));
            br.close();
            long totalMemorySize = Integer.parseInt(subMemoryLine.replaceAll("\\D+", ""));
            long availableSize = getAvailableMemory() / 1024;
            int percent = (int) ((totalMemorySize - availableSize) / (float) totalMemorySize);
            return percent+"";
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "无结果";
    }

    /**
     * 上报系统信息
     */
    public void uploadSysInfo(){
        ShareBookMessage msg = new ShareBookMessage();
        msg.head = new headMsg();
        msg.body = new bodyMsg();
        msg.body.bizObject = new bizObject();

        String deviceCode = SpHelper.getStringValue("deviceCode");
        msg.body.bizObject.setF_VendingMachine(deviceCode);//资产编码
        msg.body.bizObject.setF_MemoryUsage(getMemoryUsePenter());

        String message = JSON.toJSONString(msg);

        RequestManager.getInstance(mBase).requestPostByAsyn("vendingMachineIndicator/save"
                , message
                , new RequestManager.ReqCallBack<String>() {
                    @Override
                    public void onReqSuccess(String result) {
                        Log.d(TAG,"onReqSuccess()---> 上报系统信息成功...");
                    }

                    @Override
                    public void onReqFailed(String errorMsg) {
                        Log.d(TAG,"onReqFailed()---> 上报系统信息："+errorMsg);
                    }
                });
    }

    /**
     * 上报书柜盘点结果
     */
    public void reportInventoryBooks(int bookTotalCount, String bookRfids){
        ShareBookMessage msg = new ShareBookMessage();
        msg.head = new headMsg();
        msg.body = new bodyMsg();
        msg.body.bizObject = new bizObject();

        String deviceCode = SpHelper.getStringValue("deviceCode");
        msg.body.bizObject.setTotalCount(String.valueOf(bookTotalCount));
        msg.body.bizObject.setBookcase(deviceCode);//资产编码
        msg.body.bizObject.setBooks(bookRfids);

        String message = JSON.toJSONString(msg);

        Log.i(TAG, "书柜盘点上报, message : "+message);

        RequestManager.getInstance(mBase).requestPostByAsyn("stockCheck/stockBySelf"
                , message
                , new RequestManager.ReqCallBack<String>() {
                    @Override
                    public void onReqSuccess(String result) {
                        Log.d(TAG,"onReqSuccess()---> 书柜盘点上报服务器成功! "+result);
                    }

                    @Override
                    public void onReqFailed(String errorMsg) {
                        Log.d(TAG,"onReqFailed()---> 书柜盘点上报服务器失败："+errorMsg);
                    }
                });
    }
}
