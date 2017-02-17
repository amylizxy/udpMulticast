package com.example.amyli.my.base;

import android.util.Log;

import java.io.UnsupportedEncodingException;

/**
 * Created by amyli on 2017/2/14.
 */

public class RequestSearchData extends BaseUserData {
    private static final byte FIELD_TYPE_ASKFUNC = 0x21;

    private int askFunc;//功能位

    public RequestSearchData() {
    }

    public RequestSearchData(int flag) {
        this.askFunc = flag;
    }

    public int getAskFunc() {
        return askFunc;
    }

    public void setAskFunc(int askFunc) {
        this.askFunc = askFunc;
    }

    public static byte[] packRequestUserData(RequestSearchData data) {
        if (data == null) {
            return null;
        }

        byte[] funcBytes = Utils.intToBytes(data.getAskFunc());
        byte[] bytes = new byte[SearchConst.MAX_PACKET_LENGTH];
        int offset = 0;

        //add askFunc
        Utils.addFiledBytes(bytes, offset, funcBytes, FIELD_TYPE_ASKFUNC);

        byte[] result = new byte[bytes.length];
        System.arraycopy(bytes, 0, result, 0, bytes.length);
        return result;
    }

    public static RequestSearchData parseRequestUserData(byte[] userData) {
        if (userData == null) {
            return null;
        }

        if (userData.length < 5) {
            return null;
        }
        RequestSearchData requestSearchData = new RequestSearchData();
        int offset = 0;
        while (offset + 5 < userData.length) {
            byte dataType = userData[offset++];
            int len = Utils.bytesToInt(userData, offset);
            offset += SearchConst.INT_LENGTH;

            if (len < 0 || len + offset > userData.length) {
                return null;
            }
            switch (dataType) {
                case FIELD_TYPE_ASKFUNC:
                    int askFunc = Utils.bytesToInt(userData, offset);
                    Log.i("lx", "request parse askFunc:" + askFunc);
                    if (askFunc > 0)
                        requestSearchData.setAskFunc(askFunc);
                    break;
                default:
            }
            offset += len;
        }
        return requestSearchData;
    }

    @Override
    public String toString() {
        return "RequestSearchData={" + "ip=" + ip + ",port=" + port + ",askFunc=" + askFunc + "}";
    }
}
