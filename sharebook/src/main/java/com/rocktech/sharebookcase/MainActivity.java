package com.rocktech.sharebookcase;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.core.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.util.TypeUtils;
import com.module.interaction.ModuleConnector;
import com.nativec.tools.ModuleManager;
import com.rfid.RFIDReaderHelper;
import com.rfid.ReaderConnector;
import com.rfid.rxobserver.RXObserver;
import com.rfid.rxobserver.bean.RXInventoryTag;
import com.rocktech.sharebook.R;
import com.rocktech.sharebookcase.msgdata.BookInfo;
import com.rocktech.sharebookcase.msgdata.ShareBookMessage;
import com.rocktech.sharebookcase.msgdata.bizObject;
import com.rocktech.sharebookcase.msgdata.bodyMsg;
import com.rocktech.sharebookcase.msgdata.headMsg;
import com.rocktech.sharebookcase.tool.Constant;
import com.rocktech.sharebookcase.tool.SpHelper;
import com.tools.ExcelUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android_serialport_api.Locker;

public class MainActivity extends BaseActivity {
    String TAG = "RockTech MainActivity";
    private final boolean debug_verbose = true;

    private final Integer DOOR_CLOSE_TIMEOUT = 5000;
    private final Integer TERMINAL_MAX_TAG_NUM = 500;
    private static final int ISFIRSTINVENTY = 10;
    private static final int LASTINVENTY = 20;
    private static final int COMPAREANDCOPY = 30;
    private static final int INIRFIDMODULE = 40;

    private boolean initRfidModule;//是否已经进行过初始化操作
    private boolean inventoryFlag;//书柜盘点标识

    private static final int OPENDOOR = 100;
    private static final int CLOSEDOOR = 102;
    private static final int UPDATEINFO = 200;
    private static final int REGISTERTERM = 400;
    private static final int DOOROPENED = 800;
    private static final int CANCEQUERY = 1600;

    private final int CABINET_ATENNA_MAX_NUM = 6;
    private final int RFID_ATENNA_MAX_NUM = 8;

    private Locker locker = null;
    private Integer mdoorid = -1;
    private int rfidaddr = 0x01;

    private Integer msender = 0;
    private Integer mReceiver = 0;

    private Timer inquerytimer = new Timer();
    ModuleConnector connector = new ReaderConnector();//构建连接器
    RFIDReaderHelper mReader;

    private int index = 0;
    private int lastIndex = 0;
    private int curIndex = 0;
    private int lentnum = 0;
    private int givebacknum = 0;

    private int index2 = 0;
    private int lastIndex2 = 0;
    private int curIndex2 = 0;
    private int lentnum2 = 0;
    private int givebacknum2 = 0;

    private int index3 = 0;
    private int lastIndex3 = 0;
    private int curIndex3 = 0;
    private int lentnum3 = 0;
    private int givebacknum3 = 0;

    private int lentOutnum = 0;
    private int backInnum = 0;

    private String[] InitTagData = new String[TERMINAL_MAX_TAG_NUM];  //第一次盘库数据
    private String[] InitTagData2 = new String[TERMINAL_MAX_TAG_NUM];  //第一次盘库数据
    private String[] InitTagData3 = new String[TERMINAL_MAX_TAG_NUM];  //第一次盘库数据

    private String[] CurrentTagData = new String[TERMINAL_MAX_TAG_NUM]; // 当前拿到的数据
    private String[] CurrentTagData2 = new String[TERMINAL_MAX_TAG_NUM]; // 当前拿到的数据
    private String[] CurrentTagData3 = new String[TERMINAL_MAX_TAG_NUM]; // 当前拿到的数据

    private String[] lastTagData = new String[TERMINAL_MAX_TAG_NUM];  //每次提取出 减少或增加标签后 被覆盖的数据
    private String[] lastTagData2 = new String[TERMINAL_MAX_TAG_NUM];  //每次提取出 减少或增加标签后 被覆盖的数据
    private String[] lastTagData3 = new String[TERMINAL_MAX_TAG_NUM];  //每次提取出 减少或增加标签后 被覆盖的数据


    private String[] lendData_cabinet1 = new String[TERMINAL_MAX_TAG_NUM];//书柜1借出的书的RFID数组集合
    private String[] lendData_cabinet2 = new String[TERMINAL_MAX_TAG_NUM];//书柜2借出的书的RFID数组集合
    private String[] lendData_cabinet3 = new String[TERMINAL_MAX_TAG_NUM];
    private String[] lendOutData = new String[TERMINAL_MAX_TAG_NUM];

    private String[] giveBackData_cabinet1 = new String[TERMINAL_MAX_TAG_NUM];//书柜1归还的书的RFID数组集合
    private String[] giveBackData_cabinet2 = new String[TERMINAL_MAX_TAG_NUM];
    private String[] giveBackData_cabinet3 = new String[TERMINAL_MAX_TAG_NUM];
    private String[] backInData = new String[TERMINAL_MAX_TAG_NUM];

    //本地广播数据类型实例。
    private LocalBroadcastManager localBroadcastManager;
    private IntentFilter intentFilter;
    private LocalReceiver localReceiver;

    private boolean compare(String tmp, int doorid) {
        if (doorid == 0) {
            for (int i = 0; i < index; i++) {
                if (tmp.equals(InitTagData[i])) {
                    return true;
                }
            }
            return false;
        } else if (doorid == 1) {

            for (int i = 0; i < index2; i++) {
                if (tmp.equals(InitTagData2[i])) {
                    return true;
                }
            }
            return false;
        } else if (doorid == 2) {
            for (int i = 0; i < index3; i++) {
                if (tmp.equals(InitTagData3[i])) {
                    return true;
                }
            }
            return false;
        } else {
            Log.e(TAG, "doorid  error ");
            return false;
        }
    }

