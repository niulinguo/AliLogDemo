package com.niles.alilog_module;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.Handler;
import android.os.Message;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.sls.android.sdk.ClientConfiguration;
import com.aliyun.sls.android.sdk.LOGClient;
import com.aliyun.sls.android.sdk.LogEntity;
import com.aliyun.sls.android.sdk.SLSDatabaseManager;
import com.aliyun.sls.android.sdk.SLSLog;
import com.aliyun.sls.android.sdk.core.auth.CredentialProvider;
import com.aliyun.sls.android.sdk.core.auth.PlainTextAKSKCredentialProvider;
import com.aliyun.sls.android.sdk.core.auth.StsTokenCredentialProvider;
import com.aliyun.sls.android.sdk.model.Log;
import com.aliyun.sls.android.sdk.model.LogGroup;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Niles
 * Date 2018/8/22 15:23
 * Email niulinguo@163.com
 */
public final class AliLogManager {

    private static final int HANDLER_REFRESH_KEY = 1;
    private static final int HANDLER_CREATE_CLIENT = 2;

    private static final DateFormat DATE_FORMAT;
    private static Application sApp;
    private static AliLogConfig sConfig;
    @SuppressLint("StaticFieldLeak")
    private static LOGClient sLogClient;
    private static OkHttpClient sOkHttpClient;
    @SuppressLint("HandlerLeak")
    private static Handler sHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_CREATE_CLIENT: {
                    JSONObject jsonObject = (JSONObject) msg.obj;
                    String accessKeyId = jsonObject.getString("AccessKeyId");
                    String accessKeySecret = jsonObject.getString("AccessKeySecret");
                    String securityToken = jsonObject.getString("SecurityToken");
                    sLogClient = createLogClient(
                            createStsCredentialProvider(accessKeyId, accessKeySecret, securityToken),
                            createClientConfig()
                    );
                    SLSLog.logInfo("create logClient, AK:" + accessKeyId + ", SK:" + accessKeySecret + ", Token:" + securityToken);
                    break;
                }
                case HANDLER_REFRESH_KEY: {
                    loadSTSKey();
                    break;
                }
            }
        }
    };

    static {
        DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static void init(Application app, AliLogConfig config) {
        sApp = app;
        sConfig = config;

        SLSDatabaseManager.getInstance().setupDB(app);

        if (config.isPrintLog()) {
            SLSLog.enableLog(); // log打印在控制台
        } else {
            SLSLog.disableLog();
        }

        if (!config.isSTS()) {
            sLogClient = createLogClient(
                    createPlainTextCredentialProvider(),
                    createClientConfig()
            );
        } else {
            sOkHttpClient = new OkHttpClient.Builder()
                    .build();
            loadSTSKey();
        }
    }

    private static void loadSTSKey() {
        SLSLog.logInfo("loadSTSKey, Url:" + sConfig.getSTSUrl());
        final Request request = new Request.Builder()
                .get()
                .url(sConfig.getSTSUrl())
                .build();

        Call call = sOkHttpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                SLSLog.logWarn("loadSTSKey failure, " + e.getMessage());

                int delayMillis = 30000;
                SLSLog.logInfo((delayMillis / 1000) + "s try again");

                sHandler.sendEmptyMessageDelayed(HANDLER_REFRESH_KEY, delayMillis);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();

                SLSLog.logInfo("loadSTSKey success, " + string);

                JSONObject jsonObject = JSON.parseObject(string);
                String expiration = jsonObject.getString("Expiration");

                try {
                    long expirationTime = DATE_FORMAT.parse(expiration).getTime();
                    long delayMillis = expirationTime - System.currentTimeMillis() - 5000;
                    SLSLog.logInfo((delayMillis / 1000) + "s later refresh key");

                    sHandler.sendEmptyMessageDelayed(HANDLER_REFRESH_KEY, delayMillis);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Message.obtain(sHandler, HANDLER_CREATE_CLIENT, jsonObject).sendToTarget();
            }
        });
    }

    public static void addLog(AliLog aliLog) {
        SLSLog.logInfo("add log:" + aliLog);

        LogGroup logGroup = new LogGroup(aliLog.getTopic(), aliLog.getSource());
        Log log = new Log();
        log.PutTime((int) (aliLog.getTime() / 1000));
        log.GetContent().putAll(aliLog.getContent());
        logGroup.PutLog(log);

        LogEntity entity = new LogEntity();
        entity.setEndPoint(sConfig.getEndPoint());
        entity.setJsonString(logGroup.LogGroupToJsonString());
        entity.setStore(sConfig.getLogStore());
        entity.setProject(sConfig.getProject());
        entity.setTimestamp(System.currentTimeMillis());

        SLSDatabaseManager.getInstance().insertRecordIntoDB(entity);
    }

    private static ClientConfiguration createClientConfig() {
        // 配置信息
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(sConfig.getConnectionTimeout()); // 连接超时，默认15秒
        conf.setSocketTimeout(sConfig.getSocketTimeout()); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(sConfig.getMaxConcurrentRequest()); // 最大并发请求书，默认5个
        conf.setMaxErrorRetry(sConfig.getMaxErrorRetry()); // 失败后最大重试次数，默认2次

        conf.setCachable(sConfig.isCachable());     // 设置日志发送失败时，是否支持本地缓存。

        // 设置缓存日志发送的网络策略。
        switch (sConfig.getConnectType()) {
            case WIFI_ONLY: {
                conf.setConnectType(ClientConfiguration.NetworkPolicy.WIFI_ONLY);
                break;
            }
            case WWAN_OR_WIFI: {
                conf.setConnectType(ClientConfiguration.NetworkPolicy.WWAN_OR_WIFI);
                break;
            }
        }

        return conf;
    }

    private static CredentialProvider createPlainTextCredentialProvider() {
        // 移动端是不安全环境，不建议直接使用阿里云主账号ak，sk的方式。建议使用STS方式。具体参见
        // https://help.aliyun.com/document_detail/62681.html
        // 注意：SDK 提供的 PlainTextAKSKCredentialProvider 只建议在测试环境或者用户可以保证阿里云主账号AK，SK安全的前提下使用。
        // 具体使用如下

        // 主账户使用方式
        // noinspection deprecation
        return new PlainTextAKSKCredentialProvider(sConfig.getAccessKeyId(), sConfig.getAccessKeySecret());
    }

    private static CredentialProvider createStsCredentialProvider(String STS_AK, String STS_SK, String STS_TOKEN) {
        // STS使用方式
        return new StsTokenCredentialProvider(STS_AK, STS_SK, STS_TOKEN);
    }

    private static LOGClient createLogClient(CredentialProvider credentialProvider, ClientConfiguration conf) {
        // 初始化client
        return new LOGClient(sApp, sConfig.getEndPoint(), credentialProvider, conf);
    }

}
