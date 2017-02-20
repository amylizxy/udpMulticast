package com.example.amyli.my.base;

import java.io.UnsupportedEncodingException;

/**
 * Created by amyli on 2017/2/14.
 */

public class DeviceData extends BaseUserData {
    private static final byte FIELD_TYPE_DEVID = 0x31;
    private static final byte FIELD_TYPE_SERVICENAME = 0x32;
    private static final byte FIELD_TYPE_PKGNAME = 0x33;
    private static final byte FIELD_TYPE_FUNCTION = 0x34;

    private String devId;//设备id
    private String serviceName;
    private String pkgName;
    private int func;//功能位

    public String getDevId() {
        return devId;
    }

    public void setDevId(String devId) {
        this.devId = devId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public int getFunc() {
        return func;
    }

    public void setFunc(int func) {
        this.func = func;
    }

    /**
     * [filedType + filedLength+ filedValue]
     *
     * @param device
     * @return
     */
    public static byte[] packDeviceData(DeviceData device) {
        if (device == null || device.getDevId() == null || device
                .getServiceName() == null || device.getPkgName() == null) {
            return null;
        }

        try {
            byte[] devIdBytes = device.getDevId().getBytes("UTF-8");
            byte[] serviceNameBytes = device.getServiceName().getBytes("UTF-8");
            byte[] pkgNameBytes = device.getPkgName().getBytes("UTF-8");
            byte[] funcBytes = Utils.intToBytes(device.getFunc());

            byte[] data = new byte[SearchConst.MAX_PACKET_LENGTH];
            int offset = 0;

            //add devId
            Utils.addFiledBytes(data, offset, devIdBytes, FIELD_TYPE_DEVID);
            offset += SearchConst.FILED_HEADER_LENGTH + devIdBytes.length;

            //add serviceName
            Utils.addFiledBytes(data, offset, serviceNameBytes, FIELD_TYPE_SERVICENAME);
            offset += SearchConst.FILED_HEADER_LENGTH + serviceNameBytes.length;

            //add pkgName
            Utils.addFiledBytes(data, offset, pkgNameBytes, FIELD_TYPE_PKGNAME);
            offset += SearchConst.FILED_HEADER_LENGTH + pkgNameBytes.length;

            //add func
            Utils.addFiledBytes(data, offset, funcBytes, FIELD_TYPE_FUNCTION);

            byte[] result = new byte[data.length];
            System.arraycopy(data, 0, result, 0, data.length);
            return result;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static DeviceData parseDeviceUserData(byte[] userData) {
        DeviceData device = new DeviceData();
        if (userData.length < SearchConst.FILED_HEADER_LENGTH) {
            return null;
        }
        int offset = 0;
        while (offset + SearchConst.FILED_HEADER_LENGTH < userData.length) {
            byte dataType = userData[offset++];
            int len = Utils.bytesToInt(userData, offset);
            offset += SearchConst.INT_LENGTH;

            if (len < 0 || len + offset > userData.length) {
                return null;
            }
            switch (dataType) {
                case FIELD_TYPE_DEVID:
                    String devId = null;
                    try {
                        devId = new String(userData, offset, len, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    device.setDevId(devId);
                    break;
                case FIELD_TYPE_SERVICENAME:
                    String serviceName = null;
                    try {
                        serviceName = new String(userData, offset, len, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    device.setServiceName(serviceName);
                    break;
                case FIELD_TYPE_PKGNAME:
                    String pkgName = null;
                    try {
                        pkgName = new String(userData, offset, len, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    device.setPkgName(pkgName);
                    break;
                case FIELD_TYPE_FUNCTION:
                    int func = Utils.bytesToInt(userData, offset);
                    if (func > 0) {
                        device.setFunc(func);
                    }
                    break;
                default:
            }
            offset += len;
        }
        return device;
    }

    @Override
    public String toString() {
        return "DeviceData={ip=" + ip + ",port=" + port + ",devId=" + devId + ",serviceName=" +
                serviceName + ",pkgName=" + pkgName + ",func=" + func + "}";
    }
}
