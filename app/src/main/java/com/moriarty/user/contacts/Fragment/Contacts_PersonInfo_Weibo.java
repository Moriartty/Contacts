package com.moriarty.user.contacts.Fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.moriarty.user.contacts.Activity.Person_InfoCard;
import com.moriarty.user.contacts.Adapter.Weibo_ItemListAdapter;
import com.moriarty.user.contacts.Others.SignalManager;
import com.moriarty.user.contacts.R;
import com.moriarty.user.contacts.Service.AddContactsService;
import com.moriarty.user.contacts.User_Defind.DividerItemDecoration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by user on 16-9-28.
 */
public class Contacts_PersonInfo_Weibo extends Fragment {
    public static HashMap<String,HashMap<String,String>> weibo_map;
    public ArrayList<HashMap.Entry<String,HashMap<String,String>>> weibo_sortlist;
    public HashMap<String,String> other_info=new HashMap<>();
    BroadcastReceiver receiver_weibo;
    String tel;
    RecyclerView weibo_list;
    Weibo_ItemListAdapter weibo_itemListAdapter;
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_weibo, null);
        //Log.d("Moriarty","Contacts_PersonInfo_Weibo:"+"prepare");
        IntentFilter intentFilter8=new IntentFilter("BC_Weibo");    //动态广播，数据已经准备好了，准备更新fragment
        receiver_weibo=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                tel=intent.getStringExtra("Id");    //获取该联系人微博id
                invalidate();
            }
        };
        getContext().registerReceiver(receiver_weibo,intentFilter8);
        weibo_list=(RecyclerView)v.findViewById(R.id.weibo_list);
        Person_InfoCard.handler2.sendEmptyMessage(SignalManager.weiboFragment_Prepared);
        return v;
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        getContext().unregisterReceiver(receiver_weibo);
    }
    public void invalidate(){
        other_info.putAll(weibo_map.get("other_info"));
        weibo_map.remove("other_info");
        weibo_sortlist=sortByTime(weibo_map);

        weibo_itemListAdapter=new Weibo_ItemListAdapter(getContext(),weibo_sortlist,other_info,weibo_list);
        weibo_list.setLayoutManager(new LinearLayoutManager(getContext()));
        weibo_list.setAdapter(weibo_itemListAdapter);
        weibo_list.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL_LIST));
        weibo_list.setVisibility(View.VISIBLE);
    }

    private ArrayList<HashMap.Entry<String,HashMap<String,String>>> sortByTime(HashMap<String,HashMap<String,String>> weibo_map){
        ArrayList<HashMap.Entry<String,HashMap<String,String>>> weibo_sortlist=new ArrayList<>(weibo_map.entrySet());
        Collections.sort(weibo_sortlist, new Comparator<HashMap.Entry<String, HashMap<String, String>>>() {
            @Override
            public int compare(HashMap.Entry<String, HashMap<String, String>> lhs, HashMap.Entry<String, HashMap<String, String>> rhs) {
                return rhs.getValue().get("time").toString().compareTo(lhs.getValue().get("time").toString());
            }
        });
        return weibo_sortlist;
    }
}