    private boolean compareCurArraryelement(String tmp, int doorid) {
        if (doorid == 0) {
            for (int i = 0; i < curIndex; i++) {
                if (tmp.equals(CurrentTagData[i])) {
                    return true;
                }
            }
            return false;
        } else if (doorid == 1) {
            for (int i = 0; i < curIndex2; i++) {
                if (tmp.equals(CurrentTagData2[i])) {
                    return true;
                }
            }
            return false;
        } else if (doorid == 2) {
            for (int i = 0; i < curIndex3; i++) {
                if (tmp.equals(CurrentTagData3[i])) {
                    return true;
                }
            }
            return false;
        } else {
            Log.e(TAG, "doorid  error ");
            return false;
        }
    }

    private String[] ComputeDiffArray(String first[], int firstlenth, String second[], int seclenth, boolean islent, int doorid) {
        int i = 0;
        int j = 0;
        int num = 0;
        String diff[] = new String[TERMINAL_MAX_TAG_NUM];
        boolean print_verbose_info = true;
        for (i = 0; i < firstlenth; i++) {
            for (j = 0; j < seclenth; j++) {
                if (print_verbose_info) {
//                    Log.i(TAG, "ComputeDiffArray: first[" + i + "]= " + first[i]);
//                    Log.i(TAG, "ComputeDiffArray: second[" + j + "]= " + second[j]);
                }
                if (first[i].equals(second[j])) {
//                    Log.i(TAG, "ComputeDiffArray: got equal tag");
                    break;
                }
            }
//            Log.i(TAG, "ComputeDiffArray:  j =" + j + "seclenth = " + seclenth);
            if (j == seclenth) {
                //书被借走了
//                Log.i(TAG, "ComputeDiffArray: tag = " + first[i] + "is lended by someone");
                diff[num] = first[i];
                num++;
            }
        }
        if (islent) {
            //借书数量
            if (doorid == 1) {
                lentnum = num;
            } else if (doorid == 0) {
                lentnum2 = num;
            } else if (doorid == 2) {
                lentnum3 = num;
            } else {
                Log.e(TAG, " doorid is error");
            }

        } else {
            //还书数量
            if (doorid == 1) {
                givebacknum = num;
            } else if (doorid == 0) {
                givebacknum2 = num;
            } else if (doorid == 2) {
                givebacknum3 = num;
            } else {
                Log.e(TAG, " giveback doorid is error");
            }

        }
        return diff;
    }

    ArrayAdapter adapter1, adapter2;
    ArrayList<String> listLendData = new ArrayList<String>();
    ArrayList<String> listBackInData = new ArrayList<String>();

