package com.moriarty.user.contacts.Presenter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.moriarty.user.contacts.Activity.AddContacts;
import com.moriarty.user.contacts.Activity.MainActivity;
import com.moriarty.user.contacts.Activity.MainView;
import com.moriarty.user.contacts.Activity.Person_InfoCard;
import com.moriarty.user.contacts.Others.BroadcastManager;
import com.moriarty.user.contacts.Others.SignalManager;
import com.moriarty.user.contacts.Others.XmlToMap;
import com.moriarty.user.contacts.Service.AddContactsService;
import com.moriarty.user.contacts.Service.QueryContactsService;
import com.moriarty.user.contacts.Thread.CheckMyInfoTask;

import org.dom4j.DocumentException;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by user on 17-2-23.
 */
public class MainActivityPresenter implements IMainPresenter {
    Context context;
    SharedPreferences myInfo;
    SharedPreferences.Editor editor;
    MainView mainView;
    private String currentTag="MainActivityPresenter:";
    public MainActivityPresenter(Context context,MainView mainView){
        this.context=context;
        this.mainView=mainView;
    }
    @Override
    public void skip2AddContactsView(ArrayList<String> myInfoList) {
        Intent first=new Intent(context,AddContacts.class);
        first.putExtra("flag",1);
        first.putStringArrayListExtra("mydata",myInfoList);
        context.startActivity(first);
    }

    @Override
    public void skip2PersonInfo(ArrayList<String> myNewInfoList) {
        Intent first=new Intent(context,Person_InfoCard.class);
        first.putExtra("flag",1);
        Log.d(MainActivity.TAG,currentTag+"source_id is "+myNewInfoList.get(8));
        first.putStringArrayListExtra("mydata",myNewInfoList);
        context.startActivity(first);
    }

    @Override
    public void handleReturnData(Message message) {
        if(message.what== SignalManager.return_ReserveAction_signal){
            String returnData=message.obj.toString();
            try{
                HashMap<String,String> returnData_Map=new HashMap<>(XmlToMap.xml2map(returnData,false));
                // Log.d(TAG,"MainActivity:"+returnData_Map.size());
                for(String key:returnData_Map.keySet()){
                    if(key.contains("C"))
                        AddContactsService.addIntoPerson_Id(key,returnData_Map.get(key),context);
                    else if(key.contains("M")){
                        myInfo=context.getSharedPreferences("myself",context.MODE_PRIVATE);  //获取toolbar上需要显示的个人信息
                        editor=myInfo.edit();
                        HashMap<String ,String > recode_me= QueryContactsService.getAllMyInfoToMap(myInfo);   //获取自己的全部信息
                        recode_me.put("source_id",returnData_Map.get(key));   //将source_id修改为同步到的数据
                        AddContactsService.rewritePreference(recode_me,editor,context);  //重新将数据进行写入
                    }
                }
            }catch(DocumentException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void inspectPermission(final Handler handler) {
        //这里存在逻辑问题
        String[] permissions={Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.READ_CONTACTS};
        if(ContextCompat.checkSelfPermission(context,permissions[0])== PackageManager.PERMISSION_GRANTED
                &&ContextCompat.checkSelfPermission(context,permissions[1])==PackageManager.PERMISSION_GRANTED){
            checkMySelfInfo(handler);
        }
        else{
            mainView.enduePermission(permissions);
        }
    }


    @Override
    public void voiceSearch(int requestCode, int resultCode, Intent data) {
        if(requestCode== MainActivity.VOICE_RECOGNITION_REQUEST_CODE && resultCode==-1){
            //取得语音的字符
            ArrayList<String> results=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            StringBuffer stringBuffer=new StringBuffer();
            for(int i=0;i<results.size();i++){
                stringBuffer.append(results.get(i));
                stringBuffer.append("\n");
            }
            mainView.setCurrentItem(0);
            new BroadcastManager().sendBroadCast_Seven_WithValue(context,results);
            mainView.showToast(stringBuffer.toString());
        }
    }

    @Override
    public void checkMySelfInfo(final Handler handler) {
        new Thread(){
            @Override
            public void run(){
                mainView.invalidate();   //当两类权限都有时，直接初始化。
                CheckMyInfoTask checkMyInfoTask=new CheckMyInfoTask(context,handler);  //只会在onCreate()中进行检查自己信息是否完整,这个问题还需要得到更好的解决
                checkMyInfoTask.execute();
            }
        }.start();
    }
}
