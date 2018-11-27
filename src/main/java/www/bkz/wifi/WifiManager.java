package www.bkz.wifi;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public class WifiManager {
    private static final String TAG = "WifiManager";
    private volatile static WifiManager manager;
    private WeakReference<Context> context;
    private android.net.wifi.WifiManager wifiManager;
    private WifiInfo wifiInfo;
    private WifiBroadCastReceiver wifiStateReceiver;
    private WifiStateListener callback;
    private android.net.wifi.WifiManager.WifiLock wifiLock;
    private List<WifiConfiguration> wifiConfigurations;//已配置WIFI列表

    private WifiManager(Context context) {
        this.context = new WeakReference<>(context.getApplicationContext());
    }

    public Context getContext() {
        return context.get();
    }

    public static WifiManager getInstance(Context context) {
        if (manager == null) {
            synchronized (WifiManager.class) {
                if (manager == null) {
                    manager = new WifiManager(context);
                }
            }
        }
        return manager;
    }

    public void init() {
        wifiManager = (android.net.wifi.WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            getConnectionInfo();
            wifiConfigurations = wifiManager.getConfiguredNetworks();
        }
        wifiStateReceiver = new WifiBroadCastReceiver();
        getContext().registerReceiver(wifiStateReceiver, getIntentFilter());
    }

    public void finish() {
        callback = null;
        getContext().unregisterReceiver(wifiStateReceiver);

        if (wifiLock != null
                && wifiLock.isHeld()) {//判断wifi是否被lock锁持用
            wifiLock.release();// 释放锁
        }

        manager = null;
    }

    public boolean isWifiEnabled() {
        return wifiManager.isWifiEnabled();
    }

    public void acquire() {
        //创建wifi锁
        //在屏幕被关掉后继续使用WiFi则可以调用 acquireWifiLock来锁住WiFi，该操作会阻止WiFi进入睡眠状态。
        if (wifiLock == null) {
            wifiLock = wifiManager.createWifiLock("wifiLock");
        }
        // 使wifi锁有效
        wifiLock.acquire();
    }

    //开启关闭过程有延时
    public void wifiSwitch(boolean open) {
        if (open && !wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        } else if (!open && wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
        }
    }

    //扫描wifi
    public void wifiStartScan(Activity activity) {
        if (!isWifiEnabled()) {
            return;
        }
        if (!LocationUtil.isLocationEnabled(getContext())) {
            Toast.makeText(activity, "请开启位置服务！", Toast.LENGTH_SHORT).show();
            return;
        }
        wifiManager.startScan();
    }

    public void wifiStartConnect(final Context context, final WifiData wifiData) {
        int networkId = -1;
        if (wifiConfigurations != null) {
            for (WifiConfiguration configuration : wifiConfigurations) {
                String configId = configuration.SSID.replaceAll("\"", "");
                if (configId.equals(wifiData.SSID)) {
                    networkId = configuration.networkId;
                    break;
                }
            }
        }
        if (networkId != -1) {//已经连接配置过
            wifiManager.disconnect();
            //连接networkId所指的WIFI网络，并是其他的网络都被禁用
            wifiManager.enableNetwork(networkId, true);
        } else {//新的连接
            if (wifiData.encryptionType == EncryptionType.WIFI_CIPHER_NOPASS) { //不需要密码
                // 创建WiFi配置
                WifiConfiguration configuration = createWifiConfig(wifiData.SSID, wifiData.password, wifiData.encryptionType);
                // 添加WIFI网络
                int netId = wifiManager.addNetwork(configuration);
                if (netId != -1) wifiManager.saveConfiguration();
                // 使WIFI网络有效
                boolean addSuccess = wifiManager.enableNetwork(netId, true);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("输入密码");
                RelativeLayout view = new RelativeLayout(context);
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                view.setLayoutParams(params);
                view.setPadding(50, 50, 50, 0);
                final EditText editText = new EditText(context);
                editText.setBackgroundResource(R.drawable.bg_editext);
                view.addView(editText, params);
                builder.setView(view);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        wifiData.password = editText.getText().toString().trim();
                        if (!TextUtils.isEmpty(wifiData.password)) {
                            WifiConfiguration configuration = createWifiConfig(wifiData.SSID, wifiData.password, wifiData.encryptionType);
                            int netId = wifiManager.addNetwork(configuration);
                            if (netId != -1) wifiManager.saveConfiguration();
                            boolean addSuccess = wifiManager.enableNetwork(netId, true);
                        } else {
                            Toast.makeText(context, "请输入密码", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.show();
            }
        }
    }

    /**
     * 断开WiFi连接
     */
    public void disconnectWiFiNetWork(int networkId) {
        // 设置对应的wifi网络停用
        wifiManager.disableNetwork(networkId);
        // 断开所有网络连接
        wifiManager.disconnect();
    }

    /**
     * 创建WifiConfiguration
     * 三个安全性的排序为：WEP<WPA<WPA2。
     * WEP是Wired Equivalent Privacy的简称，有线等效保密（WEP）协议是对在两台设备间无线传输的数据进行加密的方式，
     * 用以防止非法用户窃听或侵入无线网络
     * WPA全名为Wi-Fi Protected Access，有WPA和WPA2两个标准，是一种保护无线电脑网络（Wi-Fi）安全的系统，
     * 它是应研究者在前一代的系统有线等效加密（WEP）中找到的几个严重的弱点而产生的
     * WPA是用来替代WEP的。WPA继承了WEP的基本原理而又弥补了WEP的缺点：WPA加强了生成加密密钥的算法，
     * 因此即便收集到分组信息并对其进行解析，也几乎无法计算出通用密钥；WPA中还增加了防止数据中途被篡改的功能和认证功能
     * WPA2是WPA的增强型版本，与WPA相比，WPA2新增了支持AES的加密方式
     **/
    private WifiConfiguration createWifiConfig(String SSID, String password, EncryptionType type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";

        if (type == EncryptionType.WIFI_CIPHER_NOPASS) {
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (type == EncryptionType.WIFI_CIPHER_WEP) {
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (type == EncryptionType.WIFI_CIPHER_WPA) {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.status = WifiConfiguration.Status.ENABLED;
        } else if (type == EncryptionType.WIFI_CIPHER_WPA2) {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }

    public WifiInfo getConnectionInfo() {
        wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo;
    }

    public List<ScanResult> getScanResult() {
        return wifiManager.getScanResults();
    }

    public void setWifiListener(WifiStateListener listener) {
        this.callback = listener;
    }

    private class WifiBroadCastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //noinspection ConstantConditions
            switch (intent.getAction()) {
                case android.net.wifi.WifiManager.RSSI_CHANGED_ACTION:
                    //wifi信号
                    int rssi = intent.getIntExtra(android.net.wifi.WifiManager.RSSI_CHANGED_ACTION, 0);
                    Log.e(TAG, "RSSI: " + rssi);
                    if (callback != null) {
                        callback.rssiCall(rssi);
                    }
                    break;
                case android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION: {
                    //网络状态
                    NetworkInfo info = intent.getParcelableExtra(android.net.wifi.WifiManager.EXTRA_NETWORK_INFO);
                    Log.e(TAG, "NETWORK_STATE : " + info.toString());
                    getConnectionInfo();
                    if (callback != null) {
                        callback.networkStateCall(info);
                    }
                }
                break;
                case android.net.wifi.WifiManager.WIFI_STATE_CHANGED_ACTION:
                    //WIFI开启状态
                    int wifiState = intent.getIntExtra(android.net.wifi.WifiManager.EXTRA_WIFI_STATE, android.net.wifi.WifiManager.WIFI_STATE_DISABLED);
                    switch (wifiState) {
                        case android.net.wifi.WifiManager.WIFI_STATE_DISABLED:
                            Log.e(TAG, "WIFI 已关闭");
                            if (callback != null) {
                                callback.wifiStateChangedCall(false);
                            }
                            break;
                        case android.net.wifi.WifiManager.WIFI_STATE_ENABLED:
                            Log.e(TAG, "WIFI 已开启");
                            if (callback != null) {
                                callback.wifiStateChangedCall(true);
                            }
                            break;
                    }
                    break;
                case android.net.wifi.WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
                    //扫描结果
                    boolean result = true;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        result = intent.getBooleanExtra(android.net.wifi.WifiManager.EXTRA_RESULTS_UPDATED, false);
                        Log.e(TAG, "扫描结果 : " + result);
                    }

                    List<ScanResult> scanResults = null;
                    if (result) {
                        scanResults = wifiManager.getScanResults();
                        Log.e(TAG, "扫描结果 : " + scanResults.size());
                    } else {
                        Log.e(TAG, "扫描结果 : 搜索失败");
                    }

                    if (scanResults != null
                            && !scanResults.isEmpty()
                            && callback != null) {
                        callback.scanCall(conversion(scanResults));
                    }
                    break;
                case android.net.wifi.WifiManager.SUPPLICANT_STATE_CHANGED_ACTION:
                    SupplicantState state = getConnectionInfo().getSupplicantState();
                    if (state == SupplicantState.COMPLETED) {
                        wifiManager.startScan();//验证成功,启动扫描
                    }

                    int wifiResult = intent.getIntExtra(android.net.wifi.WifiManager.EXTRA_SUPPLICANT_ERROR, 123);
                    if (wifiResult == android.net.wifi.WifiManager.ERROR_AUTHENTICATING) {
                        Log.e(TAG, "连接失败，身份验证出现问题");
                        if (callback != null) {
                            callback.connectCall(false);
                        }
                    }
                    break;
                case ConnectivityManager.CONNECTIVITY_ACTION: {
                    //联网状态的NetworkInfo对象
                    NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                    if (networkInfo != null) {
                        getConnectionInfo();
                        //当前的网络连接成功并且网络连接可用
                        if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {//网络为wifi连接
                            if (NetworkInfo.State.CONNECTED == networkInfo.getState()
                                    && networkInfo.isAvailable()) {
                                Log.e(TAG, "连接成功");
                                if (callback != null) {
                                    callback.connectCall(true);
                                }
                            } else {
                                Log.e(TAG, "连接断开");
                                if (callback != null) {
                                    callback.connectCall(false);
                                }
                            }
                        }
                    }
                }
                break;
            }
        }
    }

    public List<WifiData> conversion(List<ScanResult> scanResults) {
        //扫描数据转换WifiData
        List<WifiData> wifiDatas = null;
        if (scanResults != null
                && !scanResults.isEmpty()) {
            wifiDatas = new ArrayList<>();
            String connectWifi = "";
            if (wifiInfo != null) connectWifi = wifiInfo.getSSID().replaceAll("\"", "");
            for (ScanResult scanResult : scanResults) {
                if (TextUtils.isEmpty(scanResult.SSID)) continue;
                final WifiData data = new WifiData(scanResult);
                if (wifiDatas.contains(data)) continue;
                if (connectWifi.equals(data.SSID)) data.isConnected = true;
                wifiDatas.add(data);
            }
        }
        return wifiDatas;
    }

    @Nullable
    public List<WifiData> getWifiDatas() {
        return isWifiEnabled() ? conversion(getScanResult()) : null;
    }

    private IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(android.net.wifi.WifiManager.RSSI_CHANGED_ACTION);//wifi信号
        intentFilter.addAction(android.net.wifi.WifiManager.WIFI_STATE_CHANGED_ACTION);//wifi状态
        intentFilter.addAction(android.net.wifi.WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);//搜索结果
        intentFilter.addAction(android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION);//网络状态
        intentFilter.addAction(android.net.wifi.WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        return intentFilter;
    }
}
