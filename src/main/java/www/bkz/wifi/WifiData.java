package www.bkz.wifi;

import android.net.wifi.ScanResult;

public class WifiData {

    public String SSID;

    /**
     * 物理地址
     */
    public String BSSID;

    /**
     * 加密方案
     */
    public String capabilities;

    /**
     * 加密方案
     */
    public EncryptionType encryptionType;

    /**
     * 信号
     */
    public int level;

    /**
     * 热点频率
     */
    public int frequency;

    public String password;

    public boolean isSelected;

    public boolean isConnected;

    public WifiData() {
    }

    public WifiData(ScanResult scanResult) {
        this.SSID = scanResult.SSID;
        this.BSSID = scanResult.BSSID;
        this.capabilities = scanResult.capabilities;
        this.level = scanResult.level;
        this.frequency = scanResult.frequency;

        if (capabilities.contains("WPA2") || capabilities.contains("WPA-PSK")) {
            encryptionType = EncryptionType.WIFI_CIPHER_WPA2;
        } else if (capabilities.contains("WPA")) {
            encryptionType = EncryptionType.WIFI_CIPHER_WPA;
        } else if (capabilities.contains("WEP")) {//WIFICIPHER_WEP 加密
            encryptionType = EncryptionType.WIFI_CIPHER_WEP;
        } else {//WIFICIPHER_OPEN NOPASSWORD 开放无加密
            encryptionType = EncryptionType.WIFI_CIPHER_NOPASS;
        }

    }

    @Override
    public boolean equals(Object obj) {
        return obj != null
                && obj instanceof WifiData
                && ((WifiData) obj).BSSID.equals(BSSID);
    }

    @Override
    public String toString() {
        return "WifiData{" +
                "SSID='" + SSID + '\'' +
                ", BSSID='" + BSSID + '\'' +
                ", capabilities='" + capabilities + '\'' +
                ", level=" + level +
                ", password='" + password + '\'' +
                ", isSelected=" + isSelected +
                ", isConnected=" + isConnected +
                '}';
    }
}
