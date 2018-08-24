package com.niles.alilog_module;

import android.text.TextUtils;

/**
 * Created by Niles
 * Date 2018/8/22 15:41
 * Email niulinguo@163.com
 */
public final class AliLogConfig {

    private final boolean mIsPrintLog;
    private final String mEndPoint;

    private final boolean mIsSTS;
    private final String mAccessKeyId;
    private final String mAccessKeySecret;
    private final String mSTSUrl;

    private final String mProject;
    private final String mLogStore;

    private final int mMaxConcurrentRequest;
    private final int mSocketTimeout;
    private final int mConnectionTimeout;
    private final int mMaxErrorRetry;

    private final boolean mCachable;
    private final NetworkPolicy mConnectType;

    private AliLogConfig(
            boolean isPrintLog,
            String endPoint,
            boolean isSTS,
            String accessKeyId,
            String accessKeySecret,
            String STSUrl,
            String project,
            String logStore,
            int maxConcurrentRequest,
            int socketTimeout,
            int connectionTimeout,
            int maxErrorRetry,
            boolean cachable,
            NetworkPolicy connectType) {
        mIsPrintLog = isPrintLog;
        mEndPoint = endPoint;
        mIsSTS = isSTS;
        mAccessKeyId = accessKeyId;
        mAccessKeySecret = accessKeySecret;
        mSTSUrl = STSUrl;
        mProject = project;
        mLogStore = logStore;
        mMaxConcurrentRequest = maxConcurrentRequest;
        mSocketTimeout = socketTimeout;
        mConnectionTimeout = connectionTimeout;
        mMaxErrorRetry = maxErrorRetry;
        mCachable = cachable;
        mConnectType = connectType;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public boolean isPrintLog() {
        return mIsPrintLog;
    }

    public String getEndPoint() {
        return mEndPoint;
    }

    public boolean isSTS() {
        return mIsSTS;
    }

    public String getAccessKeySecret() {
        return mAccessKeySecret;
    }

    public String getAccessKeyId() {
        return mAccessKeyId;
    }

    public int getConnectionTimeout() {
        return mConnectionTimeout;
    }

    public int getMaxConcurrentRequest() {
        return mMaxConcurrentRequest;
    }

    public int getMaxErrorRetry() {
        return mMaxErrorRetry;
    }

    public boolean isCachable() {
        return mCachable;
    }

    public int getSocketTimeout() {
        return mSocketTimeout;
    }

    public NetworkPolicy getConnectType() {
        return mConnectType;
    }

    public String getLogStore() {
        return mLogStore;
    }

    public String getProject() {
        return mProject;
    }

    public String getSTSUrl() {
        return mSTSUrl;
    }

    public enum NetworkPolicy {
        /* wifi-only,只有wifi情况下上传 */
        WIFI_ONLY,
        /* 有网,只有wifi情况下上传 */
        WWAN_OR_WIFI
    }

    public static final class Builder {

        private boolean mIsPrintLog = true;
        private String mEndPoint;

        private boolean mIsSTS;
        private String mAccessKeyId;
        private String mAccessKeySecret;
        private String mSTSUrl;

        private String mProject;
        private String mLogStore;

        private int mMaxConcurrentRequest = 5;
        private int mSocketTimeout = 15 * 1000;
        private int mConnectionTimeout = 15 * 1000;
        private int mMaxErrorRetry = 2;

        private boolean mCachable = false;
        private NetworkPolicy mConnectType = NetworkPolicy.WIFI_ONLY;

        public Builder setPrintLog(boolean printLog) {
            mIsPrintLog = printLog;
            return this;
        }

        public Builder setEndPoint(String endPoint) {
            mEndPoint = endPoint;
            return this;
        }

        public Builder setAccessKey(String accessKeyId, String accessKeySecret) {
            mAccessKeyId = accessKeyId;
            mAccessKeySecret = accessKeySecret;
            mIsSTS = false;
            return this;
        }

        public Builder setSTSUrl(String STSUrl) {
            mSTSUrl = STSUrl;
            mIsSTS = true;
            return this;
        }

        public Builder setProject(String project) {
            mProject = project;
            return this;
        }

        public Builder setConnectType(NetworkPolicy connectType) {
            mConnectType = connectType;
            return this;
        }

        public Builder setLogStore(String logStore) {
            mLogStore = logStore;
            return this;
        }

        public AliLogConfig build() {
            check();
            return new AliLogConfig(mIsPrintLog,
                    mEndPoint,
                    mIsSTS,
                    mAccessKeyId,
                    mAccessKeySecret,
                    mSTSUrl,
                    mProject,
                    mLogStore,
                    mMaxConcurrentRequest,
                    mSocketTimeout,
                    mConnectionTimeout,
                    mMaxErrorRetry,
                    mCachable,
                    mConnectType);
        }

        private void check() {
            if (TextUtils.isEmpty(mEndPoint)) {
                throw new RuntimeException("endPoint is null");
            }
            if (mIsSTS) {
                if (TextUtils.isEmpty(mSTSUrl)) {
                    throw new RuntimeException("STS url is null");
                }
            } else {
                if (TextUtils.isEmpty(mAccessKeyId)) {
                    throw new RuntimeException("accessKeyId is null");
                }
                if (TextUtils.isEmpty(mAccessKeySecret)) {
                    throw new RuntimeException("accessKeySecret is null");
                }
            }
            if (TextUtils.isEmpty(mProject)) {
                throw new RuntimeException("project is null");
            }
            if (TextUtils.isEmpty(mLogStore)) {
                throw new RuntimeException("logStore is null");
            }
        }
    }
}
