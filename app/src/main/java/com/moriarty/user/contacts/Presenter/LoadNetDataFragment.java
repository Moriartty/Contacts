package com.moriarty.user.contacts.Presenter;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.moriarty.user.contacts.Activity.MainActivity;
import com.moriarty.user.contacts.Activity.PersonInfoCardView;
import com.moriarty.user.contacts.Activity.Person_InfoCard;
import com.moriarty.user.contacts.Fragment.Contacts_PersonInfo_Tieba;
import com.moriarty.user.contacts.Fragment.Contacts_PersonInfo_Weibo;
import com.moriarty.user.contacts.Others.BroadcastManager;
import com.moriarty.user.contacts.Others.SignalManager;
import com.moriarty.user.contacts.Others.XmlToMap;
import com.moriarty.user.contacts.R;
import com.moriarty.user.contacts.Service.SPDataManeger;
import com.moriarty.user.contacts.Thread.ClientThread;

import org.dom4j.DocumentException;

import java.util.HashMap;


/**
 * Created by user on 17-2-13.
 */
public class LoadNetDataFragment implements ILoadFragment {
    public static final String empty_TiebaUrl="http://tieba.baidu.com/home/main?un=&ie=utf-8&fr=pb";
    public static final String empty_WeiboUrl="http://weibo.cn/u/";
    private final static String currentTag="LoadNetDataFragment:";
    String tiebaTag="TiebaProfileCrawl";
    String weiboTag="WeiboCrlawer";
    String returnValue;
    PersonInfoCardView personInfoCardView;
    Context context;
    ClientThread clientThread;
    StringBuffer stringBuffer=new StringBuffer();
    SPDataManeger spDataManeger;
    BroadcastManager broadcastManager;
    public LoadNetDataFragment(Context context, PersonInfoCardView personInfoCardView){
        this.context=context;
        this.personInfoCardView=personInfoCardView;
        this.spDataManeger=new SPDataManeger(context);
        broadcastManager=new BroadcastManager();
    }

    @Override
    public void receiveMessage(Message message,String tiebaId,final String phoneText) {
        stringBuffer.append(message.obj.toString()+"\n");
        if(message.obj.toString().equals("</tieba>")){
            returnValue=stringBuffer.toString();
            stringBuffer.setLength(0);
            try{
                Contacts_PersonInfo_Tieba.tieba_map=new HashMap<>(XmlToMap.xml2map(returnValue,false));
                final HashMap<String,HashMap<String,String>> tieba_map_copy=new HashMap<>(Contacts_PersonInfo_Tieba.tieba_map);

                new Thread(){
                    @Override
                    public void run(){
                        //另开线程进行存储
                        spDataManeger.WriteInTiebaTable(tieba_map_copy,phoneText);
                    }
                }.start();
            }catch (DocumentException e){
                e.printStackTrace();
            }
            broadcastManager.sendBroadCast_Tieba_WithValue(context,tiebaId);
            personInfoCardView.setSRLayoutFalse();
        }
        else if(message.obj.toString().equals("</weibo>")){
            int flag=0;
            personInfoCardView.setSRLayoutFalse();
            returnValue=stringBuffer.toString();
            Log.d(MainActivity.TAG,currentTag+"received from server:"+returnValue);
            stringBuffer.setLength(0);
            try{
                Contacts_PersonInfo_Weibo.weibo_map=new HashMap<>(XmlToMap.xml2map(returnValue,false));
                if(Contacts_PersonInfo_Weibo.weibo_map.get("other_info").values().size()>0)
                    flag=1;
                if(flag==1){
                    final HashMap<String,HashMap<String,String>> weibo_map_copy=new HashMap<>(Contacts_PersonInfo_Weibo.weibo_map);

                    new Thread(){
                        @Override
                        public void run(){
                            //另开线程进行存储
                            spDataManeger.writeInWeiboTable(weibo_map_copy,phoneText);
                        }
                    }.start();
                }
            }catch(DocumentException e){
                e.printStackTrace();
            }
            if(flag==1)
                broadcastManager.sendBroadCast_Weibo_WithValue(context,phoneText);
        }
        else if(message.obj.toString().equals("</renren>")){

        }
    }

