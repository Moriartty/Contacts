package com.moriarty.user.contacts.Fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moriarty.user.contacts.Activity.Person_InfoCard;
import com.moriarty.user.contacts.Adapter.Tieba_ItemListAdapter;
import com.moriarty.user.contacts.Others.SignalManager;
import com.moriarty.user.contacts.R;
import com.moriarty.user.contacts.Service.AddContactsService;
import com.moriarty.user.contacts.Service.QueryContactsService;
import com.moriarty.user.contacts.User_Defind.DividerItemDecoration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by user on 16-9-28.
 */
public class Contacts_PersonInfo_Tieba extends Fragment {
    BroadcastReceiver receiver_tieba;
    RecyclerView tieba_list;
    Tieba_ItemListAdapter tieba_itemListAdapter;
    String id;
    public static HashMap<String,HashMap<String,String>> tieba_map;
    public HashMap<String,String> other_info=new HashMap<>();
    public ArrayList<HashMap.Entry<String,HashMap<String,String>>> tieba_sortlist;

    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tieba, null);
        Log.d("Moriarty","TiebaFragment:"+"is prepare to invalidate");
        IntentFilter intentFilter7=new IntentFilter("BC_Tieba");    //动态广播，数据已经准备好了，准备更新fragment
        receiver_tieba=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                id=intent.getStringExtra("Id");    //获取该联系人贴吧id
                invalidate();
                //Log.d("Moriarty","TiebaFragment:"+"is prepare to invalidate");
            }
        };
        getContext().registerReceiver(receiver_tieba,intentFilter7);

        tieba_list=(RecyclerView) v.findViewById(R.id.tieba_list);
        Person_InfoCard.handler2.sendEmptyMessage(SignalManager.tiebaFragment_Prepared);
        return v;
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        getContext().unregisterReceiver(receiver_tieba);  //取消注册广播监听器
    }

    private void invalidate() {
        other_info.putAll(tieba_map.get("other_info"));
        tieba_map.remove("other_info");
        tieba_sortlist=sortByTime(tieba_map);

        tieba_itemListAdapter=new Tieba_ItemListAdapter(getContext(),tieba_sortlist,other_info,tieba_list);
        tieba_list.setLayoutManager(new LinearLayoutManager(getContext()));
        tieba_list.setAdapter(tieba_itemListAdapter);
        tieba_list.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL_LIST));
        tieba_list.setVisibility(View.VISIBLE);
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
