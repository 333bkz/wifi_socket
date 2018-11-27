package www.bkz.wifi.socket;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class ReadThread extends Thread {

    private Handler handler;
    private Socket socket;
    private InputStream inputStream;
    private StringBuffer stringBuffer = new StringBuffer();

    public ReadThread(Socket socket, InputStream inputStream, Handler handler) {
        this.socket = socket;
        this.inputStream = inputStream;
        this.handler = handler;
    }

    @Override
    public void run() {
        super.run();
        int len;
        byte[] bytes = new byte[20];
        boolean isString = false;
        try {
            //输入流关闭时等于 -1，
            //数据读完再去读才会等于-1，数据读完了，最后结果也就是读不到数据为0而已；
            while ((len = inputStream.read(bytes)) != -1) {
                Log.e("***ReceiveThread", "read");
                for (int i = 0; i < len; i++) {
                    if (bytes[i] != '\0') {
                        stringBuffer.append((char) bytes[i]);
                    } else {
                        isString = true;
                        break;
                    }
                }
                if (isString) {
                    isString = false;
                    Message message = handler.obtainMessage();
                    message.what = 2;
                    message.obj = stringBuffer.toString();
                    handler.sendMessage(message);
                    stringBuffer.append('\n');
                }
            }
        } catch (IOException e) {
            //异常发生时，说明客户端那边的连接已经断开
            e.printStackTrace();
            try {
                inputStream.close();
                socket.close();
            } catch (IOException e1) {
                Log.e("***ReceiveThread", "连接断开");
                e1.printStackTrace();
            }

        }
    }
}
