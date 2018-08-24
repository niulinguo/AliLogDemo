# AliLogDemo
[阿里日志SDK v2.0.0][阿里日志SDK]封装

## 集成

1. 使用 jitpack 仓库

``` Gradle
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

2. 引入 Library

``` Gradle
implementation 'com.github.niulinguo:AliLogDemo:v1.0'
```

## 使用方式

1. 初始化

``` java
AliLogManager.init(this,
        AliLogConfig
                .newBuilder()
                .setPrintLog(true)
                .setEndPoint(END_POINT)
                // STS 方式
//              .setSTSUrl(BuildConfig.STS_URL)
                // 主账号方式
                .setAccessKey(BuildConfig.ACCESS_KEY_ID, BuildConfig.ACCESS_KEY_SECRET)
                .setProject(PROJECT)
                .setLogStore(LOG_STORE)
                .setConnectType(AliLogConfig.NetworkPolicy.WWAN_OR_WIFI)
                .build());
```

2. 添加日志

``` java
AliLogManager.addLog(AliLog
        .newBuilder()
        .setTopic("LogDemo")
        .setSource("Test")
        .putContent("Time", DATE_FORMAT.format(new Date()))
        .build());
```

## 日志上传策略
1. 添加日志会存储在数据库中，避免了日志的丢失
2. 每隔30秒将数据库中的日志上传到服务器，上传成功则从数据库中删除。


[阿里日志SDK]: https://github.com/aliyun/aliyun-log-android-sdk