package www.bkz.wifi.socket.client;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import www.bkz.wifi.R;
import www.bkz.wifi.socket.WriteThread;

public class ClientActivity extends AppCompatActivity {
    EditText ipEt, msgEt;
    SocketThread socketThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        ipEt = findViewById(R.id.ipEt);
        ipEt.setText("192.168.**.**");
        msgEt = findViewById(R.id.msgEt);

        findViewById(R.id.connectBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = ipEt.getText().toString();
                if (!TextUtils.isEmpty(ip)) {
                    socketThread = new SocketThread();
                    socketThread.setIp(ip);
                    socketThread.start();
                }
            }
        });

        findViewById(R.id.sendBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] msg = {msgEt.getText().toString()};
                if (!TextUtils.isEmpty(msg[0])
                        && socketThread != null) {
                    new WriteThread(socketThread.getSocket(), socketThread.getOutputStream())
                            .setMessage(msg[0])
                            .start();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(socketThread != null){
            socketThread.close();
        }
    }
}
