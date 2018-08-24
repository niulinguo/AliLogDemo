package com.niles.alilogsdk;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = findViewById(R.id.tv_text);
        String time = "2018-08-22T11:08:33Z";
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            long time1 = dateFormat.parse(time).getTime();
            long l = time1 - System.currentTimeMillis();
            mTextView.setText(String.format(Locale.getDefault(), "有效期剩余：%d分钟", l / 1000 / 60));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
