package www.bkz.wifi;

import android.net.NetworkInfo;

import java.util.List;

public interface WifiStateListener {

    void scanCall(List<WifiData> scanResults);

    void networkStateCall(NetworkInfo networkInfo);

    void wifiStateChangedCall(boolean state);

    void rssiCall(int rssi);

    void connectCall(boolean result);
}
