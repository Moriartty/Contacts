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
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

/**
 * Created by user on 17-2-15.
 */
public class RecommendThread implements Runnable {
    private Socket socket;
    private Handler handler;
    public Handler sendHandler;
    BufferedReader br=null;
    OutputStream os=null;
    Message msg;
    StringBuffer stringBuffer=new StringBuffer();
    private final static String currentTag="RecommendThread:";
    public RecommendThread(Handler handler){
        this.handler=handler;
        msg=new Message();
    }

    private boolean connectSocket(){
        try{
            socket=new Socket();
            socket.connect(new InetSocketAddress(MainActivity.networkAddress,MainActivity.networkPort),10000);
            br=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            os=socket.getOutputStream();
            return true;
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public class receiveThread implements Runnable{
        @Override
        public void run() {
            String content=null;
            try{
                while((content=br.readLine())!=null&&!content.equals("[over]")){
                    stringBuffer.append(content+"\r\n");   //通过标志来跳出阻塞
                }
                // Log.d(MainActivity.TAG,currentTag+" "+stringBuffer.toString());
                msg.what= SignalManager.return_NativePlaceSPData_signal;
                msg.obj=stringBuffer.toString();
                handler.sendMessage(msg);
                Log.d(MainActivity.TAG,currentTag+"传输完成");
               // socket.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        try{
            if(connectSocket()){
                new Thread(new receiveThread()).start();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        Looper.prepare();
        sendHandler=new Handler(){
            @Override
            public void handleMessage(Message msg){
                if(!socket.isConnected()){
                    Log.d(MainActivity.TAG,currentTag+" socket isn't connected ");
                    if(connectSocket())
                        new Thread(new receiveThread()).start();
                }
                else {
                    if(msg.what== SignalManager.send_RecommendFrag_signal){
                        try{
                            os.write((msg.obj.toString()+"\r\n").getBytes("utf-8"));
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }

            }
        };
        Looper.loop();
    }
}
