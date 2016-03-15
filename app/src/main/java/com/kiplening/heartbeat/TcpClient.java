package com.kiplening.heartbeat;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by MOON on 3/14/2016.
 */
public class TcpClient {
    private String serverMessage;

    public static final String IP = "192.168.1.102";
    public static final int PORT = 8000;
    private boolean mRun = false;
    private OnMessageReceived mMessageListener = null;

    private Socket socket = null;
    private InputStream is = null;
    private OutputStream os = null;

    public void stopClient(){mRun = false;}

    public void sendMessage(String message) {
        if (os != null) {
            System.out.println("message: "+ message);
            try {
                os.write(message.getBytes());
            } catch (IOException e) {
                mRun = false;
                System.out.println("连接断开，正在重连。。。。");
                run();
                e.printStackTrace();
            }
        }
    }
    public void heartBeat(int value){
        if (socket != null){
            try {
                socket.sendUrgentData(value);
            } catch (IOException e) {
                mRun = false;
                System.out.println("连接断开，正在重连。。。。");
                run();
                e.printStackTrace();
            }
        }
    }

    public void run(){
        mRun = true;
        try {
            System.out.println("Connecting............");
            socket = new Socket(IP, PORT);

            os = socket.getOutputStream();
            is = socket.getInputStream();

            while (mRun){
                byte[] buffer = new byte[1024];
                int len = is.read(buffer);
                serverMessage = new String(buffer, 0, len);
                System.out.println(serverMessage);
                if (serverMessage != null && mMessageListener != null) {
                    //call the method messageReceived from MyActivity class
                    mMessageListener.messageReceived(serverMessage);
                }
                serverMessage = null;
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
}
