package com.kiplening.heartbeat;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private TextView beat;
    private Button button;
    private TcpClient mTcpClient = null;
    private ConnectTask conctTask = null;
    private Handler handler ;
    private int HeartBeatTime = 4*1000;
    private int successCount = 0;
    private int successHeartBeatTime;
    private int stepTime = 30*1000;
    private int failTime = 0;
    private int STATE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        beat = (TextView) findViewById(R.id.beat);
        button = (Button) findViewById(R.id.button);

        conctTask = new ConnectTask();
        System.out.println("New Task............");
        conctTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        beat.setText(new Date().toString());

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTcpClient != null) {
                    //mTcpClient.heartBeat(1);
                }
            }
        });
        handler = new Handler() {
            public void handleMessage(Message msg) {
                // 要做的事情
                super.handleMessage(msg);
                switch (msg.what){
                    case 0:
                        try{
                            if (successCount < 3 ){
                                mTcpClient.sendMessage("beat");
                            }
                            else {
                                STATE = 1;
                                successCount = 0;
                            }
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                        break;
                    case 1:
                        try {
                            if (failTime < 3){

                                mTcpClient.sendMessage("beat");
                                successHeartBeatTime = HeartBeatTime;
                                HeartBeatTime = HeartBeatTime + stepTime;
                                failTime = 0;
                            }else {
                                failTime = 0;
                                STATE = 2;
                            }

                        } catch (IOException e) {
                            STATE = 0;
                            mTcpClient.run();
                            e.printStackTrace();
                        }
                        break;
                    case 2:
                        try {
                            mTcpClient.sendMessage("beat");
                            HeartBeatTime = 0;

                        } catch (IOException e) {
                            //STATE = 0;
                            if (failTime < 3){
                                failTime++;
                                mTcpClient.run();
                            }
                            else {
                                failTime = 0;
                                STATE = 0;
                                mTcpClient.run();
                            }
                            e.printStackTrace();
                        }
                        break;
                    default:break;
                }

            }
        };
        new Thread(new MyThread()).start();
    }

    private void reConnect() {
        HeartBeatTime = 4*1000;
        successHeartBeatTime = HeartBeatTime;
        successCount = 0;
        STATE = 0;
        mTcpClient.run();
    }

    public class ConnectTask extends AsyncTask<String,String,TcpClient> {
        @Override
        protected TcpClient doInBackground(String... message)
        {
            //we create a TCPClient object and
            System.out.println("Get in...........");
            mTcpClient = new TcpClient();
            mTcpClient.run();
            if(mTcpClient!=null)
            {
                try {
                    mTcpClient.sendMessage("Initial");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

    }

    public class MyThread implements Runnable {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            while (true) {
                try {
                    Thread.sleep(1000); //暂停一秒，让程序处理更新HeartBeatTime
                    Thread.sleep(HeartBeatTime-1000);// 线程暂停，单位毫秒
                    Message message = new Message();

                    message.what = STATE;
                    handler.sendMessage(message);// 发送消息
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}
