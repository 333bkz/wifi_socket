package www.bkz.wifi;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import www.bkz.wifi.socket.client.ClientActivity;
import www.bkz.wifi.socket.server.ServerActivity;

public class WifiActivity extends AppCompatActivity
        implements WifiStateListener, AdapterView.OnItemClickListener, CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    public static final String TAG = "TestActivity7";
    private WifiManager manager;
    private ListView wifiList;
    private ScanResultAdapter adapter;
    private Switch wifiSwitch;
    private TextView wifiInfoTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);

        findViewById(R.id.serverBtn).setOnClickListener(this);
        findViewById(R.id.clientBtn).setOnClickListener(this);
        wifiList = findViewById(R.id.lib_common_wifiList);
        wifiInfoTv = findViewById(R.id.lib_common_wifi_info);
        wifiSwitch = findViewById(R.id.lib_common_switch);
        wifiSwitch.setOnCheckedChangeListener(this);

        manager = WifiManager.getInstance(this);
        manager.init();
        manager.acquire();
        manager.setWifiListener(this);
        adapter = new ScanResultAdapter(manager.getWifiDatas(), R.layout.item, this);
        wifiList.setAdapter(adapter);
        wifiList.setOnItemClickListener(this);
        wifiSwitch.setChecked(manager.isWifiEnabled());
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.lib_common_scan) {
            manager.wifiStartScan(this);
            adapter.refresh(manager.getWifiDatas());
        } else if (view.getId() == R.id.serverBtn) {
            startActivity(new Intent(this, ServerActivity.class));
        } else if (view.getId() == R.id.clientBtn) {
            startActivity(new Intent(this, ClientActivity.class));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        manager.finish();
    }

    @Override
    public void scanCall(List<WifiData> scanResults) {
        adapter.refresh(scanResults);
    }

    @Override
    public void networkStateCall(NetworkInfo networkInfo) {
        wifiInfoTv.setText(manager.getConnectionInfo().toString());
    }

    @Override
    public void wifiStateChangedCall(boolean state) {
        if (!state) {
            adapter.refresh(null);
        }
        wifiSwitch.setChecked(state);
    }

    @Override
    public void rssiCall(int rssi) {

    }

    @Override
    public void connectCall(boolean result) {
        Toast.makeText(this, result ? "连接成功" : "连接失败", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final WifiData wifi = adapter.getScanResult(position);
        connectWifi(wifi);
    }

    private void connectWifi(WifiData wifiData) {
        if (wifiData != null) {
            if (manager.isWifiEnabled()) {
                manager.wifiStartConnect(this, wifiData);
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        manager.wifiSwitch(isChecked);
    }

    public class ScanResultAdapter extends ListBaseAdapter<WifiData, ScanResultViewHolder> {

        public ScanResultAdapter(List<WifiData> data, int layoutRes, Context context) {
            super(data, layoutRes, context);
        }

        public WifiData getScanResult(int position) {
            return data.get(position);
        }


        @Override
        public void initItemView(int position, ScanResultViewHolder vh) {
            WifiData result = data.get(position);
            if (result != null) {
                vh.name.setText(result.SSID);
                vh.mac.setText(result.BSSID);
                vh.rssi.setText(String.valueOf(result.level));
                vh.check.setChecked(result.isSelected);
                if (result.isConnected) {
                    vh.name.setTextColor(Color.RED);
                    vh.mac.setTextColor(Color.RED);
                    vh.rssi.setTextColor(Color.RED);
                } else {
                    vh.name.setTextColor(Color.BLACK);
                    vh.mac.setTextColor(Color.BLACK);
                    vh.rssi.setTextColor(Color.BLACK);
                }
            }
        }

        @Override
        public ScanResultViewHolder onCreateViewHolder(View convertView, ViewGroup parent) {
            return new ScanResultViewHolder(convertView);
        }
    }

    public class ScanResultViewHolder extends ListBaseAdapter.ViewHolder {
        public TextView name;
        public TextView mac;
        public TextView rssi;
        public CheckBox check;

        public ScanResultViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.lib_common_name);
            mac = itemView.findViewById(R.id.lib_common_mac);
            rssi = itemView.findViewById(R.id.lib_common_rssi);
            check = itemView.findViewById(R.id.lib_common_check);
            check.setVisibility(View.GONE);
        }
    }
}
