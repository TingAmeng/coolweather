package com.example.coolweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {

    /**
     * 此类封装OkHttp的HTTP请求的操作
     * okhttp3.Callback参数，是OkHttp库中自带的一个回调接口，类似于
     * 上面的HttpCallBackListener
     * client.newCall()调用enqueue()方法，此方法内部已经帮我们开辟
     * 子线程，子线程中去执行HTTP请求，并将最终的请求结果回调到
     * okhttp3.Callback中
     * */
    public static void sendOkHttpRequest(String address, okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(address)
                .build();
        client.newCall(request).enqueue(callback);
    }
}
