package com.udp.searcher;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
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

/**
 * 搜索主机 —— 简单demo
 */
public class PhoneActivity extends AppCompatActivity implements View.OnClickListener,
        NetworkUtils.OnNetworkChangeListener {
    private static final int MESSAGE_SEARCH_START = 1;
    private static final int MESSAGE_SEARCH_FINISH = 2;
    private static final int MESSAGE_SEARCH_DEV = 3;

    private Button btnStartSearch;
    private Button btnEndSearch;

    private TextView tv_device;

    private ArrayList<DeviceData> mDeviceList = new ArrayList<>();

    private MyHandler mHandler = new MyHandler(this);

    private SearchClient searchClient;

    private boolean isOpenFuc = false;

    private String curWifiId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        init();
    }

    private void init() {
        NetworkUtils.init(this);
        NetworkUtils.registerNetworkChangeListener(this);
        ClientConfig.setAskFunc(1);
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
    }

    private void initView() {
        btnStartSearch = $(R.id.start_search);
        btnEndSearch = $(R.id.end_search);
        tv_device = $(R.id.tv_device);
        tv_device.setMovementMethod(ScrollingMovementMethod.getInstance());
        btnStartSearch.setOnClickListener(this);
        btnEndSearch.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_search:
                openFunc();
                break;
            case R.id.end_search:
                closeFunc();
                break;
        }
    }

    private void openFunc() {
        if (NetworkUtils.getNetworkType(this) != NetworkUtils.NETWORK_TYPE_WIFI) {
            Toast.makeText(this, "请在wifi下使用局域网发现", Toast.LENGTH_SHORT).show();
            return;
        }
        startToSearch();
        curWifiId = getCurWifiSSID();
        isOpenFuc = true;
    }

    private void closeFunc() {
        endSearch();
        isOpenFuc = false;
    }

    /**
     * 开始搜索
     */
    private void startToSearch() {
        if (searchClient != null && searchClient.isOpen()) {
            Toast.makeText(this, "已经开启搜索，请稍等", Toast.LENGTH_SHORT).show();
            return;
        }
        searchClient.startSearch();
        Log.i("lx", "client init");
    }

    private void endSearch() {
        if (searchClient != null) {
            Log.i("lx", "endSearch");
            searchClient.close();
        }
        mDeviceList.clear();
        Log.i("lx", "clear");
    }

    //strategy:网络断开，需要close，网络重新连接，需要重新开启搜索
    @Override
    public void onNetworkChanged() {
        int curNetworkType = NetworkUtils.getNetworkType(this);
        Log.i("lx", "getNetworkType:" + curNetworkType);

        if (curNetworkType == NetworkUtils.NETWORK_TYPE_WIFI) {//是wifi网络
            Log.i("lx", "last wifiid:" + curWifiId + ",cur wifiid:" + getCurWifiSSID());
            if (isOpenFuc && getCurWifiSSID() != null && !getCurWifiSSID().equals(curWifiId))
            {//开启了搜索功能，并且切换了wifi
                Log.i("lx", "network change,start again");
                endSearch(); //只要有网络变化，就先关掉设备搜索功能，因为手机的网络切换时，即使是wifi1切换到wifi2，也可能收到多次广播的网络类型都一样；
                startToSearch();
                curWifiId = getCurWifiSSID();
            }
        } else {//无网或者移动网络
            Log.i("lx", "network change,end search");
            curWifiId = null;
            if (isOpenFuc) {
                endSearch();
            }
        }
    }

    private String getCurWifiSSID() {
        WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService
                (Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (null != wifiInfo) {
            return wifiInfo.getSSID();
        }
        return null;
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
                    for (DeviceData d : (ArrayList<DeviceData>) mDeviceList.clone()) {
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
