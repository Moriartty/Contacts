package com.moriarty.user.contacts.Thread;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.moriarty.user.contacts.Activity.MainActivity;
import com.moriarty.user.contacts.Others.SignalManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by user on 16-10-17.
 */
public class ClientThread implements Runnable {
    private Socket s;
    private Handler handler;
    private final static String currentTag= "ClientThread:";
    public Handler revHandler;
    BufferedReader br=null;
    OutputStream os=null;
    public ClientThread(Handler handler){
        this.handler=handler;
    }
    @Override
    public void run() {
        try{
            s=new Socket();
            s.connect(new InetSocketAddress(MainActivity.networkAddress,MainActivity.networkPort),10000);
            br=new BufferedReader(new InputStreamReader(s.getInputStream()));
            os=s.getOutputStream();
            new Thread(){
                @Override
                public void run(){
                    String content=null;
                    try{
                        while((content=br.readLine())!=null){
                           // stringBuffer.append(content);   //如果采用(content=br.readline())!=null这样会产生死锁
                           // Log.d("Moriarty","ClientThread:"+content);
                            Message msg=new Message();
                            msg.what= SignalManager.return_SPData_signal;
                            msg.obj=content;
                            handler.sendMessage(msg);
                        }
                        Log.d(MainActivity.TAG,currentTag+"传输完成");
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }.start();
            Looper.prepare();  //自定义线程需要自己创建looper才能使用handler.
            revHandler=new Handler(){
                @Override
                public void handleMessage(Message msg){
                    if(msg.what==SignalManager.send_TiebaRefresh_signal){
                        try{
                            //os.write((msg.obj.toString()+"\r\n").getBytes("utf-8"));
                            os.write((msg.obj.toString()+"\r\n").getBytes("utf-8"));
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    else if(msg.what==SignalManager.send_WeiboRefresh_signal){
                        try{
                            os.write((msg.obj.toString()+"\r\n").getBytes("utf-8"));
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            };
            Looper.loop();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
