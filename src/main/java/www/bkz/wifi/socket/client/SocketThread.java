package www.bkz.wifi.socket.client;

import android.util.Log;

import java.io.OutputStream;
import java.net.Socket;

public class SocketThread extends Thread {

    private OutputStream outputStream;
    private Socket socket;
    private String ip, port = "8000";

    public void setIp(String ip) {
        this.ip = ip;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public void run() {
        super.run();
        try {
            socket = new Socket(ip, Integer.parseInt(port));
            outputStream = socket.getOutputStream();
            Log.e("***客户端", "连接成功");
        } catch (Exception e) {
            Log.e("***客户端", "连接失败");
            e.printStackTrace();
        }
    }

    public void close() {
        if (socket != null) {
            try {
                socket.close();
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
