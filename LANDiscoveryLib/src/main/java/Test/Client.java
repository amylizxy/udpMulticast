package Test;

import android.util.Log;

import com.example.amyli.my.base.BaseUserData;
import com.example.amyli.my.client.SearchClient;

/**
 * Created by amyli on 2017/2/13.
 */

public class Client {
    private static final byte DEVICE_TYPE_NAME_21 = 0x21;
    private static final byte DEVICE_TYPE_ROOM_22 = 0x22;

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    public static void init() {
        SearchClient client = new SearchClient(1024) {
            @Override
            public void onSearchStart() {
                printLog("onsearch start");
            }

            @Override
            public void onSearchDev(BaseUserData dev) {
                printLog("onSearchDev:"+dev.getIp()+dev.getPort());
            }

            @Override
            protected void onSearchFinish() {
                printLog("onSearchFinish");
            }

            @Override
            public void printLog(String log) {
                System.out.println("Client:" + log);
                Log.i("lx","client:"+log);
            }
        };
        client.startSearch();
        Log.i("lx","client init");
    }

    public static void main(String[] args) {
        init();
    }

}