    @Override
    public void initializeFragment(Message message,String phoneText,String tiebaId) {
        switch (message.what){
            case SignalManager.tiebaFragment_Prepared:  //tiebaFragment加载完毕
                if(Contacts_PersonInfo_Tieba.tieba_map!=null)
                    Contacts_PersonInfo_Tieba.tieba_map.clear();
                Contacts_PersonInfo_Tieba.tieba_map=spDataManeger.getTiebaData("Contacts_"+ phoneText+"_Tieba",tiebaId);
                if(Contacts_PersonInfo_Tieba.tieba_map!=null&&Contacts_PersonInfo_Tieba.tieba_map.get("other_info").values().size()>0)
                    broadcastManager.sendBroadCast_Tieba_WithValue(context,tiebaId);
                    //如果数据库中没有该联系人的贴吧动态信息，则从网络上爬取
                else
                    personInfoCardView.getFromNet(SignalManager.tiebaFragment_Prepared);
                break;
            case SignalManager.weiboFragment_Prepared: //weiboFragment加载完毕
                if(Contacts_PersonInfo_Weibo.weibo_map!=null)
                    Contacts_PersonInfo_Weibo.weibo_map.clear();
                Contacts_PersonInfo_Weibo.weibo_map=spDataManeger.getWeiboData("Contacts_"+phoneText+"_Weibo");
                if(Contacts_PersonInfo_Weibo.weibo_map!=null&&Contacts_PersonInfo_Weibo.weibo_map.get("other_info").values().size()>0){
                    //Log.d(MainActivity.TAG,currentTag+"weibo_map size is"+Contacts_PersonInfo_Weibo.weibo_map.get("other_info").values().size());
                    broadcastManager.sendBroadCast_Weibo_WithValue(context,phoneText);
                }
                else
                    personInfoCardView.getFromNet(SignalManager.weiboFragment_Prepared);
                break;
        }
    }

    @Override
    public void initializeThread(Handler handler) {
        clientThread=new ClientThread(handler);
        new Thread(clientThread).start();
    }

    @Override
    public void sendTiebaRefreshOrder(String tiebaUrl,String phone,String name) {
        stringBuffer.setLength(0);   //如果接受到的数据不全，无法写成xml文件，为了避免影响下次刷新，需要先将上次stringbuffer中残留的数据清空
        if(tiebaUrl==null||tiebaUrl.equals("")||tiebaUrl.equals(empty_TiebaUrl))
            personInfoCardView.showToast(context.getResources().getString(R.string.person_info_tips1)
                    +name+context.getResources().getString(R.string.person_info_tips2));
        else {
            Message msg=new Message();
            msg.what= SignalManager.send_TiebaRefresh_signal;
            //msg.obj="http://tieba.baidu.com/home/main?un=飞刀杂耍者&ie=utf-8&fr=pb";
            msg.obj=tiebaUrl+tiebaTag+"*"+phone+"\r\n";
            try{
                clientThread.revHandler.sendMessage(msg);
                personInfoCardView.setSRLayoutTrue();
            }catch(NullPointerException e){
                Log.d(MainActivity.TAG,currentTag+"clientThread is null");
                personInfoCardView.showToast(context.getResources().getString(R.string.connected_filed_tips));
            }
        }
    }

    @Override
    public void sendWeiboRefreshOrder(String weiboUrl,String phone,String name) {
        stringBuffer.setLength(0);   //如果接受到的数据不全，无法写成xml文件，为了避免影响下次刷新，需要先将上次stringbuffer中残留的数据清空
        if(weiboUrl==null||weiboUrl.equals("")||weiboUrl.equals(empty_WeiboUrl)){
            Log.d(MainActivity.TAG,currentTag+"weibo is null");
            personInfoCardView.showToast(context.getResources().getString(R.string.person_info_tips1)
                    +name+context.getResources().getString(R.string.person_info_tips3));
        }
        else{
            Message msg=new Message();
            msg.what=SignalManager.send_WeiboRefresh_signal;
            //msg.obj="http://weibo.cn/u/3154080892";
            msg.obj=weiboUrl+weiboTag+"*"+phone+"\r\n";
            try{
                clientThread.revHandler.sendMessage(msg);
                personInfoCardView.setSRLayoutTrue();
            }catch (NullPointerException e){
                Log.d(MainActivity.TAG,currentTag+"clientThread is null");
            }
        }
    }
}
