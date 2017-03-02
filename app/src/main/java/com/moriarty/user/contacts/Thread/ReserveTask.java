package com.moriarty.user.contacts.Thread;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.moriarty.user.contacts.Activity.MainActivity;
import com.moriarty.user.contacts.Others.MapToXML;
import com.moriarty.user.contacts.Others.SignalManager;
import com.moriarty.user.contacts.R;
import com.moriarty.user.contacts.Service.QueryContactsService;

import org.dom4j.DocumentException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by user on 16-11-1.
 */
public class ReserveTask extends AsyncTask<Void,Void,Integer> {   //开启异步任务进行联系人数据的上传
    Context mContext;
    OutputStream os;
    String data;
    BufferedReader br;
    ProgressDialog progressDialog;
    String tag1="reserve"+"\r\n";
    String tag2="mydata"+"\r\n";
    private static final String currentTag="ReserveTask:";
    int flag;
    public Handler handler;
    StringBuffer stringBuffer=new StringBuffer();
    private QueryContactsService queryContactsService;     //查询服务对象

    public ReserveTask(Context context, int flag, Handler handler){
        mContext=context;
        this.handler=handler;
        this.flag=flag;
        progressDialog=new ProgressDialog(context);
    }

    @Override
    protected Integer doInBackground(Void... params) {
        try{
            Socket socket=new Socket();
            socket.connect(new InetSocketAddress(MainActivity.networkAddress,MainActivity.networkPort),20000);
            br=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            os=socket.getOutputStream();
        }catch (IOException e){
            return 1;  //与服务器失去连接
        }

        new Thread(){
            @Override
            public void run(){
                String content=null;
                try{
                    while((content=br.readLine())!=null&&!content.equals("[over]")){
                        stringBuffer.append(content+"\r\n");
                        Log.d(MainActivity.TAG,currentTag+content);
                    }
                    Message msg=new Message();
                    msg.what= SignalManager.return_ReserveAction_signal;
                    msg.obj=stringBuffer.toString();
                    handler.sendMessage(msg);
                    progressDialog.dismiss();
                    Log.d(MainActivity.TAG,currentTag+"传输完成");
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }.start();

        if(flag==0){    //上传本地数据
            ArrayList<HashMap<String,String>> reserveData=queryContactsService.getReserveData(mContext);
            SharedPreferences myInfo;
            myInfo=mContext.getSharedPreferences("myself",mContext.MODE_PRIVATE);
            HashMap<String,String> mydata=queryContactsService.getMyInfoToMap(myInfo);
            Log.d(MainActivity.TAG,currentTag+"my source_id is"+mydata.get("source_id"));

            if(mydata.get("phone")==null||mydata.get("phone").equals("")){
                Log.d(MainActivity.TAG,currentTag+"my data is null");
                progressDialog.dismiss();
                return 2;
            }
            else{
                reserveData.add(mydata);
                try{
                    data=MapToXML.formatXml(MapToXML.map2xml(MapToXML.listToMap(reserveData),"contacts"));
                }catch (Exception e){
                    e.printStackTrace();
                }
                try {
                    os.write((data+"\r\n"+tag1+MainActivity.overTag).getBytes("utf-8"));  //为了保证服务器端数据解析格式发统一，这里对传向服务器端的数据做了处理
                    return 0;
                }catch (IOException e){
                    progressDialog.dismiss();
                    return 1;
                }
            }
        }
        return 0;
    }

    @Override
    protected void onPostExecute(Integer Flag){
        switch (Flag){
            case 0:
                Toast.makeText(mContext,mContext.getResources().getString(R.string.synchronized_reserve_success),Toast.LENGTH_SHORT).show();
                break;
            case 1:
                progressDialog.dismiss();
                Toast.makeText(mContext,mContext.getResources().getString(R.string.synchronized_reserve_failed),Toast.LENGTH_SHORT).show();
                break;
            case 2:
                progressDialog.dismiss();
                Toast.makeText(mContext,mContext.getResources().getString(R.string.lack_of_myself_info),Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    protected void onPreExecute(){
        progressDialog=new ProgressDialog(mContext);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(mContext.getResources().getString(R.string.synchronized_reserve));
        progressDialog.show();
    }
}