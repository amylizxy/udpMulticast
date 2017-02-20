package com.udp.device;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
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
import java.util.List;


public class TVActivity extends AppCompatActivity implements View.OnClickListener {
    private static final byte DEVICE_TYPE_NAME_21 = 0x21;
    private static final byte DEVICE_TYPE_ROOM_22 = 0x22;

    private static final int SHOW_SEARCH_REQUEST = 1;
    private static final int START_BE_SEARCH = 2;
    private static final int END_BE_SEARCH = 3;

    private Button openBtn, closeBtn;
    private TextView msgTxt;
    private SearchServer searchServer;
    private MyHandler mHandler = new MyHandler(this);

    private List<RequestSearchData> requestList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        initView();
        initConfig();
    }

    private void initConfig() {
        ServerConfig.setFunc(1);
        DeviceData deviceData = new DeviceData();
        deviceData.setDevId("aaa111");
        deviceData.setFunc(1);
        deviceData.setServiceName("aa");
        deviceData.setPkgName(this.getPackageName());
        ServerConfig.setDeviceData(deviceData);
    }

    private void initView() {
        openBtn = $(R.id.open);
        closeBtn = $(R.id.close);
        msgTxt = $(R.id.msg);
        openBtn.setOnClickListener(this);
        closeBtn.setOnClickListener(this);
    }

    public void openBeSearch() {
        if (searchServer != null && searchServer.isOpen()) {
            Toast.makeText(this, "已经开启被发现功能，请稍等", Toast.LENGTH_SHORT).show();
            return;
        }
        mHandler.sendEmptyMessage(START_BE_SEARCH);
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

        searchServer.init();
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
                    for (RequestSearchData d : activity.requestList) {
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
                openBeSearch();
                break;
            case R.id.close:
                closeBeSearch();
                break;
        }
    }
}
