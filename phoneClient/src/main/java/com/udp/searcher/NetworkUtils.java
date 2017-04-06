package com.udp.searcher;

/**
 * Created by amyli on 2017/2/21.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.ArrayList;

/**
 * 获取网络状态，提供网络状态变化监听
 * CAUTION:
 * 如果注册了网络状态监听，务必在之后不再需要监听的时候解除监听。
 * 如果是Activity/Fragment等对象实现了OnNetworkChangeListener并注册监听，
 * 务必在onDestroy的时候解除监听，否则即使Activity被destroy掉，由于有对该Activity的引用，
 * 将会导致该Activity的内存无法释放。
 * @author zhaoxinyang
 */
public final class NetworkUtils {

    // 网络变化监听者list
    private static final ArrayList<OnNetworkChangeListener> sNetworkChangeListeners = new ArrayList<OnNetworkChangeListener>();
    // 监听网络变化receiver
    private static final BroadcastReceiver sBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("lx","network onReceive");

            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent
                    .getAction())) {// 监听网络连接状态变化
                notifyListeners();
            }
        }
    };

    private NetworkUtils() {
    }

    /**
     * 初始化
     */
    public static void init(Context context) {
        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            context.registerReceiver(sBroadcastReceiver, filter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void notifyListeners() {
        for (OnNetworkChangeListener listener : (ArrayList<OnNetworkChangeListener>) sNetworkChangeListeners
                .clone()) {
            if (listener != null) {
                listener.onNetworkChanged();
            }
        }
    }

    /**
     * 注册网络状态变化监听者
     */
    public synchronized static void registerNetworkChangeListener(
            OnNetworkChangeListener listener) {
        if (listener != null && !sNetworkChangeListeners.contains(listener)) {
            sNetworkChangeListeners.add(listener);
        }
    }

    /**
     * 解除网络状态监听
     */
    public synchronized static void unregisterNetworkChangeListener(
            OnNetworkChangeListener listener) {
        if (listener != null && sNetworkChangeListeners.contains(listener)) {
            sNetworkChangeListeners.remove(listener);
        }
    }

    /**
     * 网络状态变化监听接口
     */
    public interface OnNetworkChangeListener {
        public void onNetworkChanged();
    }

    /**
     * 网络连接是否可用
     */
    public static boolean isNetAvailable(Context context) {
        return getNetworkType(context) != NETWORK_TYPE_NONE;
    }

    /**
     * 没有接入移动蜂窝网
     */
    public static final int NETWORK_OPERATOR_NONE = 0;
    /**
     * 中国移动
     */
    public static final int NETWORK_OPERATOR_CMCC = 1;
    /**
     * 中国联通
     */
    public static final int NETWORK_OPERATOR_CUCC = 2;
    /**
     * 中国电信
     */
    public static final int NETWORK_OPERATOR_CTCC = 3;
    /**
     * 未知运营商
     */
    public static final int NETWORK_OPERATOR_UNKOWN = 4;

    /**
     * 获取运营商
     * @return
     *         参考NETWORK_OPERATOR_NONE;NETWORK_OPERATOR_CMCC;
     *         NETWORK_OPERATOR_CUCC;
     *         NETWORK_OPERATOR_CTCC;NETWORK_OPERATOR_UNKOWN
     */
    public static int getMobileSubscriber(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String operator = telephonyManager.getSimOperator();
        if (operator == null || operator.equals("")) {
            operator = telephonyManager.getSubscriberId();
        }
        if (operator != null) {
            if (operator.startsWith("46000") || operator.startsWith("46002")) {
                return NETWORK_OPERATOR_CMCC;
            } else if (operator.startsWith("46001")
                    || operator.startsWith("46010")) {
                return NETWORK_OPERATOR_CUCC;
            } else if (operator.startsWith("46003")) {
                return NETWORK_OPERATOR_CTCC;
            } else {
                return NETWORK_OPERATOR_UNKOWN;
            }
        } else {
            return NETWORK_OPERATOR_NONE;
        }
    }

    /**
     * 没有网络连接
     */
    public static final int NETWORK_TYPE_NONE = 0;
    /**
     * wifi
     */
    public static final int NETWORK_TYPE_WIFI = 1;
    /**
     * 2G网络连接
     */
    public static final int NETWORK_TYPE_2G = 2;
    /**
     * 3G网络连接
     */
    public static final int NETWORK_TYPE_3G = 3;
    /**
     * 4G网络连接
     */
    public static final int NETWORK_TYPE_4G = 4;
    /**
     * 未能判断网络类型，做移动网络处理
     */
    public static final int NETWORK_TYPE_MOBILE = 5;

    /**
     * 获取网络类型
     * @return 返回值，参考NETWORK_TYPE_NONE;NETWORK_TYPE_WIFI;NETWORK_TYPE_2G;
     *         NETWORK_TYPE_3G;
     *         NETWORK_TYPE_4G;NETWORK_TYPE_MOBILE
     */
    public static int getNetworkType(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isAvailable()) {
            if (ConnectivityManager.TYPE_WIFI == networkInfo.getType()) {
                return NETWORK_TYPE_WIFI;
            } else {
                TelephonyManager telephonyManager = (TelephonyManager) context
                        .getSystemService(Context.TELEPHONY_SERVICE);
                int networkType = telephonyManager.getNetworkType();
                switch (networkType) {
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        return NETWORK_TYPE_2G;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                        return NETWORK_TYPE_3G;
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        return NETWORK_TYPE_4G;
                    default:
                        return NETWORK_TYPE_MOBILE;
                }
            }
        } else {
            return NETWORK_TYPE_NONE;
        }
    }
}
