package com.udp.searcher;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.amyli.my.base.BaseUserData;
import com.example.amyli.my.base.DeviceData;
import com.example.amyli.my.client.ClientConfig;
import com.example.amyli.my.client.SearchClient;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 搜索主机 —— 简单demo
 */
public class PhoneActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int MESSAGE_SEARCH_START = 1;
    private static final int MESSAGE_SEARCH_FINISH = 2;
    private static final int MESSAGE_SEARCH_DEV = 3;

    private Button btnStartSearch;
    private Button btnEndSearch;

    private TextView tv_device;

    private List<DeviceData> mDeviceList = new ArrayList<>();

    private MyHandler mHandler = new MyHandler(this);

    private SearchClient searchClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initConfig();
    }

    private void initConfig() {
        ClientConfig.setAskFunc(1);
    }

    private void initView() {
        btnStartSearch = $(R.id.start_search);
        btnEndSearch = $(R.id.end_search);
        tv_device = $(R.id.tv_device);

        btnStartSearch.setOnClickListener(this);
        btnEndSearch.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_search:
                startToSearch();
                break;
            case R.id.end_search:
                endSearch();
                break;
        }
    }

    /**
     * 开始搜索
     */
    private void startToSearch() {
        if (searchClient != null && searchClient.isOpen()) {
            Toast.makeText(this, "已经开启搜索，请稍等", Toast.LENGTH_SHORT).show();
            return;
        }

        searchClient = new SearchClient(1024) {
            @Override
            public void onSearchStart() {
                mHandler.sendEmptyMessage(MESSAGE_SEARCH_START);
            }

            @Override
            public void onSearchDev(BaseUserData dev) {
                printLog("onSearchDev:" + dev.getIp());
                mDeviceList.add((DeviceData) dev);
                mHandler.sendEmptyMessage(MESSAGE_SEARCH_DEV);
            }

            @Override
            protected void onSearchFinish() {
                mHandler.sendEmptyMessage(MESSAGE_SEARCH_FINISH);
            }

            @Override
            public void printLog(String msg) {
                Log.i("lx", "phone," + msg);
            }
        };

        searchClient.init();
        Log.i("lx", "client init");
    }

    private void endSearch() {
        if (searchClient != null) {
            searchClient.close();
        }
        mDeviceList.clear();
    }

    /**
     * Handler
     */
    private class MyHandler extends Handler {
        private WeakReference<PhoneActivity> ref;

        MyHandler(PhoneActivity activity) {
            ref = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            PhoneActivity activity = ref.get();
            switch (msg.what) {
                case MESSAGE_SEARCH_START:
                    activity.tv_device.setText("开启搜索...\n");
                    mDeviceList.clear();
                    Toast.makeText(PhoneActivity.this, "开始搜索", Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_SEARCH_FINISH:
                    Toast.makeText(PhoneActivity.this, "结束搜索", Toast.LENGTH_SHORT).show();
                    tv_device.setText("还未开启局域网搜索设备\n");
                    break;
                case MESSAGE_SEARCH_DEV:
                    for (DeviceData d : activity.mDeviceList) {
                        String show = d.toString();
                        activity.tv_device.append(show + "\n\n");
                    }
                    Toast.makeText(PhoneActivity.this, "搜索到设备", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    private <V extends View> V $(int id) {
        return (V) findViewById(id);
    }

}
