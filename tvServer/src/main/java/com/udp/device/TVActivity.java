package com.udp.device;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.amyli.my.base.DeviceData;
import com.example.amyli.my.base.RequestSearchData;
import com.example.amyli.my.server.SearchServer;
import com.example.amyli.my.server.ServerConfig;

import java.lang.ref.WeakReference;
import java.util.ArrayList;


public class TVActivity extends AppCompatActivity implements View.OnClickListener, NetworkUtils
        .OnNetworkChangeListener {
    private static final byte DEVICE_TYPE_NAME_21 = 0x21;
    private static final byte DEVICE_TYPE_ROOM_22 = 0x22;

    private static final int SHOW_SEARCH_REQUEST = 1;
    private static final int START_BE_SEARCH = 2;
    private static final int END_BE_SEARCH = 3;

    private Button openBtn, closeBtn;
    private TextView msgTxt;
    private SearchServer searchServer;
    private MyHandler mHandler = new MyHandler(this);
    private WifiManager.MulticastLock multicastLock;

    private ArrayList<RequestSearchData> requestList = new ArrayList<>();

    private boolean isOpenFuc = false;

    private String curWifiId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        initView();
        init();
    }

    private void init() {
        ServerConfig.setFunc(1);
        DeviceData deviceData = new DeviceData();
        deviceData.setDevId("aaa111");
        deviceData.setFunc(1);
        deviceData.setServiceName("aa");
        deviceData.setPkgName(this.getPackageName());
        ServerConfig.setDeviceData(deviceData);

        NetworkUtils.init(this);
        NetworkUtils.registerNetworkChangeListener(this);
        searchServer = new SearchServer(1024) {

            @Override
            public void printLog(String log) {
                System.out.println("server:" + log);
                Log.i("lx", "tv:" + log);
            }

            @Override
            public void onReceiveSearchReq(RequestSearchData data) {
                requestList.add(data);
                mHandler.sendEmptyMessage(SHOW_SEARCH_REQUEST);
            }
        };
    }

    private void acquireMulticast() {
        WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService
                (Context.WIFI_SERVICE);
        multicastLock = wifiManager.createMulticastLock("multicast.test");
        multicastLock.acquire();
    }

    private void initView() {
        openBtn = $(R.id.open);
        closeBtn = $(R.id.close);
        msgTxt = $(R.id.msg);
        msgTxt.setMovementMethod(ScrollingMovementMethod.getInstance());
        openBtn.setOnClickListener(this);
        closeBtn.setOnClickListener(this);
    }

    public void openBeSearch() {
//        acquireMulticast();
        if (NetworkUtils.getNetworkType(this) != NetworkUtils.NETWORK_TYPE_WIFI) {
            Toast.makeText(this, "请在wifi下使用局域网发现", Toast.LENGTH_SHORT).show();
            return;
        }
        if (searchServer != null && searchServer.isOpen()) {
            Toast.makeText(this, "已经开启被发现功能，请稍等", Toast.LENGTH_SHORT).show();
            return;
        }
        mHandler.sendEmptyMessage(START_BE_SEARCH);
        searchServer.init();
    }

    private void openFunc() {
        if (NetworkUtils.getNetworkType(this) != NetworkUtils.NETWORK_TYPE_WIFI) {
            Toast.makeText(this, "请在wifi下使用局域网发现", Toast.LENGTH_SHORT).show();
            return;
        }
        openBeSearch();
        curWifiId = getCurWifiSSID();
        isOpenFuc = true;
    }

    private void closeFunc() {
        closeBeSearch();
        isOpenFuc = false;
    }

    @Override
    public void onNetworkChanged() {
        int curNetworkType = NetworkUtils.getNetworkType(this);
        Log.i("lx", "getNetworkType:" + curNetworkType);

        if (curNetworkType == NetworkUtils.NETWORK_TYPE_WIFI) {//是wifi网络
            Log.i("lx", "last wifiid:" + curWifiId + ",cur wifiid:" + getCurWifiSSID());
            if (isOpenFuc && getCurWifiSSID() != null && !getCurWifiSSID().equals(curWifiId))
            {//开启了搜索功能，并且切换了wifi
                Log.i("lx", "network change,start besearch again");
                closeBeSearch(); //只要有网络变化，就先关掉设备搜索功能，因为手机的网络切换时，即使是wifi1切换到wifi2，也可能收到多次广播的网络类型都一样；
                openBeSearch();
                curWifiId = getCurWifiSSID();
            }
        } else {//无网或者移动网络
            Log.i("lx", "network change,end besearch");
            curWifiId = null;
            if (isOpenFuc) {
                closeBeSearch();
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

    private class MyHandler extends Handler {
        private WeakReference<TVActivity> ref;

        MyHandler(TVActivity activity) {
            ref = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            TVActivity activity = ref.get();
            switch (msg.what) {
                case SHOW_SEARCH_REQUEST:
                    for (RequestSearchData d : (ArrayList<RequestSearchData>) requestList.clone()) {
                        String show = d.toString();
                        activity.msgTxt.append(show + "\n\n");
                    }
                    break;
                case START_BE_SEARCH:
                    Toast.makeText(TVActivity.this, "开启被发现功能，请稍等", Toast.LENGTH_SHORT).show();
                    msgTxt.setText("已开启被发现功能,收到被发现请求" + "\n");
                    break;
                case END_BE_SEARCH:
                    Toast.makeText(TVActivity.this, "关闭被发现功能", Toast.LENGTH_SHORT).show();
                    msgTxt.setText("关闭被发现功能");
                    break;
            }
        }
    }

    public void closeBeSearch() {
        if (multicastLock != null) {
            multicastLock.release();
        }

        if (searchServer != null) {
            searchServer.close();
        }
        mHandler.sendEmptyMessage(END_BE_SEARCH);
        requestList.clear();
    }

    private <V extends View> V $(int id) {
        return (V) findViewById(id);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.open:
                openFunc();
                break;
            case R.id.close:
                closeFunc();
                break;
        }
    }
}
