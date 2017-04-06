package com.example.amyli.my.base;

import android.content.Intent;
import android.util.Log;

import com.example.amyli.my.base.SearchConst;

import java.io.UnsupportedEncodingException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by amyli on 2017/2/14.
 */

public class Utils {


    /**
     * 封装报文
     * 协议：prefix + packType(1) + seq(4) +[userData]
     * prefix 标志性前缀
     * packType - 报文类型
     * seq - 发送序列
     * userData - 用户数据
     *
     * @param seq
     * @param packType
     * @param userData
     * @return
     */

    public static byte[] packData(int seq, byte packType, byte[] userData) {
        if (userData == null) {
            return null;
        }

        byte[] data = new byte[SearchConst.MAX_PACKET_LENGTH];
        int offset = 0;

        // 打包数据头部
        //add prefix
        data[offset++] = SearchConst.PACKET_PREFIX;
        //add msgType
        data[offset++] = packType;

        //add seq
        addInt(data, offset, seq);

        offset += SearchConst.INT_LENGTH;
        if (data.length < offset + userData.length) {//数组大小不够，则需要扩容
            byte[] tmp = new byte[offset + userData.length];
            System.arraycopy(data, 0, tmp, 0, offset);
            data = tmp;
        }

        System.arraycopy(userData, 0, data, offset, userData.length);
        offset += userData.length;

        byte[] result = new byte[offset];
        System.arraycopy(data, 0, result, 0, offset);

        return result;
    }

    public static int bytesToInt(byte[] src, int offset) {
        if (src == null || src.length < offset || offset + SearchConst.INT_LENGTH > src.length) {
            return -1;
        }
        int sendSeq;
        sendSeq = src[offset++] & 0xFF;
        sendSeq |= (src[offset++] << 8) & 0xFF00;
        sendSeq |= (src[offset++] << 16) & 0xFF0000;
        sendSeq |= (src[offset++] << 24) & 0xFF000000;
        return sendSeq;
    }

    public static byte[] intToBytes(int value) {
        byte[] src = new byte[4];
        src[0] = (byte) (value & 0xFF);
        src[1] = (byte) ((value >> 8) & 0xFF);
        src[2] = (byte) ((value >> 16) & 0xFF);
        src[3] = (byte) ((value >> 24) & 0xFF);
        return src;
    }

    public static void addInt(byte[] src, int offset, int value) {
        byte[] seqBytes = intToBytes(value);
        System.arraycopy(seqBytes, 0, src, offset, 4);
    }

    public static void printLog(String log) {
        Log.i("lx", log);
    }

    /**
     * [filedType + filedLength+ filedValue]
     *
     * @param src
     * @param filedBytes
     */
    public static void addFiledBytes(byte[] src, int offset, byte[] filedBytes, byte fieldType) {
        src[offset++] = fieldType;
        Utils.addInt(src, offset, filedBytes.length);
        offset += SearchConst.INT_LENGTH;
        System.arraycopy(filedBytes, 0, src, offset, filedBytes.length);
    }

}
