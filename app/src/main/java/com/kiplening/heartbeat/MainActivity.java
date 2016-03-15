package com.kiplening.heartbeat;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private TextView beat;
    private Button button;
    private TcpClient mTcpClient = null;
    private ConnectTask conctTask = null;
    private Handler handler ;
    private int HeartBeatTime = 4*1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        beat = (TextView) findViewById(R.id.beat);
        button = (Button) findViewById(R.id.button);

        //mTcpClient = new TcpClient();
        //mTcpClient.run();
        conctTask = new ConnectTask();
        System.out.println("New Task............");
        conctTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        //conctTask.doInBackground()

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
                    case 1:mTcpClient.sendMessage("beat");break;
                    default:break;
                }

            }
        };
        new Thread(new MyThread()).start();
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
                mTcpClient.sendMessage("Initial");
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
                    Thread.sleep(HeartBeatTime);// 线程暂停10秒，单位毫秒
                    Message message = new Message();
                    message.what = 1;
                    handler.sendMessage(message);// 发送消息
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}
