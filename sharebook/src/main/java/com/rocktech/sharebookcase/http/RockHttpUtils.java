package com.rocktech.sharebookcase.http;

import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by zhangyazhou on 2018/5/10.
 */
public class RockHttpUtils {
    public static HttpURLConnection sendReq(String spId) {
        String url = "http://ai.rockemb.net/api/v10/member/getAliPayQrCode";//二维码的url
        try {
            URL mUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) mUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(500);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.connect();
            Log.d("RockHttpUtils","出苹果 qrcode begin B");
            return connection;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
