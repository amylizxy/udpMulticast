package com.example.amyli.my.server;

/**
 * Created by amyli on 2017/2/13.
 */

import com.example.amyli.my.base.DeviceData;
import com.example.amyli.my.base.RequestSearchData;
import com.example.amyli.my.base.SearchConst;
import com.example.amyli.my.base.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public abstract class SearchServer {

    private int mUserDataMaxLen;

    private volatile boolean mOpenFlag;

    private MulticastSocket sock;
    private InetAddress multicastInet;
    private Thread serverThread;

    /**
     * 构造函数
     * 不需要用户数据
     */
    public SearchServer() {
        this(0);
    }

    /**
     * 构造函数
     *
     * @param userDataMaxLen 搜索主机发送数据的最大长度
     */
    public SearchServer(int userDataMaxLen) {
        this.mUserDataMaxLen = userDataMaxLen;
    }

    /**
     * 打开
     * 即可以上线
     */
    public synchronized boolean init() {
        printLog("init");
        try {
            sock = new MulticastSocket(SearchConst.S_PORT);
            multicastInet = InetAddress.getByName(SearchConst.MULTICAST_IP);

            sock.joinGroup(multicastInet);
            sock.setLoopbackMode(false);// 必须是false才能开启广播功能
        } catch (IOException e) {
            printLog(e.toString());
            e.printStackTrace();
            close();
            return false;
        }

        serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                receiveAndSend();
            }
        });
        mOpenFlag = true;
        serverThread.start();
        return true;
    }

    /**
     * 关闭
     */
    public synchronized void close() {
        printLog("close");

        mOpenFlag = false;
        if (serverThread != null) {
            serverThread.interrupt();
        }

        if (sock != null) {
            try {
                sock.leaveGroup(multicastInet);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                sock.close();
                sock = null;
            }
        }
    }

    private int curSeq;

    public void receiveAndSend() {
        byte[] buf = new byte[mUserDataMaxLen];
        DatagramPacket recePack = new DatagramPacket(buf, buf.length);

        if (sock == null || sock.isClosed() || recePack == null) {
            return;
        }

        while (mOpenFlag) {
            try {
                printLog("server before receive");
                // waiting for search from host
                sock.receive(recePack);
                printLog("server after receive");
                // verify the data
                if (verifySearchReq(recePack)) {
                    byte[] userData = DeviceData.packDeviceData(ServerConfig.getDeviceData());
                    if (userData == null) {
                        return;
                    }

                    byte[] sendData = Utils.packData(curSeq, SearchConst
                            .PACKET_TYPE_FIND_DEVICE_RSP, userData);
                    if (sendData == null) {
                        return;
                    }

                    printLog("send response,seq:" + curSeq + ",userdata:" + ServerConfig
                            .getDeviceData().toString());

                    DatagramPacket sendPack = new DatagramPacket(sendData, sendData.length,
                            recePack.getAddress(), recePack.getPort());
                    sock.send(sendPack);
                }

            } catch (IOException e) {
                printLog(e.toString());
                break;
            }
        }
        printLog("设备关闭或已被找到");
    }

    /**
     * 校验客户端发的搜索请求数据
     * 协议：$ + packType(1) + sendSeq(4) [+ deviceIpLen(1) + deviceIp(n<=15)] [+ userData]
     * packType - 报文类型
     * sendSeq - 发送序列
     * deviceIpLen - 设备IP长度
     * deviceIp - 设备IP，仅在确认时携带
     * userData - 用户数据
     */
    private boolean verifySearchReq(DatagramPacket pack) {
        if (pack.getLength() < 6) {
            return false;
        }

        byte[] data = pack.getData();
        int offset = pack.getOffset();

        if (data[offset++] != SearchConst.PACKET_PREFIX || data[offset++] != SearchConst
                .PACKET_TYPE_FIND_DEVICE_REQ) {
            printLog("return false");
            return false;
        }

        int sendSeq = Utils.bytesToInt(data, offset);
        if (sendSeq < 0) {
            return false;
        }

        offset += SearchConst.INT_LENGTH;
        printLog("receive seq:" + sendSeq);

        curSeq = sendSeq;
        if (mUserDataMaxLen == 0 && offset == data.length) {
            return true;
        }

        // get userData
        byte[] userData = new byte[pack.getLength() - offset];
        System.arraycopy(data, offset, userData, 0, userData.length);

        RequestSearchData requestSearchData = RequestSearchData.parseRequestUserData(userData);
        String ip = pack.getAddress().getHostAddress();
        int port = pack.getPort();
        requestSearchData.setIp(ip);
        requestSearchData.setPort(port);
        printLog("receive requestSearchData:" + requestSearchData.toString());
        onReceiveSearchReq(requestSearchData);
        if (requestSearchData.getAskFunc() == ServerConfig.getFunc()) {
            return true;
        }
        return false;
    }

    /**
     * 打印日志
     * 由调用者打印，SE和Android不同
     */
    public abstract void printLog(String log);

    public abstract void onReceiveSearchReq(RequestSearchData data);

    public boolean isOpen() {
        return mOpenFlag;
    }
}
