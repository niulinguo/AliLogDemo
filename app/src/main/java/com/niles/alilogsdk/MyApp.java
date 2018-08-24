package com.niles.alilogsdk;

import android.app.Application;

import com.niles.alilog_module.AliLog;
import com.niles.alilog_module.AliLogConfig;
import com.niles.alilog_module.AliLogManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Niles
 * Date 2018/8/22 15:34
 * Email niulinguo@163.com
 */
public class MyApp extends Application {

    private static final String END_POINT = "cn-shanghai.log.aliyuncs.com";
    private static final String PROJECT = "tt-sdk";
    private static final String LOG_STORE = "logstore-tt-sdk";
    private static final DateFormat DATE_FORMAT;

    static {
        DATE_FORMAT = SimpleDateFormat.getDateTimeInstance();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AliLogManager.init(this,
                AliLogConfig
                        .newBuilder()
                        .setPrintLog(true)
                        .setEndPoint(END_POINT)
                        // STS 方式
//                        .setSTSUrl(BuildConfig.STS_URL)
                        // 主账号方式
                        .setAccessKey(BuildConfig.ACCESS_KEY_ID, BuildConfig.ACCESS_KEY_SECRET)
                        .setProject(PROJECT)
                        .setLogStore(LOG_STORE)
                        .setConnectType(AliLogConfig.NetworkPolicy.WWAN_OR_WIFI)
                        .build());

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                AliLogManager.addLog(AliLog
                        .newBuilder()
                        .setTopic("LogDemo")
                        .setSource("Test")
                        .putContent("Time", DATE_FORMAT.format(new Date()))
                        .build());
            }
        }, 0, 60000);
    }
}
