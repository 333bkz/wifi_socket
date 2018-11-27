package www.bkz.wifi.socket.server;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import www.bkz.wifi.socket.GetIpAddress;
import www.bkz.wifi.socket.ReadThread;

public class ServerSocketThread extends Thread {
    private Handler handler;
    private ServerSocket serverSocket;
    private boolean alive = true;

    public ServerSocketThread setHandler(Handler handler) {
        this.handler = handler;
        return this;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    @Override
    public void run() {
        super.run();
        try {
            serverSocket = new ServerSocket(8000);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (serverSocket != null) {
            GetIpAddress.getLocalIpAddress(serverSocket);
            handler.sendEmptyMessage(1);

            while (alive) {
                Log.e("ServerSocketThread", Thread.currentThread().getName());
                Socket socket = null;
                InputStream inputStream = null;
                try {
                    socket = serverSocket.accept();//阻塞
                    Log.e("***服务端", "已连接： " + socket.getInetAddress());
                    inputStream = socket.getInputStream();
                } catch (IOException e) {
                    Log.e("***服务端", "连接失败");
                    e.printStackTrace();
                }
                if (socket != null) {
                    new ReadThread(socket, inputStream, handler)
                            .start();
                }
            }
        }
    }

    public void closeServerSocket() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
