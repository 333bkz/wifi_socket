package www.bkz.wifi.socket;

import android.os.SystemClock;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class WriteThread extends Thread {
    private Socket socket;
    private OutputStream outputStream;
    private byte[] data;
    private long lastTime;

    public Thread setMessage(String data) {
        data += '\0';
        this.data = data.getBytes();
        return this;
    }

    public WriteThread(Socket socket, OutputStream outputStream) {
        this.socket = socket;
        this.outputStream = outputStream;
    }

    @Override
    public void run() {
        super.run();
        if (socket != null
                && outputStream != null) {
            try {
                lastTime = SystemClock.currentThreadTimeMillis();
                outputStream.write(data);
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void close() {
        try {
            socket.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
