package com.moriarty.user.contacts.Fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.moriarty.user.contacts.Activity.MainActivity;
import com.moriarty.user.contacts.Activity.PersonInfoCardView;
import com.moriarty.user.contacts.Adapter.SPDataCollectionAdapter;
import com.moriarty.user.contacts.Others.SignalManager;
import com.moriarty.user.contacts.Presenter.IQueryContacts;
import com.moriarty.user.contacts.Presenter.IRecommendData;
import com.moriarty.user.contacts.Presenter.QueryContacts;
import com.moriarty.user.contacts.Presenter.RecomDataPresenter;
import com.moriarty.user.contacts.R;
import com.moriarty.user.contacts.Thread.ClientThread;
import com.moriarty.user.contacts.Thread.RecommendThread;
import com.moriarty.user.contacts.User_Defind.DividerItemDecoration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by user on 16-10-7.
 */
public class Contacts_ValueSortFragment extends Fragment implements PersonInfoCardView{
    private static final String currentTag="Contacts_ValueSortFragment:";
    Handler handler;
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;
    SPDataCollectionAdapter spDataCollectionAdapter;
    public static ArrayList<String> names = new ArrayList<String>();
    public static HashMap<String,String> headPortraits=new HashMap<>();
    public static HashMap<String,HashMap<String,String>> all_map;
    public ArrayList<HashMap.Entry<String,HashMap<String,String>>> all_sortlist;
    Context context;
    IRecommendData recomDataPresenter;
    BroadcastReceiver broadcastReceiver;
    int firstVisibleItem=0;
    boolean isOnTop=true;
    LinearLayoutManager linearLayoutManager;
    IQueryContacts queryContacts;
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState)
    {
        context=getContext();
        recomDataPresenter=new RecomDataPresenter(context,this);
        handler=new Handler(){
            @Override
            public void handleMessage(Message message){
                recomDataPresenter.handleData(message);
            }
        };
        recomDataPresenter.initializeThread(handler);
        queryContacts=new QueryContacts();

        linearLayoutManager=new LinearLayoutManager(getContext());

        IntentFilter intentFilter8=new IntentFilter("BC_EIGHT");    //动态广播，数据已经准备好了，准备更新fragment
        broadcastReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                changeToName();
                refreshList();
                Log.d(MainActivity.TAG,currentTag+" prepare to refresh recommendFragment");
            }
        };
        getContext().registerReceiver(broadcastReceiver,intentFilter8);
        View v = inflater.inflate(R.layout.fragment_viewpager4_layout1, null);
        swipeRefreshLayout=(SwipeRefreshLayout)v.findViewById(R.id.fragment4_refresh);
        swipeRefreshLayout.setProgressViewOffset(true,20,150);
        swipeRefreshLayout.setSize(SwipeRefreshLayout.LARGE);
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(getResources().getColor(R.color.dodgerblue));
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.white));

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(isOnTop==true)
                    recomDataPresenter.sendMessage(handler);
                else
                    setSRLayoutFalse();
            }
        });
        swipeRefreshLayout.setDistanceToTriggerSync(350);
        recyclerView=(RecyclerView)v.findViewById(R.id.testList);
        refreshList();
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState==RecyclerView.SCROLL_STATE_IDLE&&firstVisibleItem==0)
                    isOnTop=true;
                else
                    isOnTop=false;
            }
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();
            }
        });
        if(recomDataPresenter.isNetworkAvailable()){
            new Thread(){
                @Override
                public void run(){
                    while(true){
                        if(recomDataPresenter.getRecomThread()!=null){
                            try {
                                Log.d(MainActivity.TAG,currentTag+"break loop");
                                recomDataPresenter.sendMessage(handler);
                            }catch (Exception e){
                                Log.d(MainActivity.TAG,currentTag+"server is close");
                                break;
                            }
                            break;
                        }
                    }
                }
            }.start();
        }
        return v;
    }

    private void refreshList(){
        if(all_map!=null)
            all_sortlist=sortByTime(all_map);
        Log.d(MainActivity.TAG,currentTag+" refresh");
        spDataCollectionAdapter=new SPDataCollectionAdapter(getContext(),all_sortlist,recyclerView);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(spDataCollectionAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL_LIST));
        recyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        context.unregisterReceiver(broadcastReceiver);
    }


    @Override
    public void setSRLayoutFalse() {
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void setSRLayoutTrue() {
        swipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void showToast(String toast) {
        Toast.makeText(context,toast,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void getFromNet(int flag) {
        //这里由于在RecommendThread接受完服务端传来的数据后就断开连接了，
        //所以在接受完一次数据后就立刻重新再开一个链接
        recomDataPresenter.initializeThread(handler);
    }

    private void changeToName(){
        Iterator<String> iterator=all_map.keySet().iterator();
        String flag,contactName;
        while(iterator.hasNext()){
            flag=iterator.next();
            contactName=queryContacts.getContactNameFromPhoneBook(context,all_map.get(flag).get("name"));
            all_map.get(flag).put("id",contactName);
        }

    }

    private ArrayList<HashMap.Entry<String,HashMap<String,String>>> sortByTime(HashMap<String,HashMap<String,String>> tieba_map){
        ArrayList<HashMap.Entry<String,HashMap<String,String>>> tieba_sortlist=new ArrayList<>(tieba_map.entrySet());
        Collections.sort(tieba_sortlist, new Comparator<HashMap.Entry<String, HashMap<String, String>>>() {
            @Override
            public int compare(HashMap.Entry<String, HashMap<String, String>> lhs, HashMap.Entry<String, HashMap<String, String>> rhs) {
                return rhs.getValue().get("time").toString().compareTo(lhs.getValue().get("time").toString());
            }
        });
        return tieba_sortlist;
    }
}
