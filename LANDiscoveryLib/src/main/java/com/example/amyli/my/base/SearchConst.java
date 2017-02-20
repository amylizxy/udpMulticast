package com.example.amyli.my.base;

/**
 * Created by amyli on 2017/2/13.
 */

public interface SearchConst {
    byte PACKET_TYPE_FIND_DEVICE_REQ = 0x01; // 搜索请求
    byte PACKET_TYPE_FIND_DEVICE_RSP = 0x02; // 搜索响应

    public final static byte PACKET_PREFIX = '$';
    int PACKET_HEADER_LENGTH = 6;
    int INT_LENGTH = 4;
    int FILED_HEADER_LENGTH = 5;

    public final static String MULTICAST_IP = "225.0.0.2"; // 组播地址
    public final static int C_PORT = 7838; // client组播端口
    public final static int S_PORT = 7839;// server组播端口
    public final static int PACK_SIZE = 4096;

    int RESPONSE_DEVICE_MAX = 250; // 响应设备的最大个数，防止UDP广播攻击

    public static int DEFAULT_TIME_OUT = 2000; // 设备默认的接收超时时间

    int MAX_PACKET_LENGTH = 1024;//包大小 1k

}