    Handler rfidHandler = new Handler() { //RFIDReaderHelper通过RXObserver的回调
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == ISFIRSTINVENTY) {
                String epc = (String) msg.obj;
                int rfid = msg.arg1;
                int antennaid = msg.arg2;
                //判断是哪个射频模块 哪个天线
                if (rfid == 0) {//第一个模块
                    if (antennaid < CABINET_ATENNA_MAX_NUM) {//0~5
                        if (!compare(epc.replace("", ""), 0)) {
                            InitTagData[index] = epc.replace(" ", "");
                            //Log.i(TAG, "handleMessage: FIRSTINVENTY InitTagData = " + Arrays.toString(InitTagData) );
                            index++;
                        }
                    } else if ((antennaid >= CABINET_ATENNA_MAX_NUM) && (antennaid <= (RFID_ATENNA_MAX_NUM - 1))) {// 6~7
                        Log.e(TAG, " rfid 0 should not use antenna 7 and 8");
                    } else {
                        Log.e(TAG, "rfid0 not a corret rfid antenna ");
                    }

                } else if (rfid == 1) {//第二个模块
                    if (antennaid < CABINET_ATENNA_MAX_NUM) {//0~5
                        if (!compare(epc.replace(" ", ""), 1)) {
                            InitTagData2[index2] = epc.replace(" ", "");
                            //Log.i(TAG, "handleMessage: FIRSTINVENTY InitTagData2 = " + Arrays.toString(InitTagData2) );
                            index2++;
                        }
                    } else if ((antennaid >= CABINET_ATENNA_MAX_NUM) && (antennaid <= (RFID_ATENNA_MAX_NUM - 1))) {// 6~7
                        Log.e(TAG, " rfid 1 should not use antenna  7 and  8");
                    } else {
                        Log.e(TAG, "not a corret rfid antenna ");
                    }
                } else if (rfid == 2) {//第三个个模块
                    //主柜中的其中两个天线连接的 6 7 天线
                    if (!compare(epc.replace(" ", ""), 2)) {
                        InitTagData3[index3] = epc.replace(" ", "");
                        //Log.i(TAG, "handleMessage: FIRSTINVENTY InitTagData3 = " + Arrays.toString(InitTagData3) );
                        index3++;
                    }
                } else {
                    Log.e(TAG, "not a corret rfid module");
                }
            } else if (msg.what == LASTINVENTY) {
                // 第一次关门 与初始化数据比较
                String epc = (String) msg.obj;
                int rfid = msg.arg1;
                int antennaid = msg.arg2;
                // 判断是哪个射频模块 哪个天线
                if (rfid == 0) {
                    if (antennaid < CABINET_ATENNA_MAX_NUM) {//0~5
                        if (!compareCurArraryelement(epc.replace(" ", ""), 0)) {
                            CurrentTagData[curIndex] = epc.replace(" ", "");
                            curIndex++;
                        }

                    } else if ((antennaid >= CABINET_ATENNA_MAX_NUM) && (antennaid <= (RFID_ATENNA_MAX_NUM - 1))) {
                        Log.e(TAG, "rfid0 not a corret rfid antenna 7 and 8 ");
                    } else {
                        Log.e(TAG, "rfid0 not a corret rfid antenna ");
                    }
                } else if (rfid == 1) {//第二个模块
                    if (antennaid < CABINET_ATENNA_MAX_NUM) {
                        if (!compareCurArraryelement(epc.replace(" ", ""), 1)) {
                            CurrentTagData2[curIndex2] = epc.replace(" ", "");
                            curIndex2++;
                            Log.i(TAG, "handleMessage: LASTINVENTY CurrentTagData2 = " + Arrays.toString(CurrentTagData2));
                        }
                    } else if ((antennaid >= CABINET_ATENNA_MAX_NUM) && (antennaid <= (RFID_ATENNA_MAX_NUM - 1))) {
                        Log.e(TAG, "rfid1 not a corret rfid antenna 7 and 8 ");
                    } else {
                        Log.e(TAG, "rfid1 not a corret rfid antenna ");
                    }
                } else if (rfid == 2) {//第三个模块
                    //主柜中的其中两个天线 连接的 6 7 天线
                    if (!compareCurArraryelement(epc.replace(" ", ""), 2)) {
                        CurrentTagData3[curIndex3] = epc.replace(" ", "");
                        curIndex3++;
                    }
                } else {
                    Log.e(TAG, "not a corret rfid module");
                }
            } else if (msg.what == COMPAREANDCOPY) {
                if (ExcelUtils.isFirstInventy || inventoryFlag) {
                    // 这个if分支的代码需要优化，由于SDK是单例的，一次只能对其中两个数组进行操作。
                    Log.v(TAG, " COMPAREANDCOPY firstInventy  index = " + index);
                    Log.v(TAG, " COMPAREANDCOPY firstInventy  index2 = " + index2);
                    Log.v(TAG, " COMPAREANDCOPY firstInventy  index3 = " + index3);
                    /*Log.i(TAG, "handleMessage: InitTagData = " + Arrays.toString(InitTagData));
                    Log.i(TAG, "handleMessage: InitTagData2 = " + Arrays.toString(InitTagData2));
                    Log.i(TAG, "handleMessage: InitTagData3 = " + Arrays.toString(InitTagData3));*/
                    System.arraycopy(InitTagData, 0, lastTagData, 0, index);  //copy 第一次盘库数据到lastTagdata，为下一次数据比较做准备
                    System.arraycopy(InitTagData2, 0, lastTagData2, 0, index2);  //copy 第一次盘库数据到lastTagdata，为下一次数据比较做准备
                    System.arraycopy(InitTagData3, 0, lastTagData3, 0, index3);  //copy 第一次盘库数据到lastTagdata，为下一次数据比较做准备
                    /*Log.i(TAG, "handleMessage: after copy lastTagData = " + Arrays.toString(lastTagData));
                    Log.i(TAG, "handleMessage: after copy lastTagData2 = " + Arrays.toString(lastTagData2));
                    Log.i(TAG, "handleMessage: after copy lastTagData3 = " + Arrays.toString(lastTagData3));*/

                    lastIndex = index;  //更新上次的tag数量，为下次比较做准备
                    lastIndex2 = index2;  //更新上次的tag数量，为下次比较做准备
                    lastIndex3 = index3;  //更新上次的tag数量，为下次比较做准备

                    if(inventoryFlag){ //是书柜盘点，将盘点结果上报服务器
                        String book1RfidStr = "";
                        String book2RfidStr = "";//柜子2中书的RFID字符串，以“#”相连接
                        String book3RfidStr = "";
                        StringBuilder strBuilder = new StringBuilder();
                        if (index > 0) {
                            for (int i = 0; i < index; i++) {
                                book1RfidStr = book1RfidStr +InitTagData[i].toString().replace(" ", "");
                                if (i < (index - 1)) {
                                    book1RfidStr = book1RfidStr + "#";
                                }
                            }
                            strBuilder.append(book1RfidStr);
                            Log.i(TAG, "ReportBookInventory: 柜1 :" + book1RfidStr);
                        }

                        if (index2 > 0) {
                            for (int i = 0; i < index2; i++) {
                                book2RfidStr = book2RfidStr + InitTagData2[i].toString().replace(" ", "");
                                if (i < (index2 - 1)) {
                                    book2RfidStr = book2RfidStr + "#";
                                }
                            }
                            if (index > 0) {
                                strBuilder.append("#");
                            }
                            strBuilder.append(book2RfidStr);
                            Log.i(TAG, "ReportBookInventory: 柜2 :" + book2RfidStr);
                        }

                        if (index3 > 0) {
                            for (int i = 0; i < index3; i++) {
                                book3RfidStr = book3RfidStr + InitTagData3[i].toString().replace(" ", "");
                                if (i < (index3 - 1)) {
                                    book3RfidStr = book3RfidStr + "#";
                                }
                            }
                            if (index2 > 0 || index > 0) {
                                strBuilder.append("#");
                            }
                            strBuilder.append(book3RfidStr);
                            Log.i(TAG, "ReportBookInventory: 柜3 :" + book3RfidStr);
                        }
                        //开始上报
                        locker.reportInventoryBooks(index + index2 + index3 , strBuilder.toString());

                    }

                } else {
                    //判断当前关门的是哪个门？
                    if (ExcelUtils.sharebookdoornum == 1) {
                        // 当前关门后的数据 与上一次比较
                        Log.v(TAG, "COMPAREANDCOPY ");

                        //比较
                        //得到借出的书的数据
                        Log.i(TAG, "handleMessage: lastTagData = " + Arrays.toString(lastTagData));
                        Log.i(TAG, "handleMessage: CurrentTagData = " + Arrays.toString(CurrentTagData));
                        lendData_cabinet1 = ComputeDiffArray(lastTagData, lastIndex, CurrentTagData, curIndex, true, ExcelUtils.sharebookdoornum);
                        Log.i(TAG, "handleMessage: lentnum = " + lentnum);
                        //得到还书的数据
                        giveBackData_cabinet1 = ComputeDiffArray(CurrentTagData, curIndex, lastTagData, lastIndex, false, ExcelUtils.sharebookdoornum);
                        Log.i(TAG, "handleMessage: Givebacknum = " + givebacknum);
                        Log.i(TAG, "handleMessage: LentTagData = " + Arrays.toString(lendData_cabinet1));
                        Log.i(TAG, "handleMessage: GiveBackTagData = " + Arrays.toString(giveBackData_cabinet1));
                        //上报数据
                        ReportBookInfo(lendData_cabinet1, giveBackData_cabinet1, lentnum, givebacknum);
                        //copydata
                        //先清空 lastTagData， 避免CurrentTagData 比lastTagData 少数据情况下后面数据不被清空
                        Arrays.fill(lastTagData, "0");
                        System.arraycopy(CurrentTagData, 0, lastTagData, 0, curIndex);
                        lastIndex = curIndex;
                        Arrays.fill(CurrentTagData, "0");
                        curIndex = 0;
                        lendOutData = lendData_cabinet1;
                        backInData = giveBackData_cabinet1;
                        lentOutnum = lentnum;
                        backInnum = givebacknum;
                    } else if (ExcelUtils.sharebookdoornum == 0) {
                        // 当前关门后的数据 与上一次比较
                        Log.v(TAG, "COMPAREANDCOPY 2 ");
                        // 比较
                        // 得到借出的书的数据
                        Log.i(TAG, "handleMessage: lastTagData2 = " + Arrays.toString(lastTagData2));
                        Log.i(TAG, "handleMessage: CurrentTagData2 = " + Arrays.toString(CurrentTagData2));
                        Log.i(TAG, "lastTagData2.length =  " + lastTagData2.length);
                        Log.i(TAG, "CurrentTagData2.length =  " + CurrentTagData2.length);
                        lendData_cabinet2 = ComputeDiffArray(lastTagData2, lastIndex2, CurrentTagData2, curIndex2, true, ExcelUtils.sharebookdoornum);
                        Log.i(TAG, "handleMessage: lentnum = " + lentnum2);
                        //得到还书的数据
                        giveBackData_cabinet2 = ComputeDiffArray(CurrentTagData2, curIndex2, lastTagData2, lastIndex2, false, ExcelUtils.sharebookdoornum);
                        Log.i(TAG, "handleMessage: Givebacknum2 = " + givebacknum2);
                        Log.i(TAG, "handleMessage: lendData_cabinet2 = " + Arrays.toString(lendData_cabinet2));
                        Log.i(TAG, "handleMessage: giveBackData_cabinet2 = " + Arrays.toString(giveBackData_cabinet2));
                        //去掉数据中间的空格

                        //上报数据
                        ReportBookInfo(lendData_cabinet2, giveBackData_cabinet2, lentnum2, givebacknum2);

                        //copydata
                        //先清空 lastTagData2， 避免CurrentTagData2 比lastTagData2 少数据情况下后面数据不被清空
                        Arrays.fill(lastTagData2, "0");
                        System.arraycopy(CurrentTagData2, 0, lastTagData2, 0, curIndex2);
                        lastIndex2 = curIndex2;
                        Arrays.fill(CurrentTagData2, "0");
                        curIndex2 = 0;

                        lendOutData = lendData_cabinet2;
                        backInData = giveBackData_cabinet2;
                        lentOutnum = lentnum2;
                        backInnum = givebacknum2;

                    } else if (ExcelUtils.sharebookdoornum == 2) {
                        // 当前关门后的数据 与上一次比较
                        Log.v(TAG, "COMPAREANDCOPY 3");
                        //比较
                        //得到借出的书的数据
                        Log.i(TAG, "handleMessage: lastTagData3 = " + Arrays.toString(lastTagData3));
                        Log.i(TAG, "handleMessage: CurrentTagData3 = " + Arrays.toString(CurrentTagData3));
                        lendData_cabinet3 = ComputeDiffArray(lastTagData3, lastIndex3, CurrentTagData3, curIndex3, true, ExcelUtils.sharebookdoornum);
                        Log.i(TAG, "handleMessage: lentnum3 = " + lentnum3);
                        //得到还书的数据
                        giveBackData_cabinet3 = ComputeDiffArray(CurrentTagData3, curIndex3, lastTagData3, lastIndex3, false, ExcelUtils.sharebookdoornum);
                        Log.i(TAG, "handleMessage: Givebacknum3 = " + givebacknum3);
                        Log.i(TAG, "handleMessage: lendData_cabinet3 = " + Arrays.toString(lendData_cabinet3));
                        Log.i(TAG, "handleMessage: giveBackData_cabinet3 = " + Arrays.toString(giveBackData_cabinet3));

                        //上报数据
                        ReportBookInfo(lendData_cabinet3, giveBackData_cabinet3, lentnum3, givebacknum3);

                        //copydata
                        //先清空 lastTagData3， 避免CurrentTagData3 比lastTagData3 少数据情况下后面数据不被清空
                        Arrays.fill(lastTagData3, "0");
                        System.arraycopy(CurrentTagData3, 0, lastTagData3, 0, curIndex3);
                        lastIndex3 = curIndex3;
                        Arrays.fill(CurrentTagData3, "0");
                        curIndex3 = 0;

                        lendOutData = lendData_cabinet3;
                        backInData = giveBackData_cabinet3;
                        lentOutnum = lentnum3;
                        backInnum = givebacknum3;

                    } else {
                        Log.e(TAG, "door id is error");
                    }

                    mlendnum.setText(Integer.toString(lentOutnum));
                    mgivenum.setText(Integer.toString(backInnum));

                    Log.i(TAG, "借出数据：lendOutData = " + Arrays.toString(lendOutData));
                    Log.i(TAG, "归还数据：backInData = " + Arrays.toString(backInData));
                    Log.i(TAG, "借出数量：lendOutData = " + lentOutnum + ", 归还数量 = " + backInnum);

                }
            } else if (msg.what == INIRFIDMODULE) {
                if (!initRfidModule) {
                    Log.e(TAG, "INIRFIDMODULE , 初始化locker");
                    localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
                    //申请locker对象
                    try {
                        locker = new Locker(MainActivity.this, inquerytimer, localBroadcastManager);
                    } catch (Exception e) {
                        Log.e(TAG, "INIRFIDMODULE , 初始化locker异常");
                        e.printStackTrace();
                    }

                    //读取
                    locker.readCodes();

                    //初始化锁状态并开启连接websocket的后台服务
                    new InitThread().start();

                    //注册本地广播
                    //新建intentFilter并给其action标签赋值。
                    intentFilter = new IntentFilter();
                    intentFilter.addAction("com.share.switch.antenna");

                    //创建广播接收器实例，并注册。将其接收器与action标签进行绑定。
                    localReceiver = new LocalReceiver();
                    localBroadcastManager.registerReceiver(localReceiver, intentFilter);

                    //每隔1小时上传一次系统信息
                    inquerytimer.schedule(uploadSysInfoTask, 1000*60, 1000*60*60);

                    initRfidModule = true;
                }
            }
        }
    };

    class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("com.share.switch.antenna")) {
                int rfidid = intent.getIntExtra("id", 0);
                Log.d(TAG, "LocalReceiver, onReceive(), switch antenna rfid = " + rfidid);
                if (rfidid == 1) { //ch25 副柜
                    rfidaddr = 0x01;
                    mReader.setWorkAntenna((byte) rfidaddr, (byte) 0);
                    try {
                        Thread.sleep(30);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else if (rfidid == 0) { //ch12 副柜
                    rfidaddr = 0x02;
                    mReader.setWorkAntenna((byte) rfidaddr, (byte) 0);

                } else if (rfidid == 2) { //ch13 主柜
                    Log.i(TAG, "need to do  to main cabinet");
                    //设置 继续使用第二个模块的标准变量，以便继续搜索另外两个天线
                    rfidaddr = 0x03;
                    mReader.setWorkAntenna((byte) rfidaddr, (byte) 0);

                } else {
                    Log.e(TAG, "error  rfid id");
                }
            }
        }
    }

    RXObserver rxObserver = new RXObserver() {
        @Override
        protected void onInventoryTag(RXInventoryTag tag) {//注：RXObserver中的各种回调方法运行在子线程中
            /*if (debug_verbose) {
                Log.d("RXObserver", "onInventoryTag curent antenna = " + tag.btAntId + "  rfidaddr = " + rfidaddr);
            }*/
            if (ExcelUtils.isFirstInventy || inventoryFlag) {
                Message msg = Message.obtain();
                msg.what = ISFIRSTINVENTY;
                msg.obj = tag.strEPC;
                msg.arg1 = rfidaddr - 1;   //rfid id 0 ~2
                msg.arg2 = tag.btAntId - 1;
                rfidHandler.sendMessage(msg); //无线射频识别 标签（循环index++）
            } else if (!ExcelUtils.isFirstInventy) {
                Message msg = Message.obtain();
                msg.what = LASTINVENTY;
                msg.obj = tag.strEPC;
                msg.arg1 = rfidaddr - 1;   //rfid id 0 ~2
                msg.arg2 = tag.btAntId - 1;
                rfidHandler.sendMessage(msg);
            } else {
                //do nothing
            }
        }

        @Override
        protected void onExeCMDStatus(byte cmd, byte status) {
            mReader.realTimeInventory((byte) rfidaddr, (byte) 0x03);//发送实时盘存指令
        }

        @Override
        protected void onOperationTagEnd(int operationTagCount) {
            super.onOperationTagEnd(operationTagCount);
        }

        @Override
        protected void onInventoryTagEnd(RXInventoryTag.RXInventoryTagEnd tagEnd) {
            super.onInventoryTagEnd(tagEnd);
            if (debug_verbose) {
                Log.d("RXObserver", "onInventoryTagEnd() , curent antenna = " + tagEnd.mCurrentAnt + "  rfidaddr = " + rfidaddr);
            }

            if (tagEnd.mCurrentAnt < (CABINET_ATENNA_MAX_NUM - 1)) {
                if ((rfidaddr == 3) && (tagEnd.mCurrentAnt == (CABINET_ATENNA_MAX_NUM - 3))) {//第三个模块的第四根天线时
                    Message msg = Message.obtain();
                    msg.what = COMPAREANDCOPY;
                    msg.arg1 = rfidaddr - 1;  //标识为射频模块
                    msg.arg2 = tagEnd.mCurrentAnt;
                    rfidHandler.sendMessage(msg);
                    Log.e(TAG, "0~4, 且rfidaddr == 3,  ExcelUtils.isFirstInventy = " + ExcelUtils.isFirstInventy);
                } else {
                    mReader.setWorkAntenna((byte) rfidaddr, (byte) (tagEnd.mCurrentAnt + 1));
                }
            } else if (tagEnd.mCurrentAnt == (CABINET_ATENNA_MAX_NUM - 1)) {//轮询至最后一根天线时
                if (ExcelUtils.isFirstInventy || inventoryFlag) {//首次开关门或书柜定时盘点
                    if (rfidaddr == 1) {
                        Message msg1 = Message.obtain();
                        msg1.what = COMPAREANDCOPY;
                        msg1.arg1 = rfidaddr - 1;  //标识为射频模块
                        msg1.arg2 = tagEnd.mCurrentAnt;
                        rfidHandler.sendMessage(msg1);
                        //实例化第二个RFID模块
                        new ReaderInitThread2().start();
                    }
                    if (rfidaddr == 2) {
                        Message msg2 = Message.obtain();
                        msg2.what = COMPAREANDCOPY;
                        msg2.arg1 = rfidaddr - 1;  //标识为射频模块
                        msg2.arg2 = tagEnd.mCurrentAnt;
                        rfidHandler.sendMessage(msg2);
                        //实例化第三个RFID模块
                        new ReaderInitThread3().start();

                    }

                } else {
                    Message msg3 = Message.obtain();
                    msg3.what = COMPAREANDCOPY;
                    msg3.arg1 = rfidaddr - 1;  //标识为射频模块
                    msg3.arg2 = tagEnd.mCurrentAnt;
                    rfidHandler.sendMessage(msg3);
                    Log.e(TAG, "ExcelUtils.isFirstInventy = false, rfidaddr = " + rfidaddr);
                }

            } else if (tagEnd.mCurrentAnt == (RFID_ATENNA_MAX_NUM - 1)) {
                //模块1 的 后面两个天线（ 天线 mCurrentAnt = 7 ） 只是统计tag数量，
                Log.e(TAG, " the antenna 7 and 8 should not be used ");
            } else {
                Log.e(TAG, "rfid0 antenna is not  corret");
            }
        }
    };

    private void registertoServer() {
        Log.i(TAG, "instance initializer: ");
        WebSocketService.sendMsg("029000003001805310000");   //注册终端
    }

    //计时器
    TimerTask InquirytimerTask = new TimerTask() {
        @Override
        public void run() {
            if (ExcelUtils.checklockstate) {
                if (null != locker) {
                    locker.queryLock("Z00");
                }
            }
        }
    };

    //每隔一个小时上报系统信息
    TimerTask uploadSysInfoTask = new TimerTask() {
        @Override
        public void run() {
            if (null != locker) {
                locker.uploadSysInfo();
            }
        }
    };

    Handler mSharebookHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Integer doorid = -1;
            super.handleMessage(msg);
            switch (msg.what) {
                case OPENDOOR:
                    Log.d(TAG, "open door");
                    doorid = msg.arg1;
                    mdoorid = doorid;
                    if (null != locker) {
                        locker.opencloseStandbyDoor(doorid, true);//先开启备用锁
                        locker.openclosedoor(doorid, true);//主锁
                    }
                    //启动查询锁状态的定时器
                    if (ExcelUtils.checklockstate == false) {
                        ExcelUtils.checklockstate = true;
                    }
                    try {
                        Thread.sleep(DOOR_CLOSE_TIMEOUT);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Log.d(TAG, "try to close the door doorid = " + doorid);
                    if (null != locker) {
                        locker.openclosedoor(doorid, false);
                    }

                    break;
                case CLOSEDOOR:
                    locker.getControl(true);
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    doorid = msg.arg1;
                    mdoorid = doorid;
                    //locker.opendoor(doorid);
                    locker.openclosedoor(doorid, false);
                    break;
                case UPDATEINFO:
                    Log.d(TAG, "update  info");
                    break;
                case REGISTERTERM:
                    Log.i(TAG, "=== handleMessage, REGISTERTERM");
                    registertoServer();
                    break;
                case DOOROPENED:
                    Log.d(TAG, "send door opened msg ");
                    //dosendmsgtoServer();
                    break;
                case CANCEQUERY:
                    Log.d(TAG, "CANCEQUERY msg ");
                    inquerytimer.cancel();
                default:
                    break;
            }
        }
    };

    private Intent websocketServiceIntent;

    public void initlockstate() {
        // test
        try {
            Thread.sleep(800);
        } catch (Exception e) {
            e.printStackTrace();
        }
        locker.getControl(true);
        int i = 0;
        for (i = 0; i < locker.getMaxDoorNum(); i++) {

            try {
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //locker.opendoor(doorid);
//            Log.d(TAG, "initlockstate(), 初始化锁状态...");
            locker.openclosedoor(i, false);
        }
    }

    public class InitThread extends Thread {
        private final static String TAG = "InitThread ===> ";

        public void run() {
            Log.d(TAG, "run");

            initlockstate();

            websocketServiceIntent = new Intent(MainActivity.this, WebSocketService.class);
            startService(websocketServiceIntent);
        }
    }

    //初始化RFID模块
    public void initRFIDreader() {
        Log.d(TAG, "initRFIDreader()-->初始化RFID模块...");
        // 连接指定串口，返回true表示成功，false失败
        if (connector.connectCom("dev/ttymxc3", 115200)) {
            // 模块上电
            ModuleManager.newInstance().setUHFStatus(true);
            try {
                mReader = RFIDReaderHelper.getDefaultHelper();
                mReader.registerObserver(rxObserver);
            } catch (Exception e) {
                Log.d(TAG, "initRFIDreader()-->初始化RFID模块发生异常： " + e);
                e.printStackTrace();
            }
        }
    }
    // 读写模块一
    public class ReaderInitThread1 extends Thread {
        private final static String TAG = "ReaderInitThread1 ===> ";

        public void run() {
            Log.d(TAG, "ReaderInitThread1, run()......");
            // initRFIDreader();
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                e.printStackTrace();
            }

            rfidaddr = 0x01;
            mReader.setWorkAntenna((byte) rfidaddr, (byte) 0);
        }
    }
    // 读写模块二
    public class ReaderInitThread2 extends Thread {
        private final static String TAG = "ReaderInitThread2 ===> ";
        public void run() {
            Log.d(TAG, "ReaderInitThread2, run()......");
            rfidaddr = 0x02;
            try {
                Thread.sleep(15);//延迟15毫秒setWorkAntenna，处理天线不进行盘点的偶现bug
            } catch (Exception e) {
                e.printStackTrace();
            }
            mReader.setWorkAntenna((byte) rfidaddr, (byte) 0);
        }
    }
    // 读写模块三
    public class ReaderInitThread3 extends Thread {
        private final static String TAG = "ReaderInitThread3 ===> ";

        public void run() {
            Log.d(TAG, "ReaderInitThread3...... ");
            rfidaddr = 0x03;
            try {
                Thread.sleep(3000);
            } catch (Exception e) {
                e.printStackTrace();
            }

            mReader.setWorkAntenna((byte) rfidaddr, (byte) 0);
            //set anenna  output power
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Message msg = Message.obtain();
            msg.what = INIRFIDMODULE;
            rfidHandler.sendMessage(msg);
            Log.v(TAG, "ReaderInitThread3......  sendMessage INIRFIDMODULE, init locker");

        }
    }

    private ListView lendList, givebackList;//借出列表、归还列表
    private TextView mlendnum, mgivenum;//借出数量、归还数量


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        lendList = (ListView) findViewById(R.id.list_lend);
        givebackList = (ListView) findViewById(R.id.list_giveback);
        mlendnum = (TextView) findViewById(R.id.lendnum);
        mgivenum = (TextView) findViewById(R.id.givebacknum);
        ExcelUtils.isFirstInventy = true;
        // 设置门锁节点名
        Constant.setPorts();
        // new ReaderInitThread().start();
        initRFIDreader();
        new ReaderInitThread2().start();
        inquerytimer.schedule(InquirytimerTask, 1000, 1000);//每隔1秒发送一次锁状态检测
        // 注册书柜盘点的广播
        registerInventoryReceiver();
        // 启动定时服务
        startAlarmService();
    }

    private void registerInventoryReceiver(){
        inventoryReceiver = new InventoryBookReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.rocktech.sharebookcase.inventory");
        registerReceiver(inventoryReceiver, filter);
    }

    private void startAlarmService() {
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent AlarmIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent pdingIntent = PendingIntent.getBroadcast(this, 0, AlarmIntent, 0);
        // 设定定时触发的时间，每天凌晨2点
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 2);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date date = calendar.getTime();
        /* 如果第一次执行定时任务的时间 小于当前的时间，
         * 则在 第一次执行定时任务的时间加一天，以便此任务在下个时间点执行。
         * 否则任务会立即执行。
         */
        if (date.before(new Date())) {
            date = addDay(date, 1);
        }
        try {
            manager.setRepeating(AlarmManager.RTC_WAKEUP, date.getTime(), AlarmManager.INTERVAL_DAY, pdingIntent);
            Log.i(TAG, "设定一个定时重复执行的任务... time :"+date.getHours()+":"+date.getMinutes()+":"+date.getSeconds());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 增加或减少天数
    private Date addDay(Date date, int num) {
        Calendar startDT = Calendar.getInstance();
        startDT.setTime(date);
        startDT.add(Calendar.DAY_OF_MONTH, num);
        return startDT.getTime();
    }

    //拿到服务器返回的信息
    @Override
    protected void getMessage(String msg) {
        Message bookmsg = Message.obtain();
        try {
            JSONObject jsonObject = new JSONObject(msg);
            Object msgData = jsonObject.get("Message");

            if(msgData instanceof String){
                String[] strarray = msgData.toString().split("&");
                for (int i = 0; i < strarray.length; i++) {
                    Log.d(TAG, "strarray[i] == " + strarray[i]);
                }

                msender = jsonObject.getInt("Sender");
                mReceiver = jsonObject.getInt("Receiver");

                SpHelper.setIntValue("sender", msender);
                SpHelper.setIntValue("receiver", mReceiver);

                String[] tmp = strarray[1].split("=");
                Integer doorid = Integer.parseInt(tmp[1]);

                switch (doorid) {
                    case 0:
                    case 1:
                    case 2:
                        bookmsg.what = OPENDOOR;
                        bookmsg.arg1 = doorid;
                        mSharebookHandler.sendMessage(bookmsg);
                        break;
                    default:
                        break;
                }

            } else if (msgData instanceof JSONArray){ //借出归还书籍的信息
                //清空历史数据
                if (!listLendData.isEmpty()) {
                    listLendData.clear();
                }
                if (!listBackInData.isEmpty()) {
                    listBackInData.clear();
                }

                JSONArray jsonArray = (JSONArray)msgData;
                Log.i(TAG, "借出/归还书籍的信息: "+jsonArray);
                for(int i = 0; i < jsonArray.length(); i++){
                    JSONObject jsonObj = (JSONObject)jsonArray.get(i);
                    BookInfo bookInfo = new BookInfo();
                    bookInfo.setAuthor(jsonObj.optString("Author"));
                    bookInfo.setAuthorIntro(jsonObj.optString("AuthorIntro"));
                    bookInfo.setType(jsonObj.optString("Type"));
                    bookInfo.setCode(jsonObj.optString("Code"));
                    bookInfo.setF5Key(jsonObj.optString("F5Key"));
                    bookInfo.setIsbn(jsonObj.optString("ISBN"));
                    bookInfo.setBookName(jsonObj.optString("Name"));
                    bookInfo.setPageCount(jsonObj.optString("PageCount"));
                    bookInfo.setBookImagUrl(jsonObj.optString("Picture"));
                    bookInfo.setBookPrice(jsonObj.optString("Price"));
                    bookInfo.setPublishDate(jsonObj.optString("PublicationDate"));
                    bookInfo.setPublishingHouse(jsonObj.optString("PublishingHouse"));
                    bookInfo.setSummary(jsonObj.optString("Summary"));
                    bookInfo.setOption(jsonObj.optString("Option"));
                    //借出
                    if(bookInfo.getOption().equals("lend")){
                        listLendData.add(bookInfo.getBookName()+"      "+bookInfo.getAuthor());
                    } else {//归还
                        listBackInData.add(bookInfo.getBookName()+"      "+bookInfo.getAuthor());
                    }
                }

                adapter1 = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, listLendData);
                lendList.setAdapter(adapter1);

                adapter2 = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, listBackInData);
                givebackList.setAdapter(adapter2);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void ReportBookInfo(String LentTagData[], String GiveBackTagData[], int lentnumber, int givenum) {
        TypeUtils.compatibleWithFieldName = true;
        ShareBookMessage msg = new ShareBookMessage();
        String sendmsg = "";
        String tmpmsg = "action=closeDoor";
        msg.head = new headMsg();
        msg.body = new bodyMsg();
        msg.body.bizObject = new bizObject();

//        msg.body.bizObject.setSender(SpHelper.getIntValue("LinkCode"));   //后面根据需要修改
        msg.body.bizObject.setSender(37);

        // 设置开门拿走的rfid 标签
        //action=closeDoor&lend=A001,A002,A003
        Log.i(TAG, "ReportBookInfo: lentnumber  = " + lentnumber);
        if (lentnumber > 0) {
            tmpmsg = tmpmsg + "&lend=";
            for (int i = 0; i < lentnumber; i++) {
                Log.i(TAG, "ReportBookInfo: LentTagData " + i + "  = " + LentTagData[i].toString().replace(" ", ""));
                tmpmsg = tmpmsg + LentTagData[i].toString().replace(" ", "");
                if (i < (lentnumber - 1)) {
                    tmpmsg = tmpmsg + "#";
                }
            }
        }
        Log.i(TAG, "ReportBookInfo: lend tmpmsg = " + tmpmsg);
        if (givenum > 0) {
            tmpmsg = tmpmsg + "&return=";
            for (int i = 0; i < givenum; i++) {
                Log.i(TAG, "ReportBookInfo: GiveBackTagData " + i + "  = " + GiveBackTagData[i].toString().replace(" ", ""));
                tmpmsg = tmpmsg + GiveBackTagData[i].toString().replace(" ", "");
                if (i < (givenum - 1)) {
                    tmpmsg = tmpmsg + "#";
                }
            }
        }
        Log.i(TAG, "ReportBookInfo: return tmpmsg = " + tmpmsg);
        sendmsg = tmpmsg;
        //msg.body.bizObject.setMessage("action=closeDoor&lend=E2801170000002088E72B0F5,E2801170000002088E72B085&return=E2801170000002088E72B0E5");
        msg.body.bizObject.setMessage(sendmsg);
        msg.body.bizObject.setReceiver(msender);
        msg.body.bizObject.setChatType(0);
        msg.body.bizObject.setChatToken("3736");
        String message = JSON.toJSONString(msg, SerializerFeature.WriteMapNullValue);
        //String message = JSON.toJSONString( msg );

        WebSocketService.sendMsg(message);
    }

    @Override
    protected void RegisterTerminal() {
        Message sendmsg = Message.obtain();
        //Log.d(TAG,"msg == "+msg);
        try {
            //JSONObject jsonObject = new JSONObject(msg);
            //String get = jsonObject.getString("Message");
            //Integer doorid = jsonObject.getInt("Message");
            //String doorid = jsonObject.getString("");
            sendmsg.what = REGISTERTERM;
            // sendmsg.arg1 = 029000003001805310000;  //资产编码 02900000 3 00 180531 0000
            mSharebookHandler.sendMessage(sendmsg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WebSocketService.closeWebsocket(true);
        if (null != websocketServiceIntent) {
            stopService(websocketServiceIntent);
        }
        if (null != mReader && null != rxObserver) {
            mReader.unRegisterObserver(rxObserver);
        }
        if (connector != null) {
            connector.disConnect();
        }

        ModuleManager.newInstance().setUHFStatus(false);//模块断电
        ModuleManager.newInstance().release();//释放读写器上电掉电控制设备

        unregisterReceiver(inventoryReceiver);
    }

    private InventoryBookReceiver inventoryReceiver;

    class InventoryBookReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("com.rocktech.sharebookcase.inventory")){
                //每日凌晨2点，书柜盘点
                inventoryFlag = true;
                new ReaderInitThread2().start();
            }
        }
    }
}
