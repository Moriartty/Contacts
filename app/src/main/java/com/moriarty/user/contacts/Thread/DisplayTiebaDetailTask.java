package com.moriarty.user.contacts.Thread;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.moriarty.user.contacts.Activity.MainActivity;
import com.moriarty.user.contacts.Others.SignalManager;
import com.moriarty.user.contacts.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
/**
 * Created by user on 16-11-22.
 */
public class DisplayTiebaDetailTask extends AsyncTask<Void,Void,Boolean> {   //开启异步任务进行联系人数据的写入
    Context mContext;
    OutputStream os;
    String url;
    BufferedReader br;
    Handler handler;
    ProgressDialog progressDialog;
    public DisplayTiebaDetailTask(Context context, String url, Handler handler,ProgressDialog progressDialog){
        mContext=context;
        this.url=url;
        this.handler=handler;
        this.progressDialog=progressDialog;
    }
    @Override
    protected Boolean doInBackground(Void... params) {
        Socket socket=new Socket();
        HashMap<String,String> recode=new HashMap<>();
        try {
            socket.connect(new InetSocketAddress(MainActivity.networkAddress,MainActivity.networkPort),20000);
            br=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            os=socket.getOutputStream();
            os.write((url+"TiebaCrawler*0"+"\r\n").getBytes("utf-8"));  //为了保证服务器端数据解析格式发统一，这里对传向服务器端的数据做了处理

        }catch (IOException e){
            e.printStackTrace();
            progressDialog.dismiss();
            return false;
        }

        new Thread(){
            @Override
            public void run(){
                String content=null;
                try{
                    while((content=br.readLine())!=null){
                        Message msg=new Message();
                        msg.what= SignalManager.return_TiebaDetail_signal;
                        msg.obj=content;
                        handler.sendMessage(msg);
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }.start();
        return true;
    }
    @Override
    protected void onPostExecute(Boolean flag){
        if(!flag){
            Toast.makeText(mContext,mContext.getResources().getString(R.string.connected_filed_tips),Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onPreExecute(){
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(mContext.getResources().getString(R.string.wait_to_add));
        progressDialog.show();
    }
}
