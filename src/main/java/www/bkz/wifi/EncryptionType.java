package www.bkz.wifi;

/**
 * 加密类型
 */
public enum EncryptionType {

    WIFI_CIPHER_NOPASS(0), WIFI_CIPHER_WEP(1), WIFI_CIPHER_WPA(2), WIFI_CIPHER_WPA2(3);

    private final int value;

    EncryptionType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
