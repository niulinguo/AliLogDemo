package com.niles.alilog_module;

import android.text.TextUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Niles
 * Date 2018/8/22 22:56
 * Email niulinguo@163.com
 */
public class AliLog {

    private static final DateFormat DATE_FORMAT;

    static {
        DATE_FORMAT = SimpleDateFormat.getDateTimeInstance();
    }

    private final String mSource;
    private final String mTopic;
    private final long mTime;
    private final HashMap<String, String> mContent;

    AliLog(String source, String topic, long time, HashMap<String, String> content) {
        mSource = source;
        mTopic = topic;
        mTime = time;
        mContent = content;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public long getTime() {
        return mTime;
    }

    public HashMap<String, String> getContent() {
        return mContent;
    }

    public String getSource() {
        return mSource;
    }

    public String getTopic() {
        return mTopic;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("source:" + mSource + ", topic:" + mTopic + ", time:" + DATE_FORMAT.format(new Date(mTime)));
        for (Map.Entry<String, String> entry : mContent.entrySet()) {
            s.append("\n").append(entry.getKey()).append(" => ").append(entry.getValue());
        }
        return s.toString();
    }

    public static class Builder {

        private final HashMap<String, String> mContent = new HashMap<>();
        private String mSource;
        private String mTopic;
        private long mTime = System.currentTimeMillis();

        public AliLog build() {
            check();
            return new AliLog(
                    mSource,
                    mTopic,
                    mTime,
                    mContent
            );
        }

        public Builder setSource(String source) {
            mSource = source;
            return this;
        }

        public Builder setTopic(String topic) {
            mTopic = topic;
            return this;
        }

        public Builder putContent(String key, String value) {
            mContent.put(key, value);
            return this;
        }

        public Builder setTime(long time) {
            mTime = time;
            return this;
        }

        private void check() {
            if (TextUtils.isEmpty(mSource)) {
                throw new RuntimeException("source is null");
            }
            if (TextUtils.isEmpty(mTopic)) {
                throw new RuntimeException("topic is null");
            }
        }

    }
}
