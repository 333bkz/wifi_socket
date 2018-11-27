package www.bkz.wifi.socket.server;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import www.bkz.wifi.R;
import www.bkz.wifi.socket.GetIpAddress;

public class ServerActivity extends AppCompatActivity {

    TextView ipTv, portTv, msgTv;

    private ServerSocketThread serverSocketThread;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    ipTv.setText("IP: " + GetIpAddress.getIP());
                    portTv.setText("PORT: " + GetIpAddress.getPort());
                    break;
                case 2:
                    msgTv.setText((String) msg.obj);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        ipTv = findViewById(R.id.ipTv);
        portTv = findViewById(R.id.portTv);
        msgTv = findViewById(R.id.msgTv);

        serverSocketThread = new ServerSocketThread();
        serverSocketThread.setHandler(handler).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serverSocketThread != null) {
            serverSocketThread.closeServerSocket();
        }
    }
}
