package com.moriarty.user.contacts.Presenter;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.moriarty.user.contacts.Activity.MainActivity;
import com.moriarty.user.contacts.Activity.PersonInfoCardView;
import com.moriarty.user.contacts.Fragment.Contacts_ValueSortFragment;
import com.moriarty.user.contacts.Others.BroadcastManager;
import com.moriarty.user.contacts.Others.SignalManager;
import com.moriarty.user.contacts.Others.XmlToMap;
import com.moriarty.user.contacts.R;
import com.moriarty.user.contacts.Thread.RecommendThread;

import org.dom4j.DocumentException;

import java.util.HashMap;

/**
 * Created by user on 17-2-16.
 */
public class RecomDataPresenter implements IRecommendData{
    private Context context;
    private final static String currentTag="RecomDataPresenter:";
    private final static String recomTag="Recommend";
    PersonInfoCardView recomFragment;
    RecommendThread recommendThread=null;
    BroadcastManager broadcastManager;
    public RecomDataPresenter(Context context,PersonInfoCardView recomFragment){
        this.context=context;
        this.recomFragment=recomFragment;
        broadcastManager=new BroadcastManager();
    }
    @Override
    public void handleData(final Message message) {
        if(message.what== SignalManager.return_NativePlaceSPData_signal){
            Log.d(MainActivity.TAG,currentTag+"message has received");
            new ParseDataTask(message).execute();
        }
    }

    @Override
    public void sendMessage(Handler handler) {
        recomFragment.setSRLayoutTrue();
        Message message=new Message();
        message.what= SignalManager.send_RecommendFrag_signal;
        message.obj= recomTag+"\r\n";
        try {
            recommendThread.sendHandler.sendMessage(message);
        }catch(Exception e){
            initializeThread(handler);
            Log.d(MainActivity.TAG,currentTag+"recomThread is null");
            recomFragment.setSRLayoutFalse();
            e.printStackTrace();
        }
    }

    @Override
    public boolean initializeThread(Handler handler) {
        if(isNetworkAvailable()){
            recommendThread=new RecommendThread(handler);
            new Thread(recommendThread).start();
            return true;
        }
        else {
            recomFragment.showToast(context.getResources().getString(R.string.connected_filed_tips));
            return false;
        }
    }

    @Override
    public void initializeFragment() {

    }
    @Override
    public RecommendThread getRecomThread(){
        return this.recommendThread;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager.getActiveNetworkInfo() != null) {
            return manager.getActiveNetworkInfo().isAvailable();
        }
        return false;
    }

    class ParseDataTask extends AsyncTask<Void,Void,Boolean>{
        String data;
        public ParseDataTask(Message message){
            data=message.obj.toString();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Contacts_ValueSortFragment.all_map=new HashMap<>(XmlToMap.xml2map(data,false));
                if(Contacts_ValueSortFragment.all_map!=null&&Contacts_ValueSortFragment.all_map.size()>0)
                    return true;
            }catch (DocumentException e){
                e.printStackTrace();
                return false;
            }
            return false;
        }
        @Override
        protected void onPostExecute(Boolean b) {
            if(b){
                broadcastManager.sendBroadcast(context,8);
                recomFragment.setSRLayoutFalse();
                recomFragment.getFromNet(0);
            }
        }
    }
}
