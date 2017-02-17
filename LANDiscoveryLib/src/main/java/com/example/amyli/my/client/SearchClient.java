package com.example.amyli.my.client;

import com.example.amyli.my.base.DeviceData;
import com.example.amyli.my.base.RequestSearchData;
import com.example.amyli.my.base.SearchConst;
import com.example.amyli.my.base.Utils;
import com.example.amyli.my.base.BaseUserData;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by amyli on 2017/2/13.
 */

public abstract class SearchClient {

    private int mUserDataMaxLen;
    private static boolean isOpen = false;

    private Set<BaseUserData> mDeviceSet;
    private static MulticastSocket sock;
    private String mDeviceIP;
    private DatagramPacket mSendPack;
    Thread sendThread, receiveThread;
    private InetAddress multicastInet;
    private int seq;

    public SearchClient(int userDataMaxLen) {
        seq = 0;
        mUserDataMaxLen = userDataMaxLen;
        mDeviceSet = new HashSet<>();
        try {
            sock = new MulticastSocket(SearchConst.C_PORT);
            multicastInet = InetAddress.getByName(SearchConst.MULTICAST_IP);
            sock.joinGroup(multicastInet);
            sock.setLoopbackMode(false);// 必须是false才能开启广播功能

            byte[] sendData = new byte[1024];
            mSendPack = new DatagramPacket(sendData, sendData.length, multicastInet, SearchConst
                    .S_PORT);

        } catch (IOException e) {
            printLog(e.toString());
            e.printStackTrace();
            close();
        }
    }

    public boolean init() {
        isOpen = true;
        onSearchStart();

        sendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                printLog("start send thread");
                send(sock);
            }
        });
        sendThread.start();

        receiveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                printLog("start receive thread");
                receive(sock);
            }
        });
        receiveThread.start();

        return true;
    }


    public void close() {
        isOpen = false;
        if (sendThread != null) {
            sendThread.interrupt();
        }
        if (receiveThread != null) {
            receiveThread.interrupt();
        }
        if (sock != null) {
            try {
                sock.leaveGroup(multicastInet);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                sock.close();
            }
        }
        onSearchFinish();
    }

    public static boolean isOpen() {
        return isOpen;
    }

    public static void setIsOpen(boolean isOpen) {
        SearchClient.isOpen = isOpen;
    }

    public abstract void onSearchStart();

    public abstract void onSearchDev(BaseUserData dev);

    protected abstract void onSearchFinish();

    public abstract void printLog(String msg);

    private void send(MulticastSocket sock) {
        if (sock == null || sock.isClosed()) {
            return;
        }

        while (isOpen) {
            byte mPackType = SearchConst.PACKET_TYPE_FIND_DEVICE_REQ;
            RequestSearchData request = new RequestSearchData(ClientConfig.getAskFunc());
            byte[] userData = RequestSearchData.packRequestUserData
                    (request);
            if (userData == null) {
                printLog("userdata null,return");
                return;
            }

            byte[] bytes = Utils.packData(seq, mPackType, userData);
            if (bytes == null) {
                printLog("send null,return");
                return;
            }

            mSendPack.setData(bytes);
            try {
                sock.send(mSendPack);
                printLog("send seq:" + seq);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
            seq++;
        }
        close();
    }

    private void receive(MulticastSocket sock) {
        if (sock == null || sock.isClosed()) {
            return;
        }

        byte[] receData = new byte[SearchConst.PACKET_HEADER_LENGTH + mUserDataMaxLen];
        DatagramPacket recePack = new DatagramPacket(receData, receData.length);

        while (isOpen) {
            recePack.setData(receData);
            try {
                sock.receive(recePack);
                if (recePack.getLength() > 0) {
                    mDeviceIP = recePack.getAddress().getHostAddress();
                    //check if it's itself
                    //check the ip if already exist
                    if (parseResponsePack(recePack)) {
                        printLog("a response from：" + mDeviceIP);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
        close();
    }

    /**
     * 解析报文
     * 协议：$ + packType(1) + userData(n)
     *
     * @param pack 数据报
     */
    private boolean parseResponsePack(DatagramPacket pack) {
        if (pack == null || pack.getAddress() == null) {
            return false;
        }

        String ip = pack.getAddress().getHostAddress();
        int port = pack.getPort();
        for (BaseUserData d : mDeviceSet) {
            if (d.getIp().equals(ip)) {
                printLog("is the same ip device");
                return false;
            }
        }

        // 解析头部数据
        byte[] data = pack.getData();
        int dataLen = pack.getLength();
        int offset = pack.getOffset();

        if (dataLen < SearchConst.PACKET_HEADER_LENGTH || data[offset++] != SearchConst
                .PACKET_PREFIX || data[offset++] !=
                SearchConst.PACKET_TYPE_FIND_DEVICE_RSP) {
            printLog("parse return false");
            return false;
        }

        int sendSeq = Utils.bytesToInt(data, offset);
        printLog("receive response,seq:" + sendSeq);
        if (sendSeq < 0) {
            return false;
        }
        if (mUserDataMaxLen == 0 && dataLen == SearchConst.PACKET_HEADER_LENGTH) {
            return false;
        }

        // 解析用户数据
        int userDataLen = dataLen - SearchConst.PACKET_HEADER_LENGTH;
        byte[] userData = new byte[userDataLen];
        System.arraycopy(data, SearchConst.PACKET_HEADER_LENGTH, userData, 0, userDataLen);

        DeviceData device = DeviceData.parseDeviceUserData(userData);
        device.setIp(ip);
        device.setPort(port);
        printLog("receive response,device:" + device.toString());
        mDeviceSet.add(device);
        onSearchDev(device);
        return true;
    }

}
