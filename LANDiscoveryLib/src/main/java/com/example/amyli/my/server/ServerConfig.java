package com.example.amyli.my.server;

import com.example.amyli.my.base.DeviceData;
import com.example.amyli.my.base.SearchConst;
import com.example.amyli.my.base.Utils;

/**
 * Created by amyli on 2017/2/15.
 */

public class ServerConfig {
    private static int func;
    private static DeviceData deviceData;

    public static int getFunc() {
        return func;
    }

    public static void setFunc(int func) {
        ServerConfig.func = func;
    }

    public static DeviceData getDeviceData() {
        return deviceData;
    }

    public static void setDeviceData(DeviceData deviceData) {
        ServerConfig.deviceData = deviceData;
    }
}
