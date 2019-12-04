
package com.rocktech.sharebookcase;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.alibaba.fastjson.JSON;
import com.rocktech.sharebookcase.msgdata.ShareBookMessage;
import com.rocktech.sharebookcase.msgdata.bizObject;
import com.rocktech.sharebookcase.msgdata.bodyMsg;
import com.rocktech.sharebookcase.msgdata.headMsg;
import com.rocktech.sharebookcase.tool.RequestManager;
import com.rocktech.sharebookcase.tool.SpHelper;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Timer;
import java.util.TimerTask;
import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;
import de.tavendo.autobahn.WebSocketOptions;

/**
 * Created by wxs on 16/8/17.
 * modify by maodongwei on 18/8/3
 *
 * notice : 根据资产编号动态获取websocket链接码，成功后进行websocket链接。。
 *
 */

public class WebSocketService extends Service {
    private static final String TAG = WebSocketService.class.getSimpleName();

    public static final String WEBSOCKET_ACTION = "WEBSOCKET_ACTION";
    private Context mContext;

    private Timer timer = new Timer();
    private static boolean isClosed = true;
    private static WebSocketConnection webSocketConnection;
    private static WebSocketOptions options = new WebSocketOptions();
    private static boolean isExitApp = false;

    //ws://www.rockemb.net:5656/api/v10/webSocket/7

 private static String webSocketHost = "ws://ai.rockemb.net:5600/api/v10/webSocket/38"; //webSocket服务端的url,ws是协议,和http一样*//*


    private int linkCode;//webSocket动态链接码
    private boolean printCloseLog = true;//是否打印webSocket断开的log


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mContext = this;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectionReceiver, intentFilter);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void closeWebsocket(boolean exitApp) {
        isExitApp = exitApp;
        if (webSocketConnection != null && webSocketConnection.isConnected()) {
            webSocketConnection.disconnect();
            webSocketConnection = null;
            Log.d(TAG,"@@@ closeWebsocket(), webSocketConnection is set null !  exitApp = "+exitApp);
        }
    }


     /**
     * 根据资产编号，动态获取Websocket链接码
     */

    private void GetWebsocketCodeByDeviceId(){
        ShareBookMessage msg = new ShareBookMessage();
        msg.head = new headMsg();
        msg.body = new bodyMsg();
        msg.body.bizObject = new bizObject();

        String deviceCode = SpHelper.getStringValue("deviceCode");
        Log.i(TAG, "@@@ deviceCode : "+deviceCode);
        msg.body.bizObject.setCode("029000003001805310000");//资产编码029000003001805310000, 绵阳816000002001805310000

        String message = JSON.toJSONString(msg);

        Log.i(TAG, "@@@ 动态获取Websocket链接码, 参数 message : "+message);
        RequestManager.getInstance(mContext).requestPostByAsyn("companyBookcase/getWebSocketByCode"
                , message
                , new RequestManager.ReqCallBack<String>() {
                    @Override
                    public void onReqSuccess(String result) {
                        try {
                            JSONObject jsObj = new JSONObject(result);
                            JSONObject jsBody = jsObj.getJSONObject("body");
                            linkCode = jsBody.optInt("bizObject");
                            Log.d(TAG,"@@@ onReqSuccess()---> webSocket动态链接码 : "+linkCode);
                            webSocketConnect();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onReqFailed(String errorMsg) {
                        Log.d(TAG,"@@@ onReqFailed()---> 动态获取Websocket链接码 : "+errorMsg);
                    }
                });
    }

    private void webSocketConnect() {
        String websocketUrl = "ws://bs.rockemb.net/api/v10/webSocket/" + linkCode;
        webSocketConnection = new WebSocketConnection();
        try {
            webSocketConnection.connect(websocketUrl, new WebSocketHandler(){
                //websocket启动时候的回调
                @Override
                public void onOpen() {
                    Log.i(TAG,"@@@ webSocketConnect()--->onOpen(), ========= websocket成功连接！");
                    isClosed = false;
                    printCloseLog = true;
                }

                //websocket接收到消息后的回调
                @Override
                public void onTextMessage(String payload) {
                    //如果是心跳检测 就不理；如果涉及服务器返回的指令，做响应操作
                    if(!payload.equals("#$>@!&&<!)*>*$)!")){
                        Log.i(TAG, "@@@ webSocketConnect()--->onTextMessage(), ========= 接收到服务器返回消息: " + payload);
                        Intent intent = new Intent(WEBSOCKET_ACTION);
                        intent.putExtra("message", payload);
                        mContext.sendBroadcast(intent);
                    }
                }

                //websocket关闭时候的回调(一旦断开，循环调用)
                @Override
                public void onClose(int code, String reason) {
                    isClosed = true;
                    if(printCloseLog){
                        Log.e(TAG, "@@@ webSocketConnect()--->onClose() ===断开!!"+"; code = " + code + "; reason : " + reason);
                        printCloseLog = false;
                    }
                    switch (code) {
                        case 1:
                            closeWebsocket(false);
                            webSocketConnect();//重连
                            break;
                        case 2:
                            closeWebsocket(false);
                            webSocketConnect();//重连
                            break;
                        case 3:
                            //手动断开连接，则重新建立连接
//                            if (!isExitApp) {
                                webSocketConnect();
//                            }
                            break;
                        case 4:
                            closeWebsocket(false);
                            webSocketConnect();//重连
                            break;
                        case 5:
                            //如果网络断开，则先断开连接，后再建立连接
                            closeWebsocket(false);
                            webSocketConnect();
                            break;
                        default:
                            break;
                    }
                }
            } , options);

        } catch (WebSocketException e) {
            e.printStackTrace();
        }
    }

    public static void sendMsg(String s) {
        Log.d(TAG, "@@@ sendMsg = " + s);
        if (!TextUtils.isEmpty(s)){
            if (webSocketConnection != null) {
                webSocketConnection.sendTextMessage(s);
            }
        }
    }

    private BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo == null || !networkInfo.isAvailable()) {
                Toast.makeText(mContext, "网络已断开，请重新连接", Toast.LENGTH_SHORT).show();
            } else {
                if (webSocketConnection != null) {
                    webSocketConnection.disconnect();
                }
                if (isClosed) {
                    GetWebsocketCodeByDeviceId();
                    timer.schedule(timerTask,1000*60,1000*60);//每隔1分钟发送一次心跳检测
                }
            }
        }
    };

    //计时器
    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            if(!isClosed){
                sendMsg("#$>@!&&<!)*>*$)!");
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (connectionReceiver != null) {
            unregisterReceiver(connectionReceiver);
        }
    }
}

